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
package org.apache.commons.vfs.provider.ram;

import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemOptions;

/**
 * Config Builder for the RAM filesystem.
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 */
public final class RamFileSystemConfigBuilder extends FileSystemConfigBuilder
{

    /** max size key. */
    private static final String MAX_SIZE_KEY = "maxsize";

    /** config builder singleton. */
    private static RamFileSystemConfigBuilder singleton = new RamFileSystemConfigBuilder();

    /**
     * Constructor
     */
    private RamFileSystemConfigBuilder()
    {
        super("ram.");
    }

    /**
     * @return the config builder singleton
     */
    public static RamFileSystemConfigBuilder getInstance()
    {
        return singleton;
    }

    /**
     * {@inheritDoc}
     */
    protected Class getConfigClass()
    {
        return RamFileSystem.class;
    }

    /**
     * @param opts The FileSystem options.
     * @return The maximum size of the file.
     * @see #setMaxSize
     */
    public int getMaxSize(FileSystemOptions opts)
    {
        return getInteger(opts, MAX_SIZE_KEY, Integer.MAX_VALUE);
    }

    /**
     * Sets the maximum size of the file system.
     *
     * @param opts The FileSystem options.
     * @param sizeInBytes The maximum file size.
     */
    public void setMaxSize(FileSystemOptions opts, int sizeInBytes)
    {
        setParam(opts, MAX_SIZE_KEY, new Integer(sizeInBytes));
    }

}
