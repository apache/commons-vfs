/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import java.io.File;

/**
 * Used for a file system provider to access the services it needs, such
 * as the file system cache or other file system providers.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.4 $ $Date: 2002/06/17 00:06:16 $
 */
public interface FileSystemProviderContext
{
    /**
     * Locate a file by name.  See
     * {@link FileSystemManager#resolveFile(FileObject, String)} for a
     * description of how this works.
     */
    FileObject resolveFile( FileObject baseFile, String name )
        throws FileSystemException;

    /**
     * Locate a file by name.  See
     * {@link FileSystemManager#resolveFile( String)} for a
     * description of how this works.
     */
    FileObject resolveFile( String name )
        throws FileSystemException;

    /**
     * Locates a file replicator for the provider to use.
     */
    FileReplicator getReplicator() throws FileSystemException;

    /**
     * Returns a {@link FileObject} for a local file.
     */
    FileObject getFile( File file )
        throws FileSystemException;
}
