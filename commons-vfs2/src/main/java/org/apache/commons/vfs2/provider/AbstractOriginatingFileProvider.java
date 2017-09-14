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

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;

/**
 * A {@link FileProvider} that handles physical files, such as the files in a local fs, or on an FTP server. An
 * originating file system cannot be layered on top of another file system.
 */
public abstract class AbstractOriginatingFileProvider extends AbstractFileProvider {
    public AbstractOriginatingFileProvider() {
        super();
    }

    /**
     * Locates a file object, by absolute URI.
     *
     * @param baseFile The base file object.
     * @param uri The URI of the file to locate
     * @param fileSystemOptions The FileSystem options.
     * @return The located FileObject
     * @throws FileSystemException if an error occurs.
     */
    @Override
    public FileObject findFile(final FileObject baseFile, final String uri, final FileSystemOptions fileSystemOptions)
            throws FileSystemException {
        // Parse the URI
        final FileName name;
        try {
            name = parseUri(baseFile != null ? baseFile.getName() : null, uri);
        } catch (final FileSystemException exc) {
            throw new FileSystemException("vfs.provider/invalid-absolute-uri.error", uri, exc);
        }

        // Locate the file
        return findFile(name, fileSystemOptions);
    }

    /**
     * Locates a file from its parsed URI.
     *
     * @param name The file name.
     * @param fileSystemOptions FileSystem options.
     * @return A FileObject associated with the file.
     * @throws FileSystemException if an error occurs.
     */
    protected FileObject findFile(final FileName name, final FileSystemOptions fileSystemOptions)
            throws FileSystemException {
        // Check in the cache for the file system
        final FileName rootName = getContext().getFileSystemManager().resolveName(name, FileName.ROOT_PATH);

        final FileSystem fs = getFileSystem(rootName, fileSystemOptions);

        // Locate the file
        // return fs.resolveFile(name.getPath());
        return fs.resolveFile(name);
    }

    /**
     * Returns the FileSystem associated with the specified root.
     *
     * @param rootName The root path.
     * @param fileSystemOptions The FileSystem options.
     * @return The FileSystem.
     * @throws FileSystemException if an error occurs.
     * @since 2.0
     */
    protected synchronized FileSystem getFileSystem(final FileName rootName, final FileSystemOptions fileSystemOptions)
            throws FileSystemException {
        FileSystem fs = findFileSystem(rootName, fileSystemOptions);
        if (fs == null) {
            // Need to create the file system, and cache it
            fs = doCreateFileSystem(rootName, fileSystemOptions);
            addFileSystem(rootName, fs);
        }
        return fs;
    }

    /**
     * Creates a {@link FileSystem}. If the returned FileSystem implements {@link VfsComponent}, it will be initialised.
     *
     * @param rootName The name of the root file of the file system to create.
     * @param fileSystemOptions The FileSystem options.
     * @return The FileSystem.
     * @throws FileSystemException if an error occurs.
     */
    protected abstract FileSystem doCreateFileSystem(final FileName rootName, final FileSystemOptions fileSystemOptions)
            throws FileSystemException;
}
