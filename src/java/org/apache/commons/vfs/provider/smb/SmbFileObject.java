/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.smb;

import java.io.InputStream;
import java.io.OutputStream;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileObject;

/**
 * A file in an SMB file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.5 $ $Date: 2002/07/05 04:08:19 $
 */
class SmbFileObject
    extends AbstractFileObject
    implements FileObject
{
    private final String fileName;
    private SmbFile file;

    protected SmbFileObject( final String fileName,
                             final FileName name,
                             final SmbFileSystem fileSystem )
    {
        super( name, fileSystem );
        this.fileName = fileName;
    }

    /**
     * Attaches this file object to its file resource.
     */
    protected void doAttach() throws Exception
    {
        // Defer creation of the SmbFile to here
        if ( file == null )
        {
            file = new SmbFile( fileName );
        }
    }

    /**
     * Detaches this file object from its file resource.
     */
    protected void doDetach()
    {
        // Need to throw away the file when the file's type changes, because
        // the SmbFile caches the type
        file = null;
    }

    /**
     * Determines the type of the file, returns null if the file does not
     * exist.
     */
    protected FileType doGetType() throws Exception
    {
        // Need to check whether parent exists or not, because SmbFile.exists()
        // throws an exception if it does not
        // TODO - patch jCIFS?

        FileObject parent = getParent();
        if ( parent != null && !parent.exists() )
        {
            return null;
        }

        if ( !file.exists() )
        {
            return null;
        }
        if ( file.isDirectory() )
        {
            return FileType.FOLDER;
        }
        if ( file.isFile() )
        {
            return FileType.FILE;
        }
        throw new FileSystemException( "vfs.provider.smb/get-type.error", getName() );
    }

    /**
     * Lists the children of the file.  Is only called if {@link #doGetType}
     * returns {@link FileType#FOLDER}.
     */
    protected String[] doListChildren() throws Exception
    {
        return file.list();
    }

    /**
     * Deletes the file.
     */
    protected void doDelete() throws Exception
    {
        file.delete();
    }

    /**
     * Creates this file as a folder.
     */
    protected void doCreateFolder() throws Exception
    {
        file.mkdir();
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    protected long doGetContentSize() throws Exception
    {
        return file.length();
    }

    /**
     * Creates an input stream to read the file content from.
     */
    protected InputStream doGetInputStream() throws Exception
    {
        return new SmbFileInputStream( file );
    }

    /**
     * Creates an output stream to write the file content to.
     */
    protected OutputStream doGetOutputStream() throws Exception
    {
        return new SmbFileOutputStream( file );
    }
}
