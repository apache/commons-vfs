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
 */
public class SftpFileSystemConfigBuilder extends FileSystemConfigBuilder
{
    private final static SftpFileSystemConfigBuilder builder = new SftpFileSystemConfigBuilder();

    private final static String USER_DIR_IS_ROOT = SftpFileSystemConfigBuilder.class.getName() + ".USER_DIR_IS_ROOT";
    private final static String TIMEOUT = SftpFileSystemConfigBuilder.class.getName() + ".TIMEOUT";

    public final static ProxyType PROXY_HTTP = new ProxyType("http");
    public final static ProxyType PROXY_SOCKS5 = new ProxyType("socks");

    public static class ProxyType implements Serializable, Comparable
    {
        private final String proxyType;

        private ProxyType(final String proxyType)
        {
            this.proxyType = proxyType;
        }

        public int compareTo(Object o)
        {
            return proxyType.compareTo(((ProxyType) o).proxyType);
        }


        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            ProxyType proxyType1 = (ProxyType) o;

            if (proxyType != null ? !proxyType.equals(proxyType1.proxyType) : proxyType1.proxyType != null)
            {
                return false;
            }

            return true;
        }
    }

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
     * @see #setCompression
     */
    public String getCompression(FileSystemOptions opts)
    {
        return (String) getParam(opts, "compression");
    }

    /**
     * @param opts
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

    /**
     * use user directory as root (do not change to fs root)
     *
     * @param opts
     * @param userDirIsRoot
     */
    public void setUserDirIsRoot(FileSystemOptions opts, boolean userDirIsRoot)
    {
        setParam(opts, USER_DIR_IS_ROOT, userDirIsRoot ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * @param opts
     * @see #setUserDirIsRoot
     */
    public Boolean getUserDirIsRoot(FileSystemOptions opts)
    {
        return (Boolean) getParam(opts, USER_DIR_IS_ROOT);
    }

    /**
     * set the timeout value on jsch session
     *
     * @param opts
     * @param timeout
     */
    public void setTimeout(FileSystemOptions opts, Integer timeout)
    {
        setParam(opts, TIMEOUT, timeout);
    }

    /**
     * @param opts
     * @see #setTimeout
     */
    public Integer getTimeout(FileSystemOptions opts)
    {
        return (Integer) getParam(opts, TIMEOUT);
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
        setParam(opts, "proxyHost", proxyHost);
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
        setParam(opts, "proxyPort", new Integer(proxyPort));
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
        return (String) getParam(opts, "proxyHost");
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
        if (!hasParam(opts, "proxyPort"))
        {
            return 0;
        }

        return ((Number) getParam(opts, "proxyPort")).intValue();
    }

    /**
     * Set the proxy type to use for sftp connection.
     */
    public void setProxyType(FileSystemOptions opts, ProxyType proxyType)
    {
        setParam(opts, "proxyType", proxyType);
    }

    /**
     * Get the proxy type to use for sftp connection.
     */
    public ProxyType getProxyType(FileSystemOptions opts)
    {
        return (ProxyType) getParam(opts, "proxyType");
    }
}
