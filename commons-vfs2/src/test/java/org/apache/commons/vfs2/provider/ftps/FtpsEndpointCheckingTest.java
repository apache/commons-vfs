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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URL;
import java.time.Duration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfiguration;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.Md5PasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests that the ftps provider verifies the server host name against its certificate.
 * <p>
 * The embedded test server presents a certificate whose only host name is {@code localhost} (CN=localhost, no subject
 * alternative names). Connecting through the loopback IP {@code 127.0.0.1} therefore must fail once endpoint identity
 * checking is in effect, and succeed only when it is explicitly disabled.
 * </p>
 */
public class FtpsEndpointCheckingTest {

    private static final String LISTENER_NAME = "default";

    private static final String USER_PROPS_RES = "org.apache.ftpsserver/users.properties";

    private static final String SERVER_JKS_RES = "org.apache.ftpsserver/ftpserver.jks";

    private static FtpServer embeddedFtpServer;

    private static int socketPort;

    @BeforeAll
    static void setUpClass() throws Exception {
        final FtpServerFactory serverFactory = new FtpServerFactory();
        final PropertiesUserManagerFactory propertiesUserManagerFactory = new PropertiesUserManagerFactory();
        propertiesUserManagerFactory.setPasswordEncryptor(new Md5PasswordEncryptor());
        final URL userPropsResource = ClassLoader.getSystemClassLoader().getResource(USER_PROPS_RES);
        assertNotNull(userPropsResource, USER_PROPS_RES);
        propertiesUserManagerFactory.setUrl(userPropsResource);
        final UserManager userManager = propertiesUserManagerFactory.createUserManager();
        final BaseUser user = (BaseUser) userManager.getUserByName("test");
        user.setHomeDirectory(getTestDirectory());
        serverFactory.setUserManager(userManager);

        final ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(0);

        final URL serverJksResource = ClassLoader.getSystemClassLoader().getResource(SERVER_JKS_RES);
        assertNotNull(serverJksResource, SERVER_JKS_RES);
        final SslConfigurationFactory sslConfigFactory = new SslConfigurationFactory();
        final File keyStoreFile = FileUtils.toFile(serverJksResource);
        assertTrue(keyStoreFile.exists(), keyStoreFile.toString());
        sslConfigFactory.setKeystoreFile(keyStoreFile);
        sslConfigFactory.setKeystorePassword("password");
        final SslConfiguration sslConfiguration = sslConfigFactory.createSslConfiguration();
        listenerFactory.setSslConfiguration(new NoProtocolSslConfigurationProxy(sslConfiguration));
        listenerFactory.setImplicitSsl(false);

        serverFactory.addListener(LISTENER_NAME, listenerFactory.createListener());
        embeddedFtpServer = serverFactory.createServer();
        embeddedFtpServer.start();
        socketPort = ((org.apache.ftpserver.impl.DefaultFtpServer) embeddedFtpServer).getListener(LISTENER_NAME).getPort();
    }

    @AfterAll
    static void tearDownClass() {
        if (embeddedFtpServer != null) {
            embeddedFtpServer.stop();
            embeddedFtpServer = null;
        }
    }

    private DefaultFileSystemManager newManager() throws FileSystemException {
        final DefaultFileSystemManager manager = new DefaultFileSystemManager();
        manager.addProvider("ftps", new FtpsFileProvider());
        manager.init();
        return manager;
    }

    private FileSystemOptions newOptions(final Boolean endpointCheckingEnabled) {
        final FileSystemOptions options = new FileSystemOptions();
        final FtpsFileSystemConfigBuilder builder = FtpsFileSystemConfigBuilder.getInstance();
        builder.setConnectTimeout(options, Duration.ofSeconds(10));
        builder.setDataTimeout(options, Duration.ofSeconds(10));
        if (endpointCheckingEnabled != null) {
            builder.setEndpointCheckingEnabled(options, endpointCheckingEnabled.booleanValue());
        }
        return options;
    }

    /** With verification disabled, the same mismatched host connects. */
    @Test
    public void testMismatchedHostAllowedWhenCheckingDisabled() throws Exception {
        try (DefaultFileSystemManager manager = newManager()) {
            final String uri = "ftps://test:test@127.0.0.1:" + socketPort + "/";
            final FileSystemOptions options = newOptions(Boolean.FALSE);
            assertDoesNotThrow(() -> manager.resolveFile(uri, options).exists());
        }
    }

    /** CN=localhost matches the connected host, so a valid connection still works. */
    @Test
    public void testMatchingHostIsAccepted() throws Exception {
        try (DefaultFileSystemManager manager = newManager()) {
            final String uri = "ftps://test:test@localhost:" + socketPort + "/";
            final FileObject fileObject = manager.resolveFile(uri, newOptions(null));
            assertTrue(fileObject.exists());
        }
    }

    /** The certificate is only valid for localhost, so connecting via 127.0.0.1 must be rejected by default. */
    @Test
    public void testMismatchedHostRejectedByDefault() throws Exception {
        try (DefaultFileSystemManager manager = newManager()) {
            final String uri = "ftps://test:test@127.0.0.1:" + socketPort + "/";
            final FileSystemOptions options = newOptions(null);
            assertThrows(FileSystemException.class, () -> manager.resolveFile(uri, options).exists());
        }
    }
}
