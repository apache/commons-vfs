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
package org.apache.commons.vfs2.provider.sftp.test;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileProvider;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.TrustEveryoneUserInfo;
import org.apache.commons.vfs2.test.AbstractProviderTestConfig;
import org.apache.commons.vfs2.test.ProviderTestSuite;
import org.apache.commons.vfs2.util.FreeSocketPortUtil;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.Session;
import org.apache.sshd.common.util.SecurityUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.FileSystemFactory;
import org.apache.sshd.server.FileSystemView;
import org.apache.sshd.server.ForwardingFilter;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.SshFile;
import org.apache.sshd.server.auth.UserAuthNone;
import org.apache.sshd.server.filesystem.NativeSshFile;
import org.apache.sshd.server.keyprovider.PEMGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;

import com.jcraft.jsch.TestIdentityRepositoryFactory;

/**
 * Tests cases for the SFTP provider.
 * <p>
 * Starts and stops an embedded Apache SSHd (MINA) server.
 * </p>
 * 
 */
public class SftpProviderTestCase extends AbstractProviderTestConfig
{
    /**
     * Implements FileSystemFactory because SSHd does not know about users and home directories.
     */
    static final class TestFileSystemFactory implements FileSystemFactory
    {
        /**
         * Accepts only the known test user.
         */
        public FileSystemView createFileSystemView(Session session) throws IOException
        {
            final String userName = session.getUsername();
            if (!DEFAULT_USER.equals(userName))
            {
                return null;
            }
            return new TestFileSystemView(AbstractVfsTestCase.getTestDirectoryString(), userName);
        }
    }

    /**
     * Implements FileSystemView because SSHd does not know about users and home directories.
     */
    static final class TestFileSystemView implements FileSystemView
    {
        private final String homeDirStr;

        private final String userName;

        // private boolean caseInsensitive;

        public TestFileSystemView(String homeDirStr, String userName)
        {
            this.homeDirStr = new File(homeDirStr).getAbsolutePath();
            this.userName = userName;
        }

        public SshFile getFile(SshFile baseDir, String file)
        {
            return this.getFile(baseDir.getAbsolutePath(), file);
        }

        public SshFile getFile(final String file)
        {
            return this.getFile(homeDirStr, file);
        }

        protected SshFile getFile(final String dir, final String file)
        {
            final String home = removePrefix(NativeSshFile.normalizeSeparateChar(homeDirStr));
            String userFileName = removePrefix(NativeSshFile.normalizeSeparateChar(file));
            final File sshFile = userFileName.startsWith(home) ? new File(userFileName) : new File(home, userFileName);
            userFileName = removePrefix(NativeSshFile.normalizeSeparateChar(sshFile.getAbsolutePath()));
            return new TestNativeSshFile(userFileName, sshFile, userName);
        }

        private String removePrefix(final String s)
        {
            final int index = s.indexOf('/');
            if (index < 1)
            {
                return s;
            }
            return s.substring(index);
        }
    }

    /**
     * Extends NativeSshFile because its constructor is protected and I do not want to create a whole NativeSshFile implementation for
     * testing.
     */
    static class TestNativeSshFile extends NativeSshFile
    {
        TestNativeSshFile(String fileName, File file, String userName)
        {
            super(fileName, file, userName);
        }
    }

    private static int SocketPort;

    private static final String DEFAULT_USER = "testtest";

    // private static final String DEFAULT_PWD = "testtest";

    private static String ConnectionUri;

    private static SshServer Server;

    private static final String TEST_URI = "test.sftp.uri";

    private static String getSystemTestUriOverride()
    {
        return System.getProperty(TEST_URI);
    }

    /**
     * Creates and starts an embedded Apache SSHd Server (MINA).
     * 
     * @throws FtpException
     * @throws IOException
     */
    private static void setUpClass() throws FtpException, IOException
    {
        if (Server != null)
        {
            return;
        }
        // System.setProperty("vfs.sftp.sshdir", getTestDirectory() + "/../vfs.sftp.sshdir");
        String tmpDir = System.getProperty("java.io.tmpdir");
        Server = SshServer.setUpDefaultServer();
        Server.setPort(SocketPort);
        if (SecurityUtils.isBouncyCastleRegistered())
        {
            Server.setKeyPairProvider(new PEMGeneratorHostKeyProvider(tmpDir + "/key.pem"));
        } else
        {
            Server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(tmpDir + "/key.ser"));
        }
        List<NamedFactory<Command>> list = new ArrayList<NamedFactory<Command>>(1);
        list.add(new SftpSubsystem.Factory());
        Server.setSubsystemFactories(list);
        Server.setPasswordAuthenticator(new PasswordAuthenticator()
        {
            public boolean authenticate(String username, String password, ServerSession session)
            {
                return username != null && username.equals(password);
            }
        });
        Server.setPublickeyAuthenticator(new PublickeyAuthenticator()
        {
            public boolean authenticate(String username, PublicKey key, ServerSession session)
            {
                // File f = new File("/Users/" + username + "/.ssh/authorized_keys");
                return true;
            }
        });
        Server.setForwardingFilter(new ForwardingFilter()
        {
            public boolean canConnect(InetSocketAddress address, ServerSession session)
            {
                return true;
            }

            public boolean canForwardAgent(ServerSession session)
            {
                return true;
            }

            public boolean canForwardX11(ServerSession session)
            {
                return true;
            }

            public boolean canListen(InetSocketAddress address, ServerSession session)
            {
                return true;
            }
        });
        // HACK Start
        // How do we really do simple user to directory matching?
        Server.setFileSystemFactory(new TestFileSystemFactory());
        // HACK End
        Server.start();
        // HACK Start
        // How do we really do simple security?
        // Do this after we start the server to simplify this set up code.
        Server.getUserAuthFactories().add(new UserAuthNone.Factory());
        // HACK End
    }

    /**
     * Creates the test suite for the ftp file system.
     */
    public static Test suite() throws Exception
    {
        return new ProviderTestSuite(new SftpProviderTestCase())
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
     * Stops the embedded Apache SSHd Server (MINA).
     * 
     * @throws InterruptedException
     */
    private static void tearDownClass() throws InterruptedException
    {
        if (Server != null)
        {
            Server.stop();
        }
    }

    public SftpProviderTestCase() throws IOException
    {
        SocketPort = FreeSocketPortUtil.findFreeLocalPort();
        // Use %40 for @ in a URL
        ConnectionUri = String.format("sftp://%s@localhost:%d", DEFAULT_USER, SocketPort);
    }

    /**
     * Returns the base folder for tests.
     */
    @Override
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception
    {
        String uri = getSystemTestUriOverride();
        if (uri == null)
        {
            uri = ConnectionUri;
        }

        FileSystemOptions fileSystemOptions = new FileSystemOptions();
        final SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();
        builder.setStrictHostKeyChecking(fileSystemOptions, "no");
        builder.setUserInfo(fileSystemOptions, new TrustEveryoneUserInfo());
        builder.setIdentityRepositoryFactory(fileSystemOptions, new TestIdentityRepositoryFactory());

        return manager.resolveFile(uri, fileSystemOptions);
    }

    /**
     * Prepares the file system manager.
     */
    @Override
    public void prepare(final DefaultFileSystemManager manager) throws Exception
    {
        manager.addProvider("sftp", new SftpFileProvider());
    }
}
