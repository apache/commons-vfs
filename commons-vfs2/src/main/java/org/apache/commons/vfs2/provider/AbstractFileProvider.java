/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.provider;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.local.GenericFileNameParser;

/**
 * A partial {@link FileProvider} implementation. Takes care of managing the file systems created by the provider.
 */
public abstract class AbstractFileProvider extends AbstractVfsContainer implements FileProvider {

    private static final AbstractFileSystem[] EMPTY_ABSTRACT_FILE_SYSTEMS = {};

    /**
     * The cached file systems.
     * <p>
     * This is a mapping from {@link FileSystemKey} (root URI and options) to {@link FileSystem}.
     * </p>
     */
    private final Map<FileSystemKey, FileSystem> fileSystemMap = new TreeMap<>(); // @GuardedBy("self")

    private FileNameParser fileNameParser;

    /**
     * Constructs a new instance for subclasses.
     */
    public AbstractFileProvider() {
        fileNameParser = GenericFileNameParser.getInstance();
    }

    /**
     * Adds a file system to those cached by this provider.
     * <p>
     * The file system may implement {@link VfsComponent}, in which case it is initialized.
     * </p>
     *
     * @param key The root file of the file system, part of the cache key.
     * @param fs the file system to add.
     * @throws FileSystemException if any error occurs.
     */
    protected void addFileSystem(final Comparable<?> key, final FileSystem fs) throws FileSystemException {
        // Add to the container and initialize
        addComponent(fs);
        final FileSystemKey treeKey = new FileSystemKey(key, fs.getFileSystemOptions());
        ((AbstractFileSystem) fs).setCacheKey(treeKey);
        synchronized (fileSystemMap) {
            fileSystemMap.put(treeKey, fs);
        }
    }

    /**
     * Closes the file systems created by this provider.
     */
    @Override
    public void close() {
        synchronized (fileSystemMap) {
            fileSystemMap.clear();
        }
        super.close();
    }

    /**
     * Closes the FileSystem.
     *
     * @param fileSystem The FileSystem to close.
     */
    public void closeFileSystem(final FileSystem fileSystem) {
        final AbstractFileSystem fs = (AbstractFileSystem) fileSystem;

        final FileSystemKey key = fs.getCacheKey();
        if (key != null) {
            synchronized (fileSystemMap) {
                fileSystemMap.remove(key);
            }
        }

        removeComponent(fs);
        fs.close();
    }

    /**
     * Creates a layered file system. This method throws a 'not supported' exception.
     *
     * @param scheme The protocol to use to access the file.
     * @param file a FileObject.
     * @param fileSystemOptions Options to the file system.
     * @return A FileObject associated with the new FileSystem.
     * @throws FileSystemException if an error occurs.
     */
    @Override
    public FileObject createFileSystem(final String scheme, final FileObject file,
        final FileSystemOptions fileSystemOptions) throws FileSystemException {
        // Can't create a layered file system
        throw new FileSystemException("vfs.provider/not-layered-fs.error", scheme);
    }

    /**
     * Locates a cached file system.
     *
     * @param key The root file of the file system, part of the cache key.
     * @param fileSystemOptions file system options the file system instance must have, may be null.
     * @return The file system instance, or null if it is not cached.
     */
    protected FileSystem findFileSystem(final Comparable<?> key, final FileSystemOptions fileSystemOptions) {
        synchronized (fileSystemMap) {
            return fileSystemMap.get(new FileSystemKey(key, fileSystemOptions));
        }
    }

    /**
     * Frees unused resources.
     */
    public void freeUnusedResources() {
        final AbstractFileSystem[] abstractFileSystems;
        synchronized (fileSystemMap) {
            // create snapshot under lock
            abstractFileSystems = fileSystemMap.values().toArray(EMPTY_ABSTRACT_FILE_SYSTEMS);
        }

        // process snapshot outside lock
        Stream.of(abstractFileSystems).filter(AbstractFileSystem::isReleaseable)
                                      .forEach(AbstractFileSystem::closeCommunicationLink);
    }

    /**
     * Gets the FileSystemConfigBuilder.
     *
     * @return the FileSystemConfigBuilder.
     */
    @Override
    public FileSystemConfigBuilder getConfigBuilder() {
        return null;
    }

    /**
     * Gets the file name parser.
     *
     * @return the file name parser.
     */
    protected FileNameParser getFileNameParser() {
        return fileNameParser;
    }

    /**
     * Parses an absolute URI.
     *
     * @param base The base file - if null the {@code uri} needs to be absolute
     * @param uri The URI to parse.
     * @return The FileName.
     * @throws FileSystemException if an error occurs.
     */
    @Override
    public FileName parseUri(final FileName base, final String uri) throws FileSystemException {
        if (getFileNameParser() != null) {
            return getFileNameParser().parseUri(getContext(), base, uri);
        }
        throw new FileSystemException("vfs.provider/filename-parser-missing.error");
    }

    /**
     * Sets the file name parser.
     *
     * @param parser a file name parser.
     */
    protected void setFileNameParser(final FileNameParser parser) {
        this.fileNameParser = parser;
    }
}
