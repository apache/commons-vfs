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
package org.apache.commons.vfs;

import org.apache.commons.vfs.cache.DefaultFilesCache;

/**
 * Global parameters to configure the VFS system.
 * 
 * @author <a href="mailto:imario@apache.org">Mario Ivanovits</a>
 * @version $Revision: 1.1 $ $Date: 2004/05/17 17:56:57 $
 */
public final class GlobalConfiguration
{
    private boolean inUse = false;

    private FilesCache filesCache = new DefaultFilesCache();

    public GlobalConfiguration()
    {
    }

    /**
     * checks to see if this configuration is already attached to an filesystemmanager.
     *
     * @throws FileSystemException if is is already attached
     */
    private void assertNotInUse() throws FileSystemException
    {
        if (inUse)
        {
            throw new FileSystemException("vfs.impl/configuration-already-in-use.error");
        }
    }

    /**
     * init
     * marks this configuration as "in use"
     */
    public void init()
    {
        inUse = true;
    }

    /**
     * Sets the filesCache implementation used to cache files.
     *
     * @param filesCache
     * @throws FileSystemException if this configuration is already attached to an filesystemmanager
     */
    public void setFilesCache(FilesCache filesCache) throws FileSystemException
    {
        assertNotInUse();

        this.filesCache = filesCache;
    }

    /**
     * Returns the filesCache implementation.<br>
     * Default: {@link DefaultFilesCache}
     */
    public FilesCache getFilesCache()
    {
        return filesCache;
    }
}
