/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.ftp;

import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileObject;

/**
 * An FTP file.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.7 $ $Date: 2002/07/05 04:08:18 $
 */
final class FtpFileObject
    extends AbstractFileObject
{
    private static final FTPFile[] EMPTY_FTP_FILE_ARRAY = {};

    private final FtpFileSystem ftpFs;

    // Cached info
    private FTPFile fileInfo;
    private FTPFile[] children;

    public FtpFileObject( final FileName name, final FtpFileSystem fileSystem )
    {
        super( name, fileSystem );
        ftpFs = fileSystem;
    }

    /**
     * Called by child file objects, to locate their ftp file info.
     */
    private FTPFile getChildFile( String name ) throws Exception
    {
        if ( children == null )
        {
            // List the children of this file
            children = ftpFs.getClient().listFiles( getName().getPath() );
            if ( children == null )
            {
                children = EMPTY_FTP_FILE_ARRAY;
            }
        }

        // Look for the requested child
        // TODO - use hash table
        for ( int i = 0; i < children.length; i++ )
        {
            FTPFile child = children[ i ];
            if ( child.getName().equals( name ) )
            {
                // TODO - should be using something else to compare names
                return child;
            }
        }

        return null;
    }

    /**
     * Attaches this file object to its file resource.
     */
    protected void doAttach()
        throws Exception
    {
        // Get the parent folder to find the info for this file
        FtpFileObject parent = (FtpFileObject)getParent();
        fileInfo = parent.getChildFile( getName().getBaseName() );
        if ( fileInfo == null || !fileInfo.isDirectory() )
        {
            children = EMPTY_FTP_FILE_ARRAY;
        }
    }

    /**
     * Detaches this file object from its file resource.
     */
    protected void doDetach()
    {
        fileInfo = null;
        children = null;
    }

    /**
     * Called when the children of this file change.
     */
    protected void onChildrenChanged()
    {
        children = null;
    }

    /**
     * Determines the type of the file, returns null if the file does not
     * exist.
     */
    protected FileType doGetType()
        throws Exception
    {
        if ( fileInfo == null )
        {
            // Does not exist
            return null;
        }
        if ( fileInfo.isDirectory() )
        {
            return FileType.FOLDER;
        }
        if ( fileInfo.isFile() )
        {
            return FileType.FILE;
        }

        throw new FileSystemException( "vfs.provider.ftp/get-type.error", getName() );
    }

    /**
     * Lists the children of the file.
     */
    protected String[] doListChildren()
        throws Exception
    {
        if ( children == null )
        {
            // List the children of this file
            children = ftpFs.getClient().listFiles( getName().getPath() );
            if ( children == null )
            {
                children = EMPTY_FTP_FILE_ARRAY;
            }
        }

        String[] childNames = new String[ children.length ];
        for ( int i = 0; i < children.length; i++ )
        {
            FTPFile child = children[ i ];
            childNames[ i ] = child.getName();
        }

        return childNames;
    }

    /**
     * Deletes the file.
     */
    protected void doDelete() throws Exception
    {
        final FTPClient ftpClient = ftpFs.getClient();
        boolean ok;
        if ( fileInfo.isDirectory() )
        {
            ok = ftpClient.removeDirectory( getName().getPath() );
        }
        else
        {
            ok = ftpClient.deleteFile( getName().getPath() );
        }
        if ( !ok )
        {
            throw new FileSystemException( "vfs.provider.ftp/delete-file.error", getName() );
        }
    }

    /**
     * Creates this file as a folder.
     */
    protected void doCreateFolder()
        throws Exception
    {
        if ( !ftpFs.getClient().makeDirectory( getName().getPath() ) )
        {
            throw new FileSystemException( "vfs.provider.ftp/create-folder.error", getName() );
        }
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    protected long doGetContentSize() throws Exception
    {
        return fileInfo.getSize();
    }

    /**
     * Creates an input stream to read the file content from.
     */
    protected InputStream doGetInputStream() throws Exception
    {
        return ftpFs.getClient().retrieveFileStream( getName().getPath() );
    }

    /**
     * Notification of the input stream being closed.
     */
    protected void doEndInput()
        throws Exception
    {
        if ( !ftpFs.getClient().completePendingCommand() )
        {
            throw new FileSystemException( "vfs.provider.ftp/finish-get.error", getName() );
        }
    }

    /**
     * Creates an output stream to write the file content to.
     */
    protected OutputStream doGetOutputStream()
        throws Exception
    {
        return ftpFs.getClient().storeFileStream( getName().getPath() );
    }

    /**
     * Notification of the output stream being closed.
     */
    protected void doEndOutput()
        throws Exception
    {
        if ( !ftpFs.getClient().completePendingCommand() )
        {
            throw new FileSystemException( "vfs.provider.ftp/finish-put.error", getName() );
        }
    }
}
