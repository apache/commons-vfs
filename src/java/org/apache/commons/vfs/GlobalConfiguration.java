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
import org.apache.commons.vfs.impl.FileContentInfoFilenameFactory;
import org.apache.commons.vfs.provider.AbstractVfsComponent;
import org.apache.commons.vfs.provider.VfsComponent;

/**
 * Global parameters to configure the VFS system.
 * 
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision: 1.4 $ $Date: 2004/05/21 20:54:34 $
 */
public final class GlobalConfiguration extends AbstractVfsComponent
{
    private boolean inUse = false;

    private FilesCache filesCache = new DefaultFilesCache();
    private FileContentInfoFactory fileContentInfoFactory = new FileContentInfoFilenameFactory();

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
    public void init() throws FileSystemException
    {
        super.init();

        setupComponent(this.filesCache);

        inUse = true;
    }

    private void setupComponent(final Object component) throws FileSystemException
    {
        if (component instanceof VfsComponent)
        {
            final VfsComponent vfsComponent = (VfsComponent) component;
            vfsComponent.setLogger(getLogger());
            vfsComponent.setContext(getContext());
            vfsComponent.init();
        }
    }

    /**
     * close this component
     */
    public void close()
    {
        super.close();

        filesCache.close();
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

    /**
     * returns the fileContentInfoFactory used to determine the infos of a file content.
     */
    public FileContentInfoFactory getFileContentInfoFactory()
    {
        return fileContentInfoFactory;
    }

    /**
     * set the fileContentInfoFactory used to determine the infos of a file content.
     */
    public void setFileContentInfoFactory(FileContentInfoFactory fileContentInfoFactory)
    {
        this.fileContentInfoFactory = fileContentInfoFactory;
    }
}
