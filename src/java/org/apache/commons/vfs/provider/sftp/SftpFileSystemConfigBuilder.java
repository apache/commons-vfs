/*
 * Copyright 2002-2005 The Apache Software Foundation.
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
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;

import java.io.File;

/**
 * The config builder for various sftp configuration options
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date$
 */
public class SftpFileSystemConfigBuilder extends FileSystemConfigBuilder
{
    private final static SftpFileSystemConfigBuilder builder = new SftpFileSystemConfigBuilder();

    public static SftpFileSystemConfigBuilder getInstance()
    {
        return builder;
    }

    private SftpFileSystemConfigBuilder()
    {
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

    /**
     * Set the known_hosts file. e.g. /home/user/.ssh/known_hosts2<br>
     * Need to use a java.io.File as JSch cant deal with vfs FileObjects ;-)
     *
     * @param opts
     * @param sshdir
     */
    public void setKnownHosts(FileSystemOptions opts, File sshdir) throws FileSystemException
    {
        setParam(opts, "knownHosts", sshdir);
    }

    /**
     * @param opts
     * @return
     * @see #setKnownHosts
     */
    public File getKnownHosts(FileSystemOptions opts)
    {
        return (File) getParam(opts, "knownHosts");
    }

    /**
     * Set the identity files (your private key files).<br>
     * Need to use a java.io.File as JSch cant deal with vfs FileObjects ;-)
     *
     * @param opts
     * @param identities
     */
    public void setIdentities(FileSystemOptions opts, File[] identities) throws FileSystemException
    {
        setParam(opts, "identities", identities);
    }

    /**
     * configure the compression to use.<br>
     * e.g. pass "zlib,none" to enable the compression.<br>
     * See the jsch documentation for details.
     *
     * @param opts
     * @param compression
     * @throws FileSystemException
     */
    public void setCompression(FileSystemOptions opts, String compression) throws FileSystemException
    {
        setParam(opts, "compression", compression);
    }

    /**
     * @param opts
     * @return
     * @see #setCompression
     */
    public String getCompression(FileSystemOptions opts)
    {
        return (String) getParam(opts, "compression");
    }

    /**
     * @param opts
     * @return
     * @see #setIdentities
     */
    public File[] getIdentities(FileSystemOptions opts)
    {
        return (File[]) getParam(opts, "identities");
    }

    /**
     * configure the host key checking to use.<br>
     * valid arguments are only yes, no and ask.<br>
     * See the jsch documentation for details.
     *
     * @param opts
     * @param hostKeyChecking
     * @throws FileSystemException
     */
    public void setStrictHostKeyChecking(FileSystemOptions opts, String hostKeyChecking) throws FileSystemException
    {
        if (hostKeyChecking == null || (!hostKeyChecking.equals("ask") && !hostKeyChecking.equals("no") && !hostKeyChecking.equals("yes")))
        {
            throw new FileSystemException("vfs.provider.sftp/StrictHostKeyChecking-arg.error", hostKeyChecking);
        }

        setParam(opts, "StrictHostKeyChecking", hostKeyChecking);
    }

    /**
     * @param opts
     * @return the option value
     * @see #setStrictHostKeyChecking(FileSystemOptions, String)
     */
    public String getStrictHostKeyChecking(FileSystemOptions opts)
    {
        return (String) getParam(opts, "StrictHostKeyChecking");
    }

    protected Class getConfigClass()
    {
        return SftpFileSystem.class;
    }
}
