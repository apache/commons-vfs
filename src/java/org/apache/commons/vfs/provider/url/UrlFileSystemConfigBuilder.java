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
package org.apache.commons.vfs.provider.url;

import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemOptions;

/**
 * The config builder for various ftp configuration options
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision: 1.1 $ $Date: 2004/05/20 17:40:56 $
 */
public class UrlFileSystemConfigBuilder extends FileSystemConfigBuilder
{
    private final static UrlFileSystemConfigBuilder builder = new UrlFileSystemConfigBuilder();

    public static UrlFileSystemConfigBuilder getInstance()
    {
        return builder;
    }

    private UrlFileSystemConfigBuilder()
    {
    }

    public void setClassLoader(FileSystemOptions opts, ClassLoader classLoader)
    {
        setParam(opts, ClassLoader.class.getName(), classLoader);
    }

    public ClassLoader getClassLoader(FileSystemOptions opts)
    {
        return (ClassLoader) getParam(opts, ClassLoader.class.getName());
    }

    protected Class getConfigClass()
    {
        return UrlFileSystem.class;
    }
}
