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
package org.apache.commons.vfs.provider.webdav;

import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.http.HttpFileSystemOptions;

/**
 * Webdav File System Options
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 */
public class WebdavFileSystemOptions extends HttpFileSystemOptions
{
    public WebdavFileSystemOptions()
    {
        this("webdav.");
    }

    protected WebdavFileSystemOptions(String scheme)
    {
        super(scheme);
    }

    public static WebdavFileSystemOptions getInstance(FileSystemOptions opts)
    {
        return FileSystemOptions.makeSpecific(WebdavFileSystemOptions.class, opts);
    }

    /**
     * The user name to be associated with changes to the file.
     * @param creatorName The creator name to be associated with the file.
     */
    public void setCreatorName(String creatorName)
    {
        setParam("creatorName", creatorName);
    }

    /**
     * Return the user name to be associated with changes to the file.
     * @return The creatorName.
     */
    public String getCreatorName()
    {
        return getString("creatorName");
    }

    /**
     * Whether to use versioning.
     * @param versioning true if versioning should be enabled.
     */
    public void setVersioning(boolean versioning)
    {
        setParam("versioning", Boolean.valueOf(versioning));
    }

    /**
     * The cookies to add to the request.
     * @return true if versioning is enabled.
     */
    public boolean isVersioning()
    {
        return getBoolean("versioning", false);
    }
}