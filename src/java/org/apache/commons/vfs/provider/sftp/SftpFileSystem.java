/*
 * Copyright 2003,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs.provider.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.GenericFileName;

import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;

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
        if (this.session == null)
        {
            // channel closed. e.g. by freeUnusedResources, but now we need it again
            Session session;
            try
            {
                final GenericFileName rootName = (GenericFileName) getRootName();

                session = SftpClientFactory.createConnection(rootName.getHostName(),
                    rootName.getPort(),
                    rootName.getUserName(),
                    rootName.getPassword(),
                    getFileSystemOptions());

                Hashtable config = null;

                String compression = SftpFileSystemConfigBuilder.getInstance().getCompression(getFileSystemOptions());
                if (compression != null)
                {
                    if (config == null)
                    {
                        config = new Hashtable();
                    }
                    config.put("compression.c2s", compression);
                    config.put("compression.s2c", compression);
                }

                if (config != null)
                {
                    session.setConfig(config);
                }
            }
            catch (final Exception e)
            {
                throw new FileSystemException("vfs.provider.sftp/connect.error",
                    getRootName(),
                    e);
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
            idleChannel = channel;
        }
        else
        {
            channel.disconnect();
        }
    }

    /**
     * Adds the capabilities of this file system.
     */
    protected void addCapabilities(final Collection caps)
    {
        caps.addAll(SftpFileProvider.capabilities);
    }

    /**
     * Creates a file object.  This method is called only if the requested
     * file is not cached.
     */
    protected FileObject createFile(final FileName name)
        throws FileSystemException
    {
        return new SftpFileObject(name, this);
    }

    /**
     * last mod time is only a int and in seconds, thus can be off by 999
     *
     * @return 1000
     */
    public double getLastModTimeAccuracy()
    {
        return 1000L;
    }
}
