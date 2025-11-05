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
import org.apache.ftpserver.command.CommandFactoryFactory;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpReply;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.impl.FtpReplyTranslator;
import org.apache.ftpserver.impl.LocalizedFtpReply;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.Md5PasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;

/**
 * JUnit 5 tests for FTP file systems with MDTM disabled.
 * <p>
 * Explicitly removes MDTM feature from underlying Apache MINA FTP server so we can fall back to LIST timestamp
 * (existing default behavior).
 * </p>
 */
public class FtpProviderMdtmOffTest extends ProviderTestSuiteJunit5 {

    private static FtpServer server;
    private static int socketPort;
    private static String connectionUri;
    private static final String TEST_URI = "test.ftp.uri";
    private static final String USER_PROPS_RES = "org.apache.ftpserver/users.properties";

    public FtpProviderMdtmOffTest() throws Exception {
        super(new FtpProviderMdtmOffTestConfig(), "", false);
    }

    @Override
    protected void addBaseTests() throws Exception {
        addTests(FtpMdtmOffLastModifiedTests.class);
    }

    protected static String getSystemTestUriOverride() {
        return System.getProperty(TEST_URI);
    }

    @Override
    protected void setUp() throws Exception {
        if (getSystemTestUriOverride() == null) {
            setUpClass(getTestDirectory(), null, getCommandFactory());
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
        // Pickup the home dir value at runtime even though we have it set in the user prop file
        // The user prop file requires the "homedirectory" to be set
        user.setHomeDirectory(rootDirectory);
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
        server = serverFactory.createServer();
        server.start();
        socketPort = ((org.apache.ftpserver.impl.DefaultFtpServer) server).getListener("default").getPort();
        connectionUri = "ftp://test:test@localhost:" + socketPort;
    }

    /**
     * Returns a custom command factory that removes MDTM from FEAT response.
     */
    protected static CommandFactory getCommandFactory() {
        final CommandFactoryFactory factory = new CommandFactoryFactory();
        final String commandName = "FEAT";
        factory.addCommand(commandName, (session, context, request) -> {
            session.resetState();

            final String replyMsg = FtpReplyTranslator.translateMessage(session, request, context,
                FtpReply.REPLY_211_SYSTEM_STATUS_REPLY, commandName, null);
            final LocalizedFtpReply reply = new LocalizedFtpReply(FtpReply.REPLY_211_SYSTEM_STATUS_REPLY,
                replyMsg.replaceFirst(" MDTM\\n", ""));

            session.write(reply);
        });
        return factory.createCommandFactory();
    }

    /**
     * Configuration for FTP provider tests with MDTM disabled.
     */
    private static class FtpProviderMdtmOffTestConfig extends AbstractProviderTestConfig {

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
            builder.setMdtmLastModifiedTime(options, false);
        }
    }
}

