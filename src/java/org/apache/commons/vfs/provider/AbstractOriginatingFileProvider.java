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

/**
 * A {@link FileProvider} that handles physical files, such as the files in a
 * local fs, or on an FTP server.  An originating file system cannot be
 * layered on top of another file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.3 $ $Date: 2002/10/23 10:56:33 $
 */
public abstract class AbstractOriginatingFileProvider
    extends AbstractFileSystemProvider
{
    /**
     * Creates a layered file system.
     */
    public FileObject createFileSystem( final String scheme, final FileObject file )
        throws FileSystemException
    {
        // Can't create a layered file system
        throw new FileSystemException( "vfs.provider/not-layered-fs.error", scheme );
    }

    /**
     * Locates a file object, by absolute URI.
     *
     * @param uri
     *          The absolute URI of the file to find.
     */
    public FileObject findFile( final FileObject baseFile,
                                final String uri ) throws FileSystemException
    {
        // Parse the URI
        ParsedUri parsedUri;
        try
        {
            parsedUri = parseUri( baseFile, uri );
        }
        catch ( FileSystemException exc )
        {
            throw new FileSystemException( "vfs.provider/invalid-absolute-uri.error", new Object[]{uri}, exc );
        }

        // Locate the file
        return findFile( parsedUri );
    }

    /**
     * Locates a file from its parsed URI.
     */
    private FileObject findFile( final ParsedUri parsedUri )
        throws FileSystemException
    {
        // Check in the cache for the file system
        final String rootUri = parsedUri.getRootUri();
        FileSystem fs = findFileSystem( rootUri );
        if ( fs == null )
        {
            // Need to create the file system, and cache it
            fs = doCreateFileSystem( parsedUri );
            addFileSystem( rootUri, fs );
        }

        // Locate the file
        return fs.resolveFile( parsedUri.getPath() );
    }

    /**
     * Parses a URI into its components.  The returned value is used to
     * locate the file system in the cache (using the root prefix).
     *
     * <p>The provider can annotate this object with any additional
     * information it requires to create a file system from the URI.
     */
    protected abstract ParsedUri parseUri( final FileObject baseFile,
                                           final String uri )
        throws FileSystemException;

    /**
     * Creates the filesystem.
     */
    protected abstract FileSystem doCreateFileSystem( final ParsedUri uri )
        throws FileSystemException;

}
