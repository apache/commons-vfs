/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.impl;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.FileReplicator;
import org.apache.commons.vfs.provider.FileSystemProviderContext;

/**
 * A provider context implementation.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.4 $ $Date: 2002/07/05 03:21:54 $
 */
final class DefaultProviderContext
    implements FileSystemProviderContext
{
    private final DefaultFileSystemManager m_manager;

    public DefaultProviderContext( final DefaultFileSystemManager manager )
    {
        m_manager = manager;
    }

    /**
     * Locate a file by name.
     */
    public FileObject resolveFile( final FileObject baseFile, final String name )
        throws FileSystemException
    {
        return m_manager.resolveFile( baseFile, name );
    }

    /**
     * Locate a file by name.
     */
    public FileObject resolveFile( final String name )
        throws FileSystemException
    {
        return m_manager.resolveFile( name );
    }

    /**
     * Locates a file replicator for the provider to use.
     */
    public FileReplicator getReplicator() throws FileSystemException
    {
        return m_manager.getReplicator();
    }
}
