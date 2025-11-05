/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.provider.ftps;

import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectory;

import java.io.File;
import java.net.URL;
import java.time.Duration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.Md5PasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.junit.jupiter.api.Assertions;

/**
 * Abstract tests for FTP file systems.
 */
abstract class AbstractFtpsProviderTestCase extends AbstractProviderTestConfig {

    private static final String LISTENER_NAME = "default";

    private static int socketPort;

    /**
     * Use %40 for @ in URLs
     */
    private static String connectionUri;

    private static FtpServer embeddedFtpServer;

    private static final String TEST_URI = "test.ftps.uri";

    private static final String USER_PROPS_RES = "org.apache.ftpsserver/users.properties";

    private static final String SERVER_JKS_RES = "org.apache.ftpsserver/ftpserver.jks";

    static String getConnectionUri() {
        return connectionUri;
    }

    static int getSocketPort() {
        return socketPort;
    }

    static String getSystemTestUriOverride() {
        return System.getProperty(TEST_URI);
    }

    /**
     * Creates and starts an embedded Apache FTP EmbeddedFtpServer (MINA).
     *
     * @param implicit FTPS connection mode
     * @throws FtpException
     */
    synchronized static void setUpClass(final boolean implicit) throws FtpException {
        if (embeddedFtpServer != null) {
            return;
        }
        // Let the OS find use an ephemeral port by using 0.
        socketPort = 0;
        final FtpServerFactory serverFactory = new FtpServerFactory();
        final PropertiesUserManagerFactory propertiesUserManagerFactory = new PropertiesUserManagerFactory();
        // TODO Update to SHA512
        propertiesUserManagerFactory.setPasswordEncryptor(new Md5PasswordEncryptor());
        final URL userPropsResource = ClassLoader.getSystemClassLoader().getResource(USER_PROPS_RES);
        Assertions.assertNotNull(userPropsResource, USER_PROPS_RES);
        propertiesUserManagerFactory.setUrl(userPropsResource);
        final UserManager userManager = propertiesUserManagerFactory.createUserManager();
        final BaseUser user = (BaseUser) userManager.getUserByName("test");
        // Pickup the home dir value at runtime even though we have it set in the user prop file
        // The user prop file requires the "homedirectory" to be set
        user.setHomeDirectory(getTestDirectory());
        serverFactory.setUserManager(userManager);
        final ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(socketPort);

        // define SSL configuration
        final URL serverJksResource = ClassLoader.getSystemClassLoader().getResource(SERVER_JKS_RES);
        Assertions.assertNotNull(serverJksResource, SERVER_JKS_RES);
        // System.out.println("Loading " + serverJksResource);
        final SslConfigurationFactory sllConfigFactory = new SslConfigurationFactory();
        final File keyStoreFile = FileUtils.toFile(serverJksResource);
        Assertions.assertTrue(keyStoreFile.exists(), keyStoreFile.toString());
        sllConfigFactory.setKeystoreFile(keyStoreFile);
        sllConfigFactory.setKeystorePassword("password");

        // set the SSL configuration for the listener
        final SslConfiguration sslConfiguration = sllConfigFactory.createSslConfiguration();
        final NoProtocolSslConfigurationProxy noProtocolSslConfigurationProxy = new NoProtocolSslConfigurationProxy(sslConfiguration);
        listenerFactory.setSslConfiguration(noProtocolSslConfigurationProxy);
        listenerFactory.setImplicitSsl(implicit);

        // replace the default listener
        serverFactory.addListener(LISTENER_NAME, listenerFactory.createListener());

        // start the server
        embeddedFtpServer = serverFactory.createServer();
        embeddedFtpServer.start();
        Thread.yield();

        // Wait for server to be ready
        int retries = 20; // Wait up to 2 seconds
        while (retries-- > 0 && (embeddedFtpServer.isStopped() || embeddedFtpServer.isSuspended())) {
            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Additional wait to ensure the server is fully ready to accept connections
        try {
            Thread.sleep(200);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        socketPort = ((org.apache.ftpserver.impl.DefaultFtpServer) embeddedFtpServer).getListener(LISTENER_NAME).getPort();
        // System.out.println("Using port " + SocketPort);
        // System.out.printf("jdk.tls.disabledAlgorithms = %s%n", System.getProperty("jdk.tls.disabledAlgorithms"));
        connectionUri = "ftps://test:test@localhost:" + socketPort;
    }

    /**
     * Stops the embedded Apache FTP EmbeddedFtpServer (MINA).
     */
    synchronized static void tearDownClass() {
        if (embeddedFtpServer != null) {
            embeddedFtpServer.suspend();
            embeddedFtpServer.stop();
            Thread.yield();
            int count = 10;
            while (count-- > 0 && !embeddedFtpServer.isStopped()) {
                final int millis = 200;
                System.out.println(String.format("Waiting %,d milliseconds for %s to stop", millis, embeddedFtpServer));
                try {
                    Thread.sleep(millis);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }
            embeddedFtpServer = null;
            socketPort = 0;
            connectionUri = null;
        }
    }

    protected FileSystemOptions fileSystemOptions;

    /**
     * Returns the base folder for tests. You can override the DEFAULT_URI by using the system property name defined by TEST_URI.
     */
    @Override
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
        String uri = getSystemTestUriOverride();
        if (uri == null) {
            uri = connectionUri;
        }
        return manager.resolveFile(uri, getFileSystemOptions());
    }

    protected FileSystemOptions getFileSystemOptions() {
        if (fileSystemOptions == null) {
            fileSystemOptions = new FileSystemOptions();
            setupOptions(FtpsFileSystemConfigBuilder.getInstance());
        }
        return fileSystemOptions;
    }

    protected abstract boolean isImplicit();

    /**
     * Prepares the file system manager.
     */
    @Override
    public void prepare(final DefaultFileSystemManager manager) throws Exception {
        manager.addProvider("ftps", new FtpsFileProvider());
    }

    protected void setupOptions(final FtpsFileSystemConfigBuilder builder) {
        builder.setConnectTimeout(fileSystemOptions, Duration.ofSeconds(10));
        builder.setDataTimeout(fileSystemOptions, Duration.ofSeconds(10));
    }

}
