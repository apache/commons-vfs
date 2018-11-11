/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.impl;

import java.io.File;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.FileReplicator;
import org.apache.commons.vfs2.provider.TemporaryFileStore;
import org.apache.commons.vfs2.provider.VfsComponentContext;

/**
 * The default context implementation.
 */
final class DefaultVfsComponentContext implements VfsComponentContext {
    private final DefaultFileSystemManager manager;

    public DefaultVfsComponentContext(final DefaultFileSystemManager manager) {
        this.manager = manager;
    }

    /**
     * Locate a file by name.
     */
    @Override
    public FileObject resolveFile(final FileObject baseFile, final String name,
            final FileSystemOptions fileSystemOptions) throws FileSystemException {
        return manager.resolveFile(baseFile, name, fileSystemOptions);
    }

    /**
     * Locate a file by name.
     */
    @Override
    public FileObject resolveFile(final String name, final FileSystemOptions fileSystemOptions)
            throws FileSystemException {
        return manager.resolveFile(name, fileSystemOptions);
    }

    @Override
    public FileName parseURI(final String uri) throws FileSystemException {
        return manager.resolveURI(uri);
    }

    /**
     * Returns a {@link FileObject} for a local file.
     */
    @Override
    public FileObject toFileObject(final File file) throws FileSystemException {
        return manager.toFileObject(file);
    }

    /**
     * Locates a file replicator for the provider to use.
     */
    @Override
    public FileReplicator getReplicator() throws FileSystemException {
        return manager.getReplicator();
    }

    /**
     * Locates a temporary file store for the provider to use.
     */
    @Override
    public TemporaryFileStore getTemporaryFileStore() throws FileSystemException {
        return manager.getTemporaryFileStore();
    }

    /**
     * Returns the filesystem manager for the current context
     *
     * @return the filesystem manager
     */
    @Override
    public FileSystemManager getFileSystemManager() {
        return manager;
    }
}
