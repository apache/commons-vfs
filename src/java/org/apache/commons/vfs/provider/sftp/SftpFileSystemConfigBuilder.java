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
package org.apache.commons.vfs.provider.sftp;

import com.jcraft.jsch.UserInfo;
import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemOptions;

/**
 * The config builder for various sftp configuration options
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision: 1.3 $ $Date: 2004/05/10 20:09:51 $
 */
public class SftpFileSystemConfigBuilder extends FileSystemConfigBuilder
{
    private final static SftpFileSystemConfigBuilder builder = new SftpFileSystemConfigBuilder();

    public static SftpFileSystemConfigBuilder getInstance()
    {
        return builder;
    }

    /**
     * Set the userinfo class to use if e.g. a password or a not known host
     * will be contacted
     *
     * @param opts
     * @param info
     */
    public void setUserInfo(FileSystemOptions opts, UserInfo info)
    {
        setParam(opts, UserInfo.class.getName(), info);
    }

    /**
     * @param opts
     * @return
     * @see #setUserInfo
     */
    public UserInfo getUserInfo(FileSystemOptions opts)
    {
        return (UserInfo) getParam(opts, UserInfo.class.getName());
    }

    protected Class getConfigClass()
    {
        return SftpFileSystem.class;
    }
}
