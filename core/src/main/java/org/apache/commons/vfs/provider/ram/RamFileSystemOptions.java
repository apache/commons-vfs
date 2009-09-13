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

import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.DefaultFileSystemOptions;

/**
 * RAM File System Options
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 */
public class RamFileSystemOptions extends DefaultFileSystemOptions
{
    /** max size key */
    private static final String MAX_SIZE_KEY = "maxsize";

    public RamFileSystemOptions()
    {
        this("ram.");
    }

    protected RamFileSystemOptions(String scheme)
    {
        super(scheme);
    }

    public static RamFileSystemOptions getInstance(FileSystemOptions opts)
    {
        return FileSystemOptions.makeSpecific(RamFileSystemOptions.class, opts);
    }

   /**
     * @return The maximum size of the file system.
     * @see #setMaxSize
     */
    public int getMaxSize()
    {
        return getInteger(MAX_SIZE_KEY, Integer.MAX_VALUE);
    }

    /**
     * Sets the maximum size of the file system
     *
     * @param sizeInBytes The maximum size.
     */
    public void setMaxSize(int sizeInBytes)
    {
        setParam(MAX_SIZE_KEY, new Integer(sizeInBytes));
    }
}