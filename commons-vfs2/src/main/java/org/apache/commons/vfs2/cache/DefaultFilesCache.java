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
package org.apache.commons.vfs2.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;

/**
 * A simple {@link org.apache.commons.vfs2.FilesCache FilesCache} implementation.
 * <p>
 * This implementation caches every file with no expire or limit. All files and file systems are hard reachable
 * references. This implementation holds a list of file system specific {@linkplain ConcurrentHashMap ConcurrentHashMaps}
 * in the main cache map.
 * </p>
 * <p>
 * Cached {@linkplain FileObject FileObjects} as well as {@linkplain FileSystem FileSystems} are only removed when
 * {@link #clear(FileSystem)} is called (i.e. on file system close). When the used
 * {@link org.apache.commons.vfs2.FileSystemManager FileSystemManager} is closed, it will also {@linkplain #close()
 * close} this cache (which frees all entries).
 * </p>
 * <p>
 * Despite its name, this is not the fallback implementation used by
 * {@link org.apache.commons.vfs2.impl.DefaultFileSystemManager#init() DefaultFileSystemManager#init()} anymore.
 * </p>
 */
public class DefaultFilesCache extends AbstractFilesCache {

    private static final float LOAD_FACTOR = 0.75f;
    private static final int INITIAL_CAPACITY = 200;

    /** The FileSystem cache. Keeps one Map for each FileSystem. */
    private final ConcurrentMap<FileSystem, ConcurrentMap<FileName, FileObject>> fileSystemCache = new ConcurrentHashMap<>(10);

    @Override
    public void clear(final FileSystem filesystem) {
        // avoid keeping a reference to the FileSystem (key) object
        final Map<FileName, FileObject> files = fileSystemCache.remove(filesystem);
        if (files != null) {
            files.clear(); // help GC
        }
    }

    @Override
    public void close() {
        super.close();
        fileSystemCache.clear();
    }

    @Override
    public FileObject getFile(final FileSystem filesystem, final FileName name) {
        // avoid creating filesystem entry for empty filesystem cache:
        final Map<FileName, FileObject> files = fileSystemCache.get(filesystem);
        if (files == null) {
            // cache for filesystem is not known => file is not cached:
            return null;
        }
        return files.get(name); // or null
    }

    /**
     * Gets or creates a Map.
     *
     * @param fileSystem the key
     * @return an existing or new Map.
     */
    protected ConcurrentMap<FileName, FileObject> getOrCreateFilesystemCache(final FileSystem fileSystem) {
        ConcurrentMap<FileName, FileObject> files = fileSystemCache.get(fileSystem);
        // we loop to make sure we never return null even when concurrent clean is called
        while (files == null) {
            files = fileSystemCache.computeIfAbsent(fileSystem,
                k -> new ConcurrentHashMap<>(INITIAL_CAPACITY, LOAD_FACTOR, Math.max(2, Runtime.getRuntime().availableProcessors()) / 2));
        }
        return files;
    }

    @Override
    public void putFile(final FileObject file) {
        final Map<FileName, FileObject> files = getOrCreateFilesystemCache(file.getFileSystem());
        files.put(file.getName(), file);
    }

    @Override
    public boolean putFileIfAbsent(final FileObject file) {
        final ConcurrentMap<FileName, FileObject> files = getOrCreateFilesystemCache(file.getFileSystem());
        return files.putIfAbsent(file.getName(), file) == null;
    }

    @Override
    public void removeFile(final FileSystem filesystem, final FileName name) {
        // avoid creating filesystem entry for empty filesystem cache:
        final Map<FileName, FileObject> files = fileSystemCache.get(filesystem);
        if (files != null) {
            files.remove(name);
            // This would be too racey:
            // if (files.empty()) filesystemCache.remove(filessystem);
        }
    }
}
