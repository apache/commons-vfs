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
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
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
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.util.MonitorOutputStream;

/**
 * An SFTP file.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.5 $ $Date: 2003/10/13 08:44:27 $
 */
class SftpFileObject
    extends AbstractFileObject
    implements FileObject
{
    private final SftpFileSystem fileSystem;
    private SftpATTRS attrs;

    public SftpFileObject( final FileName name,
                           final SftpFileSystem fileSystem )
    {
        super( name, fileSystem );
        this.fileSystem = fileSystem;
    }

    /**
     * Determines the type of this file, returns null if the file does not
     * exist.
     */
    protected FileType doGetType()
        throws Exception
    {
        statSelf();

        if ( attrs == null )
        {
            return FileType.IMAGINARY;
        }

        if ( (attrs.getFlags() & SftpATTRS.SSH_FILEXFER_ATTR_PERMISSIONS) == 0 )
        {
            throw new FileSystemException( "vfs.provider.sftp/unknown-permissions.error" );
        }
        if ( attrs.isDir() )
        {
            return FileType.FOLDER;
        }
        else
        {
            return FileType.FILE;
        }
    }

    /**
     * Called when the type or content of this file changes.
     */
    protected void onChange()
        throws Exception
    {
        statSelf();
    }

    /** Fetches file attrs from server. */
    private void statSelf()
        throws Exception
    {
        final ChannelSftp channel = fileSystem.getChannel();
        try
        {
            attrs = channel.stat( getName().getPath() );
        }
        catch ( final SftpException e )
        {
            // TODO - not strictly true, but jsch 0.1.2 does not give us
            // enough info in the exception.  Should be using:
            // if ( e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE )
            // However, sometimes the exception has the correct id, and sometimes
            // it does not.  Need to look into why.

            // Does not exist
            attrs = null;
        }
        finally
        {
            fileSystem.putChannel( channel );
        }
    }

    /**
     * Creates this file as a folder.
     */
    protected void doCreateFolder()
        throws Exception
    {
        final ChannelSftp channel = fileSystem.getChannel();
        try
        {
            channel.mkdir( getName().getPath() );
        }
        finally
        {
            fileSystem.putChannel( channel );
        }
    }

    /**
     * Deletes the file.
     */
    protected void doDelete()
        throws Exception
    {
        final ChannelSftp channel = fileSystem.getChannel();
        try
        {
            if ( getType() == FileType.FILE )
            {
                channel.rm( getName().getPath() );
            }
            else
            {
                channel.rmdir( getName().getPath() );
            }
        }
        finally
        {
            fileSystem.putChannel( channel );
        }
    }

    /**
     * Lists the children of this file.
     */
    protected String[] doListChildren()
        throws Exception
    {
        // List the contents of the folder
        final Vector vector;
        final ChannelSftp channel = fileSystem.getChannel();
        try
        {
            vector = channel.ls( getName().getPath() );
        }
        finally
        {
            fileSystem.putChannel( channel );
        }
        if ( vector == null )
        {
            throw new FileSystemException( "vfs.provider.sftp/list-children.error" );
        }

        // Extract the child names
        final ArrayList children = new ArrayList();
        for ( Iterator iterator = vector.iterator(); iterator.hasNext(); )
        {
            // Each entry is in unix ls format <perms> <?> <user> <group> <size> <date> <name>
            final String stat = (String)iterator.next();
            final StringTokenizer tokens = new StringTokenizer( stat );
            // TODO - check there are the correct number of tokens
            // TODO - handle names with spaces in 'em
            for ( int i = 0; i < 8; tokens.nextToken(), i++ )
            {
            }
            final String name = tokens.nextToken();
            if ( name.equals( "." ) || name.equals( ".." ) )
            {
                continue;
            }
            children.add( name );
        }
        return (String[])children.toArray( new String[ children.size() ] );
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    protected long doGetContentSize()
        throws Exception
    {
        if ( (attrs.getFlags() & SftpATTRS.SSH_FILEXFER_ATTR_SIZE) == 0 )
        {
            throw new FileSystemException( "vfs.provider.sftp/unknown-size.error" );
        }
        return attrs.getSize();
    }

    /**
     * Creates an input stream to read the file content from.
     */
    protected InputStream doGetInputStream()
        throws Exception
    {
        final ChannelSftp channel = fileSystem.getChannel();
        try
        {
            // TODO - Don't read the entire file into memory.  Use the
            // stream-based methods on ChannelSftp once they work properly
            final ByteArrayOutputStream outstr = new ByteArrayOutputStream();
            channel.get( getName().getPath(), outstr );
            outstr.close();
            return new ByteArrayInputStream( outstr.toByteArray() );
        }
        finally
        {
            fileSystem.putChannel( channel );
        }
    }

    /**
     * Creates an output stream to write the file content to.
     */
    protected OutputStream doGetOutputStream()
        throws Exception
    {
        // TODO - Don't write the entire file into memory.  Use the stream-based
        // methods on ChannelSftp once the work properly
        final ChannelSftp channel = fileSystem.getChannel();
        return new SftpOutputStream( channel );
    }

    /**
     * An OutputStream that wraps an sftp OutputStream, and closes the channel
     * when the stream is closed.
     */
    private class SftpOutputStream
        extends MonitorOutputStream
    {
        private final ChannelSftp channel;

        public SftpOutputStream( final ChannelSftp channel )
        {
            super( new ByteArrayOutputStream() );
            this.channel = channel;
        }

        /**
         * Called after this stream is closed.
         */
        protected void onClose()
            throws IOException
        {
            try
            {
                final ByteArrayOutputStream outstr = (ByteArrayOutputStream)out;
                channel.put( new ByteArrayInputStream( outstr.toByteArray() ),
                             getName().getPath() );
            }
            catch ( final SftpException e )
            {
                throw new FileSystemException( e );
            }
            finally
            {
                fileSystem.putChannel( channel );
            }
        }
    }
}
