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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileProvider;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystem;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpStreamProxy;
import org.apache.commons.vfs2.provider.sftp.TrustEveryoneUserInfo;
import org.apache.commons.vfs2.test.AbstractProviderTestConfig;
import org.apache.commons.vfs2.test.PermissionsTests;
import org.apache.commons.vfs2.test.ProviderReadTests;
import org.apache.commons.vfs2.test.ProviderTestConfig;
import org.apache.commons.vfs2.test.ProviderTestSuite;
import org.apache.commons.vfs2.util.FreeSocketPortUtil;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.Session;
import org.apache.sshd.common.SshException;
import org.apache.sshd.common.session.AbstractSession;
import org.apache.sshd.common.util.Buffer;
import org.apache.sshd.common.util.SecurityUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.FileSystemFactory;
import org.apache.sshd.server.FileSystemView;
import org.apache.sshd.server.ForwardingFilter;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.SshFile;
import org.apache.sshd.server.auth.UserAuthNone;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.filesystem.NativeSshFile;
import org.apache.sshd.server.keyprovider.PEMGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;

import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.TestIdentityRepositoryFactory;

/**
 * Tests cases for the SFTP provider.
 * <p>
 * Starts and stops an embedded Apache SSHd (MINA) server.
 * </p>
 */
public class SftpProviderTestCase extends AbstractProviderTestConfig {
    /**
     * The underlying filesystem
     */
    private SftpFileSystem filesystem;

    /**
     * Implements FileSystemFactory because SSHd does not know about users and home directories.
     */
    static final class TestFileSystemFactory implements FileSystemFactory {
        /**
         * Accepts only the known test user.
         */
        @Override
        public FileSystemView createFileSystemView(final Session session) throws IOException {
            final String userName = session.getUsername();
            if (!DEFAULT_USER.equals(userName)) {
                return null;
            }
            return new TestFileSystemView(AbstractVfsTestCase.getTestDirectory(), userName);
        }
    }

    /**
     * Implements FileSystemView because SSHd does not know about users and home directories.
     */
    static final class TestFileSystemView implements FileSystemView {
        private final String homeDirStr;

        private final String userName;

        // private boolean caseInsensitive;

        public TestFileSystemView(final String homeDirStr, final String userName) {
            this.homeDirStr = new File(homeDirStr).getAbsolutePath();
            this.userName = userName;
        }

        @Override
        public SshFile getFile(final SshFile baseDir, final String file) {
            return this.getFile(baseDir.getAbsolutePath(), file);
        }

        @Override
        public SshFile getFile(final String file) {
            return this.getFile(homeDirStr, file);
        }

        protected SshFile getFile(final String dir, final String file) {
            final String home = removePrefix(NativeSshFile.normalizeSeparateChar(homeDirStr));
            String userFileName = removePrefix(NativeSshFile.normalizeSeparateChar(file));
            final File sshFile = userFileName.startsWith(home) ? new File(userFileName) : new File(home, userFileName);
            userFileName = removePrefix(NativeSshFile.normalizeSeparateChar(sshFile.getAbsolutePath()));
            return new TestNativeSshFile(userFileName, sshFile, userName);
        }

        private String removePrefix(final String s) {
            final int index = s.indexOf('/');
            if (index < 1) {
                return s;
            }
            return s.substring(index);
        }
    }

    /**
     * Extends NativeSshFile because its constructor is protected and I do not want to create a whole NativeSshFile
     * implementation for testing.
     */
    static class TestNativeSshFile extends NativeSshFile {
        TestNativeSshFile(final String fileName, final File file, final String userName) {
            super(fileName, file, userName);
        }
    }

    private static int SocketPort;

    private static final String DEFAULT_USER = "testtest";

    // private static final String DEFAULT_PWD = "testtest";

    private static String ConnectionUri;

    private static SshServer Server;

    private static final String TEST_URI = "test.sftp.uri";

    /** True if we are testing the SFTP stream proxy */
    private final boolean streamProxyMode;

    private static String getSystemTestUriOverride() {
        return System.getProperty(TEST_URI);
    }

    /**
     * Creates and starts an embedded Apache SSHd Server (MINA).
     *
     * @throws FtpException
     * @throws IOException
     */
    private static void setUpClass() throws FtpException, IOException, InterruptedException {
        SocketPort = FreeSocketPortUtil.findFreeLocalPort();
        // Use %40 for @ in a URL
        ConnectionUri = String.format("sftp://%s@localhost:%d", DEFAULT_USER, SocketPort);

        if (Server != null) {
            return;
        }
        // System.setProperty("vfs.sftp.sshdir", getTestDirectory() + "/../vfs.sftp.sshdir");
        final String tmpDir = System.getProperty("java.io.tmpdir");
        Server = SshServer.setUpDefaultServer();
        Server.setPort(SocketPort);
        if (SecurityUtils.isBouncyCastleRegistered()) {
            // A temporary file will hold the key
            final File keyFile = File.createTempFile("key", ".pem", new File(tmpDir));
            keyFile.deleteOnExit();
            // It has to be deleted in order to be generated
            keyFile.delete();

            final PEMGeneratorHostKeyProvider keyProvider = new PEMGeneratorHostKeyProvider(keyFile.getAbsolutePath());
            Server.setKeyPairProvider(keyProvider);
        } else {
            Server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(tmpDir + "/key.ser"));
        }
        final List<NamedFactory<Command>> list = new ArrayList<>(1);
        list.add(new NamedFactory<Command>() {

            @Override
            public String getName() {
                return "sftp";
            }

            @Override
            public Command create() {
                return new MySftpSubsystem();
            }
        });
        Server.setSubsystemFactories(list);
        Server.setPasswordAuthenticator(new PasswordAuthenticator() {
            @Override
            public boolean authenticate(final String username, final String password, final ServerSession session) {
                return username != null && username.equals(password);
            }
        });
        Server.setPublickeyAuthenticator(new PublickeyAuthenticator() {
            @Override
            public boolean authenticate(final String username, final PublicKey key, final ServerSession session) {
                // File f = new File("/Users/" + username + "/.ssh/authorized_keys");
                return true;
            }
        });
        Server.setForwardingFilter(new ForwardingFilter() {
            @Override
            public boolean canConnect(final InetSocketAddress address, final ServerSession session) {
                return true;
            }

            @Override
            public boolean canForwardAgent(final ServerSession session) {
                return true;
            }

            @Override
            public boolean canForwardX11(final ServerSession session) {
                return true;
            }

            @Override
            public boolean canListen(final InetSocketAddress address, final ServerSession session) {
                return true;
            }
        });
        // Allows the execution of commands
        Server.setCommandFactory(new ScpCommandFactory(new TestCommandFactory()));
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

    static private class BaseProviderTestSuite extends ProviderTestSuite {

        public BaseProviderTestSuite(final ProviderTestConfig providerConfig) throws Exception {
            super(providerConfig);
        }

        @Override
        protected void tearDown() throws Exception {
            // Close all active sessions
            // Note that it should be done by super.tearDown()
            // while closing
            for (final AbstractSession session : Server.getActiveSessions()) {
                session.close(true);
            }
            super.tearDown();
        }

    }

    /**
     * Creates the test suite for the ftp file system.
     */
    public static Test suite() throws Exception {
        // The test suite to be returned
        final TestSuite suite = new TestSuite();

        // --- Standard VFS test suite
        final SftpProviderTestCase standardTestCase = new SftpProviderTestCase(false);
        final ProviderTestSuite sftpSuite = new BaseProviderTestSuite(standardTestCase);

        // VFS-405: set/get permissions
        sftpSuite.addTests(PermissionsTests.class);

        suite.addTest(sftpSuite);

        // --- VFS-440: stream proxy test suite
        // We override the addBaseTests method so that only
        // one test is run (we just test that the input/output are correctly forwarded, and
        // hence if the reading test succeeds/fails the other will also succeed/fail)
        final SftpProviderTestCase streamProxyTestCase = new SftpProviderTestCase(true);
        final ProviderTestSuite sftpStreamSuite = new BaseProviderTestSuite(streamProxyTestCase) {
            @Override
            protected void addBaseTests() throws Exception {
                // Just tries to read
                addTests(ProviderReadTests.class);
            }
        };
        suite.addTest(sftpStreamSuite);

        // Decorate the test suite to set up the Sftp server
        final TestSetup setup = new TestSetup(suite) {
            @Override
            protected void setUp() throws Exception {
                if (getSystemTestUriOverride() == null) {
                    setUpClass();
                }
                super.setUp();
            }

            @Override
            protected void tearDown() throws Exception {
                // Close SFTP server if needed
                tearDownClass();
                super.tearDown();
            }
        };

        return setup;
    }

    /**
     * Stops the embedded Apache SSHd Server (MINA).
     *
     * @throws InterruptedException
     */
    private static void tearDownClass() throws InterruptedException {
        if (Server != null) {
            Server.stop();
        }
    }

    public SftpProviderTestCase(final boolean streamProxyMode) throws IOException {
        this.streamProxyMode = streamProxyMode;
    }

    /**
     * Returns the base folder for tests.
     */
    @Override
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
        String uri = getSystemTestUriOverride();
        if (uri == null) {
            uri = ConnectionUri;
        }

        final FileSystemOptions fileSystemOptions = new FileSystemOptions();
        final SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();
        builder.setStrictHostKeyChecking(fileSystemOptions, "no");
        builder.setUserInfo(fileSystemOptions, new TrustEveryoneUserInfo());
        builder.setIdentityRepositoryFactory(fileSystemOptions, new TestIdentityRepositoryFactory());

        if (streamProxyMode) {
            final FileSystemOptions proxyOptions = (FileSystemOptions) fileSystemOptions.clone();

            final URI parsedURI = new URI(uri);
            final String userInfo = parsedURI.getUserInfo();
            final String[] userFields = userInfo.split(":", 2);

            builder.setProxyType(fileSystemOptions, SftpFileSystemConfigBuilder.PROXY_STREAM);
            builder.setProxyUser(fileSystemOptions, userFields[0]);
            if (userFields.length > 1) {
                builder.setProxyPassword(fileSystemOptions, userFields[1]);
            }
            builder.setProxyHost(fileSystemOptions, parsedURI.getHost());
            builder.setProxyPort(fileSystemOptions, parsedURI.getPort());
            builder.setProxyCommand(fileSystemOptions, SftpStreamProxy.NETCAT_COMMAND);
            builder.setProxyOptions(fileSystemOptions, proxyOptions);
            builder.setProxyPassword(fileSystemOptions, parsedURI.getAuthority());

            // Set up the new URI
            uri = String.format("sftp://%s@localhost:%d", userInfo, parsedURI.getPort());
        }

        final FileObject fileObject = manager.resolveFile(uri, fileSystemOptions);
        this.filesystem = (SftpFileSystem) fileObject.getFileSystem();
        return fileObject;
    }

    /**
     * Prepares the file system manager.
     */
    @Override
    public void prepare(final DefaultFileSystemManager manager) throws Exception {
        manager.addProvider("sftp", new SftpFileProvider());
    }

    /**
     * The command factory for the SSH server: Handles these commands
     * <p>
     * <li><code>id -u</code> (permissions test)</li>
     * <li><code>id -G</code> (permission tests)</li>
     * <li><code>nc -q 0 localhost port</code> (Stream proxy tests)</li>
     * </p>
     */
    private static class TestCommandFactory extends ScpCommandFactory {

        public static final Pattern NETCAT_COMMAND = Pattern.compile("nc -q 0 localhost (\\d+)");

        @Override
        public Command createCommand(final String command) {
            return new Command() {
                public ExitCallback callback = null;
                public OutputStream out = null;
                public OutputStream err = null;
                public InputStream in = null;

                @Override
                public void setInputStream(final InputStream in) {
                    this.in = in;
                }

                @Override
                public void setOutputStream(final OutputStream out) {
                    this.out = out;
                }

                @Override
                public void setErrorStream(final OutputStream err) {
                    this.err = err;
                }

                @Override
                public void setExitCallback(final ExitCallback callback) {
                    this.callback = callback;

                }

                @Override
                public void start(final Environment env) throws IOException {
                    int code = 0;
                    if (command.equals("id -G") || command.equals("id -u")) {
                        new PrintStream(out).println(0);
                    } else if (NETCAT_COMMAND.matcher(command).matches()) {
                        final Matcher matcher = NETCAT_COMMAND.matcher(command);
                        matcher.matches();
                        final int port = Integer.parseInt(matcher.group(1));

                        final Socket socket = new Socket((String) null, port);

                        if (out != null) {
                            connect("from nc", socket.getInputStream(), out, null);
                        }

                        if (in != null) {
                            connect("to nc", in, socket.getOutputStream(), callback);
                        }

                        return;

                    } else {
                        if (err != null) {
                            new PrintStream(err).format("Unknown command %s%n", command);
                        }
                        code = -1;
                    }

                    if (out != null) {
                        out.flush();
                    }
                    if (err != null) {
                        err.flush();
                    }
                    callback.onExit(code);
                }

                @Override
                public void destroy() {
                }
            };
        }
    }

    /**
     * Creates a pipe thread that connects an input to an output
     *
     * @param name The name of the thread (for debugging purposes)
     * @param in The input stream
     * @param out The output stream
     * @param callback An object whose method {@linkplain ExitCallback#onExit(int)} will be called when the pipe is
     *            broken. The integer argument is 0 if everything went well.
     */
    private static void connect(final String name, final InputStream in, final OutputStream out,
            final ExitCallback callback) {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int code = 0;
                try {
                    final byte buffer[] = new byte[1024];
                    int len;
                    while ((len = in.read(buffer, 0, buffer.length)) != -1) {
                        out.write(buffer, 0, len);
                        out.flush();
                    }
                } catch (final SshException ex) {
                    // Nothing to do, this occurs when the connection
                    // is closed on the remote side
                } catch (final IOException ex) {
                    if (!ex.getMessage().equals("Pipe closed")) {
                        code = -1;
                    }
                }
                if (callback != null) {
                    callback.onExit(code);
                }
            }
        }, name);
        thread.setDaemon(true);
        thread.start();
    }

    private static class SftpAttrs {
        int flags = 0;
        private int uid;
        long size = 0;
        private int gid;
        private int atime;
        private int permissions;
        private int mtime;
        private String[] extended;

        private SftpAttrs(final Buffer buf) {
            int flags = 0;
            flags = buf.getInt();

            if ((flags & SftpATTRS.SSH_FILEXFER_ATTR_SIZE) != 0) {
                size = buf.getLong();
            }
            if ((flags & SftpATTRS.SSH_FILEXFER_ATTR_UIDGID) != 0) {
                uid = buf.getInt();
                gid = buf.getInt();
            }
            if ((flags & SftpATTRS.SSH_FILEXFER_ATTR_PERMISSIONS) != 0) {
                permissions = buf.getInt();
            }
            if ((flags & SftpATTRS.SSH_FILEXFER_ATTR_ACMODTIME) != 0) {
                atime = buf.getInt();
            }
            if ((flags & SftpATTRS.SSH_FILEXFER_ATTR_ACMODTIME) != 0) {
                mtime = buf.getInt();
            }

        }
    }

    private static class MySftpSubsystem extends SftpSubsystem {
        TreeMap<String, Integer> permissions = new TreeMap<>();
        private int _version;

        @Override
        protected void process(final Buffer buffer) throws IOException {
            final int rpos = buffer.rpos();
            final int length = buffer.getInt();
            final int type = buffer.getByte();
            final int id = buffer.getInt();

            switch (type) {
            case SSH_FXP_SETSTAT:
            case SSH_FXP_FSETSTAT: {
                // Get the path
                final String path = buffer.getString();
                // Get the permission
                final SftpAttrs attrs = new SftpAttrs(buffer);
                permissions.put(path, attrs.permissions);
                // System.err.format("Setting [%s] permission to %o%n", path, attrs.permissions);
                break;
            }

            case SSH_FXP_REMOVE: {
                // Remove cached attributes
                final String path = buffer.getString();
                permissions.remove(path);
                // System.err.format("Removing [%s] permission cache%n", path);
                break;
            }

            case SSH_FXP_INIT: {
                // Just grab the version here
                this._version = id;
                break;
            }
            }

            buffer.rpos(rpos);
            super.process(buffer);

        }

        @Override
        protected void writeAttrs(final Buffer buffer, final SshFile file, final int flags) throws IOException {
            if (!file.doesExist()) {
                throw new FileNotFoundException(file.getAbsolutePath());
            }

            int p = 0;

            final Integer cached = permissions.get(file.getAbsolutePath());
            if (cached != null) {
                // Use cached permissions
                // System.err.format("Using cached [%s] permission of %o%n", file.getAbsolutePath(), cached);
                p |= cached;
            } else {
                // Use permissions from Java file
                if (file.isReadable()) {
                    p |= S_IRUSR;
                }
                if (file.isWritable()) {
                    p |= S_IWUSR;
                }
                if (file.isExecutable()) {
                    p |= S_IXUSR;
                }
            }

            if (_version >= 4) {
                final long size = file.getSize();
                // String username = session.getUsername();
                final long lastModif = file.getLastModified();
                if (file.isFile()) {
                    buffer.putInt(SSH_FILEXFER_ATTR_PERMISSIONS);
                    buffer.putByte((byte) SSH_FILEXFER_TYPE_REGULAR);
                    buffer.putInt(p);
                } else if (file.isDirectory()) {
                    buffer.putInt(SSH_FILEXFER_ATTR_PERMISSIONS);
                    buffer.putByte((byte) SSH_FILEXFER_TYPE_DIRECTORY);
                    buffer.putInt(p);
                } else {
                    buffer.putInt(0);
                    buffer.putByte((byte) SSH_FILEXFER_TYPE_UNKNOWN);
                }
            } else {
                if (file.isFile()) {
                    p |= 0100000;
                }
                if (file.isDirectory()) {
                    p |= 0040000;
                }

                if (file.isFile()) {
                    buffer.putInt(SSH_FILEXFER_ATTR_SIZE | SSH_FILEXFER_ATTR_PERMISSIONS | SSH_FILEXFER_ATTR_ACMODTIME);
                    buffer.putLong(file.getSize());
                    buffer.putInt(p);
                    buffer.putInt(file.getLastModified() / 1000);
                    buffer.putInt(file.getLastModified() / 1000);
                } else if (file.isDirectory()) {
                    buffer.putInt(SSH_FILEXFER_ATTR_PERMISSIONS | SSH_FILEXFER_ATTR_ACMODTIME);
                    buffer.putInt(p);
                    buffer.putInt(file.getLastModified() / 1000);
                    buffer.putInt(file.getLastModified() / 1000);
                } else {
                    buffer.putInt(0);
                }
            }
        }

    }
}
