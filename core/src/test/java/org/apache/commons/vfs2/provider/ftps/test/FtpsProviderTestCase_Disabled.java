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
package org.apache.commons.vfs2.provider.ftps.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import junit.framework.Test;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.ftps.FtpsFileProvider;
import org.apache.commons.vfs2.provider.ftps.FtpsFileSystemConfigBuilder;
import org.apache.commons.vfs2.test.AbstractProviderTestConfig;
import org.apache.commons.vfs2.test.ProviderTestConfig;
import org.apache.commons.vfs2.test.ProviderTestSuite;
import org.apache.commons.vfs2.util.FreeSocketPortUtil;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.junit.Assert;

/**
 * Tests for FTP file systems.
 * 
 * This is test fails because we cannot read from more than two input streams at the same time.
 */
public class FtpsProviderTestCase_Disabled extends AbstractProviderTestConfig implements ProviderTestConfig
{

    private static int SocketPort;

    /**
     * Use %40 for @ in URLs
     */
    private static String ConnectionUri;

    private static FtpServer Server;

    protected FileSystemOptions fileSystemOptions;

    private static final String TEST_URI = "test.ftps.uri";

    private static final String USER_PROPS_RES = "org.apache.ftpsserver/users.properties";

    private static final String SERVER_JKS_RES = "org.apache.ftpsserver/ftpserver.jks";

    static String getConnectionUri()
    {
        return ConnectionUri;
    }

    static int getSocketPort()
    {
        return SocketPort;
    }

    private static String getSystemTestUriOverride()

    {
        return System.getProperty(TEST_URI);
    }

    static void init() throws IOException
    {
        SocketPort = FreeSocketPortUtil.findFreeLocalPort();
        // Use %40 for @ in a URL
        ConnectionUri = "ftps://test:test@localhost:" + SocketPort;
    }

    /**
     * Creates and starts an embedded Apache FTP Server (MINA).
     * 
     * @throws FtpException
     * @throws IOException
     */
    static void setUpClass() throws FtpException, IOException
    {
        if (Server != null)
        {
            return;
        }
        init();
        final FtpServerFactory serverFactory = new FtpServerFactory();
        final PropertiesUserManagerFactory propertiesUserManagerFactory = new PropertiesUserManagerFactory();
        final URL userPropsResource = ClassLoader.getSystemClassLoader().getResource(USER_PROPS_RES);
        Assert.assertNotNull(USER_PROPS_RES, userPropsResource);
        propertiesUserManagerFactory.setUrl(userPropsResource);
        final UserManager userManager = propertiesUserManagerFactory.createUserManager();
        final BaseUser user = (BaseUser) userManager.getUserByName("test");
        // Pickup the home dir value at runtime even though we have it set in the user prop file
        // The user prop file requires the "homedirectory" to be set
        user.setHomeDirectory(getTestDirectory());
        serverFactory.setUserManager(userManager);
        ListenerFactory factory = new ListenerFactory();
        // set the port of the listener
        factory.setPort(SocketPort);

        // define SSL configuration
        final URL serverJksResource = ClassLoader.getSystemClassLoader().getResource(SERVER_JKS_RES);
        Assert.assertNotNull(SERVER_JKS_RES, serverJksResource);
        SslConfigurationFactory ssl = new SslConfigurationFactory();
        final File keyStoreFile = new File(serverJksResource.getFile());
        Assert.assertTrue(keyStoreFile.toString(), keyStoreFile.exists());
        ssl.setKeystoreFile(keyStoreFile);
        ssl.setKeystorePassword("password");

        // set the SSL configuration for the listener
        factory.setSslConfiguration(ssl.createSslConfiguration());
        factory.setImplicitSsl(true);

        // replace the default listener
        serverFactory.addListener("default", factory.createListener());

        // start the server
        Server = serverFactory.createServer();
        Server.start();
    }

    /**
     * Creates the test suite for the ftp file system.
     */
    public static Test suite() throws Exception
    {
        return new ProviderTestSuite(new FtpsProviderTestCase_Disabled())
        {
            @Override
            protected void setUp() throws Exception
            {
                if (getSystemTestUriOverride() == null)
                {
                    setUpClass();
                }
                super.setUp();
            }

            @Override
            protected void tearDown() throws Exception
            {
                tearDownClass();
                super.tearDown();
            }
        };
    }

    /**
     * Stops the embedded Apache FTP Server (MINA).
     */
    static void tearDownClass()
    {
        if (Server != null)
        {
            Server.stop();
            Server = null;
        }
    }

    /**
     * Returns the base folder for tests. You can override the DEFAULT_URI by using the system property name defined by
     * TEST_URI.
     */
    @Override
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception
    {
        String uri = getSystemTestUriOverride();
        if (uri == null)
        {
            uri = ConnectionUri;
        }
        return manager.resolveFile(uri, getFileSystemOptions());
    }

    protected FileSystemOptions getFileSystemOptions()
    {
        if (fileSystemOptions == null)
        {
            fileSystemOptions = new FileSystemOptions();
            final FtpsFileSystemConfigBuilder builder = FtpsFileSystemConfigBuilder.getInstance();
            builder.setPassiveMode(fileSystemOptions, true);
            builder.setDataTimeout(fileSystemOptions, Integer.valueOf(2000));
            builder.setFtpsType(fileSystemOptions, FtpsFileSystemConfigBuilder.FTPS_TYPE_IMPLICIT);
        }
        return fileSystemOptions;
    }

    /**
     * Prepares the file system manager.
     */
    @Override
    public void prepare(final DefaultFileSystemManager manager) throws Exception
    {
        manager.addProvider("ftps", new FtpsFileProvider());
    }
}
