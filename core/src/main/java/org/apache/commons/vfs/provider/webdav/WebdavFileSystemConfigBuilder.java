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

import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.UserAuthenticator;
import org.apache.commons.vfs.provider.http.HttpFileSystemConfigBuilder;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

/**
 * Configuration options for WebDav
 * 
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 * @version $Revision$ $Date$
 */
public class WebdavFileSystemConfigBuilder extends HttpFileSystemConfigBuilder
{
    private final static WebdavFileSystemConfigBuilder builder = new WebdavFileSystemConfigBuilder();

    public static HttpFileSystemConfigBuilder getInstance()
    {
        return builder;
    }

    private WebdavFileSystemConfigBuilder()
    {
        super("webdav.");
    }

    /**
     * The user name to be associated with changes to the file.
     * @param opts
     * @param creatorName
     */
    public void setCreatorName(FileSystemOptions opts, String creatorName)
    {
        setParam(opts, "creatorName", creatorName);
    }

    /**
     * Return the user name to be associated with changes to the file.
     * @param opts
     * @return
     */
    public String getCreatorName(FileSystemOptions opts)
    {
        return getString(opts, "creatorName");
    }

    /**
     * Whether to use versioning
     */
    public void setVersioning(FileSystemOptions opts, boolean versioning)
    {
        setParam(opts, "versioning", new Boolean(versioning));
    }

    /**
     * The cookies to add to the reqest
     */
    public boolean isVersioning(FileSystemOptions opts)
    {
        return getBoolean(opts, "versioning", false);
    }
    
    protected Class getConfigClass()
    {
        return WebdavFileSystem.class;
    }
}
