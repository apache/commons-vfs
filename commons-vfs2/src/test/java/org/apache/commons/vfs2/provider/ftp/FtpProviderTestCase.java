/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.provider.ftp;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;

import org.apache.commons.vfs2.AbstractProviderTestCase;
import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.ProviderTestSuite;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.command.CommandFactory;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.junit.Assert;

import junit.framework.Test;

/**
 * Tests for FTP file systems.
 */
public class FtpProviderTestCase extends AbstractProviderTestConfig {

    private static int SocketPort;

    /**
     * Use %40 for @ in URLs
     */
    private static String ConnectionUri;

    private static FtpServer Server;

    private static final String TEST_URI = "test.ftp.uri";

    private static final String USER_PROPS_RES = "org.apache.ftpserver/users.properties";

    static String getConnectionUri() {
        return ConnectionUri;
    }

    static int getSocketPort() {
        return SocketPort;
    }

    private static String getSystemTestUriOverride() {
        return System.getProperty(TEST_URI);
    }

    /**
     * Creates and starts an embedded Apache FTP Server (MINA).
     *
     * @param rootDirectory the local FTP server rootDirectory.
     * @param fileSystemFactory optional local FTP server FileSystemFactory.
     * @param commandFactory FTP server command factory.
     * @throws FtpException
     */
    static void setUpClass(final String rootDirectory, final FileSystemFactory fileSystemFactory,
        final CommandFactory commandFactory) throws FtpException {
        if (Server != null) {
            return;
        }
        final FtpServerFactory serverFactory = new FtpServerFactory();
        final PropertiesUserManagerFactory propertiesUserManagerFactory = new PropertiesUserManagerFactory();
        final URL userPropsResource = ClassLoader.getSystemClassLoader().getResource(USER_PROPS_RES);
        Assert.assertNotNull(USER_PROPS_RES, userPropsResource);
        propertiesUserManagerFactory.setUrl(userPropsResource);
        final UserManager userManager = propertiesUserManagerFactory.createUserManager();
        final BaseUser user = (BaseUser) userManager.getUserByName("test");
        // Pickup the home dir value at runtime even though we have it set in the user prop file
        // The user prop file requires the "homedirectory" to be set
        user.setHomeDirectory(rootDirectory);
        userManager.save(user);
        serverFactory.setUserManager(userManager);
        if (fileSystemFactory != null) {
            serverFactory.setFileSystem(fileSystemFactory);
        }
        if (commandFactory != null) {
            serverFactory.setCommandFactory(commandFactory);
        }
        final ListenerFactory factory = new ListenerFactory();
        // set the port of the listener
        factory.setPort(0);

        // replace the default listener
        serverFactory.addListener("default", factory.createListener());

        // start the server
        Server = serverFactory.createServer();
        Server.start();
        SocketPort = ((org.apache.ftpserver.impl.DefaultFtpServer) Server).getListener("default").getPort();
        ConnectionUri = "ftp://test:test@localhost:" + SocketPort;
    }

    /**
     * Creates the test suite for the FTP file system.
     */
    public static Test suite() throws Exception {
        return suite(new FtpProviderTestCase());
    }

    /**
     * Creates the test suite for subclasses of the FTP file system.
     */
    protected static Test suite(final FtpProviderTestCase testCase,
        final Class<? extends AbstractProviderTestCase>... testClasses) throws Exception {
        return new ProviderTestSuite(testCase) {

            @Override
            protected void addBaseTests() throws Exception {
                if (testClasses.length == 0) {
                    super.addBaseTests();
                } else {
                    for (final Class<?> test : testClasses) {
                        addTests(test);
                    }
                }
            }

            @Override
            protected void setUp() throws Exception {
                if (getSystemTestUriOverride() == null) {
                    setUpClass(testCase.getFtpRootDir(), testCase.getFtpFileSystem(), testCase.getCommandFactory());
                }
                super.setUp();
            }

            @Override
            protected void tearDown() throws Exception {
                try {
                    // This will report running threads of the FTP server.
                    // However, shutting down the FTP server first will always
                    // report an exception closing the manager, because the
                    // server is already down
                    super.tearDown();
                } finally {
                    tearDownClass();
                }
            }
        };
    }

    /**
     * Stops the embedded Apache FTP Server (MINA).
     */
    static void tearDownClass() {
        if (Server != null) {
            Server.stop();
            Server = null;
        }
    }

    private final boolean mdtmLastModifiedTime;

    public FtpProviderTestCase() {
        this(false);
    }

    public FtpProviderTestCase(final boolean mdtmLastModifiedTime) {
        this.mdtmLastModifiedTime = mdtmLastModifiedTime;
    }

    /**
     * Returns the base folder for tests. You can override the DEFAULT_URI by using the system property name defined by
     * TEST_URI.
     */
    @Override
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
        String uri = getSystemTestUriOverride();
        if (uri == null) {
            uri = ConnectionUri;
        }
        final FileSystemOptions options = new FileSystemOptions();
        final FtpFileSystemConfigBuilder builder = FtpFileSystemConfigBuilder.getInstance();
        init(builder, options);
        return manager.resolveFile(uri, options);
    }

    /**
     * Gets the FTP server command factory. Defaults to null for no override.
     *
     * @return the FTP server command factory or null.
     */
    protected CommandFactory getCommandFactory() {
        return null;
    }

    /**
     * Gets option file system factory for local FTP server.
     */
    protected FileSystemFactory getFtpFileSystem() throws IOException {
        // use default
        return null;
    }

    /**
     * Gets the root of the local FTP Server file system.
     */
    protected String getFtpRootDir() {
        return getTestDirectory();
    }

    /**
     * Gets the setting for UserDirIsRoot. Defaults to false.
     */
    protected boolean getUserDirIsRoot() {
        return false;
    }

    protected void init(final FtpFileSystemConfigBuilder builder, final FileSystemOptions options) {
        builder.setUserDirIsRoot(options, getUserDirIsRoot());
        builder.setPassiveMode(options, true);
        // FtpFileType.BINARY is the default
        builder.setFileType(options, FtpFileType.BINARY);
        builder.setConnectTimeout(options, Duration.ofSeconds(10));
        builder.setControlEncoding(options, "UTF-8");
        builder.setControlKeepAliveReplyTimeout(options, Duration.ofSeconds(35));
        builder.setControlKeepAliveTimeout(options, Duration.ofSeconds(30));
        builder.setMdtmLastModifiedTime(options, mdtmLastModifiedTime);
    }

    /**
     * Prepares the file system manager.
     */
    @Override
    public void prepare(final DefaultFileSystemManager manager) throws Exception {
        manager.addProvider("ftp", new FtpFileProvider());
    }
}
