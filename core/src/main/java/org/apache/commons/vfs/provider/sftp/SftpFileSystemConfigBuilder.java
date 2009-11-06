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
package org.apache.commons.vfs.provider.sftp;

import com.jcraft.jsch.UserInfo;
import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;

import java.io.File;
import java.io.Serializable;

/**
 * The config builder for various sftp configuration options
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date$
 * @deprecated Use SftpFileSystemOptions.
 */
public class SftpFileSystemConfigBuilder extends FileSystemConfigBuilder
{
    private final static SftpFileSystemConfigBuilder builder = new SftpFileSystemConfigBuilder();

    public final static ProxyType PROXY_HTTP = new ProxyType("http");
    public final static ProxyType PROXY_SOCKS5 = new ProxyType("socks");

    public static SftpFileSystemConfigBuilder getInstance()
    {
        return builder;
    }

    private SftpFileSystemConfigBuilder()
    {
        super("sftp.");
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
        SftpFileSystemOptions.getInstance(opts).setUserInfo(info);
    }

    /**
     * @param opts
     * @see #setUserInfo
     */
    public UserInfo getUserInfo(FileSystemOptions opts)
    {
        return SftpFileSystemOptions.getInstance(opts).getUserInfo();
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
        SftpFileSystemOptions.getInstance(opts).setKnownHosts(sshdir);
    }

    /**
     * @param opts
     * @see #setKnownHosts
     */
    public File getKnownHosts(FileSystemOptions opts)
    {
        return SftpFileSystemOptions.getInstance(opts).getKnownHosts();
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
        SftpFileSystemOptions.getInstance(opts).setIdentities(identities);
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
        SftpFileSystemOptions.getInstance(opts).setCompression(compression);
    }

    /**
     * @param opts
     * @see #setCompression
     */
    public String getCompression(FileSystemOptions opts)
    {
        return SftpFileSystemOptions.getInstance(opts).getCompression();
    }

    /**
     * @param opts
     * @see #setIdentities
     */
    public File[] getIdentities(FileSystemOptions opts)
    {
        return SftpFileSystemOptions.getInstance(opts).getIdentities();
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
        SftpFileSystemOptions.getInstance(opts).setStrictHostKeyChecking(hostKeyChecking);
    }

    /**
     * @param opts
     * @return the option value
     * @see #setStrictHostKeyChecking(FileSystemOptions, String)
     */
    public String getStrictHostKeyChecking(FileSystemOptions opts)
    {
        return SftpFileSystemOptions.getInstance(opts).getStrictHostKeyChecking();
    }

    /**
     * use user directory as root (do not change to fs root)
     *
     * @param opts
     * @param userDirIsRoot
     */
    public void setUserDirIsRoot(FileSystemOptions opts, boolean userDirIsRoot)
    {
        SftpFileSystemOptions.getInstance(opts).setUserDirIsRoot(userDirIsRoot);
    }

    /**
     * @param opts
     * @see #setUserDirIsRoot
     */
    public Boolean getUserDirIsRoot(FileSystemOptions opts)
    {
        return SftpFileSystemOptions.getInstance(opts).getUserDirIsRoot();
    }

    /**
     * set the timeout value on jsch session
     *
     * @param opts
     * @param timeout
     */
    public void setTimeout(FileSystemOptions opts, Integer timeout)
    {
        SftpFileSystemOptions.getInstance(opts).setTimeout(timeout);
    }

    /**
     * @param opts
     * @see #setTimeout
     */
    public Integer getTimeout(FileSystemOptions opts)
    {
        return SftpFileSystemOptions.getInstance(opts).getTimeout();
    }

    protected Class getConfigClass()
    {
        return SftpFileSystem.class;
    }

    /**
     * Set the proxy to use for sftp connection.<br>
     * You have to set the ProxyPort too if you would like to have the proxy relly used.
     *
     * @param proxyHost the host
     * @see #setProxyPort
     */
    public void setProxyHost(FileSystemOptions opts, String proxyHost)
    {
        SftpFileSystemOptions.getInstance(opts).setProxyHost(proxyHost);
    }

    /**
     * Set the proxy-port to use for sftp connection
     * You have to set the ProxyHost too if you would like to have the proxy relly used.
     *
     * @param proxyPort the port
     * @see #setProxyHost
     */
    public void setProxyPort(FileSystemOptions opts, int proxyPort)
    {
        SftpFileSystemOptions.getInstance(opts).setProxyPort(proxyPort);
    }

    /**
     * Get the proxy to use for sftp connection
     * You have to set the ProxyPort too if you would like to have the proxy relly used.
     *
     * @return proxyHost
     * @see #setProxyPort
     */
    public String getProxyHost(FileSystemOptions opts)
    {
        return SftpFileSystemOptions.getInstance(opts).getProxyHost();
    }

    /**
     * Get the proxy-port to use for sftp the connection
     * You have to set the ProxyHost too if you would like to have the proxy relly used.
     *
     * @return proxyPort: the port number or 0 if it is not set
     * @see #setProxyHost
     */
    public int getProxyPort(FileSystemOptions opts)
    {
        return SftpFileSystemOptions.getInstance(opts).getProxyPort();
    }

    /**
     * Set the proxy type to use for sftp connection.
     */
    public void setProxyType(FileSystemOptions opts, ProxyType proxyType)
    {
        SftpFileSystemOptions.getInstance(opts).setProxyType(proxyType);
    }

    /**
     * Get the proxy type to use for sftp connection.
     */
    public ProxyType getProxyType(FileSystemOptions opts)
    {
        return SftpFileSystemOptions.getInstance(opts).getProxyType();
    }

   /**
    * Configure authentication order
    */
    public void setPreferredAuthentications(FileSystemOptions opts, String preferredAuthentications)
    {
        SftpFileSystemOptions.getInstance(opts).setPreferredAuthentications(preferredAuthentications);
    }
    /**
    * Get authentication order
    */
    public String getPreferredAuthentications(FileSystemOptions opts)
    {
        return SftpFileSystemOptions.getInstance(opts).getPreferredAuthentications();
    }
}
