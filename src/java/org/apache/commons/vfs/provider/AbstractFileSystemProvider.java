/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;

/**
 * A partial {@link FileProvider} implementation.  Takes care of managing the
 * file systems created by the provider.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.9 $ $Date: 2002/07/05 03:47:04 $
 */
public abstract class AbstractFileSystemProvider
    extends AbstractVfsComponent
    implements FileProvider
{
    /**
     * The cached file systems.  This is a mapping from root URI to
     * FileSystem object.
     */
    private final Map fileSystems = new HashMap();

    /**
     * Closes the file systems created by this provider.
     */
    public void close()
    {
        // Close all the filesystems created by this provider
        for ( Iterator iterator = fileSystems.values().iterator(); iterator.hasNext(); )
        {
            Object fileSystem = iterator.next();
            if ( fileSystem instanceof VfsComponent )
            {
                final VfsComponent vfsComponent = (VfsComponent)fileSystem;
                vfsComponent.close();
            }
        }
        fileSystems.clear();
    }

    /**
     * Adds a file system to those cached by this provider.
     */
    protected void addFileSystem( final Object key, final FileSystem fs )
        throws FileSystemException
    {
        // Initialise
        if ( fs instanceof VfsComponent )
        {
            VfsComponent vfsComponent = (VfsComponent)fs;
            vfsComponent.setLogger( getLogger() );
            vfsComponent.setContext( getContext() );
            vfsComponent.init();
        }

        // Add to the cache
        fileSystems.put( key, fs );
    }

    /**
     * Locates a cached file system
     * @return The provider, or null if it is not cached.
     */
    protected FileSystem findFileSystem( final Object key )
    {
        return (FileSystem)fileSystems.get( key );
    }
}
