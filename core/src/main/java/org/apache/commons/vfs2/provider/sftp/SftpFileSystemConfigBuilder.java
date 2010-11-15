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
package org.apache.commons.vfs2.provider.sftp;

import com.jcraft.jsch.UserInfo;

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;

import java.io.File;
import java.io.Serializable;

/**
 * The config BUILDER for various sftp configuration options.
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date$
 */
public final class SftpFileSystemConfigBuilder extends FileSystemConfigBuilder
{
    /** HTTP Proxy. */
    public static final ProxyType PROXY_HTTP = new ProxyType("http");
    /** SOCKS Proxy. */
    public static final ProxyType PROXY_SOCKS5 = new ProxyType("socks");

    private static final SftpFileSystemConfigBuilder BUILDER = new SftpFileSystemConfigBuilder();
    private static final String USER_DIR_IS_ROOT = SftpFileSystemConfigBuilder.class.getName() + ".USER_DIR_IS_ROOT";
    private static final String TIMEOUT = SftpFileSystemConfigBuilder.class.getName() + ".TIMEOUT";

    private SftpFileSystemConfigBuilder()
    {
        super("sftp.");
    }

    public static SftpFileSystemConfigBuilder getInstance()
    {
        return BUILDER;
    }

    /**
     * Proxy type.
     */
    public static final class ProxyType implements Serializable, Comparable<ProxyType>
    {
        private final String proxyType;

        private ProxyType(final String proxyType)
        {
            this.proxyType = proxyType;
        }

        public int compareTo(ProxyType o)
        {
            return proxyType.compareTo(o.proxyType);
        }

        @Override
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

        /** @since 2.0 */
        @Override
        public int hashCode()
        {
            return proxyType.hashCode();
        }
    }

    /**
     * Set the userinfo class to use if e.g. a password or a not known host
     * will be contacted.
     *
     * @param opts The FileSystem options.
     * @param info User information.
     */
    public void setUserInfo(FileSystemOptions opts, UserInfo info)
    {
        setParam(opts, UserInfo.class.getName(), info);
    }

    /**
     * @param opts The FileSystem options.
     * @return The UserInfo.
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
     * @param opts The FileSystem options.
     * @param sshdir The known hosts directory.
     * @throws FileSystemException if an error occurs.
     */
    public void setKnownHosts(FileSystemOptions opts, File sshdir) throws FileSystemException
    {
        setParam(opts, "knownHosts", sshdir);
    }

    /**
     * @param opts The FileSystem options.
     * @return the known hosts File.
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
     * @param opts The FileSystem options.
     * @param identities An array of identity Files.
     * @throws FileSystemException if an error occurs.
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
     * @param opts The FileSystem options.
     * @param compression The compression algorithm name.
     * @throws FileSystemException if an error occurs.
     */
    public void setCompression(FileSystemOptions opts, String compression) throws FileSystemException
    {
        setParam(opts, "compression", compression);
    }

    /**
     * @param opts The FileSystem options.
     * @return The name of the compression algorithm.
     * @see #setCompression
     */
    public String getCompression(FileSystemOptions opts)
    {
        return getString(opts, "compression");
    }

    /**
     * @param opts The FileSystem options.
     * @return the array of identity Files.
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
     * @param opts The FileSystem options.
     * @param hostKeyChecking The host key checking to use.
     * @throws FileSystemException if an error occurs.
     */
    public void setStrictHostKeyChecking(FileSystemOptions opts, String hostKeyChecking) throws FileSystemException
    {
        if (hostKeyChecking == null || (!hostKeyChecking.equals("ask") && !hostKeyChecking.equals("no") &&
            !hostKeyChecking.equals("yes")))
        {
            throw new FileSystemException("vfs.provider.sftp/StrictHostKeyChecking-arg.error", hostKeyChecking);
        }

        setParam(opts, "StrictHostKeyChecking", hostKeyChecking);
    }

    /**
     * @param opts The FileSystem options.
     * @return the option value The host key checking.
     * @see #setStrictHostKeyChecking(FileSystemOptions, String)
     */
    public String getStrictHostKeyChecking(FileSystemOptions opts)
    {
        return getString(opts, "StrictHostKeyChecking", "no");
    }

    /**
     * use user directory as root (do not change to fs root).
     *
     * @param opts The FileSystem options.
     * @param userDirIsRoot true if the user dir is the root directory.
     */
    public void setUserDirIsRoot(FileSystemOptions opts, boolean userDirIsRoot)
    {
        setParam(opts, USER_DIR_IS_ROOT, userDirIsRoot ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * @param opts The FileSystemOptions.
     * @return true if the user directory is the root.
     * @see #setUserDirIsRoot
     */
    public Boolean getUserDirIsRoot(FileSystemOptions opts)
    {
        return getBoolean(opts, USER_DIR_IS_ROOT, Boolean.TRUE);
    }

    /**
     * set the timeout value on jsch session.
     *
     * @param opts The FileSystem options.
     * @param timeout The timeout.
     */
    public void setTimeout(FileSystemOptions opts, Integer timeout)
    {
        setParam(opts, TIMEOUT, timeout);
    }

    /**
     * @param opts The FileSystem options.
     * @return The timeout value.
     * @see #setTimeout
     */
    public Integer getTimeout(FileSystemOptions opts)
    {
        return getInteger(opts, TIMEOUT);
    }

    @Override
    protected Class<? extends FileSystem> getConfigClass()
    {
        return SftpFileSystem.class;
    }

    /**
     * Set the proxy to use for sftp connection.<br>
     * You have to set the ProxyPort too if you would like to have the proxy relly used.
     *
     * @param opts The FileSystem options.
     * @param proxyHost the host
     * @see #setProxyPort
     */
    public void setProxyHost(FileSystemOptions opts, String proxyHost)
    {
        setParam(opts, "proxyHost", proxyHost);
    }

    /**
     * Set the proxy-port to use for sftp connection.
     * You have to set the ProxyHost too if you would like to have the proxy relly used.
     *
     * @param opts The FileSystem options.
     * @param proxyPort the port
     * @see #setProxyHost
     */
    public void setProxyPort(FileSystemOptions opts, int proxyPort)
    {
        setParam(opts, "proxyPort", new Integer(proxyPort));
    }

    /**
     * Get the proxy to use for sftp connection.
     * You have to set the ProxyPort too if you would like to have the proxy relly used.
     *
     * @param opts The FileSystem options.
     * @return proxyHost
     * @see #setProxyPort
     */
    public String getProxyHost(FileSystemOptions opts)
    {
        return getString(opts, "proxyHost");
    }

    /**
     * Get the proxy-port to use for sftp the connection
     * You have to set the ProxyHost too if you would like to have the proxy relly used.
     *
     * @param opts The FileSystem options.
     * @return proxyPort: the port number or 0 if it is not set
     * @see #setProxyHost
     */
    public int getProxyPort(FileSystemOptions opts)
    {
        return getInteger(opts, "proxyPort", 0);
    }

    /**
     * Set the proxy type to use for sftp connection.
     * @param opts The FileSystem options.
     * @param proxyType the type of the proxy to use.
     */
    public void setProxyType(FileSystemOptions opts, ProxyType proxyType)
    {
        setParam(opts, "proxyType", proxyType);
    }

    /**
     * Get the proxy type to use for sftp connection.
     * @param opts The FileSystem options.
     * @return The ProxyType.
     */
    public ProxyType getProxyType(FileSystemOptions opts)
    {
        return (ProxyType) getParam(opts, "proxyType");
    }

    /**
     * Configure authentication order.
     * @param opts The FileSystem options.
     * @param preferredAuthentications The authentication order.
     * @since 2.0
     */
    public void setPreferredAuthentications(FileSystemOptions opts, String preferredAuthentications)
    {
        setParam(opts, "PreferredAuthentications", preferredAuthentications);
    }

    /**
     * Get authentication order.
     * @param opts The FileSystem options.
     * @return The authentication order.
     * @since 2.0
     */
    public String getPreferredAuthentications(FileSystemOptions opts)
    {
        return (String) getParam(opts, "PreferredAuthentications");
    }
}
