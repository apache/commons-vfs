/*
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
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
import org.apache.commons.vfs.FilesCache;
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
 * @author <a href="mailto:imario@apache.org">Mario Ivanovits</a>
 * @version $Revision: 1.3 $ $Date: 2004/05/10 20:09:48 $
 * @see SoftReference
 */
public class SoftRefFilesCache implements FilesCache
{
    /**
     * The logger to use.
     */
    private Log log = LogFactory.getLog(SoftRefFilesCache.class);

    private final Map files = new TreeMap();
    private final Map refReverseMap = new HashMap(100);
    private final ReferenceQueue refqueue = new ReferenceQueue();

    private Thread softRefReleaseThread = null;

    /**
     * This thread will listen on the ReferenceQueue and remove the entry in the
     * filescache as soon as the vm removes the reference
     */
    private class SoftRefReleaseThread implements Runnable
    {
        private SoftRefReleaseThread()
        {
        }

        public void run()
        {
            loop: while (!Thread.currentThread().isInterrupted())
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

                            removeFile(key);
                        }
                    }
                }
                catch (InterruptedException e)
                {
                    log.info(Messages.getString("vfs.impl/SoftRefReleaseThread-interrupt.info"));
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

        softRefReleaseThread = new Thread(new SoftRefReleaseThread());
        softRefReleaseThread.setDaemon(true);
        softRefReleaseThread.start();
    }

    private void endThread()
    {
        if (softRefReleaseThread != null)
        {
            softRefReleaseThread.interrupt();
            softRefReleaseThread = null;
        }
    }

    public void putFile(final FileObject file)
    {
        synchronized (this)
        {
            if (files.size() < 1)
            {
                startThread();
            }

            SoftReference ref = new SoftReference(file, refqueue);
            FileSystemAndNameKey key = new FileSystemAndNameKey(file.getFileSystem(), file.getName());
            files.put(key, ref);
            refReverseMap.put(ref, key);
        }
    }

    public FileObject getFile(final FileSystem filesystem, final FileName name)
    {
        synchronized (this)
        {
            FileSystemAndNameKey key = new FileSystemAndNameKey(filesystem, name);

            SoftReference ref = (SoftReference) files.get(key);
            if (ref == null)
            {
                return null;
            }

            FileObject fo = (FileObject) ref.get();
            if (fo == null)
            {
                removeFile(key);
            }
            return fo;
        }
    }

    public void clear(FileSystem filesystem)
    {
        synchronized (this)
        {
            Iterator iterKeys = files.keySet().iterator();
            while (iterKeys.hasNext())
            {
                FileSystemAndNameKey key = (FileSystemAndNameKey) iterKeys.next();
                if (key.getFileSystem() == filesystem)
                {
                    Object ref = files.get(key);
                    iterKeys.remove();
                    refReverseMap.remove(ref);
                }
            }

            if (files.size() < 1)
            {
                endThread();
            }
        }
    }

    public void clear()
    {
        synchronized (this)
        {
            endThread();

            files.clear();
            refReverseMap.clear();
        }
    }

    public void removeFile(FileSystem filesystem, FileName name)
    {
        removeFile(new FileSystemAndNameKey(filesystem, name));
    }

    public void accessFile(FileObject file)
    {
    }

    private void removeFile(final FileSystemAndNameKey key)
    {
        synchronized (this)
        {
            Object ref = files.remove(key);
            if (ref != null)
            {
                refReverseMap.remove(ref);
            }

            if (files.size() < 1)
            {
                endThread();
            }
        }
    }

}
