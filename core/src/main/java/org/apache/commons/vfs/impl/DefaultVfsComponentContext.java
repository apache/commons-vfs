/*
 * Copyright 2002-2005 The Apache Software Foundation.
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

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.FileReplicator;
import org.apache.commons.vfs.provider.TemporaryFileStore;
import org.apache.commons.vfs.provider.VfsComponentContext;

import java.io.File;


/**
 * The default context implementation.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
final class DefaultVfsComponentContext
    implements VfsComponentContext
{
    private final DefaultFileSystemManager manager;

    public DefaultVfsComponentContext(final DefaultFileSystemManager manager)
    {
        this.manager = manager;
    }

    /**
     * Locate a file by name.
     */
    public FileObject resolveFile(final FileObject baseFile, final String name, final FileSystemOptions fileSystemOptions)
        throws FileSystemException
    {
        return manager.resolveFile(baseFile, name, fileSystemOptions);
    }

    /**
     * Locate a file by name.
     */
    public FileObject resolveFile(final String name, final FileSystemOptions fileSystemOptions)
        throws FileSystemException
    {
        return manager.resolveFile(name, fileSystemOptions);
    }

    public FileName parseURI(String uri) throws FileSystemException
    {
        return manager.resolveURI(uri);
    }

    /**
     * Returns a {@link FileObject} for a local file.
     */
    public FileObject toFileObject(File file)
        throws FileSystemException
    {
        return manager.toFileObject(file);
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

    /**
     * Returns the filesystem manager for the current context
     *
     * @return the filesystem manager
     */
    public FileSystemManager getFileSystemManager()
    {
        return manager;
    }
}
