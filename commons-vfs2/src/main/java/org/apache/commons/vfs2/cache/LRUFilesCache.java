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
package org.apache.commons.vfs2.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.collections4.map.AbstractLinkedMap;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.concurrent.locks.LockingVisitors;
import org.apache.commons.lang3.concurrent.locks.LockingVisitors.ReadWriteLockVisitor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.VfsLog;
import org.apache.commons.vfs2.util.Messages;

/**
 * Caches every file using an {@link LRUMap}.
 * <p>
 * The default constructor uses a LRU size of 100 per file system.
 * </p>
 */
public class LRUFilesCache extends AbstractFilesCache {

    /**
     * The file cache.
     */
    private class MyLRUMap extends LRUMap<FileName, FileObject> {

        /**
         * serialVersionUID format is YYYYMMDD for the date of the last binary change.
         */
        private static final long serialVersionUID = 20101208L;

        /** The FileSystem */
        private final FileSystem fileSystem;

        MyLRUMap(final FileSystem filesystem, final int size) {
            super(size, true);
            this.fileSystem = filesystem;
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
                    // force detach
                    IOUtils.closeQuietly(fileObject, e -> VfsLog.warn(getLogger(), log, Messages.getString("vfs.impl/LRUFilesCache-remove-ex.warn"), e));
                    final Map<?, ?> files = fileSystemCache.get(fileSystem);
                    if (files.isEmpty()) {
                        fileSystemCache.remove(fileSystem);
                    }
                    return true;
                }
                return false;
            }
        }
    }

    /** The default LRU size. */
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
     * @param lruSize the LRU size.
     */
    public LRUFilesCache(final int lruSize) {
        this.lruSize = lruSize;
    }

    @Override
    public void clear(final FileSystem filesystem) {
        createLockingVisitors(filesystem).acceptReadLocked(m -> {
            m.clear();
            fileSystemCache.remove(filesystem);
        });
    }

    @Override
    public void close() {
        super.close();
        fileSystemCache.clear();
    }

    private ReadWriteLockVisitor<Map<FileName, FileObject>> createLockingVisitors(final FileObject file) {
        return createLockingVisitors(file.getFileSystem());
    }

    private ReadWriteLockVisitor<Map<FileName, FileObject>> createLockingVisitors(final FileSystem filesystem) {
        return LockingVisitors.create(getOrCreateFilesystemCache(filesystem), rwLock);
    }

    @Override
    public FileObject getFile(final FileSystem filesystem, final FileName name) {
        return createLockingVisitors(filesystem).applyReadLocked(m -> m.get(name));
    }

    /**
     * Gets or creates a new Map.
     *
     * @param fileSystem the key.
     * @return an existing or new Map.
     */
    protected Map<FileName, FileObject> getOrCreateFilesystemCache(final FileSystem fileSystem) {
        return fileSystemCache.computeIfAbsent(fileSystem, k -> new MyLRUMap(k, lruSize));
    }

    @Override
    public void putFile(final FileObject file) {
        createLockingVisitors(file).acceptWriteLocked(m -> m.put(file.getName(), file));
    }

    @Override
    public boolean putFileIfAbsent(final FileObject file) {
        return createLockingVisitors(file).applyWriteLocked(m -> m.putIfAbsent(file.getName(), file) == null);
    }

    @Override
    public void removeFile(final FileSystem filesystem, final FileName name) {
        createLockingVisitors(filesystem).acceptWriteLocked(m -> {
            m.remove(name);
            if (m.isEmpty()) {
                fileSystemCache.remove(filesystem);
            }
        });
    }

    @Override
    public void touchFile(final FileObject file) {
        // this moves the file back on top
        getFile(file.getFileSystem(), file.getName());
    }
}
