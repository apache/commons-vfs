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

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.util.MonitorOutputStream;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.provider.TemporaryFileStore;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;

/**
 * An SFTP file.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1 $ $Date: 2003/02/20 07:30:36 $
 */
class SftpFileObject
    extends AbstractFileObject
    implements FileObject
{
    // TODO - This was copied from SftpATTRS source.  Need to fix jsch to make the constant public
    static final int S_IFDIR = 0x4000;

    private final SftpFileSystem fileSystem;
    private SftpATTRS attrs;
    private TemporaryFileStore tempFileStore;

    public SftpFileObject( final FileName name,
                           final SftpFileSystem fileSystem,
                           final TemporaryFileStore tempFileStore )
    {
        super( name, fileSystem );
        this.fileSystem = fileSystem;
        this.tempFileStore = tempFileStore;
    }

    /**
     * Determines the type of this file, returns null if the file does not
     * exist.
     */
    protected FileType doGetType() throws Exception
    {
        statSelf();

        if ( attrs == null )
        {
            // TODO - not quite true, but ChannelSftp.stat() swallows exceptions
            return null;
        }
        if ( ( attrs.getFlags() & SftpATTRS.SSH_FILEXFER_ATTR_PERMISSIONS ) == 0 )
        {
            throw new FileSystemException( "vfs.provider.sftp/unknown-permissions.error" );
        }
        if ( ( attrs.getPermissions() & S_IFDIR ) != 0 )
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
    protected void onChange() throws Exception
    {
        statSelf();
    }

    /** Fetches file attrs from server. */
    private void statSelf() throws Exception
    {
        final ChannelSftp channel = fileSystem.getChannel();
        try
        {
            attrs = channel.stat( getName().getPath() );
        }
        finally
        {
            fileSystem.putChannel( channel );
        }
    }

    /**
     * Creates this file as a folder.
     */
    protected void doCreateFolder() throws Exception
    {
        final ChannelSftp channel = fileSystem.getChannel();
        try
        {
            final boolean ok = channel.mkdir( getName().getPath() );
            if ( !ok )
            {
                throw new FileSystemException( "vfs.provider.sftp/create-folder.error" );
            }
        }
        finally
        {
            fileSystem.putChannel( channel );
        }
    }

    /**
     * Deletes the file.
     */
    protected void doDelete() throws Exception
    {
        final ChannelSftp channel = fileSystem.getChannel();
        try
        {
            final boolean ok;
            if ( getType() == FileType.FILE )
            {
                ok = channel.rm( getName().getPath() );
            }
            else
            {
                ok = channel.rmdir( getName().getPath() );
            }
            if ( !ok )
            {
                throw new FileSystemException( "vfs.provider.sftp/delete.error" );
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
    protected String[] doListChildren() throws Exception
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
    protected long doGetContentSize() throws Exception
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
    protected InputStream doGetInputStream() throws Exception
    {
        final ChannelSftp channel = fileSystem.getChannel();
        try
        {
            // TODO - this is a dud, need to add stream based methods to Jsch
            // TODO - reuse the cached file
            // TODO - delete the file on end-of-stream
            final File file = tempFileStore.allocateFile( getName().getBaseName() );
            final boolean ok = channel.get( getName().getPath(), '/' + file.getAbsolutePath() );
            if ( !ok )
            {
                throw new FileSystemException( "vfs.provider.sftp/get-file.error" );
            }
            return new FileInputStream( file );
        }
        finally
        {
            fileSystem.putChannel( channel );
        }
    }

    /**
     * Creates an output stream to write the file content to.
     */
    protected OutputStream doGetOutputStream() throws Exception
    {
        // TODO - this is a dud, need to add stream based methods to Jsch
        // TODO - reuse the same file for all content operations
        final File file = tempFileStore.allocateFile( getName().getBaseName() );
        return new SftpOutputStream( file );
    }

    /**
     * Writes the content from a file.
     */
    private void putContent( final File file ) throws IOException
    {
        final ChannelSftp channel = fileSystem.getChannel();
        try
        {
            final boolean ok = channel.put( '/' + file.getAbsolutePath(), getName().getPath() );
            if ( !ok )
            {
                throw new FileSystemException( "vfs.provider.sftp/put-file.error" );
            }
        }
        finally
        {
            fileSystem.putChannel( channel );
        }
    }

    /**
     * An output stream that pushes content to the server when this stream
     * is closed.
     */
    private class SftpOutputStream
        extends MonitorOutputStream
    {
        private File file;

        public SftpOutputStream( final File file ) throws IOException
        {
            super( new FileOutputStream( file ) );
            this.file = file;
        }

        /**
         * Called after this stream is closed.
         */
        protected void onClose() throws IOException
        {
            // TODO - need to delete the file
            putContent( file );
        }
    }
}
