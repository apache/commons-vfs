/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileName;

/**
 * A {@link FileProvider} that is layered on top of another, such as the
 * contents of a zip or tar file.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1 $ $Date: 2002/08/22 08:00:38 $
 */
public abstract class AbstractLayeredFileProvider
    extends AbstractFileSystemProvider
    implements FileProvider
{
    /**
     * Locates a file object, by absolute URI.
     */
    public FileObject findFile( final FileObject baseFile,
                                final String uri ) throws FileSystemException
    {
        // Split the URI up into its parts
        final ParsedLayeredUri parsedUri = parseUri( uri );

        // Make the URI canonical

        // Resolve the outer file name
        final String fileName = parsedUri.getOuterFileUri();
        final FileObject file = getContext().resolveFile( baseFile, fileName );

        // Create the file system
        final FileObject rootFile = createFileSystem( parsedUri.getScheme(), file );

        // Resolve the file
        return rootFile.resolveFile( parsedUri.getPath() );
    }

    /**
     * Creates a layered file system.
     */
    public FileObject createFileSystem( final String scheme,
                                        final FileObject file )
        throws FileSystemException
    {
        // Check if cached
        final FileName rootName = file.getName();
        FileSystem fs = findFileSystem( rootName );
        if ( fs == null )
        {
            // Create the file system
            fs = doCreateFileSystem( scheme, file );
            addFileSystem( rootName, fs );
        }
        return fs.getRoot();
    }

    /**
     * Creates a layered file system.  This method is called if the file system
     * is not cached.
     * @param scheme The URI scheme.
     * @param file The file to create the file system on top of.
     * @return The file system.
     */
    protected abstract FileSystem doCreateFileSystem( String scheme,
                                                      FileObject file )
        throws FileSystemException;

    /**
     * Parses an absolute URI.
     * @param uri The URI to parse.
     */
    protected abstract ParsedLayeredUri parseUri( String uri )
        throws FileSystemException;
}
