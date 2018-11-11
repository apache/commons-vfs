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
 * The default constructor uses a LRU size of 100 per filesystem.
 */
public class LRUFilesCache extends AbstractFilesCache {
    /** The default LRU size */
    private static final int DEFAULT_LRU_SIZE = 100;

    /** The logger to use. */
    private static final Log log = LogFactory.getLog(LRUFilesCache.class);

    /** The FileSystem cache */
    private final ConcurrentMap<FileSystem, Map<FileName, FileObject>> filesystemCache = new ConcurrentHashMap<>(10);

    /** The size of the cache */
    private final int lruSize;

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

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

        public MyLRUMap(final FileSystem filesystem, final int size) {
            super(size, true);
            this.filesystem = filesystem;
        }

        @Override
        protected boolean removeLRU(final AbstractLinkedMap.LinkEntry<FileName, FileObject> linkEntry) {
            synchronized (LRUFilesCache.this) {
                final FileObject file = linkEntry.getValue();

                // System.err.println(">>> " + size() + " check removeLRU:" + linkEntry.getKey().toString());

                if (file.isAttached() || file.isContentOpen()) {
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
                        file.close();
                    } catch (final FileSystemException e) {
                        VfsLog.warn(getLogger(), log, Messages.getString("vfs.impl/LRUFilesCache-remove-ex.warn"), e);
                    }

                    final Map<?, ?> files = filesystemCache.get(filesystem);
                    if (files.size() < 1) {
                        filesystemCache.remove(filesystem);
                    }

                    return true;
                }

                return false;
            }
        }
    }

    /**
     * Default constructor. Uses a LRU size of 100 per filesystem.
     */
    public LRUFilesCache() {
        this(DEFAULT_LRU_SIZE);
    }

    /**
     * Set the desired LRU size.
     *
     * @param lruSize the LRU size
     */
    public LRUFilesCache(final int lruSize) {
        this.lruSize = lruSize;
    }

    @Override
    public void putFile(final FileObject file) {
        final Map<FileName, FileObject> files = getOrCreateFilesystemCache(file.getFileSystem());

        writeLock.lock();
        try {
            files.put(file.getName(), file);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean putFileIfAbsent(final FileObject file) {
        final Map<FileName, FileObject> files = getOrCreateFilesystemCache(file.getFileSystem());

        writeLock.lock();
        try {
            final FileName name = file.getName();

            if (files.containsKey(name)) {
                return false;
            }

            files.put(name, file);
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public FileObject getFile(final FileSystem filesystem, final FileName name) {
        final Map<FileName, FileObject> files = getOrCreateFilesystemCache(filesystem);

        readLock.lock();
        try {
            return files.get(name);
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void clear(final FileSystem filesystem) {
        final Map<FileName, FileObject> files = getOrCreateFilesystemCache(filesystem);

        writeLock.lock();
        try {
            files.clear();

            filesystemCache.remove(filesystem);
        } finally {
            writeLock.unlock();
        }
    }

    protected Map<FileName, FileObject> getOrCreateFilesystemCache(final FileSystem filesystem) {
        Map<FileName, FileObject> files = filesystemCache.get(filesystem);
        if (files == null) {
            files = new MyLRUMap(filesystem, lruSize);
            filesystemCache.putIfAbsent(filesystem, files);
        }
        return files;
    }

    @Override
    public void close() {
        super.close();
        filesystemCache.clear();
    }

    @Override
    public void removeFile(final FileSystem filesystem, final FileName name) {
        final Map<?, ?> files = getOrCreateFilesystemCache(filesystem);

        writeLock.lock();
        try {
            files.remove(name);

            if (files.size() < 1) {
                filesystemCache.remove(filesystem);
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void touchFile(final FileObject file) {
        // this moves the file back on top
        getFile(file.getFileSystem(), file.getName());
    }
}
