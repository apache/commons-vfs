/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.jar;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.DefaultFileName;
import org.apache.commons.vfs.provider.FileSystem;
import org.apache.commons.vfs.provider.ParsedUri;
import org.apache.commons.vfs.provider.zip.ParsedZipUri;
import org.apache.commons.vfs.provider.zip.ZipFileSystemProvider;

/**
 * A file system provider for Jar files.  Provides read-only file
 * systems, for local Jar files only.
 * This provides access to Jar specific features like Signing and
 * Manifest Attributes.
 *
 * @author <a href="mailto:brian@mmmanager.org">Brian Olsen</a>
 * @version $Revision: 1.1 $ $Date: 2002/08/22 01:32:49 $
 */
public class JarFileSystemProvider
    extends ZipFileSystemProvider
{
    /**
     * Creates the filesystem.
     */
    protected FileSystem createFileSystem( final ParsedUri uri )
        throws FileSystemException
    {
        final ParsedZipUri jarUri = (ParsedZipUri)uri;
        final FileObject file = jarUri.getZipFile();

        // Create the file system
        DefaultFileName name = new DefaultFileName( getParser(), jarUri.getRootUri(), "/" );
        return new JarFileSystem( name, file );
    }

}
