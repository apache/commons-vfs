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
package org.apache.commons.vfs.cache;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.AbstractLinkedMap;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.VfsLog;
import org.apache.commons.vfs.util.Messages;

/**
 * This implementation caches every file using {@link LRUMap}.<br>
 * The default constructor uses a LRU size of 100 per filesystem.
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date$
 */
public class LRUFilesCache extends AbstractFilesCache
{
    /** The default LRU size */
    private static final int DEFAULT_LRU_SIZE = 100;
    /**
     * The logger to use.
     */
    private Log log = LogFactory.getLog(LRUFilesCache.class);

    /** The FileSystem cache */
    private final Map filesystemCache = new HashMap(10);

    /** The size of the cache */
    private final int lruSize;

    /**
     * The file cache
     */
    private class MyLRUMap extends LRUMap
    {
        /** The FileSystem */
        private final FileSystem filesystem;

        public MyLRUMap(final FileSystem filesystem, int size)
        {
            super(size, true);
            this.filesystem = filesystem;
        }

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

                    Map files = (Map) filesystemCache.get(filesystem);
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
        synchronized (this)
        {
            Map files = getOrCreateFilesystemCache(file.getFileSystem());

            // System.err.println(">>> " + files.size() + " put:" + file.toString());

            files.put(file.getName(), file);
        }
    }

    public FileObject getFile(final FileSystem filesystem, final FileName name)
    {
        synchronized (this)
        {
            Map files = getOrCreateFilesystemCache(filesystem);

            // FileObject fo = (FileObject) files.get(name);
            // System.err.println(">>> " + files.size() + " get:" + name.toString() + " " + fo);

            return (FileObject) files.get(name);
        }
    }

    public void clear(final FileSystem filesystem)
    {
        synchronized (this)
        {
            // System.err.println(">>> clear fs " + filesystem);

            Map files = getOrCreateFilesystemCache(filesystem);
            files.clear();

            filesystemCache.remove(filesystem);
        }
    }

    protected Map getOrCreateFilesystemCache(final FileSystem filesystem)
    {
        Map files = (Map) filesystemCache.get(filesystem);
        if (files == null)
        {
            // System.err.println(">>> create fs " + filesystem);

            files = new MyLRUMap(filesystem, lruSize);
            filesystemCache.put(filesystem, files);
        }

        return files;
    }

    public void close()
    {
        super.close();

        synchronized (this)
        {
            // System.err.println(">>> clear all");

            filesystemCache.clear();
        }
    }

    public void removeFile(final FileSystem filesystem, final FileName name)
    {
        synchronized (this)
        {
            Map files = getOrCreateFilesystemCache(filesystem);

            // System.err.println(">>> " + files.size() + " remove:" + name.toString());

            files.remove(name);

            if (files.size() < 1)
            {
                filesystemCache.remove(filesystem);
            }
        }
    }

    public void touchFile(final FileObject file)
    {
        // this moves the file back on top
        getFile(file.getFileSystem(), file.getName());
    }
}
