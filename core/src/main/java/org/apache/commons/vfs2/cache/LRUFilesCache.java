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

import org.apache.commons.collections.map.AbstractLinkedMap;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VfsLog;
import org.apache.commons.vfs2.util.Messages;

/**
 * This implementation caches every file using {@link LRUMap}.<br>
 * The default constructor uses a LRU size of 100 per filesystem.
 *
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 * @version $Revision$ $Date$
 */
public class LRUFilesCache extends AbstractFilesCache
{
    /** The default LRU size */
    private static final int DEFAULT_LRU_SIZE = 100;
    /**
     * The logger to use.
     */
    private final Log log = LogFactory.getLog(LRUFilesCache.class);

    /** The FileSystem cache */
    private final ConcurrentMap<FileSystem, Map<FileName, FileObject>> filesystemCache =
          new ConcurrentHashMap<FileSystem, Map<FileName, FileObject>>(10);

    /** The size of the cache */
    private final int lruSize;

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    /**
     * The file cache
     */
    private class MyLRUMap extends LRUMap /* implements Map<FileName, FileObject> */
    {
        /** The FileSystem */
        private final FileSystem filesystem;

        public MyLRUMap(final FileSystem filesystem, int size)
        {
            super(size, true);
            this.filesystem = filesystem;
        }

        @Override
        protected boolean removeLRU(final AbstractLinkedMap.LinkEntry linkEntry)
        {
            synchronized (LRUFilesCache.this)
            {
                FileObject file = (FileObject) linkEntry.getValue();

                // System.err.println(">>> " + size() + " check removeLRU:" + linkEntry.getKey().toString());

                if (file.isAttached() || file.isContentOpen())
                {
                    // do not allow open or attached files to be removed
                    // System.err.println(">>> " + size() + " VETO removeLRU:" +
                    //    linkEntry.getKey().toString() + " (" + file.isAttached() + "/" +
                    //    file.isContentOpen() + ")");
                    return false;
                }

                // System.err.println(">>> " + size() + " removeLRU:" + linkEntry.getKey().toString());
                if (super.removeLRU(linkEntry))
                {
                    try
                    {
                        // force detach
                        file.close();
                    }
                    catch (FileSystemException e)
                    {
                        VfsLog.warn(getLogger(), log, Messages.getString("vfs.impl/LRUFilesCache-remove-ex.warn"), e);
                    }

                    Map<?, ?> files = filesystemCache.get(filesystem);
                    if (files.size() < 1)
                    {
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
    public LRUFilesCache()
    {
        this(DEFAULT_LRU_SIZE);
    }

    /**
     * Set the desired LRU size.
     *
     * @param lruSize the LRU size
     */
    public LRUFilesCache(int lruSize)
    {
        this.lruSize = lruSize;
    }

    public void putFile(final FileObject file)
    {
        Map<FileName, FileObject> files = getOrCreateFilesystemCache(file.getFileSystem());

        writeLock.lock();
        try
        {
            // System.err.println(">>> " + files.size() + " put:" + file.toString());

            files.put(file.getName(), file);
        }
        finally
        {
            writeLock.unlock();
        }
    }


    public boolean putFileIfAbsent(final FileObject file)
    {
        Map<FileName, FileObject> files = getOrCreateFilesystemCache(file.getFileSystem());

        writeLock.lock();
        try
        {
            FileName name = file.getName();

            // System.err.println(">>> " + files.size() + " put:" + file.toString());
            if (files.containsKey(name))
            {
                return false;
            }

            files.put(name, file);
            return true;
        }
        finally
        {
            writeLock.unlock();
        }
    }

    public FileObject getFile(final FileSystem filesystem, final FileName name)
    {
        Map<FileName, FileObject> files = getOrCreateFilesystemCache(filesystem);

        readLock.lock();
        try
        {
            // FileObject fo = (FileObject) files.get(name);
            // System.err.println(">>> " + files.size() + " get:" + name.toString() + " " + fo);

            return files.get(name);
        }
        finally
        {
            readLock.unlock();
        }
    }

    public void clear(final FileSystem filesystem)
    {
        Map<FileName, FileObject> files = getOrCreateFilesystemCache(filesystem);

        writeLock.lock();
        try
        {
            // System.err.println(">>> clear fs " + filesystem);

            files.clear();

            filesystemCache.remove(filesystem);
        }
        finally
        {
            writeLock.unlock();
        }
    }

    @SuppressWarnings("unchecked") // (1)
    protected Map<FileName, FileObject> getOrCreateFilesystemCache(final FileSystem filesystem)
    {
        Map<FileName, FileObject> files = filesystemCache.get(filesystem);
        if (files == null)
        {
            // System.err.println(">>> create fs " + filesystem);

            files = new MyLRUMap(filesystem, lruSize);
            filesystemCache.putIfAbsent(filesystem, files);
        }

        return files;
    }

    @Override
    public void close()
    {
        super.close();

        // System.err.println(">>> clear all");

        filesystemCache.clear();
    }

    public void removeFile(final FileSystem filesystem, final FileName name)
    {
        Map<?, ?> files = getOrCreateFilesystemCache(filesystem);

        writeLock.lock();
        try
        {
            // System.err.println(">>> " + files.size() + " remove:" + name.toString());

            files.remove(name);

            if (files.size() < 1)
            {
                filesystemCache.remove(filesystem);
            }
        }
        finally
        {
            writeLock.unlock();
        }
    }

    public void touchFile(final FileObject file)
    {
        // this moves the file back on top
        getFile(file.getFileSystem(), file.getName());
    }
}
