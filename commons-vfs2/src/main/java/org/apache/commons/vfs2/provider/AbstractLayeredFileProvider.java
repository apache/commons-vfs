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
 * A {@link FileProvider} that is layered on top of another, such as the contents of a zip or tar file.
 */
public abstract class AbstractLayeredFileProvider extends AbstractFileProvider {
    public AbstractLayeredFileProvider() {
        super();
        setFileNameParser(LayeredFileNameParser.getInstance());
    }

    /**
     * Locates a file object, by absolute URI.
     *
     * @param baseFile The base FileObject.
     * @param uri The name of the file to locate.
     * @param fileSystemOptions The FileSystemOptions.
     * @return The FileObject if it is located, null otherwise.
     * @throws FileSystemException if an error occurs.
     */
    @Override
    public FileObject findFile(final FileObject baseFile, final String uri, final FileSystemOptions fileSystemOptions)
            throws FileSystemException {
        // Split the URI up into its parts
        final LayeredFileName name = (LayeredFileName) parseUri(baseFile != null ? baseFile.getName() : null, uri);

        // Make the URI canonical

        // Resolve the outer file name
        final FileName fileName = name.getOuterName();
        final FileObject file = getContext().resolveFile(baseFile, fileName.getURI(), fileSystemOptions);

        // Create the file system
        final FileObject rootFile = createFileSystem(name.getScheme(), file, fileSystemOptions);

        // Resolve the file
        return rootFile.resolveFile(name.getPath());
    }

    /**
     * Creates a layered file system.
     *
     * @param scheme The protocol to use.
     * @param file a FileObject.
     * @param fileSystemOptions Options to access the FileSystem.
     * @return A FileObject associated with the new FileSystem.
     * @throws FileSystemException if an error occurs.
     */
    @Override
    public synchronized FileObject createFileSystem(final String scheme, final FileObject file,
            final FileSystemOptions fileSystemOptions) throws FileSystemException {
        // Check if cached
        final FileName rootName = file.getName();
        FileSystem fs = findFileSystem(rootName, fileSystemOptions);
        if (fs == null) {
            // Create the file system
            fs = doCreateFileSystem(scheme, file, fileSystemOptions);
            addFileSystem(rootName, fs);
        }
        return fs.getRoot();
    }

    /**
     * Creates a layered file system.
     * <p>
     * This method is called if the file system is not cached.
     *
     * @param scheme The URI scheme.
     * @param file The file to create the file system on top of.
     * @param fileSystemOptions options for new and underlying file systems.
     * @return The file system, never null. Might implement {@link VfsComponent}.
     * @throws FileSystemException if the file system cannot be created.
     */
    protected abstract FileSystem doCreateFileSystem(final String scheme, final FileObject file,
            final FileSystemOptions fileSystemOptions) throws FileSystemException;

}
