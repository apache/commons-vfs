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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.collections4.map.AbstractLinkedMap;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VfsLog;
import org.apache.commons.vfs2.util.Messages;

/**
 * This implementation caches every file using {@link LRUMap}.
 * <p>
 * The default constructor uses a LRU size of 100 per file system.
 * </p>
 */
public class LRUFilesCache extends AbstractFilesCache {

    /**
     * The file cache
     */
    private class MyLRUMap extends LRUMap<FileName, FileObject> {
        /**
         * serialVersionUID format is YYYYMMDD for the date of the last binary change.
         */
        private static final long serialVersionUID = 20101208L;

        /** The FileSystem */
        private final FileSystem filesystem;

        MyLRUMap(final FileSystem filesystem, final int size) {
            super(size, true);
            this.filesystem = filesystem;
        }

        @Override
        protected boolean removeLRU(final AbstractLinkedMap.LinkEntry<FileName, FileObject> linkEntry) {
            synchronized (LRUFilesCache.this) {
                @SuppressWarnings("resource") // FileObject allocated elsewhere.
                final FileObject fileObject = linkEntry.getValue();

                // System.err.println(">>> " + size() + " check removeLRU:" + linkEntry.getKey().toString());

                if (fileObject.isAttached() || fileObject.isContentOpen()) {
                    // do not allow open or attached files to be removed
                    // System.err.println(">>> " + size() + " VETO removeLRU:" +
                    // linkEntry.getKey().toString() + " (" + file.isAttached() + "/" +
                    // file.isContentOpen() + ")");
                    return false;
                }

                // System.err.println(">>> " + size() + " removeLRU:" + linkEntry.getKey().toString());
                if (super.removeLRU(linkEntry)) {
                    try {
                        // force detach
                        fileObject.close();
                    } catch (final FileSystemException e) {
                        VfsLog.warn(getLogger(), log, Messages.getString("vfs.impl/LRUFilesCache-remove-ex.warn"), e);
                    }

                    final Map<?, ?> files = fileSystemCache.get(filesystem);
                    if (files.isEmpty()) {
                        fileSystemCache.remove(filesystem);
                    }

                    return true;
                }

                return false;
            }
        }
    }

    /** The default LRU size */
    private static final int DEFAULT_LRU_SIZE = 100;

    /** The logger to use. */
    private static final Log log = LogFactory.getLog(LRUFilesCache.class);

    /** The FileSystem cache */
    private final ConcurrentMap<FileSystem, Map<FileName, FileObject>> fileSystemCache = new ConcurrentHashMap<>();

    /** The size of the cache */
    private final int lruSize;
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    /**
     * Constructs a new instance. Uses an LRU size of 100 per file system.
     */
    public LRUFilesCache() {
        this(DEFAULT_LRU_SIZE);
    }

    /**
     * Constructs a new instance with the desired LRU size.
     *
     * @param lruSize the LRU size
     */
    public LRUFilesCache(final int lruSize) {
        this.lruSize = lruSize;
    }

    @Override
    public void clear(final FileSystem filesystem) {
        final Map<FileName, FileObject> files = getOrCreateFilesystemCache(filesystem);

        writeLock().lock();
        try {
            files.clear();

            fileSystemCache.remove(filesystem);
        } finally {
            writeLock().unlock();
        }
    }

    @Override
    public void close() {
        super.close();
        fileSystemCache.clear();
    }

    @Override
    public FileObject getFile(final FileSystem filesystem, final FileName name) {
        final Map<FileName, FileObject> files = getOrCreateFilesystemCache(filesystem);
        readLock().lock();
        try {
            return files.get(name);
        } finally {
            readLock().unlock();
        }
    }

    /**
     * Gets or creates a new Map.
     *
     * @param fileSystem the key
     * @return an existing or new Map.
     */
    protected Map<FileName, FileObject> getOrCreateFilesystemCache(final FileSystem fileSystem) {
        return fileSystemCache.computeIfAbsent(fileSystem, k -> new MyLRUMap(k, lruSize));
    }

    @Override
    public void putFile(final FileObject file) {
        final Map<FileName, FileObject> files = getOrCreateFilesystemCache(file.getFileSystem());

        writeLock().lock();
        try {
            files.put(file.getName(), file);
        } finally {
            writeLock().unlock();
        }
    }

    @Override
    public boolean putFileIfAbsent(final FileObject file) {
        final Map<FileName, FileObject> files = getOrCreateFilesystemCache(file.getFileSystem());

        writeLock().lock();
        try {
            return files.putIfAbsent(file.getName(), file) == null;
        } finally {
            writeLock().unlock();
        }
    }

    private Lock readLock() {
        return rwLock.readLock();
    }

    @Override
    public void removeFile(final FileSystem filesystem, final FileName name) {
        final Map<?, ?> files = getOrCreateFilesystemCache(filesystem);

        writeLock().lock();
        try {
            files.remove(name);

            if (files.isEmpty()) {
                fileSystemCache.remove(filesystem);
            }
        } finally {
            writeLock().unlock();
        }
    }

    @Override
    public void touchFile(final FileObject file) {
        // this moves the file back on top
        getFile(file.getFileSystem(), file.getName());
    }

    private Lock writeLock() {
        return rwLock.writeLock();
    }
}
