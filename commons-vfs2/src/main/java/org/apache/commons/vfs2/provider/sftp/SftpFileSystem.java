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

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * Represents the files on an SFTP server.
 */
public class SftpFileSystem extends AbstractFileSystem {
    private static final int SLEEP_MILLIS = 100;

    private static final int EXEC_BUFFER_SIZE = 128;

    private static final long LAST_MOD_TIME_ACCURACY = 1000L;

    private Session session;

    // private final JSch jSch;

    private ChannelSftp idleChannel;

    /**
     * Cache for the user ID (-1 when not set)
     */
    private int uid = -1;

    /**
     * Cache for the user groups ids (null when not set)
     */
    private int[] groupsIds;

    protected SftpFileSystem(final GenericFileName rootName, final Session session,
            final FileSystemOptions fileSystemOptions) {
        super(rootName, null, fileSystemOptions);
        this.session = session;
    }

    @Override
    protected void doCloseCommunicationLink() {
        if (idleChannel != null) {
            idleChannel.disconnect();
            idleChannel = null;
        }

        if (session != null) {
            session.disconnect();
            session = null;
        }
    }

    /**
     * Returns an SFTP channel to the server.
     *
     * @return new or reused channel, never null.
     * @throws FileSystemException if a session cannot be created.
     * @throws IOException if an I/O error is detected.
     */
    protected ChannelSftp getChannel() throws IOException {
        ensureSession();
        try {
            // Use the pooled channel, or create a new one
            final ChannelSftp channel;
            if (idleChannel != null) {
                channel = idleChannel;
                idleChannel = null;
            } else {
                channel = (ChannelSftp) session.openChannel("sftp");
                channel.connect();
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
    private void ensureSession() throws FileSystemException {
        if (this.session == null || !this.session.isConnected()) {
            doCloseCommunicationLink();

            // channel closed. e.g. by freeUnusedResources, but now we need it again
            Session session;
            UserAuthenticationData authData = null;
            try {
                final GenericFileName rootName = (GenericFileName) getRootName();

                authData = UserAuthenticatorUtils.authenticate(getFileSystemOptions(),
                        SftpFileProvider.AUTHENTICATOR_TYPES);

                session = SftpClientFactory.createConnection(rootName.getHostName(), rootName.getPort(),
                        UserAuthenticatorUtils.getData(authData, UserAuthenticationData.USERNAME,
                                UserAuthenticatorUtils.toChar(rootName.getUserName())),
                        UserAuthenticatorUtils.getData(authData, UserAuthenticationData.PASSWORD,
                                UserAuthenticatorUtils.toChar(rootName.getPassword())),
                        getFileSystemOptions());
            } catch (final Exception e) {
                throw new FileSystemException("vfs.provider.sftp/connect.error", getRootName(), e);
            } finally {
                UserAuthenticatorUtils.cleanup(authData);
            }
            this.session = session;
        }
    }

    /**
     * Returns a channel to the pool.
     *
     * @param channel the used channel.
     */
    protected void putChannel(final ChannelSftp channel) {
        if (idleChannel == null) {
            // put back the channel only if it is still connected
            if (channel.isConnected() && !channel.isClosed()) {
                idleChannel = channel;
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
     * @throws IOException if an I/O error is detected.
     * @since 2.1
     */
    public int[] getGroupsIds() throws JSchException, IOException {
        if (groupsIds == null) {
            final StringBuilder output = new StringBuilder();
            final int code = executeCommand("id -G", output);
            if (code != 0) {
                throw new JSchException("Could not get the groups id of the current user (error code: " + code + ")");
            }

            // Retrieve the different groups
            final String[] groups = output.toString().trim().split("\\s+");

            final int[] groupsIds = new int[groups.length];
            for (int i = 0; i < groups.length; i++) {
                groupsIds[i] = Integer.parseInt(groups[i]);
            }

            this.groupsIds = groupsIds;
        }
        return groupsIds;
    }

    /**
     * Get the (numeric) group IDs.
     *
     * @return The numeric user ID
     * @throws JSchException If a problem occurs while retrieving the group ID.
     * @throws IOException if an I/O error is detected.
     * @since 2.1
     */
    public int getUId() throws JSchException, IOException {
        if (uid < 0) {
            final StringBuilder output = new StringBuilder();
            final int code = executeCommand("id -u", output);
            if (code != 0) {
                throw new FileSystemException(
                        "Could not get the user id of the current user (error code: " + code + ")");
            }
            uid = Integer.parseInt(output.toString().trim());
        }
        return uid;
    }

    /**
     * Execute a command and returns the (standard) output through a StringBuilder.
     *
     * @param command The command
     * @param output The output
     * @return The exit code of the command
     * @throws JSchException if a JSch error is detected.
     * @throws FileSystemException if a session cannot be created.
     * @throws IOException if an I/O error is detected.
     */
    private int executeCommand(final String command, final StringBuilder output) throws JSchException, IOException {
        ensureSession();
        final ChannelExec channel = (ChannelExec) session.openChannel("exec");

        channel.setCommand(command);
        channel.setInputStream(null);
        try (final InputStreamReader stream = new InputStreamReader(channel.getInputStream())) {
            channel.setErrStream(System.err, true);
            channel.connect();

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
        channel.disconnect();
        return channel.getExitStatus();
    }
}
