/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.jar;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.DefaultFileName;
import org.apache.commons.vfs.provider.zip.ZipFileSystemProvider;

/**
 * A file system provider for Jar files.  Provides read-only file
 * systems, for local Jar files only.
 * This provides access to Jar specific features like Signing and
 * Manifest Attributes.
 *
 * @author <a href="mailto:brian@mmmanager.org">Brian Olsen</a>
 * @version $Revision: 1.3 $ $Date: 2002/08/22 08:00:39 $
 */
public class JarFileSystemProvider
    extends ZipFileSystemProvider
{
    /**
     * Creates a layered file system.  This method is called if the file system
     * is not cached.
     * @param scheme The URI scheme.
     * @param file The file to create the file system on top of.
     * @return The file system.
     */
    protected FileSystem doCreateFileSystem( String scheme,
                                             FileObject file )
        throws FileSystemException
    {
        final String rootUri = getParser().buildRootUri( scheme, file.getName().getURI() );
        final DefaultFileName name = new DefaultFileName( getParser(), rootUri, "/" );
        return new JarFileSystem( name, file );
    }
}
