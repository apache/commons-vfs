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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.apache.commons.vfs2.impl.DecoratedFileObject;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.command.CommandFactory;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.Md5PasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;

/**
 * JUnit 5 tests for FTP file systems.
 * <p>
 * This class replaces {@link FtpProviderTestCase} with a pure JUnit 5 implementation.
 * </p>
 */
public class FtpProviderTest extends ProviderTestSuiteJunit5 {

    private static FtpServer server;
    private static int socketPort;
    private static String connectionUri;
    private static final String TEST_URI = "test.ftp.uri";
    private static final String USER_PROPS_RES = "org.apache.ftpserver/users.properties";

    private final boolean mdtmLastModifiedTime = false;

    public FtpProviderTest() throws Exception {
        super(new FtpProviderTestConfig(false), "", false);
    }

    protected static String getSystemTestUriOverride() {
        return System.getProperty(TEST_URI);
    }

    @Override
    protected void setUp() throws Exception {
        if (getSystemTestUriOverride() == null) {
            setUpClass(getFtpRootDir(), getFtpFileSystem(), getCommandFactory());
        }
        super.setUp();
    }

    @AfterAll
    public static void tearDownClass() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    public static String getConnectionUri() {
        return connectionUri;
    }

    public static int getSocketPort() {
        return socketPort;
    }

    /**
     * Creates and starts an embedded Apache FTP Server (MINA).
     */
    public static void setUpClass(final String rootDirectory, final FileSystemFactory fileSystemFactory,
        final CommandFactory commandFactory) throws FtpException {
        if (server != null) {
            return;
        }
        final FtpServerFactory serverFactory = new FtpServerFactory();
        final PropertiesUserManagerFactory propertiesUserManagerFactory = new PropertiesUserManagerFactory();
        propertiesUserManagerFactory.setPasswordEncryptor(new Md5PasswordEncryptor());
        final URL userPropsResource = ClassLoader.getSystemClassLoader().getResource(USER_PROPS_RES);
        Assertions.assertNotNull(userPropsResource, USER_PROPS_RES);
        propertiesUserManagerFactory.setUrl(userPropsResource);
        final UserManager userManager = propertiesUserManagerFactory.createUserManager();
        final BaseUser user = (BaseUser) userManager.getUserByName("test");
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
        factory.setPort(0);
        serverFactory.addListener("default", factory.createListener());
        server = serverFactory.createServer();
        server.start();
        socketPort = ((org.apache.ftpserver.impl.DefaultFtpServer) server).getListener("default").getPort();
        connectionUri = "ftp://test:test@localhost:" + socketPort;
    }

    protected String getFtpRootDir() {
        return getTestDirectory();
    }

    protected FileSystemFactory getFtpFileSystem() throws IOException {
        return null;
    }

    protected CommandFactory getCommandFactory() {
        return null;
    }

    protected boolean getUserDirIsRoot() {
        return false;
    }

    /**
     * Configuration for FTP provider tests.
     */
    private static class FtpProviderTestConfig extends AbstractProviderTestConfig {

        private final boolean mdtmLastModifiedTime;

        FtpProviderTestConfig(final boolean mdtmLastModifiedTime) {
            this.mdtmLastModifiedTime = mdtmLastModifiedTime;
        }

        @Override
        public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
            String uri = getSystemTestUriOverride();
            if (uri == null) {
                uri = connectionUri;
            }
            final FileSystemOptions options = new FileSystemOptions();
            final FtpFileSystemConfigBuilder builder = FtpFileSystemConfigBuilder.getInstance();
            init(builder, options);
            final FileObject remoteFolder = manager.resolveFile(uri, options);
            final FtpFileObject ftpFileObject = remoteFolder instanceof DecoratedFileObject
                    ? (FtpFileObject) ((DecoratedFileObject) remoteFolder).getDecoratedFileObject()
                    : (FtpFileObject) remoteFolder;
            final FtpFileSystem ftpFileSystem = (FtpFileSystem) ftpFileObject.getFileSystem();
            final FTPClientWrapper client = (FTPClientWrapper) ftpFileSystem.getClient();
            return remoteFolder;
        }

        @Override
        public void prepare(final DefaultFileSystemManager manager) throws Exception {
            manager.addProvider("ftp", new FtpFileProvider());
        }

        protected void init(final FtpFileSystemConfigBuilder builder, final FileSystemOptions options) {
            builder.setUserDirIsRoot(options, false);
            builder.setPassiveMode(options, true);
            builder.setFileType(options, FtpFileType.BINARY);
            builder.setConnectTimeout(options, Duration.ofSeconds(10));
            final Charset charset = StandardCharsets.UTF_8;
            final String charsetName = charset.name();
            builder.setControlEncoding(options, charsetName);
            builder.setControlEncoding(options, charset);
            builder.setControlKeepAliveReplyTimeout(options, Duration.ofSeconds(35));
            builder.setControlKeepAliveTimeout(options, Duration.ofSeconds(30));
            builder.setMdtmLastModifiedTime(options, mdtmLastModifiedTime);
        }
    }
}

