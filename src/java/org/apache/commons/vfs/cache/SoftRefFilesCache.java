/*
 * Copyright 2002-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.VfsLog;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.util.Messages;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * This implementation caches every file as long as it is strongly reachable by the java vm.
 * As soon as the vm needs memory - every softly reachable file will be discarded.
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date$
 * @see SoftReference
 */
public class SoftRefFilesCache extends AbstractFilesCache
{
    /**
     * The logger to use.
     */
    private Log log = LogFactory.getLog(SoftRefFilesCache.class);

    private final Map filesystemCache = new HashMap();
    private final Map refReverseMap = new HashMap(100);
    private final ReferenceQueue refqueue = new ReferenceQueue();

    private SoftRefReleaseThread softRefReleaseThread = null;

    /**
     * This thread will listen on the ReferenceQueue and remove the entry in the
     * filescache as soon as the vm removes the reference
     */
    private class SoftRefReleaseThread extends Thread
    {
        private boolean requestEnd = false;

        private SoftRefReleaseThread()
        {
            setName(SoftRefReleaseThread.class.getName());
            setDaemon(true);
        }

        public void run()
        {
            loop: while (!requestEnd && !Thread.currentThread().isInterrupted())
            {
                try
                {
                    Reference ref = refqueue.remove(1000);
                    if (ref == null)
                    {
                        continue;
                    }

                    if (ref != null)
                    {
                        synchronized (SoftRefFilesCache.this)
                        {
                            FileSystemAndNameKey key = (FileSystemAndNameKey) refReverseMap.get(ref);

                            if (key != null)
                            {
                                removeFile(key);
                            }
                        }
                    }
                }
                catch (InterruptedException e)
                {
                    if (!requestEnd)
                    {
                        VfsLog.warn(getLogger(), log, Messages.getString("vfs.impl/SoftRefReleaseThread-interrupt.info"));
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
            throw new IllegalStateException(Messages.getString("vfs.impl/SoftRefReleaseThread-already-running.warn"));
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
        synchronized (this)
        {
            Map files = getOrCreateFilesystemCache(file.getFileSystem());

            SoftReference ref = new SoftReference(file, refqueue);
            FileSystemAndNameKey key = new FileSystemAndNameKey(file.getFileSystem(), file.getName());
            files.put(file.getName(), ref);
            refReverseMap.put(ref, key);
        }
    }

    public FileObject getFile(final FileSystem filesystem, final FileName name)
    {
        synchronized (this)
        {
            Map files = getOrCreateFilesystemCache(filesystem);

            SoftReference ref = (SoftReference) files.get(name);
            if (ref == null)
            {
                return null;
            }

            FileObject fo = (FileObject) ref.get();
            if (fo == null)
            {
                removeFile(filesystem, name);
            }
            return fo;
        }
    }

    public void clear(FileSystem filesystem)
    {
        synchronized (this)
        {
            Map files = getOrCreateFilesystemCache(filesystem);

            Iterator iterKeys = refReverseMap.values().iterator();
            while (iterKeys.hasNext())
            {
                FileSystemAndNameKey key = (FileSystemAndNameKey) iterKeys.next();
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
    }

    private void filesystemClose(FileSystem filesystem)
    {
        filesystemCache.remove(filesystem);
        if (filesystemCache.size() < 1)
        {
            endThread();
        }
        ((DefaultFileSystemManager) getContext().getFileSystemManager()).closeFileSystem(filesystem);
    }

    public void close()
    {
        super.close();

        synchronized (this)
        {
            endThread();

            // files.clear();
            filesystemCache.clear();
            refReverseMap.clear();
        }
    }

    public void removeFile(FileSystem filesystem, FileName name)
    {
        removeFile(new FileSystemAndNameKey(filesystem, name));
    }

    public void touchFile(FileObject file)
    {
    }

    private void removeFile(final FileSystemAndNameKey key)
    {
        synchronized (this)
        {
            Map files = getOrCreateFilesystemCache(key.getFileSystem());

            Object ref = files.remove(key.getFileName());
            if (ref != null)
            {
                refReverseMap.remove(ref);
            }

            if (files.size() < 1)
            {
                filesystemClose(key.getFileSystem());
            }
        }
    }

    protected Map getOrCreateFilesystemCache(final FileSystem filesystem)
    {
        if (filesystemCache.size() < 1)
        {
            startThread();
        }

        Map files = (Map) filesystemCache.get(filesystem);
        if (files == null)
        {
            files = new TreeMap();
            filesystemCache.put(filesystem, files);
        }

        return files;
    }
}
