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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.VfsLog;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.util.Messages;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This implementation caches every file as long as it is strongly reachable by
 * the java vm. As soon as the vm needs memory - every softly reachable file
 * will be discarded.
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
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

    private final Map<FileSystem, Map<FileName, Reference<FileObject>>> filesystemCache =
          new HashMap<FileSystem, Map<FileName, Reference<FileObject>>>();
    private final Map<Reference<FileObject>, FileSystemAndNameKey> refReverseMap =
          new HashMap<Reference<FileObject>, FileSystemAndNameKey>(100);
    private final ReferenceQueue<FileObject> refqueue = new ReferenceQueue<FileObject>();

    private SoftRefReleaseThread softRefReleaseThread;

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

                    FileSystemAndNameKey key;
                    synchronized (refReverseMap)
                    {
                        key = refReverseMap.get(ref);
                    }

                    if (key != null)
                    {
                        if (removeFile(key))
                        {
                            /* This is not thread safe
                            filesystemClose(key.getFileSystem());
                            */
                        }
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
        if (softRefReleaseThread != null)
        {
            throw new IllegalStateException(
                    Messages.getString("vfs.impl/SoftRefReleaseThread-already-running.warn"));
        }

        softRefReleaseThread = new SoftRefReleaseThread();
        softRefReleaseThread.start();
    }

    private void endThread()
    {
        if (softRefReleaseThread != null)
        {
            softRefReleaseThread.requestEnd = true;
            softRefReleaseThread.interrupt();
            softRefReleaseThread = null;
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
        FileSystemAndNameKey key = new FileSystemAndNameKey(file
                .getFileSystem(), file.getName());

        synchronized (files)
        {
            Reference<FileObject> old = files.put(file.getName(), ref);
            synchronized (refReverseMap)
            {
                if (old != null)
                {
                    refReverseMap.remove(old);
                }
                refReverseMap.put(ref, key);
            }
        }
    }

    protected Reference<FileObject> createReference(FileObject file, ReferenceQueue<FileObject> refqueue)
    {
        return new SoftReference<FileObject>(file, refqueue);
    }

    public FileObject getFile(final FileSystem filesystem, final FileName name)
    {
        Map<FileName, Reference<FileObject>> files = getOrCreateFilesystemCache(filesystem);

        synchronized (files)
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
    }

    public void clear(FileSystem filesystem)
    {
        Map<FileName, Reference<FileObject>> files = getOrCreateFilesystemCache(filesystem);

        boolean closeFilesystem;

        synchronized (files)
        {
            synchronized (refReverseMap)
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

                closeFilesystem = files.size() < 1;
            }
        }

        if (closeFilesystem)
        {
            filesystemClose(filesystem);
        }
    }

    private void filesystemClose(FileSystem filesystem)
    {
        if (log.isDebugEnabled())
        {
            log.debug("close fs: " + filesystem.getRootName());
        }
        synchronized (filesystemCache)
        {
            filesystemCache.remove(filesystem);
            if (filesystemCache.size() < 1)
            {
                endThread();
            }
        }
        ((DefaultFileSystemManager) getContext().getFileSystemManager())
                ._closeFileSystem(filesystem);
    }

    @Override
    public void close()
    {
        super.close();

        endThread();

        // files.clear();
        synchronized (filesystemCache)
        {
            filesystemCache.clear();
        }

        synchronized (refReverseMap)
        {
            refReverseMap.clear();
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

        synchronized (files)
        {
            Object ref = files.remove(key.getFileName());
            if (ref != null)
            {
                synchronized (refReverseMap)
                {
                    refReverseMap.remove(ref);
                }
            }

            return files.size() < 1;
        }
    }

    protected Map<FileName, Reference<FileObject>> getOrCreateFilesystemCache(final FileSystem filesystem)
    {
        synchronized (filesystemCache)
        {
            if (filesystemCache.size() < 1)
            {
                startThread();
            }

            Map<FileName, Reference<FileObject>> files = filesystemCache.get(filesystem);
            if (files == null)
            {
                files = new HashMap<FileName, Reference<FileObject>>();
                filesystemCache.put(filesystem, files);
            }

            return files;
        }
    }
}
