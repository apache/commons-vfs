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
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.DefaultFileSystemOptions;

import java.io.File;

/**
 * The config builder for various sftp configuration options
 *
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 * @version $Revision$
 */
public class SftpFileSystemOptions extends DefaultFileSystemOptions
{
    private final static SftpFileSystemOptions builder = new SftpFileSystemOptions();

    private final static String USER_DIR_IS_ROOT = SftpFileSystemOptions.class.getName() + ".USER_DIR_IS_ROOT";
    private final static String TIMEOUT = SftpFileSystemOptions.class.getName() + ".TIMEOUT";

    public final static ProxyType PROXY_HTTP = new ProxyType("http");
    public final static ProxyType PROXY_SOCKS5 = new ProxyType("socks");

    public SftpFileSystemOptions()
    {
        this("sftp.");
    }

    protected SftpFileSystemOptions(String scheme)
    {
        super(scheme);
    }

    public static SftpFileSystemOptions getInstance(FileSystemOptions opts)
    {
        return FileSystemOptions.makeSpecific(SftpFileSystemOptions.class, opts);
    }

    /**
     * Set the userinfo class to use if e.g. a password or a not known host
     * will be contacted
     *
     * @param info The UserInfo.
     */
    public void setUserInfo(UserInfo info)
    {
        setParam(UserInfo.class.getName(), info);
    }

    /**
     * @see #setUserInfo
     * @return The UserInfo.
     */
    public UserInfo getUserInfo()
    {
        return (UserInfo) getParam(UserInfo.class.getName());
    }

    /**
     * Set the known_hosts file. e.g. /home/user/.ssh/known_hosts2<br>
     * Need to use a java.io.File as JSch cant deal with vfs FileObjects ;-)
     *
     * @param sshdir The location of the known_hosts file.
     * @throws FileSystemException if an error occurs.
     */
    public void setKnownHosts(File sshdir) throws FileSystemException
    {
        setParam("knownHosts", sshdir);
    }

    /**
     * @see #setKnownHosts
     * @return the location of the known_hosts file.
     */
    public File getKnownHosts()
    {
        return (File) getParam("knownHosts");
    }

    /**
     * Set the identity files (your private key files).<br>
     * Need to use a java.io.File as JSch cant deal with vfs FileObjects ;-)
     *
     * @param identities The identity files.
     * @throws FileSystemException if an error occurs.
     */
    public void setIdentities(File[] identities) throws FileSystemException
    {
        setParam("identities", identities);
    }

    /**
     * configure the compression to use.<br>
     * e.g. pass "zlib,none" to enable the compression.<br>
     * See the jsch documentation for details.
     *
     * @param compression The compression type.
     * @throws FileSystemException if an error occurs.
     */
    public void setCompression(String compression) throws FileSystemException
    {
        setParam("compression", compression);
    }

    /**
     * @see #setCompression
     * @return the compression type.
     */
    public String getCompression()
    {
        return getString("compression");
    }

    /**
     * @see #setIdentities
     * @return The identity files.
     */
    public File[] getIdentities()
    {
        return (File[]) getParam("identities");
    }

    /**
     * configure the host key checking to use.<br>
     * valid arguments are only yes, no and ask.<br>
     * See the jsch documentation for details.
     *
     * @param hostKeyChecking The host key checking.
     * @throws FileSystemException if an error occurs.
     */
    public void setStrictHostKeyChecking(String hostKeyChecking) throws FileSystemException
    {
        if (hostKeyChecking == null || (!hostKeyChecking.equals("ask") && !hostKeyChecking.equals("no")
            && !hostKeyChecking.equals("yes")))
        {
            throw new FileSystemException("vfs.provider.sftp/StrictHostKeyChecking-arg.error", hostKeyChecking);
        }

        setParam("StrictHostKeyChecking", hostKeyChecking);
    }

    /**
     * @return the option value
     * @see #setStrictHostKeyChecking(String)
     */
    public String getStrictHostKeyChecking()
    {
        return getString("StrictHostKeyChecking");
    }

    /**
     * use user directory as root (do not change to fs root)
     *
     * @param userDirIsRoot true if the user's diretory is the root directory.
     */
    public void setUserDirIsRoot(boolean userDirIsRoot)
    {
        setParam(USER_DIR_IS_ROOT, userDirIsRoot ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * @see #setUserDirIsRoot
     * @return true if the user's directory is the root directory.
     */
    public Boolean getUserDirIsRoot()
    {
        return getBoolean(USER_DIR_IS_ROOT);
    }

    /**
     * set the timeout value on jsch session
     *
     * @param timeout The timeout value.
     */
    public void setTimeout(Integer timeout)
    {
        setParam(TIMEOUT, timeout);
    }

    /**
     * @see #setTimeout
     * @return the timeout value.
     */
    public Integer getTimeout()
    {
        return getInteger(TIMEOUT);
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
    public void setProxyHost(String proxyHost)
    {
        setParam("proxyHost", proxyHost);
    }

    /**
     * Set the proxy-port to use for sftp connection
     * You have to set the ProxyHost too if you would like to have the proxy relly used.
     *
     * @param proxyPort the port
     * @see #setProxyHost
     */
    public void setProxyPort(int proxyPort)
    {
        setParam("proxyPort", new Integer(proxyPort));
    }

    /**
     * Get the proxy to use for sftp connection
     * You have to set the ProxyPort too if you would like to have the proxy relly used.
     *
     * @return proxyHost
     * @see #setProxyPort
     */
    public String getProxyHost()
    {
        return getString("proxyHost");
    }

    /**
     * Get the proxy-port to use for sftp the connection
     * You have to set the ProxyHost too if you would like to have the proxy relly used.
     *
     * @return proxyPort: the port number or 0 if it is not set
     * @see #setProxyHost
     */
    public int getProxyPort()
    {
        return getInteger("proxyPort", 0);
    }

    /**
     * Set the proxy type to use for sftp connection.
     * @param proxyType the proxy type.
     */
    public void setProxyType(ProxyType proxyType)
    {
        setParam("proxyType", proxyType);
    }

    /**
     * Get the proxy type to use for sftp connection.
     * @return the proxy type.
     */
    public ProxyType getProxyType()
    {
        return (ProxyType) getParam("proxyType");
    }
}