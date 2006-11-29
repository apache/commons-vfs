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
package org.apache.commons.vfs.provider.res;

import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.url.UrlFileSystem;

/**
 * The config builder for various ftp configuration options
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date$
 */
public class ResourceFileSystemConfigBuilder extends FileSystemConfigBuilder
{
    private final static ResourceFileSystemConfigBuilder builder = new ResourceFileSystemConfigBuilder();

    public static ResourceFileSystemConfigBuilder getInstance()
    {
        return builder;
    }

    private ResourceFileSystemConfigBuilder()
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
