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

import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.DefaultFileSystemOptions;
import org.apache.commons.vfs.provider.ram.RamFileSystemOptions;

/**
 * RAM File System Options
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 */
public class ResourceFileSystemOptions extends DefaultFileSystemOptions
{
    public ResourceFileSystemOptions()
    {
        this("resource.");
    }

    protected ResourceFileSystemOptions(String scheme)
    {
        super(scheme);
    }

    public static ResourceFileSystemOptions getInstance(FileSystemOptions opts)
    {
        return FileSystemOptions.makeSpecific(ResourceFileSystemOptions.class, opts);
    }

  /**
   * Set the class loader.
   * @param classLoader The class loader.
   */
    public void setClassLoader(ClassLoader classLoader)
    {
        setParam(ClassLoader.class.getName(), classLoader);
    }

  /**
   * Retrieve the class loader.
   * @return The class loader.
   */
    public ClassLoader getClassLoader()
    {
        return (ClassLoader) getParam(ClassLoader.class.getName());
    }
}