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
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.IOException;
import java.util.Collection;
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.GenericFileName;

/**
 * Represents the files on an SFTP server.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.6 $ $Date: 2004/02/28 03:35:51 $
 */
class SftpFileSystem
    extends AbstractFileSystem
    implements FileSystem
{
    private Session session;
    private final JSch jSch;
    private ChannelSftp idleChannel;

    public SftpFileSystem( final GenericFileName rootName,
                           final JSch jSch )
    {
        super( rootName, null );
        this.jSch = jSch;
    }

    /**
     * Closes this file system.
     */
    public void close()
    {
        if ( session != null )
        {
            session.disconnect();
        }
        super.close();
    }

    /**
     * Returns an SFTP channel to the server.
     */
    protected ChannelSftp getChannel() throws IOException
    {
        try
        {
            // Create the session
            if ( session == null )
            {
                final GenericFileName rootName = (GenericFileName)getRootName();
                session = jSch.getSession( rootName.getUserName(),
                                           rootName.getHostName(),
                                           rootName.getPort() );
                session.setPassword( rootName.getPassword() );
                session.connect();
            }

            // Use the pooled channel, or create a new one
            final ChannelSftp channel;
            if ( idleChannel != null )
            {
                channel = idleChannel;
                idleChannel = null;
            }
            else
            {
                channel = (ChannelSftp)session.openChannel( "sftp" );
                channel.connect();
            }

            return channel;
        }
        catch ( final JSchException e )
        {
            throw new FileSystemException( "vfs.provider.sftp/connect.error",
                                           getRootName(),
                                           e );
        }
    }

    /**
     * Returns a channel to the pool.
     */
    protected void putChannel( final ChannelSftp channel )
    {
        if ( idleChannel == null )
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
    protected void addCapabilities( final Collection caps )
    {
        caps.add( Capability.CREATE );
        caps.add( Capability.DELETE );
        caps.add( Capability.GET_TYPE );
        caps.add( Capability.LIST_CHILDREN );
        caps.add( Capability.READ_CONTENT );
        caps.add( Capability.URI );
        caps.add( Capability.WRITE_CONTENT );
    }

    /**
     * Creates a file object.  This method is called only if the requested
     * file is not cached.
     */
    protected FileObject createFile( final FileName name )
        throws FileSystemException
    {
        return new SftpFileObject( name, this );
    }
}
