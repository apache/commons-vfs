/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.avalon.excalibur.i18n.ResourceManager;
import org.apache.avalon.excalibur.i18n.Resources;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileObject;

/**
 * A file object implementation which uses direct file access.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.6 $ $Date: 2002/04/07 02:27:57 $
 */
final class LocalFile
    extends AbstractFileObject
    implements FileObject
{
    private static final Resources REZ =
        ResourceManager.getPackageResources( LocalFile.class );

    private File file;
    private final String fileName;
    private FilePermission requiredPerm;

    /**
     * Creates a non-root file.
     */
    public LocalFile( final LocalFileSystem fileSystem,
                      final String fileName,
                      final FileName name )
    {
        super( name, fileSystem );
        this.fileName = fileName;
    }

    /**
     * Attaches this file object to its file resource.
     */
    protected void doAttach()
        throws Exception
    {
        if ( file == null )
        {
            file = new File( fileName );
            requiredPerm = new FilePermission( file.getAbsolutePath(), "read" );
        }
    }

    /**
     * Returns the file's type.
     */
    protected FileType doGetType()
        throws Exception
    {
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

        final String message = REZ.getString( "get-type.error", file );
        throw new FileSystemException( message );
    }

    /**
     * Returns the children of the file.
     */
    protected String[] doListChildren()
        throws Exception
    {
        return file.list();
    }

    /**
     * Deletes this file, and all children.
     */
    protected void doDelete()
        throws Exception
    {
        if ( !file.delete() )
        {
            final String message = REZ.getString( "delete-file.error", file );
            throw new FileSystemException( message );
        }
    }

    /**
     * Creates this folder.
     */
    protected void doCreateFolder()
        throws Exception
    {
        if ( !file.mkdir() )
        {
            final String message = REZ.getString( "create-folder.error", file );
            throw new FileSystemException( message );
        }
    }

    /**
     * Creates an input stream to read the content from.
     */
    protected InputStream doGetInputStream()
        throws Exception
    {
        return new FileInputStream( file );
    }

    /**
     * Creates an output stream to write the file content to.
     */
    protected OutputStream doGetOutputStream()
        throws Exception
    {
        return new FileOutputStream( file );
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    protected long doGetContentSize()
        throws Exception
    {
        return file.length();
    }

    /**
     * Creates a temporary local copy of this file, and its descendents.
     */
    protected File doReplicateFile( final FileSelector selector )
        throws FileSystemException
    {
        final SecurityManager sm = System.getSecurityManager();
        if ( sm != null )
        {
            sm.checkPermission( requiredPerm );
        }
        return file;
    }
}
