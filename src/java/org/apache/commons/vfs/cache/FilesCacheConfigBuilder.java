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

import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.FilesCache;

/**
 * The config builder for various cache configuration options
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision: 1.2 $ $Date: 2004/05/10 20:09:48 $
 */
public class FilesCacheConfigBuilder extends FileSystemConfigBuilder
{
    private final static FilesCacheConfigBuilder builder = new FilesCacheConfigBuilder();

    public static FilesCacheConfigBuilder getInstance()
    {
        return builder;
    }

    public void setFilesCache(FileSystemOptions opts, FilesCache cache)
    {
        setParam(opts, FilesCache.class.getName(), cache);
    }

    public FilesCache getFilesCache(FileSystemOptions opts)
    {
        return (FilesCache) getParam(opts, FilesCache.class.getName());
    }

    protected Class getConfigClass()
    {
        return FilesCache.class;
    }
}
