/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.url;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystemProvider;
import org.apache.commons.vfs.provider.DefaultFileName;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.provider.ParsedUri;
import org.apache.commons.vfs.provider.UriParser;

/**
 * A file provider backed by Java's URL API.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.3 $ $Date: 2002/08/22 02:42:46 $
 */
public class UrlFileProvider
    extends AbstractFileSystemProvider
{
    private final UriParser parser = new UriParser();

    /**
     * Parses a URI into its components.
     */
    protected ParsedUri parseUri( FileObject baseFile, String uri )
        throws FileSystemException
    {
        return parser.parseUri( uri );
    }

    /**
     * Creates the filesystem.
     */
    protected FileSystem createFileSystem( ParsedUri uri )
        throws FileSystemException
    {
        final DefaultFileName rootName = new DefaultFileName( parser, uri.getRootUri(), "/" );
        return new UrlFileSystem( rootName );
    }
}
