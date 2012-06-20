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
package org.apache.commons.vfs2.provider.webdav;

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.http.HttpFileSystemConfigBuilder;

/**
 * Configuration options for WebDav.
 *
 * @since 2.0
 */
public final class WebdavFileSystemConfigBuilder extends HttpFileSystemConfigBuilder
{
    private static final WebdavFileSystemConfigBuilder BUILDER = new WebdavFileSystemConfigBuilder();

    private WebdavFileSystemConfigBuilder()
    {
        super("webdav.");
    }

    /**
     * Gets the singleton builder.
     * 
     * @return the singleton builder.
     */
    public static HttpFileSystemConfigBuilder getInstance()
    {
        return BUILDER;
    }

    /**
     * The user name to be associated with changes to the file.
     * @param opts The FileSystem options
     * @param creatorName The creator name to be associated with the file.
     */
    public void setCreatorName(FileSystemOptions opts, String creatorName)
    {
        setParam(opts, "creatorName", creatorName);
    }

    /**
     * Return the user name to be associated with changes to the file.
     * @param opts The FileSystem options
     * @return The creatorName.
     */
    public String getCreatorName(FileSystemOptions opts)
    {
        return getString(opts, "creatorName");
    }

    /**
     * Whether to use versioning.
     * @param opts The FileSystem options.
     * @param versioning true if versioning should be enabled.
     */
    public void setVersioning(FileSystemOptions opts, boolean versioning)
    {
        setParam(opts, "versioning", Boolean.valueOf(versioning));
    }

    /**
     * The cookies to add to the request.
     * @param opts The FileSystem options.
     * @return true if versioning is enabled.
     */
    public boolean isVersioning(FileSystemOptions opts)
    {
        return getBoolean(opts, "versioning", false);
    }

    /**
     * @return The Webdav FileSystem Class object.
     */
    @Override
    protected Class<? extends FileSystem> getConfigClass()
    {
        return WebdavFileSystem.class;
    }
}
