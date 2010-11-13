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

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.provider.GenericFileName;

import java.io.IOException;
import java.util.Collection;

/**
 * Represents the files on an SFTP server.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class SftpFileSystem
    extends AbstractFileSystem
    implements FileSystem
{

    private Session session;
    // private final JSch jSch;
    private ChannelSftp idleChannel;

    protected SftpFileSystem(final GenericFileName rootName,
                             final Session session,
                             final FileSystemOptions fileSystemOptions)
    {
        super(rootName, null, fileSystemOptions);
        this.session = session;
    }

    @Override
    protected void doCloseCommunicationLink()
    {
        if (idleChannel != null)
        {
            idleChannel.disconnect();
            idleChannel = null;
        }

        if (session != null)
        {
            session.disconnect();
            session = null;
        }
    }

    /**
     * Returns an SFTP channel to the server.
     */
    protected ChannelSftp getChannel() throws IOException
    {
        if (this.session == null || !this.session.isConnected())
        {
            doCloseCommunicationLink();

            // channel closed. e.g. by freeUnusedResources, but now we need it again
            Session session;
            UserAuthenticationData authData = null;
            try
            {
                final GenericFileName rootName = (GenericFileName) getRootName();

                authData = UserAuthenticatorUtils.authenticate(getFileSystemOptions(),
                    SftpFileProvider.AUTHENTICATOR_TYPES);

                session = SftpClientFactory.createConnection(
                    rootName.getHostName(),
                    rootName.getPort(),
                    UserAuthenticatorUtils.getData(authData, UserAuthenticationData.USERNAME,
                        UserAuthenticatorUtils.toChar(rootName.getUserName())),
                    UserAuthenticatorUtils.getData(authData, UserAuthenticationData.PASSWORD,
                        UserAuthenticatorUtils.toChar(rootName.getPassword())),
                    getFileSystemOptions());
            }
            catch (final Exception e)
            {
                throw new FileSystemException("vfs.provider.sftp/connect.error",
                    getRootName(),
                    e);
            }
            finally
            {
                UserAuthenticatorUtils.cleanup(authData);
            }

            this.session = session;
        }

        try
        {
            // Use the pooled channel, or create a new one
            final ChannelSftp channel;
            if (idleChannel != null)
            {
                channel = idleChannel;
                idleChannel = null;
            }
            else
            {
                channel = (ChannelSftp) session.openChannel("sftp");
                channel.connect();

                Boolean userDirIsRoot =
                    SftpFileSystemConfigBuilder.getInstance().getUserDirIsRoot(getFileSystemOptions());
                String workingDirectory = getRootName().getPath();
                if (workingDirectory != null && (userDirIsRoot == null || !userDirIsRoot.booleanValue()))
                {
                    try
                    {
                        channel.cd(workingDirectory);
                    }
                    catch (SftpException e)
                    {
                        throw new FileSystemException("vfs.provider.sftp/change-work-directory.error",
                            workingDirectory);
                    }
                }
            }

            return channel;
        }
        catch (final JSchException e)
        {
            throw new FileSystemException("vfs.provider.sftp/connect.error",
                getRootName(),
                e);
        }
    }

    /**
     * Returns a channel to the pool.
     */
    protected void putChannel(final ChannelSftp channel)
    {
        if (idleChannel == null)
        {
            // put back the channel only if it is still connected
            if (channel.isConnected() && !channel.isClosed())
            {
                idleChannel = channel;
            }
        }
        else
        {
            channel.disconnect();
        }
    }

    /**
     * Adds the capabilities of this file system.
     */
    @Override
    protected void addCapabilities(final Collection<Capability> caps)
    {
        caps.addAll(SftpFileProvider.capabilities);
    }

    /**
     * Creates a file object.  This method is called only if the requested
     * file is not cached.
     */
    @Override
    protected FileObject createFile(final FileName name)
        throws FileSystemException
    {
        return new SftpFileObject(name, this);
    }

    /**
     * last mod time is only a int and in seconds, thus can be off by 999.
     *
     * @return 1000
     */
    @Override
    public double getLastModTimeAccuracy()
    {
        return 1000L;
    }
}
