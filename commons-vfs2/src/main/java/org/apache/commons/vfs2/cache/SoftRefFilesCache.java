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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.util.Messages;

/**
 * This implementation caches every file as long as it is strongly reachable by the java vm. As soon as the vm needs
 * memory - every softly reachable file will be discarded.
 *
 * @see SoftReference
 */
public class SoftRefFilesCache extends AbstractFilesCache {

    private static final Log log = LogFactory.getLog(SoftRefFilesCache.class);

    private final Map<FileSystem, Map<FileName, Reference<FileObject>>> fileSystemCache = new HashMap<>();
    private final Map<Reference<FileObject>, FileSystemAndNameKey> refReverseMap = new HashMap<>(100);
    private final ReferenceQueue<FileObject> refQueue = new ReferenceQueue<>();

    private SoftRefReleaseThread softRefReleaseThread; // @GuardedBy("lock")

    private final Lock lock = new ReentrantLock();

    /**
     * This thread will listen on the ReferenceQueue and remove the entry in the filescache as soon as the vm removes
     * the reference
     */
    private final class SoftRefReleaseThread extends Thread {
        private SoftRefReleaseThread() {
            setName(SoftRefReleaseThread.class.getName());
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    final Reference<?> ref = refQueue.remove(0);
                    if (ref == null) {
                        continue;
                    }

                    lock.lock();
                    try {
                        removeFile(ref);
                    } finally {
                        lock.unlock();
                    }
                }
            } catch (final InterruptedException e) {
            }
        }
    }

    public SoftRefFilesCache() {
    }

    /**
     * Called while the lock is held
     */
    private void startThread() {
        if (softRefReleaseThread == null) {
            softRefReleaseThread = new SoftRefReleaseThread();
            softRefReleaseThread.start();
        }
    }

    /**
     * Called while the lock is held
     */
    private void endThread() {
        final SoftRefReleaseThread thread = softRefReleaseThread;
        softRefReleaseThread = null;
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void putFile(final FileObject fileObject) {
        if (log.isDebugEnabled()) {
            log.debug("putFile: " + this.getSafeName(fileObject));
        }

        lock.lock();
        try {
            final Map<FileName, Reference<FileObject>> files = getOrCreateFilesystemCache(fileObject.getFileSystem());

            final Reference<FileObject> ref = createReference(fileObject, refQueue);
            final FileSystemAndNameKey key = new FileSystemAndNameKey(fileObject.getFileSystem(), fileObject.getName());

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

        lock.lock();
        try {
            final Map<FileName, Reference<FileObject>> files = getOrCreateFilesystemCache(fileObject.getFileSystem());

            final Reference<FileObject> ref = createReference(fileObject, refQueue);
            final FileSystemAndNameKey key = new FileSystemAndNameKey(fileObject.getFileSystem(), fileObject.getName());

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
        lock.lock();
        try {
            final Map<FileName, Reference<FileObject>> files = getOrCreateFilesystemCache(fileSystem);

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
        lock.lock();
        try {
            final Map<FileName, Reference<FileObject>> files = getOrCreateFilesystemCache(fileSystem);

            final Iterator<FileSystemAndNameKey> iterKeys = refReverseMap.values().iterator();
            while (iterKeys.hasNext()) {
                final FileSystemAndNameKey key = iterKeys.next();
                if (key.getFileSystem() == fileSystem) {
                    iterKeys.remove();
                    files.remove(key.getFileName());
                }
            }

            if (files.isEmpty()) {
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
        if (fileSystemCache.isEmpty()) {
            endThread();
        }
    }

    @Override
    public void close() {
        lock.lock();
        try {
            endThread();

            fileSystemCache.clear();

            refReverseMap.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeFile(final FileSystem fileSystem, final FileName fileName) {
        lock.lock();
        try {
            if (removeFile(new FileSystemAndNameKey(fileSystem, fileName))) {
                close(fileSystem);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Called while the lock is held
     */
    private boolean removeFile(final FileSystemAndNameKey key) {
        if (log.isDebugEnabled()) {
            log.debug("removeFile: " + this.getSafeName(key.getFileName()));
        }

        final Map<?, ?> files = getOrCreateFilesystemCache(key.getFileSystem());

        final Object ref = files.remove(key.getFileName());
        if (ref != null) {
            refReverseMap.remove(ref);
        }

        return files.isEmpty();
    }

    /**
     * Called while the lock is held
     */
    private void removeFile(final Reference<?> ref) {
        final FileSystemAndNameKey key = refReverseMap.get(ref);

        if (key != null && removeFile(key)) {
            close(key.getFileSystem());
        }
    }

    /**
     * Called while the lock is held
     */
    protected Map<FileName, Reference<FileObject>> getOrCreateFilesystemCache(final FileSystem fileSystem) {
        if (fileSystemCache.isEmpty()) {
            startThread();
        }

        Map<FileName, Reference<FileObject>> files = fileSystemCache.get(fileSystem);
        if (files == null) {
            files = new HashMap<>();
            fileSystemCache.put(fileSystem, files);
        }

        return files;
    }
}
