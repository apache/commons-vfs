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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;

/**
 * This implementation caches every file as long as it is strongly reachable by the JVM. As soon as the JVM needs
 * memory - every softly reachable file will be discarded.
 *
 * @see SoftReference
 */
public class SoftRefFilesCache extends AbstractFilesCache {

    /**
     * This thread will listen on the ReferenceQueue and remove the entry in the file cache as soon as the JVM removes
     * the reference.
     */
    private final class ReleaseThread extends Thread {
        private ReleaseThread() {
            setName(ReleaseThread.class.getName());
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    removeFile(refQueue.remove(0));
                }
            } catch (final InterruptedException e) {
                // end thread run.
                // System.out.println("Thread caught InterruptedException, ending " + getId());
                // System.out.flush();
            }
        }
    }

    private static final Log log = LogFactory.getLog(SoftRefFilesCache.class);
    private final Map<FileSystem, Map<FileName, Reference<FileObject>>> fileSystemCache = new HashMap<>();
    private final Map<Reference<FileObject>, FileSystemAndNameKey> refReverseMap = new HashMap<>(100);
    private final ReferenceQueue<FileObject> refQueue = new ReferenceQueue<>();
    private ReleaseThread releaseThread;

    /**
     * Constructs a new instance.
     */
    public SoftRefFilesCache() {
        // empty
    }

    @Override
    public synchronized void clear(final FileSystem fileSystem) {
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
    }

    @Override
    public synchronized void close() {
        super.close();
        endThread();
        fileSystemCache.clear();
        refReverseMap.clear();
    }

    /**
     * @param fileSystem The file system to close.
     */
    private synchronized void close(final FileSystem fileSystem) {
        if (log.isDebugEnabled()) {
            log.debug("Close FileSystem: " + fileSystem.getRootName());
        }

        fileSystemCache.remove(fileSystem);
        if (fileSystemCache.isEmpty()) {
            endThread();
        }
    }

    protected Reference<FileObject> createReference(final FileObject file, final ReferenceQueue<FileObject> refqueue) {
        return new SoftReference<>(file, refqueue);
    }

    private synchronized void endThread() {
        final ReleaseThread thread = releaseThread;
        releaseThread = null;
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public synchronized FileObject getFile(final FileSystem fileSystem, final FileName fileName) {
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
    }

    protected synchronized Map<FileName, Reference<FileObject>> getOrCreateFilesystemCache(final FileSystem fileSystem) {
        if (fileSystemCache.isEmpty()) {
            startThread();
        }

        return fileSystemCache.computeIfAbsent(fileSystem, k -> new HashMap<>());
    }

    private String getSafeName(final FileName fileName) {
        return fileName.getFriendlyURI();
    }

    private String getSafeName(final FileObject fileObject) {
        return this.getSafeName(fileObject.getName());
    }

    @Override
    public void putFile(final FileObject fileObject) {
        if (log.isDebugEnabled()) {
            log.debug("putFile: " + this.getSafeName(fileObject));
        }

        synchronized(this) {
            final Map<FileName, Reference<FileObject>> files = getOrCreateFilesystemCache(fileObject.getFileSystem());

            final Reference<FileObject> ref = createReference(fileObject, refQueue);
            final FileSystemAndNameKey key = new FileSystemAndNameKey(fileObject.getFileSystem(), fileObject.getName());

            final Reference<FileObject> old = files.put(fileObject.getName(), ref);
            if (old != null) {
                refReverseMap.remove(old);
            }
            refReverseMap.put(ref, key);
        }
    }

    @Override
    public boolean putFileIfAbsent(final FileObject fileObject) {
        if (log.isDebugEnabled()) {
            log.debug("putFile: " + this.getSafeName(fileObject));
        }

        synchronized(this) {
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
        }
    }

    @Override
    public synchronized void removeFile(final FileSystem fileSystem, final FileName fileName) {
        if (removeFile(new FileSystemAndNameKey(fileSystem, fileName))) {
            close(fileSystem);
        }
    }

    private synchronized boolean removeFile(final FileSystemAndNameKey key) {
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

    private synchronized void removeFile(final Reference<?> ref) {
        final FileSystemAndNameKey key = refReverseMap.get(ref);
        if (key != null && removeFile(key)) {
            close(key.getFileSystem());
        }
    }

    private synchronized void startThread() {
        if (releaseThread == null) {
            releaseThread = new ReleaseThread();
            releaseThread.start();
            // System.out.println("Started thread ID " + releaseThread.getId());
            // System.out.flush();
            // Thread.dumpStack();
        }
    }

    @Override
    public String toString() {
        return super.toString() + " [releaseThread=" + releaseThread
            + (releaseThread == null ? "" : "(ID " + releaseThread.getId() + " is " + releaseThread.getState() + ")")
            + "]";
    }
}
