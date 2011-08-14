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
import java.util.concurrent.atomic.AtomicReference;
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
 * This implementation caches every file as long as it is strongly reachable by
 * the java vm. As soon as the vm needs memory - every softly reachable file
 * will be discarded.
 *
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 * @version $Revision$ $Date: 2005-09-30 09:02:41 +0200 (Fr, 30 Sep
 *          2005) $
 * @see SoftReference
 */
public class SoftRefFilesCache extends AbstractFilesCache
{
    private static final int TIMEOUT = 1000;
    /**
     * The logger to use.
     */
    private final Log log = LogFactory.getLog(SoftRefFilesCache.class);

    private final ConcurrentMap<FileSystem, Map<FileName, Reference<FileObject>>> filesystemCache =
          new ConcurrentHashMap<FileSystem, Map<FileName, Reference<FileObject>>>();
    private final Map<Reference<FileObject>, FileSystemAndNameKey> refReverseMap =
          new HashMap<Reference<FileObject>, FileSystemAndNameKey>(100);
    private final ReferenceQueue<FileObject> refqueue = new ReferenceQueue<FileObject>();

    private final AtomicReference<SoftRefReleaseThread> softRefReleaseThread = new AtomicReference<SoftRefReleaseThread>();

    private final Lock lock = new ReentrantLock();


    /**
     * This thread will listen on the ReferenceQueue and remove the entry in the
     * filescache as soon as the vm removes the reference
     */
    private final class SoftRefReleaseThread extends Thread
    {
        private volatile boolean requestEnd; // used for inter-thread communication

        private SoftRefReleaseThread()
        {
            setName(SoftRefReleaseThread.class.getName());
            setDaemon(true);
        }

        @Override
        public void run()
        {
            loop: while (!requestEnd && !Thread.currentThread().isInterrupted())
            {
                try
                {
                    Reference<?> ref = refqueue.remove(TIMEOUT);
                    if (ref == null)
                    {
                        continue;
                    }

                    lock.lock();
                    try
                    {
                        FileSystemAndNameKey key = refReverseMap.get(ref);

                        if (key != null)
                        {
                            if (removeFile(key))
                            {
                                filesystemClose(key.getFileSystem());
                            }
                        }
                    }
                    finally
                    {
                        lock.unlock();
                    }
                }
                catch (InterruptedException e)
                {
                    if (!requestEnd)
                    {
                        VfsLog.warn(getLogger(), log,
                                    Messages.getString("vfs.impl/SoftRefReleaseThread-interrupt.info"));
                    }
                    break loop;
                }
            }
        }
    }

    public SoftRefFilesCache()
    {
    }

    private void startThread()
    {
        Thread thread;
        SoftRefReleaseThread newThread;
        do
        {
            newThread = null;
            thread = softRefReleaseThread.get();
            if (thread != null)
            {
                break;
            }
            newThread = new SoftRefReleaseThread();
        } while (softRefReleaseThread.compareAndSet(null, newThread));
        if (newThread != null)
        {
            newThread.start();
        }
    }

    private void endThread()
    {
        SoftRefReleaseThread thread = softRefReleaseThread.getAndSet(null);
        if (thread != null)
        {
            thread.requestEnd = true;
            thread.interrupt();
        }
    }

    public void putFile(final FileObject file)
    {
        if (log.isDebugEnabled())
        {
            log.debug("putFile: " + file.getName());
        }

        Map<FileName, Reference<FileObject>> files = getOrCreateFilesystemCache(file.getFileSystem());

        Reference<FileObject> ref = createReference(file, refqueue);
        FileSystemAndNameKey key = new FileSystemAndNameKey(file.getFileSystem(), file.getName());

        lock.lock();
        try
        {
            Reference<FileObject> old = files.put(file.getName(), ref);
            if (old != null)
            {
                refReverseMap.remove(old);
            }
            refReverseMap.put(ref, key);
        }
        finally
        {
            lock.unlock();
        }
    }


    public boolean putFileIfAbsent(final FileObject file)
    {
        if (log.isDebugEnabled())
        {
            log.debug("putFile: " + file.getName());
        }

        Map<FileName, Reference<FileObject>> files = getOrCreateFilesystemCache(file.getFileSystem());

        Reference<FileObject> ref = createReference(file, refqueue);
        FileSystemAndNameKey key = new FileSystemAndNameKey(file.getFileSystem(), file.getName());

        lock.lock();
        try
        {
            if (files.containsKey(file.getName()) && files.get(file.getName()).get() != null)
            {
                return false;
            }
            Reference<FileObject> old = files.put(file.getName(), ref);
            if (old != null)
            {
                refReverseMap.remove(old);
            }
            refReverseMap.put(ref, key);
            return true;
        }
        finally
        {
            lock.unlock();
        }
    }

    protected Reference<FileObject> createReference(FileObject file, ReferenceQueue<FileObject> refqueue)
    {
        return new SoftReference<FileObject>(file, refqueue);
    }

    public FileObject getFile(final FileSystem filesystem, final FileName name)
    {
        Map<FileName, Reference<FileObject>> files = getOrCreateFilesystemCache(filesystem);

        lock.lock();
        try
        {
            Reference<FileObject> ref = files.get(name);
            if (ref == null)
            {
                return null;
            }

            FileObject fo = ref.get();
            if (fo == null)
            {
                removeFile(filesystem, name);
            }
            return fo;
        }
        finally
        {
            lock.unlock();
        }
    }

    public void clear(FileSystem filesystem)
    {
        Map<FileName, Reference<FileObject>> files = getOrCreateFilesystemCache(filesystem);

        lock.lock();
        try
        {
            Iterator<FileSystemAndNameKey> iterKeys = refReverseMap.values().iterator();
            while (iterKeys.hasNext())
            {
                FileSystemAndNameKey key = iterKeys.next();
                if (key.getFileSystem() == filesystem)
                {
                    iterKeys.remove();
                    files.remove(key.getFileName());
                }
            }

            if (files.size() < 1)
            {
                filesystemClose(filesystem);
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Called while the lock is held
     * @param filesystem The file system to close.
     */
    private void filesystemClose(FileSystem filesystem)
    {
        if (log.isDebugEnabled())
        {
            log.debug("close fs: " + filesystem.getRootName());
        }

        filesystemCache.remove(filesystem);
        if (filesystemCache.size() < 1)
        {
            endThread();
        }
        /* This is not thread-safe as another thread might be opening the file system
        ((DefaultFileSystemManager) getContext().getFileSystemManager())
                ._closeFileSystem(filesystem);
         */
    }

    @Override
    public void close()
    {
        super.close();

        endThread();

        lock.lock();
        try
        {
            filesystemCache.clear();

            refReverseMap.clear();
        }
        finally
        {
            lock.unlock();
        }
    }

    public void removeFile(FileSystem filesystem, FileName name)
    {
        if (removeFile(new FileSystemAndNameKey(filesystem, name)))
        {
            filesystemClose(filesystem);
        }
    }

    public void touchFile(FileObject file)
    {
    }

    private boolean removeFile(final FileSystemAndNameKey key)
    {
        if (log.isDebugEnabled())
        {
            log.debug("removeFile: " + key.getFileName());
        }

        Map<?, ?> files = getOrCreateFilesystemCache(key.getFileSystem());

        lock.lock();
        try
        {
            Object ref = files.remove(key.getFileName());
            if (ref != null)
            {
                refReverseMap.remove(ref);
            }

            return files.size() < 1;
        }
        finally
        {
            lock.unlock();
        }
    }

    protected Map<FileName, Reference<FileObject>> getOrCreateFilesystemCache(final FileSystem filesystem)
    {
        if (filesystemCache.size() < 1)
        {
            startThread();
        }

        Map<FileName, Reference<FileObject>> files;

        do
        {
            files = filesystemCache.get(filesystem);
            if (files != null)
            {
                break;
            }
            files = new HashMap<FileName, Reference<FileObject>>();
        } while (filesystemCache.putIfAbsent(filesystem, files) == null);

        return files;
    }
}
