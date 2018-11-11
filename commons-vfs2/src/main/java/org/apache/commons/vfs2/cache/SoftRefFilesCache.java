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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.VfsLog;
import org.apache.commons.vfs2.util.Messages;

/**
 * This implementation caches every file as long as it is strongly reachable by the java vm. As soon as the vm needs
 * memory - every softly reachable file will be discarded.
 *
 * @see SoftReference
 */
public class SoftRefFilesCache extends AbstractFilesCache {
    private static final int TIMEOUT = 1000;

    private static final Log log = LogFactory.getLog(SoftRefFilesCache.class);

    private final ConcurrentMap<FileSystem, Map<FileName, Reference<FileObject>>> fileSystemCache = new ConcurrentHashMap<>();
    private final Map<Reference<FileObject>, FileSystemAndNameKey> refReverseMap = new HashMap<>(100);
    private final ReferenceQueue<FileObject> refQueue = new ReferenceQueue<>();

    private volatile SoftRefReleaseThread softRefReleaseThread = null; // @GuardedBy("lock")

    private final Lock lock = new ReentrantLock();

    /**
     * This thread will listen on the ReferenceQueue and remove the entry in the filescache as soon as the vm removes
     * the reference
     */
    private final class SoftRefReleaseThread extends Thread {
        private volatile boolean requestEnd; // used for inter-thread communication

        private SoftRefReleaseThread() {
            setName(SoftRefReleaseThread.class.getName());
            setDaemon(true);
        }

        @Override
        public void run() {
            loop: while (!requestEnd && !Thread.currentThread().isInterrupted()) {
                try {
                    final Reference<?> ref = refQueue.remove(TIMEOUT);
                    if (ref == null) {
                        continue;
                    }

                    lock.lock();
                    try {
                        final FileSystemAndNameKey key = refReverseMap.get(ref);

                        if (key != null && removeFile(key)) {
                            close(key.getFileSystem());
                        }
                    } finally {
                        lock.unlock();
                    }
                } catch (final InterruptedException e) {
                    if (!requestEnd) {
                        VfsLog.warn(getLogger(), log,
                                Messages.getString("vfs.impl/SoftRefReleaseThread-interrupt.info"));
                    }
                    break loop;
                }
            }
        }
    }

    public SoftRefFilesCache() {
    }

    private void startThread() {
        // Double Checked Locking is allowed when volatile
        if (softRefReleaseThread != null) {
            return;
        }

        synchronized (lock) {
            if (softRefReleaseThread == null) {
                softRefReleaseThread = new SoftRefReleaseThread();
                softRefReleaseThread.start();
            }
        }
    }

    private void endThread() {
        synchronized (lock) {
            final SoftRefReleaseThread thread = softRefReleaseThread;
            softRefReleaseThread = null;
            if (thread != null) {
                thread.requestEnd = true;
                thread.interrupt();
            }
        }
    }

    @Override
    public void putFile(final FileObject fileObject) {
        if (log.isDebugEnabled()) {
            log.debug("putFile: " + this.getSafeName(fileObject));
        }

        final Map<FileName, Reference<FileObject>> files = getOrCreateFilesystemCache(fileObject.getFileSystem());

        final Reference<FileObject> ref = createReference(fileObject, refQueue);
        final FileSystemAndNameKey key = new FileSystemAndNameKey(fileObject.getFileSystem(), fileObject.getName());

        lock.lock();
        try {
            final Reference<FileObject> old = files.put(fileObject.getName(), ref);
            if (old != null) {
                refReverseMap.remove(old);
            }
            refReverseMap.put(ref, key);
        } finally {
            lock.unlock();
        }
    }

    private String getSafeName(final FileName fileName) {
        return fileName.getFriendlyURI();
    }

    private String getSafeName(final FileObject fileObject) {
        return this.getSafeName(fileObject.getName());
    }

    @Override
    public boolean putFileIfAbsent(final FileObject fileObject) {
        if (log.isDebugEnabled()) {
            log.debug("putFile: " + this.getSafeName(fileObject));
        }

        final Map<FileName, Reference<FileObject>> files = getOrCreateFilesystemCache(fileObject.getFileSystem());

        final Reference<FileObject> ref = createReference(fileObject, refQueue);
        final FileSystemAndNameKey key = new FileSystemAndNameKey(fileObject.getFileSystem(), fileObject.getName());

        lock.lock();
        try {
            if (files.containsKey(fileObject.getName()) && files.get(fileObject.getName()).get() != null) {
                return false;
            }
            final Reference<FileObject> old = files.put(fileObject.getName(), ref);
            if (old != null) {
                refReverseMap.remove(old);
            }
            refReverseMap.put(ref, key);
            return true;
        } finally {
            lock.unlock();
        }
    }

    protected Reference<FileObject> createReference(final FileObject file, final ReferenceQueue<FileObject> refqueue) {
        return new SoftReference<>(file, refqueue);
    }

    @Override
    public FileObject getFile(final FileSystem fileSystem, final FileName fileName) {
        final Map<FileName, Reference<FileObject>> files = getOrCreateFilesystemCache(fileSystem);

        lock.lock();
        try {
            final Reference<FileObject> ref = files.get(fileName);
            if (ref == null) {
                return null;
            }

            final FileObject fo = ref.get();
            if (fo == null) {
                removeFile(fileSystem, fileName);
            }
            return fo;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear(final FileSystem fileSystem) {
        final Map<FileName, Reference<FileObject>> files = getOrCreateFilesystemCache(fileSystem);

        lock.lock();
        try {
            final Iterator<FileSystemAndNameKey> iterKeys = refReverseMap.values().iterator();
            while (iterKeys.hasNext()) {
                final FileSystemAndNameKey key = iterKeys.next();
                if (key.getFileSystem() == fileSystem) {
                    iterKeys.remove();
                    files.remove(key.getFileName());
                }
            }

            if (files.size() < 1) {
                close(fileSystem);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Called while the lock is held
     *
     * @param fileSystem The file system to close.
     */
    private void close(final FileSystem fileSystem) {
        if (log.isDebugEnabled()) {
            log.debug("close fs: " + fileSystem.getRootName());
        }

        fileSystemCache.remove(fileSystem);
        if (fileSystemCache.size() < 1) {
            endThread();
        }
        /*
         * This is not thread-safe as another thread might be opening the file system ((DefaultFileSystemManager)
         * getContext().getFileSystemManager()) ._closeFileSystem(filesystem);
         */
    }

    @Override
    public void close() {
        super.close();

        endThread();

        lock.lock();
        try {
            fileSystemCache.clear();

            refReverseMap.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeFile(final FileSystem fileSystem, final FileName fileName) {
        if (removeFile(new FileSystemAndNameKey(fileSystem, fileName))) {
            close(fileSystem);
        }
    }

    private boolean removeFile(final FileSystemAndNameKey key) {
        if (log.isDebugEnabled()) {
            log.debug("removeFile: " + this.getSafeName(key.getFileName()));
        }

        final Map<?, ?> files = getOrCreateFilesystemCache(key.getFileSystem());

        lock.lock();
        try {
            final Object ref = files.remove(key.getFileName());
            if (ref != null) {
                refReverseMap.remove(ref);
            }

            return files.size() < 1;
        } finally {
            lock.unlock();
        }
    }

    protected Map<FileName, Reference<FileObject>> getOrCreateFilesystemCache(final FileSystem fileSystem) {
        if (fileSystemCache.size() < 1) {
            startThread();
        }

        Map<FileName, Reference<FileObject>> files;

        do {
            files = fileSystemCache.get(fileSystem);
            if (files != null) {
                break;
            }
            files = new HashMap<>();
        } while (fileSystemCache.putIfAbsent(fileSystem, files) == null);

        return files;
    }
}
