/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.zip;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import org.apache.commons.vfs.FileConstants;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystemProvider;
import org.apache.commons.vfs.provider.DefaultFileName;
import org.apache.commons.vfs.provider.FileProvider;
import org.apache.commons.vfs.provider.FileSystem;
import org.apache.commons.vfs.provider.ParsedUri;

/**
 * A file system provider for Zip/Jar files.  Provides read-only file
 * systems, for local Zip files only.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.7 $ $Date: 2002/07/05 04:08:19 $
 *
 * @ant.type type="file-provider" name="zip"
 */
public final class ZipFileSystemProvider
    extends AbstractFileSystemProvider
    implements FileProvider
{
    private final ZipFileNameParser parser = new ZipFileNameParser();

    /**
     * Parses a URI into its components.
     */
    protected ParsedUri parseUri( final FileObject baseFile,
                                  final String uriStr )
        throws FileSystemException
    {
        // Parse the URI
        final ParsedZipUri uri = parser.parseZipUri( uriStr );

        // Make the URI canonical

        // Resolve the Zip file name
        final String fileName = uri.getZipFileName();
        final FileObject file = getContext().resolveFile( baseFile, fileName );
        uri.setZipFile( file );

        // Rebuild the root URI
        final String rootUri = parser.buildRootUri( uri );
        uri.setRootUri( rootUri );

        return uri;
    }

    /**
     * Builds the URI for the root of a layered file system.
     */
    protected ParsedUri buildUri( final String scheme,
                                  final FileObject file )
        throws FileSystemException
    {
        ParsedZipUri uri = new ParsedZipUri();
        uri.setScheme( scheme );
        uri.setZipFile( file );
        final String rootUri = parser.buildRootUri( uri );
        uri.setRootUri( rootUri );
        uri.setPath( "/" );
        return uri;
    }

    /**
     * Creates the filesystem.
     */
    protected FileSystem createFileSystem( final ParsedUri uri )
        throws FileSystemException
    {
        final ParsedZipUri zipUri = (ParsedZipUri)uri;
        final FileObject file = zipUri.getZipFile();

        // Create the file system
        final DefaultFileName name = new DefaultFileName( parser, zipUri.getRootUri(), "/" );

        // Make a local copy of the file
        final File zipFile = file.replicateFile( FileConstants.SELECT_SELF );

        try
        {
            return (ZipFileSystem)AccessController.doPrivileged(
                new PrivilegedExceptionAction()
                {
                    public Object run() throws FileSystemException
                    {
                        return new ZipFileSystem( getContext(), name, zipFile );
                    }
                } );
        }
        catch ( PrivilegedActionException pae )
        {
            throw (FileSystemException)pae.getException();
        }
    }

}
