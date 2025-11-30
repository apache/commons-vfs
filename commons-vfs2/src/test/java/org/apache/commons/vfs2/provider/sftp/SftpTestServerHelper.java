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
package org.apache.commons.vfs2.provider.sftp;

import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectoryFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.Strings;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.Session;
import org.apache.sshd.common.session.AbstractSession;
import org.apache.sshd.common.util.Buffer;
import org.apache.sshd.common.util.SecurityUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.FileSystemFactory;
import org.apache.sshd.server.FileSystemView;
import org.apache.sshd.server.ForwardingFilter;
import org.apache.sshd.server.SshFile;
import org.apache.sshd.server.auth.UserAuthNone;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.filesystem.NativeSshFile;
import org.apache.sshd.server.keyprovider.PEMGeneratorHostKeyProvider;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;

/**
 * Helper class to start and stop an embedded Apache SSHd (MINA) server for SFTP testing.
 */
public final class SftpTestServerHelper {

    private static final String DEFAULT_USER = "testuser";
    private static SshServer server;
    private static String connectionUri;

    private SftpTestServerHelper() {
        // Utility class
    }

    /**
     * Custom SFTP subsystem that handles permissions.
     */
    private static class MySftpSubsystem extends SftpSubsystem {
        // Static permissions cache shared across all SFTP subsystem instances
        private static final TreeMap<String, Integer> permissions = new TreeMap<>();
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
                    synchronized (permissions) {
                        permissions.put(path, attrs.permissions);
                    }
                    break;
                }

                case SSH_FXP_REMOVE: {
                    // Remove cached attributes
                    final String path = buffer.getString();
                    synchronized (permissions) {
                        permissions.remove(path);
                    }
                    break;
                }

                case SSH_FXP_INIT: {
                    // Just grab the version here
                    _version = id;
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

            final Integer cached;
            synchronized (permissions) {
                cached = permissions.get(file.getAbsolutePath());
            }
            if (cached != null) {
                // Use cached permissions
                p |= cached;
            } else {
                // Default permissions for testing: always readable and writable
                // This ensures tests work regardless of underlying file system permissions
                p |= S_IRUSR | S_IWUSR;

                // For directories, always add execute bit (needed to traverse/access the directory)
                // For files, add execute bit only if the file is actually executable
                if (file.isDirectory() || file.isExecutable()) {
                    p |= S_IXUSR;
                }
            }

            if (_version >= 4) {
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

                // SSH_FILEXFER_ATTR_UIDGID constant doesn't exist in SSHD 0.8.0, use literal value 0x00000002
                final int SSH_FILEXFER_ATTR_UIDGID = 0x00000002;
                // Use UID/GID 1000 to match what TestCommandFactory returns for "id -u" and "id -G"
                final int TEST_UID = 1000;
                final int TEST_GID = 1000;

                if (file.isFile()) {
                    buffer.putInt(SSH_FILEXFER_ATTR_SIZE | SSH_FILEXFER_ATTR_UIDGID | SSH_FILEXFER_ATTR_PERMISSIONS | SSH_FILEXFER_ATTR_ACMODTIME);
                    buffer.putLong(file.getSize());
                    buffer.putInt(TEST_UID); // UID - matches "id -u" output
                    buffer.putInt(TEST_GID); // GID - matches "id -G" output
                    buffer.putInt(p);
                    buffer.putInt(file.getLastModified() / 1000);
                    buffer.putInt(file.getLastModified() / 1000);
                } else if (file.isDirectory()) {
                    buffer.putInt(SSH_FILEXFER_ATTR_UIDGID | SSH_FILEXFER_ATTR_PERMISSIONS | SSH_FILEXFER_ATTR_ACMODTIME);
                    buffer.putInt(TEST_UID); // UID - matches "id -u" output
                    buffer.putInt(TEST_GID); // GID - matches "id -G" output
                    buffer.putInt(p);
                    buffer.putInt(file.getLastModified() / 1000);
                    buffer.putInt(file.getLastModified() / 1000);
                } else {
                    buffer.putInt(0);
                }
            }
        }
    }

    /**
     * SFTP attributes helper class.
     */
    private static class SftpAttrs {
        int flags;
        long size;
        int permissions;

        SftpAttrs(final Buffer buffer) {
            final int flags = buffer.getInt();
            this.flags = flags;
            if ((flags & SftpSubsystem.SSH_FILEXFER_ATTR_SIZE) != 0) {
                size = buffer.getLong();
            }
            // Note: SSH_FILEXFER_ATTR_UIDGID constant doesn't exist in SSHD 0.8.0
            // Skip UID/GID if present (flag value is 0x00000002)
            if ((flags & 0x00000002) != 0) {
                buffer.getInt(); // uid
                buffer.getInt(); // gid
            }
            if ((flags & SftpSubsystem.SSH_FILEXFER_ATTR_PERMISSIONS) != 0) {
                permissions = buffer.getInt();
            }
            if ((flags & SftpSubsystem.SSH_FILEXFER_ATTR_ACMODTIME) != 0) {
                buffer.getInt(); // atime
                buffer.getInt(); // mtime
            }
        }
    }

    /**
     * Simple file system factory that maps users to directories.
     */
    private static class TestFileSystemFactory implements FileSystemFactory {
        @Override
        public FileSystemView createFileSystemView(final Session session) throws IOException {
            final String userName = session.getUsername();
            if (!DEFAULT_USER.equals(userName)) {
                return null;
            }
            final File homeDir = getTestDirectoryFile();
            return new TestFileSystemView(homeDir.getAbsolutePath(), userName);
        }
    }

    /**
     * Simple file system view for testing.
     */
    private static class TestFileSystemView implements FileSystemView {
        private final String homeDirStr;
        private final String userName;

        TestFileSystemView(final String homeDirStr, final String userName) {
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
     * Extends NativeSshFile for testing.
     */
    private static class TestNativeSshFile extends NativeSshFile {
        TestNativeSshFile(final String fileName, final File file, final String userName) {
            super(fileName, file, userName);
        }
    }

    /**
     * Command factory for handling special commands.
     */
    private static class TestCommandFactory extends ScpCommandFactory {
        @Override
        public Command createCommand(final String command) {
            // Handle special commands for tests
            if (command.startsWith("id -u")) {
                return createIdCommand("1000");
            }
            if (command.startsWith("id -G")) {
                return createIdCommand("1000 1001 1002");
            }
            return super.createCommand(command);
        }

        private Command createIdCommand(final String output) {
            return new Command() {
                private ExitCallback callback;
                private OutputStream out;
                private OutputStream err;

                @Override
                public void destroy() {
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
                public void setInputStream(final InputStream in) {
                }

                @Override
                public void setOutputStream(final OutputStream out) {
                    this.out = out;
                }

                @Override
                public void start(final Environment env) throws IOException {
                    out.write((output + "\n").getBytes());
                    out.flush();
                    callback.onExit(0);
                }
            };
        }
    }

    /**
     * Starts the embedded SFTP server.
     *
     * @throws IOException if server cannot be started
     */
    public static synchronized void startServer() throws IOException {
        if (server != null) {
            return; // Already started
        }

        final Path tmpDir = PathUtils.getTempDirectory();
        server = SshServer.setUpDefaultServer();
        server.setPort(0); // Use any available port

        // Set up key provider
        if (SecurityUtils.isBouncyCastleRegistered()) {
            final Path keyFile = Files.createTempFile(tmpDir, "key", ".pem");
            keyFile.toFile().deleteOnExit();
            Files.delete(keyFile);
            final PEMGeneratorHostKeyProvider keyProvider = new PEMGeneratorHostKeyProvider(keyFile.toAbsolutePath().toString());
            server.setKeyPairProvider(keyProvider);
        } else {
            server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(tmpDir.resolve("key.ser").toString()));
        }

        // Set up SFTP subsystem
        final List<NamedFactory<Command>> list = new ArrayList<>(1);
        list.add(new NamedFactory<Command>() {
            @Override
            public Command create() {
                return new MySftpSubsystem();
            }

            @Override
            public String getName() {
                return "sftp";
            }
        });
        server.setSubsystemFactories(list);

        // Set up authentication
        server.setPasswordAuthenticator((username, password, session) -> Strings.CS.equals(username, password));
        server.setPublickeyAuthenticator((username, key, session) -> true);

        // Set up forwarding
        server.setForwardingFilter(new ForwardingFilter() {
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

        // Set up command factory
        server.setCommandFactory(new ScpCommandFactory(new TestCommandFactory()));

        // Set up file system
        server.setFileSystemFactory(new TestFileSystemFactory());

        // Start server
        server.start();

        final int socketPort = server.getPort();
        connectionUri = String.format("sftp://%s@localhost:%d", DEFAULT_USER, socketPort);

        // Allow no-auth for testing
        server.getUserAuthFactories().add(new UserAuthNone.Factory());
    }

    /**
     * Stops the embedded SFTP server.
     *
     * @throws InterruptedException if interrupted while stopping
     */
    public static synchronized void stopServer() throws InterruptedException {
        if (server != null) {
            // Close all active sessions
            for (final AbstractSession session : server.getActiveSessions()) {
                session.close(true);
            }
            server.stop();
            server = null;
            connectionUri = null;

            // Clear the permissions cache to avoid test interference
            MySftpSubsystem.permissions.clear();
        }
    }

    /**
     * Gets the connection URI for the embedded server.
     *
     * @return the connection URI, or null if server is not started
     */
    public static String getConnectionUri() {
        return connectionUri;
    }

    /**
     * Checks if the server is running.
     *
     * @return true if server is running
     */
    public static boolean isServerRunning() {
        return server != null;
    }
}
