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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.Strings;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.UserAuthFactory;
import org.apache.sshd.server.auth.UserAuthNoneFactory;
import org.apache.sshd.server.auth.pubkey.AcceptAllPublickeyAuthenticator;
import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.command.CommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpFileSystemAccessor;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.apache.sshd.sftp.server.SftpSubsystemProxy;

/**
 * Helper class to start and stop an embedded Apache SSHd (MINA) server for SFTP testing.
 */
public final class SftpTestServerHelper {

    private static final String DEFAULT_USER = "testuser";
    private static final int TEST_UID = 1000;
    private static final int TEST_GID = 1000;
    private static SshServer server;
    private static String connectionUri;

    private SftpTestServerHelper() {
    }

    /**
     * Custom {@link SftpFileSystemAccessor} that tracks POSIX permissions in memory so that
     * {@code setExecutable}/{@code setReadable}/{@code setWritable} round-trip correctly on
     * platforms without native POSIX support (e.g.&nbsp;Windows).
     */
    private static class TestSftpFileSystemAccessor implements SftpFileSystemAccessor {
        private static final ConcurrentHashMap<String, Set<PosixFilePermission>> permissionsCache =
                new ConcurrentHashMap<>();

        @Override
        public void setFilePermissions(
                final SftpSubsystemProxy subsystem, final Path file,
                final Set<PosixFilePermission> perms, final LinkOption... options) throws IOException {
            permissionsCache.put(file.toAbsolutePath().toString(), new HashSet<>(perms));
            try {
                SftpFileSystemAccessor.super.setFilePermissions(subsystem, file, perms, options);
            } catch (final UnsupportedOperationException | IOException ignored) {
                // Silently swallow on systems without native POSIX support
            }
        }

        @Override
        public void setFileOwner(
                final SftpSubsystemProxy subsystem, final Path file,
                final Principal owner, final LinkOption... options) throws IOException {
            // No-op: the test server always reports TEST_UID/TEST_GID via
            // resolveReportedFileAttributes, so the client sends those back
            // in setStat. Silently ignore to avoid chown failures on
            // non-root Unix processes.
        }

        @Override
        public void setGroupOwner(
                final SftpSubsystemProxy subsystem, final Path file,
                final Principal group, final LinkOption... options) throws IOException {
            // No-op: same rationale as setFileOwner.
        }

        @Override
        public Map<String, ?> readFileAttributes(
                final SftpSubsystemProxy subsystem, final Path file,
                final String view, final LinkOption... options) throws IOException {
            try {
                return SftpFileSystemAccessor.super.readFileAttributes(subsystem, file, view, options);
            } catch (final UnsupportedOperationException | IllegalArgumentException e) {
                if (view.startsWith("unix:") || view.startsWith("posix:")) {
                    final Map<String, Object> attrs = new HashMap<>(
                            Files.readAttributes(file, "basic:*", options));
                    final Set<PosixFilePermission> cached =
                            permissionsCache.get(file.toAbsolutePath().toString());
                    attrs.put("permissions", cached != null ? cached : defaultPermissions(file, options));
                    attrs.put("uid", TEST_UID);
                    attrs.put("gid", TEST_GID);
                    return attrs;
                }
                throw e;
            }
        }

        @Override
        public NavigableMap<String, Object> resolveReportedFileAttributes(
                final SftpSubsystemProxy subsystem, final Path file, final int flags,
                final NavigableMap<String, Object> attrs, final LinkOption... options) throws IOException {
            // Always override uid/gid to match TestCommandFactory's "id -u"/"id -G" responses,
            // so that PosixPermissions owner checks are consistent across all platforms.
            attrs.put("uid", TEST_UID);
            attrs.put("gid", TEST_GID);
            final Set<PosixFilePermission> cached =
                    permissionsCache.get(file.toAbsolutePath().toString());
            if (cached != null) {
                attrs.put("permissions", cached);
            } else if (!attrs.containsKey("permissions")) {
                attrs.put("permissions", defaultPermissions(file, options));
            }
            return attrs;
        }

        @Override
        public void removeFile(
                final SftpSubsystemProxy subsystem, final Path path,
                final boolean isDirectory) throws IOException {
            permissionsCache.remove(path.toAbsolutePath().toString());
            SftpFileSystemAccessor.super.removeFile(subsystem, path, isDirectory);
        }

        private static Set<PosixFilePermission> defaultPermissions(
                final Path file, final LinkOption... options) {
            final Set<PosixFilePermission> perms = EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE);
            try {
                if (Files.isDirectory(file, options)) {
                    perms.add(PosixFilePermission.OWNER_EXECUTE);
                }
            } catch (final Exception ignored) {
                // ignore
            }
            return perms;
        }

        static void clearCache() {
            permissionsCache.clear();
        }
    }

    /**
     * Command factory for handling special commands needed by VFS tests.
     */
    private static class TestCommandFactory implements CommandFactory {
        @Override
        public Command createCommand(final ChannelSession channel, final String command) throws IOException {
            if (command.startsWith("id -u")) {
                return createIdCommand("1000");
            }
            if (command.startsWith("id -G")) {
                return createIdCommand("1000 1001 1002");
            }
            throw new IOException("Unknown command: " + command);
        }

        private Command createIdCommand(final String output) {
            return new Command() {
                private ExitCallback callback;
                private OutputStream out;

                @Override
                public void destroy(final ChannelSession channel) throws Exception {
                }

                @Override
                public void setErrorStream(final OutputStream err) {
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
                public void start(final ChannelSession channel, final Environment env) throws IOException {
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
            return;
        }

        final Path tmpDir = PathUtils.getTempDirectory();
        server = SshServer.setUpDefaultServer();
        server.setPort(0);

        final SimpleGeneratorHostKeyProvider keyProvider =
                new SimpleGeneratorHostKeyProvider(tmpDir.resolve("hostkey.ser"));
        keyProvider.setAlgorithm("RSA");
        server.setKeyPairProvider(keyProvider);

        final SftpSubsystemFactory sftpFactory = new SftpSubsystemFactory();
        sftpFactory.setFileSystemAccessor(new TestSftpFileSystemAccessor());
        server.setSubsystemFactories(Collections.singletonList(sftpFactory));

        server.setPasswordAuthenticator(
                (username, password, session) -> Strings.CS.equals(username, password));
        server.setPublickeyAuthenticator(AcceptAllPublickeyAuthenticator.INSTANCE);

        server.setCommandFactory(new TestCommandFactory());

        final File homeDir = getTestDirectoryFile();
        server.setFileSystemFactory(
                new VirtualFileSystemFactory(homeDir.toPath().toAbsolutePath()));

        server.start();

        final List<UserAuthFactory> authFactories = new ArrayList<>(server.getUserAuthFactories());
        authFactories.add(UserAuthNoneFactory.INSTANCE);
        server.setUserAuthFactories(authFactories);

        final int socketPort = server.getPort();
        connectionUri = String.format("sftp://%s@localhost:%d", DEFAULT_USER, socketPort);
    }

    /**
     * Stops the embedded SFTP server.
     *
     * @throws InterruptedException if interrupted while stopping
     */
    public static synchronized void stopServer() throws InterruptedException {
        if (server != null) {
            try {
                server.close();
            } catch (final IOException e) {
                // ignore
            }
            server = null;
            connectionUri = null;
            TestSftpFileSystemAccessor.clearCache();
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
