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
package org.apache.commons.vfs2.provider.ftp;

import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectory;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.apache.commons.vfs2.impl.DecoratedFileObject;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.filesystem.nativefs.NativeFileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.Md5PasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;

import java.net.URL;

/**
 * JUnit 5 tests for FTP file systems with homeDirIsRoot=true.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FtpProviderUserDirTest extends ProviderTestSuiteJunit5 {

    private static FtpServer server;
    private static int socketPort;
    private static String connectionUri;
    private static final String TEST_URI = "test.ftp.uri";
    private static final String USER_PROPS_RES = "org.apache.ftpserver/users.properties";

    public FtpProviderUserDirTest() throws Exception {
        super(new FtpProviderUserDirTestConfig(), "", false);
    }

    protected static String getSystemTestUriOverride() {
        return System.getProperty(TEST_URI);
    }

    @Override
    protected void setUp() throws Exception {
        if (getSystemTestUriOverride() == null) {
            setUpClass();
        }
        super.setUp();
    }

    @AfterAll
    protected void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            tearDownClass();
        }
    }

    /**
     * Starts the embedded Apache FTP Server (MINA).
     */
    private static void setUpClass() throws FtpException, IOException {
        if (server != null) {
            return;
        }

        // Create test directory structure with homeDirIsRoot
        final File testDir = new File(getTestDirectory());
        final File rootDir = new File(testDir, "homeDirIsRoot");
        final File homesDir = new File(rootDir, "home");
        final File initialDir = new File(homesDir, "test");
        FileUtils.deleteDirectory(rootDir);
        rootDir.mkdir();
        FileUtils.copyDirectory(testDir, initialDir, pathName -> !pathName.getPath().contains(rootDir.getName()));

        // Let the OS find an ephemeral port
        socketPort = 0;
        final FtpServerFactory serverFactory = new FtpServerFactory();
        final PropertiesUserManagerFactory propertiesUserManagerFactory = new PropertiesUserManagerFactory();
        propertiesUserManagerFactory.setPasswordEncryptor(new Md5PasswordEncryptor());
        final URL userPropsResource = ClassLoader.getSystemClassLoader().getResource(USER_PROPS_RES);
        Assertions.assertNotNull(userPropsResource, USER_PROPS_RES);
        propertiesUserManagerFactory.setUrl(userPropsResource);
        final UserManager userManager = propertiesUserManagerFactory.createUserManager();
        final BaseUser user = (BaseUser) userManager.getUserByName("test");
        user.setHomeDirectory(rootDir.getAbsolutePath());
        serverFactory.setUserManager(userManager);

        // Set up file system factory with homeDirIsRoot behavior
        final FileSystemFactory fileSystemFactory = new NativeFileSystemFactory() {
            @Override
            public FileSystemView createFileSystemView(final User user) throws FtpException {
                final FileSystemView fsView = super.createFileSystemView(user);
                fsView.changeWorkingDirectory("home/test");
                return fsView;
            }
        };
        serverFactory.setFileSystem(fileSystemFactory);

        final ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(socketPort);
        serverFactory.addListener("default", listenerFactory.createListener());

        server = serverFactory.createServer();
        server.start();
        socketPort = serverFactory.getListener("default").getPort();
        connectionUri = "ftp://test:test@localhost:" + socketPort;
    }

    /**
     * Stops the embedded Apache FTP Server (MINA).
     */
    private static void tearDownClass() {
        if (server != null) {
            server.stop();
            server = null;

            // Clean up test directory
            try {
                final File testDir = new File(getTestDirectory());
                final File rootDir = new File(testDir, "homeDirIsRoot");
                FileUtils.deleteDirectory(rootDir);
            } catch (final IOException e) {
                // Ignore cleanup errors
            }
        }
    }

    /**
     * Configuration for FTP provider tests with homeDirIsRoot=true.
     */
    private static class FtpProviderUserDirTestConfig extends AbstractProviderTestConfig {

        @Override
        public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
            String uri = getSystemTestUriOverride();
            if (uri == null) {
                uri = connectionUri;
            }
            final FileSystemOptions options = new FileSystemOptions();
            final FtpFileSystemConfigBuilder builder = FtpFileSystemConfigBuilder.getInstance();
            builder.setUserDirIsRoot(options, true);
            final FileObject remoteFolder = manager.resolveFile(uri, options);
            return remoteFolder;
        }

        @Override
        public void prepare(final DefaultFileSystemManager manager) throws Exception {
            manager.addProvider("ftp", new FtpFileProvider());
        }
    }
}

