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
package org.apache.commons.vfs.provider;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;

import java.io.File;

/**
 * Allows VFS components to access the services they need, such as the file
 * replicator.  A VFS component is supplied with a context as part of its
 * initialisation.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 * @see VfsComponent#setContext
 */
public interface VfsComponentContext
{
    /**
     * Locate a file by name.  See
     * {@link FileSystemManager#resolveFile(FileObject, String)} for a
     * description of how this works.
     */
    FileObject resolveFile(FileObject baseFile, String name, FileSystemOptions fileSystemOptions)
        throws FileSystemException;

    /**
     * Locate a file by name.  See
     * {@link FileSystemManager#resolveFile( String)} for a
     * description of how this works.
     */
    FileObject resolveFile(String name, FileSystemOptions fileSystemOptions)
        throws FileSystemException;

    FileName parseURI(String uri) throws FileSystemException;

    /**
     * Locates a file replicator for the provider to use.
     */
    FileReplicator getReplicator() throws FileSystemException;

    /**
     * Locates a temporary file store for the provider to use.
     */
    TemporaryFileStore getTemporaryFileStore() throws FileSystemException;

    /**
     * Returns a {@link FileObject} for a local file.
     */
    FileObject toFileObject(File file)
        throws FileSystemException;

    /**
     * Returns the filesystem manager for the current context
     *
     * @return the filesystem manager
     */
    FileSystemManager getFileSystemManager();
}
