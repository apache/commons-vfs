/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.zip;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractLayeredFileProvider;
import org.apache.commons.vfs.provider.DefaultFileName;
import org.apache.commons.vfs.provider.FileProvider;
import org.apache.commons.vfs.provider.ParsedLayeredUri;

/**
 * A file system provider for Zip/Jar files.  Provides read-only file
 * systems, for local Zip files only.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.7 $ $Date: 2002/07/05 04:08:19 $
 */
public class ZipFileSystemProvider
    extends AbstractLayeredFileProvider
    implements FileProvider
{
    private final ZipFileNameParser parser = new ZipFileNameParser();

    /**
     * Parses an absolute URI.
     * @param uri The URI to parse.
     */
    protected ParsedLayeredUri parseUri( final String uri )
        throws FileSystemException
    {
        return parser.parseZipUri( uri );
    }

    /**
     * Creates a layered file system.  This method is called if the file system
     * is not cached.
     * @param scheme The URI scheme.
     * @param file The file to create the file system on top of.
     * @return The file system.
     */
    protected FileSystem doCreateFileSystem( final String scheme,
                                             final FileObject file )
        throws FileSystemException
    {
        final String rootUri = parser.buildRootUri( scheme, file.getName().getURI() );
        final DefaultFileName name = new DefaultFileName( parser, rootUri, "/" );
        return new ZipFileSystem( name, file );
    }

    /**
     * Returns the URI parser.
     */
    protected ZipFileNameParser getParser()
    {
        return parser;
    }

}
