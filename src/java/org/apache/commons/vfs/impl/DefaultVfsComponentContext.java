/*
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs.impl;

import java.io.File;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.FileReplicator;
import org.apache.commons.vfs.provider.TemporaryFileStore;
import org.apache.commons.vfs.provider.VfsComponentContext;

/**
 * The default context implementation.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.5 $ $Date: 2004/02/28 03:35:50 $
 */
final class DefaultVfsComponentContext
    implements VfsComponentContext
{
    private final DefaultFileSystemManager manager;

    public DefaultVfsComponentContext( final DefaultFileSystemManager manager )
    {
        this.manager = manager;
    }

    /**
     * Locate a file by name.
     */
    public FileObject resolveFile( final FileObject baseFile, final String name )
        throws FileSystemException
    {
        return manager.resolveFile( baseFile, name );
    }

    /**
     * Locate a file by name.
     */
    public FileObject resolveFile( final String name )
        throws FileSystemException
    {
        return manager.resolveFile( name );
    }

    /**
     * Returns a {@link FileObject} for a local file.
     */
    public FileObject toFileObject( File file )
        throws FileSystemException
    {
        return manager.toFileObject( file );
    }

    /**
     * Locates a file replicator for the provider to use.
     */
    public FileReplicator getReplicator() throws FileSystemException
    {
        return manager.getReplicator();
    }

    /**
     * Locates a temporary file store for the provider to use.
     */
    public TemporaryFileStore getTemporaryFileStore() throws FileSystemException
    {
        return manager.getTemporaryFileStore();
    }
}
