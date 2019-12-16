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

import org.apache.commons.vfs2.test.PermissionsTests;

import junit.framework.Test;

public class SftpProviderTestCase extends AbstractSftpProviderTestCase {

    @Override
    protected boolean isExecChannelClosed() {
        return false;
    }

    /**
     * Creates the test suite for the sftp file system.
     */
    public static Test suite() throws Exception {
        final SftpProviderTestSuite suite = new SftpProviderTestSuite(new SftpProviderTestCase());
        // VFS-405: set/get permissions
        suite.addTests(PermissionsTests.class);
        return suite;
    }

<<<<<<< a62149bf5bd4f304abab90b90a46d41d07cd32ec
    public SftpProviderTestCase(final boolean streamProxyMode) {
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
            final String[] userFields = userInfo == null ? null : userInfo.split(":", 2);

            builder.setProxyType(fileSystemOptions, SftpFileSystemConfigBuilder.PROXY_STREAM);
            if (userFields != null) {
                if (userFields.length > 0) {
                    builder.setProxyUser(fileSystemOptions, userFields[0]);
                }
                if (userFields.length > 1) {
                    builder.setProxyPassword(fileSystemOptions, userFields[1]);
                }
            }
            builder.setProxyHost(fileSystemOptions, parsedURI.getHost());
            builder.setProxyPort(fileSystemOptions, parsedURI.getPort());
            builder.setProxyCommand(fileSystemOptions, SftpStreamProxy.NETCAT_COMMAND);
            builder.setProxyOptions(fileSystemOptions, proxyOptions);
            builder.setProxyPassword(fileSystemOptions, parsedURI.getAuthority());

            // Set up the new URI
            if (userInfo == null) {
                uri = String.format("sftp://localhost:%d", parsedURI.getPort());
            } else {
                uri = String.format("sftp://%s@localhost:%d", userInfo, parsedURI.getPort());
            }
        }

        final FileObject fileObject = manager.resolveFile(uri, fileSystemOptions);
        this.fileSystem = (SftpFileSystem) fileObject.getFileSystem();
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
     * <li>{@code id -u} (permissions test)</li>
     * <li>{@code id -G} (permission tests)</li>
     * <li>{@code nc -q 0 localhost port} (Stream proxy tests)</li>
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
     * @param name     The name of the thread (for debugging purposes)
     * @param in       The input stream
     * @param out      The output stream
     * @param callback An object whose method {@linkplain ExitCallback#onExit(int)} will be called when the pipe is
     *                 broken. The integer argument is 0 if everything went well.
     */
    private static void connect(final String name, final InputStream in, final OutputStream out,
            final ExitCallback callback) {
        final Thread thread = new Thread((Runnable) () -> {
            int code = 0;
            try {
                final byte buffer[] = new byte[1024];
                int len;
                while ((len = in.read(buffer, 0, buffer.length)) != -1) {
                    out.write(buffer, 0, len);
                    out.flush();
                }
            } catch (final SshException ex1) {
                // Nothing to do, this occurs when the connection
                // is closed on the remote side
            } catch (final IOException ex2) {
                if (!ex2.getMessage().equals("Pipe closed")) {
                    code = -1;
                }
            }
            if (callback != null) {
                callback.onExit(code);
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
=======
>>>>>>> VFS-590 VFS-617 Proceed with moveTo operation if the exec channel for permission checks is closed
}
