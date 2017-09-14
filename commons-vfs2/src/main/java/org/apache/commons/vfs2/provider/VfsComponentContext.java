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
package org.apache.commons.vfs2.provider;

import java.io.File;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;

/**
 * Allows VFS components to access the services they need, such as the file replicator. A VFS component is supplied with
 * a context as part of its initialisation.
 *
 * @see VfsComponent#setContext
 */
public interface VfsComponentContext {
    /**
     * Locate a file by name. See {@link FileSystemManager#resolveFile(FileObject, String)} for a description of how
     * this works.
     *
     * @param baseFile The base FileObject.
     * @param name The name of the file to locate.
     * @param fileSystemOptions The FileSystemOptions.
     * @return The FileObject for the located file.
     * @throws FileSystemException if an error occurs.
     */
    FileObject resolveFile(FileObject baseFile, String name, FileSystemOptions fileSystemOptions)
            throws FileSystemException;

    /**
     * Locate a file by name. See {@link FileSystemManager#resolveFile( String)} for a description of how this works.
     *
     * @param name The name of the file to locate.
     * @param fileSystemOptions The FileSystemOptions.
     * @return The FileObject for the located file.
     * @throws FileSystemException if an error occurs.
     */
    FileObject resolveFile(String name, FileSystemOptions fileSystemOptions) throws FileSystemException;

    /**
     * Parse a URI into a FileName.
     *
     * @param uri The URI String.
     * @return The FileName.
     * @throws FileSystemException if an error occurs.
     */
    FileName parseURI(String uri) throws FileSystemException;

    /**
     * Locates a file replicator for the provider to use.
     *
     * @return The FileReplicator.
     * @throws FileSystemException if an error occurs.
     */
    FileReplicator getReplicator() throws FileSystemException;

    /**
     * Locates a temporary file store for the provider to use.
     *
     * @return The TemporaryFileStore.
     * @throws FileSystemException if an error occurs.
     */
    TemporaryFileStore getTemporaryFileStore() throws FileSystemException;

    /**
     * Returns a {@link FileObject} for a local file.
     *
     * @param file The File to convert to a FileObject.
     * @return the FileObject.
     * @throws FileSystemException if an error occurs.
     */
    FileObject toFileObject(File file) throws FileSystemException;

    /**
     * Returns the filesystem manager for the current context.
     *
     * @return the filesystem manager
     */
    FileSystemManager getFileSystemManager();
}
