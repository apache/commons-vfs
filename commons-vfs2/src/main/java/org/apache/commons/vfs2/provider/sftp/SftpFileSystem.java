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
package org.apache.commons.vfs2.provider.sftp;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.provider.GenericFileName;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * Represents the files on an SFTP server.
 */
public class SftpFileSystem extends AbstractFileSystem {

    private static final Log LOG = LogFactory.getLog(SftpFileSystem.class);

    private static final int UNIDENTIFED = -1;

    private static final int SLEEP_MILLIS = 100;

    private static final int EXEC_BUFFER_SIZE = 128;

    private static final long LAST_MOD_TIME_ACCURACY = 1000L;

    /**
     * Session; never null.
     * <p>
     * DCL pattern requires that the ivar be volatile.
     * </p>
     */
    private volatile Session session;

    private volatile ChannelSftp idleChannel;

    private final int connectTimeoutMillis;

    /**
     * Cache for the user ID (-1 when not set)
     * <p>
     * DCL pattern requires that the ivar be volatile.
     * </p>
     */
    private volatile int uid = UNIDENTIFED;

    /**
     * Cache for the user groups ids (null when not set)
     * <p>
     * DCL pattern requires that the ivar be volatile.
     * </p>
     */
    private volatile int[] groupsIds;

    /**
     * Some SFTP-only servers disable the exec channel. When exec is disabled, things like getUId() will always fail.
     */
    private final boolean execDisabled;

    protected SftpFileSystem(final GenericFileName rootName, final Session session,
            final FileSystemOptions fileSystemOptions) {
        super(rootName, null, fileSystemOptions);
        this.session = Objects.requireNonNull(session, "session");
        this.connectTimeoutMillis = SftpFileSystemConfigBuilder.getInstance()
                .getConnectTimeoutMillis(fileSystemOptions);

        if (SftpFileSystemConfigBuilder.getInstance().isDisableDetectExecChannel(fileSystemOptions)) {
            this.execDisabled = true;
        } else {
            this.execDisabled = detectExecDisabled();
        }
    }

    @Override
    protected void doCloseCommunicationLink() {
        if (idleChannel != null) {
            synchronized (this) {
                if (idleChannel != null) {
                    idleChannel.disconnect();
                    idleChannel = null;
                }
            }
        }

        if (session != null) {
            session.disconnect();
        }
    }

    /**
     * Returns an SFTP channel to the server.
     *
     * @return new or reused channel, never null.
     * @throws FileSystemException if a session cannot be created.
     * @throws IOException         if an I/O error is detected.
     */
    protected ChannelSftp getChannel() throws IOException {
        try {
            // Use the pooled channel, or create a new one
            ChannelSftp channel = null;
            if (idleChannel != null) {
                synchronized (this) {
                    if (idleChannel != null) {
                        channel = idleChannel;
                        idleChannel = null;
                    }
                }
            } else {
                channel = (ChannelSftp) getSession().openChannel("sftp");
                channel.connect(connectTimeoutMillis);
                final Boolean userDirIsRoot = SftpFileSystemConfigBuilder.getInstance()
                        .getUserDirIsRoot(getFileSystemOptions());
                final String workingDirectory = getRootName().getPath();
                if (workingDirectory != null && (userDirIsRoot == null || !userDirIsRoot.booleanValue())) {
                    try {
                        channel.cd(workingDirectory);
                    } catch (final SftpException e) {
                        throw new FileSystemException("vfs.provider.sftp/change-work-directory.error", workingDirectory,
                                e);
                    }
                }
            }

            final String fileNameEncoding = SftpFileSystemConfigBuilder.getInstance()
                    .getFileNameEncoding(getFileSystemOptions());

            if (fileNameEncoding != null) {
                try {
                    channel.setFilenameEncoding(fileNameEncoding);
                } catch (final SftpException e) {
                    throw new FileSystemException("vfs.provider.sftp/filename-encoding.error", fileNameEncoding);
                }
            }
            return channel;
        } catch (final JSchException e) {
            throw new FileSystemException("vfs.provider.sftp/connect.error", getRootName(), e);
        }
    }

    /**
     * Ensures that the session link is established.
     *
     * @throws FileSystemException if a session cannot be created.
     */
    private Session getSession() throws FileSystemException {
        if (!this.session.isConnected()) {
            synchronized (this) {
                if (!this.session.isConnected()) {
                    doCloseCommunicationLink();
                    this.session = SftpFileProvider.createSession((GenericFileName) getRootName(),
                            getFileSystemOptions());
                }
            }
        }
        return this.session;
    }

    /**
     * Returns a channel to the pool.
     *
     * @param channel the used channel.
     */
    protected void putChannel(final ChannelSftp channel) {
        if (idleChannel == null) {
            synchronized (this) {
                if (idleChannel == null) {
                    // put back the channel only if it is still connected
                    if (channel.isConnected() && !channel.isClosed()) {
                        idleChannel = channel;
                    }
                } else {
                    channel.disconnect();
                }
            }
        } else {
            channel.disconnect();
        }
    }

    /**
     * Adds the capabilities of this file system.
     */
    @Override
    protected void addCapabilities(final Collection<Capability> caps) {
        caps.addAll(SftpFileProvider.capabilities);
    }

    /**
     * Creates a file object. This method is called only if the requested file is not cached.
     */
    @Override
    protected FileObject createFile(final AbstractFileName name) throws FileSystemException {
        return new SftpFileObject(name, this);
    }

    /**
     * Last modification time is only an int and in seconds, thus can be off by 999.
     *
     * @return 1000
     */
    @Override
    public double getLastModTimeAccuracy() {
        return LAST_MOD_TIME_ACCURACY;
    }

    /**
     * Gets the (numeric) group IDs.
     *
     * @return the (numeric) group IDs.
     * @throws JSchException If a problem occurs while retrieving the group IDs.
     * @throws IOException   if an I/O error is detected.
     * @since 2.1
     */
    public int[] getGroupsIds() throws JSchException, IOException {
        if (groupsIds == null) {
            synchronized (this) {
                // DCL pattern requires that the ivar be volatile.
                if (groupsIds == null) {
                    final StringBuilder output = new StringBuilder();
                    final int code = executeCommand("id -G", output);
                    if (code != 0) {
                        throw new JSchException(
                                "Could not get the groups id of the current user (error code: " + code + ")");
                    }
                    // Retrieve the different groups
                    final String[] groups = output.toString().trim().split("\\s+");

                    final int[] groupsIds = new int[groups.length];
                    for (int i = 0; i < groups.length; i++) {
                        groupsIds[i] = Integer.parseInt(groups[i]);
                    }
                    this.groupsIds = groupsIds;

                }
            }
        }
        return groupsIds;
    }

    /**
     * Gets the (numeric) group IDs.
     *
     * @return The numeric user ID
     * @throws JSchException If a problem occurs while retrieving the group ID.
     * @throws IOException   if an I/O error is detected.
     * @since 2.1
     */
    public int getUId() throws JSchException, IOException {
        if (uid == UNIDENTIFED) {
            synchronized (this) {
                if (uid == UNIDENTIFED) {
                    final StringBuilder output = new StringBuilder();
                    final int code = executeCommand("id -u", output);
                    if (code != 0) {
                        throw new FileSystemException(
                                "Could not get the user id of the current user (error code: " + code + ")");
                    }
                    final String uidString = output.toString().trim();
                    try {
                        uid = Integer.parseInt(uidString);
                    } catch (final NumberFormatException e) {
                        LOG.debug("Cannot convert UID to integer: '" + uidString + "'", e);
                    }
                }
            }
        }
        return uid;
    }

    /**
     * Executes a command and returns the (standard) output through a StringBuilder.
     *
     * @param command The command
     * @param output  The output
     * @return The exit code of the command
     * @throws JSchException       if a JSch error is detected.
     * @throws FileSystemException if a session cannot be created.
     * @throws IOException         if an I/O error is detected.
     */
    private int executeCommand(final String command, final StringBuilder output) throws JSchException, IOException {
        final ChannelExec channel = (ChannelExec) getSession().openChannel("exec");
        try {
            channel.setCommand(command);
            channel.setInputStream(null);
            try (final InputStreamReader stream = new InputStreamReader(channel.getInputStream())) {
                channel.setErrStream(System.err, true);
                channel.connect(connectTimeoutMillis);

                // Read the stream
                final char[] buffer = new char[EXEC_BUFFER_SIZE];
                int read;
                while ((read = stream.read(buffer, 0, buffer.length)) >= 0) {
                    output.append(buffer, 0, read);
                }
            }

            // Wait until the command finishes (should not be long since we read the output stream)
            while (!channel.isClosed()) {
                try {
                    Thread.sleep(SLEEP_MILLIS);
                } catch (final Exception ee) {
                    // TODO: swallow exception, really?
                }
            }
        } finally {
            channel.disconnect();
        }
        return channel.getExitStatus();
    }

    /**
     * @return Whether the exec channel is disabled.
     * @see SftpFileSystem#execDisabled
     */
    public boolean isExecDisabled() {
        return execDisabled;
    }

    /**
     * Some SFTP-only servers disable the exec channel.
     *
     * Attempt to detect this by calling getUid.
     */
    private boolean detectExecDisabled() {
        try {
            return getUId() == UNIDENTIFED;
        } catch(JSchException | IOException e) {
            LOG.debug("Cannot get UID, assuming no exec channel is present", e);
            return true;
        }
    }

}
