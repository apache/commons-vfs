/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.commons.vfs.provider.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.JSchException;
import java.util.Collection;
import java.io.IOException;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.GenericFileName;

/**
 * Represents the files on an SFTP server.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.3 $ $Date: 2003/06/24 10:36:30 $
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
