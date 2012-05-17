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

import java.io.File;
import java.io.Serializable;

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;

import com.jcraft.jsch.UserInfo;

/**
 * The config BUILDER for various sftp configuration options.
 */
public final class SftpFileSystemConfigBuilder extends FileSystemConfigBuilder
{

    /**
     * Proxy type.
     */
    public static final class ProxyType implements Serializable, Comparable<ProxyType>
    {
        /**
         * serialVersionUID format is YYYYMMDD for the date of the last binary change.
         */
        private static final long serialVersionUID = 20101208L;

        private final String proxyType;

        private ProxyType(final String proxyType)
        {
            this.proxyType = proxyType;
        }

        @Override
        public int compareTo(final ProxyType pType)
        {
            return this.proxyType.compareTo(pType.proxyType);
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass())
            {
                return false;
            }

            final ProxyType pType = (ProxyType) obj;

            if (this.proxyType != null ? !this.proxyType.equals(pType.proxyType) : pType.proxyType != null)
            {
                return false;
            }

            return true;
        }

        /**
         * @return a hash code value for this object.
         * @since 2.0
         */
        @Override
        public int hashCode()
        {
            return this.proxyType.hashCode();
        }
    }

    private static final String _PREFIX = SftpFileSystemConfigBuilder.class.getName();

    private static final SftpFileSystemConfigBuilder BUILDER = new SftpFileSystemConfigBuilder();

    private static final String COMPRESSION = _PREFIX + "COMPRESSION";
    private static final String HOST_KEY_CHECK_ASK = "ask";
    private static final String HOST_KEY_CHECK_NO = "no";
    private static final String HOST_KEY_CHECK_YES = "yes";
    private static final String IDENTITIES = _PREFIX + ".IDENTITIES";
    private static final String KNOWN_HOSTS = _PREFIX + ".KNOWN_HOSTS";
    private static final String PREFERRED_AUTHENTICATIONS = _PREFIX + ".PREFERRED_AUTHENTICATIONS";
    private static final String PROXY_HOST = _PREFIX + ".PROXY_HOST";

    /** HTTP Proxy. */
    public static final ProxyType PROXY_HTTP = new ProxyType("http");
    private static final String PROXY_PORT = _PREFIX + ".PROXY_PORT";

    /** SOCKS Proxy. */
    public static final ProxyType PROXY_SOCKS5 = new ProxyType("socks");
    private static final String PROXY_TYPE = _PREFIX + ".PROXY_TYPE";
    private static final String STRICT_HOST_KEY_CHECKING = _PREFIX + ".STRICT_HOST_KEY_CHECKING";
    private static final String TIMEOUT = _PREFIX + ".TIMEOUT";
    private static final String USER_DIR_IS_ROOT = _PREFIX + ".USER_DIR_IS_ROOT";

    public static SftpFileSystemConfigBuilder getInstance()
    {
        return BUILDER;
    }

    private SftpFileSystemConfigBuilder()
    {
        super("sftp.");
    }

    /**
     * @param opts
     *            The FileSystem options.
     * @return The name of the compression algorithm.
     * @see #setCompression
     */
    public String getCompression(final FileSystemOptions opts)
    {
        return this.getString(opts, COMPRESSION);
    }

    @Override
    protected Class<? extends FileSystem> getConfigClass()
    {
        return SftpFileSystem.class;
    }

    /**
     * Gets the identity files (your private key files).
     * <p>
     * We use java.io.File because JSch cannot deal with VFS FileObjects.
     * </p>
     * 
     * @param opts
     *            The FileSystem options.
     * @return the array of identity Files.
     * @see #setIdentities
     */
    public File[] getIdentities(final FileSystemOptions opts)
    {
        return (File[]) this.getParam(opts, IDENTITIES);
    }

    /**
     * @param opts
     *            The FileSystem options.
     * @return the known hosts File.
     * @see #setKnownHosts
     */
    public File getKnownHosts(final FileSystemOptions opts)
    {
        return (File) this.getParam(opts, KNOWN_HOSTS);
    }

    /**
     * Get authentication order.
     * 
     * @param opts
     *            The FileSystem options.
     * @return The authentication order.
     * @since 2.0
     */
    public String getPreferredAuthentications(final FileSystemOptions opts)
    {
        return (String) this.getParam(opts, PREFERRED_AUTHENTICATIONS);
    }

    /**
     * Get the proxy to use for sftp connection. You have to set the ProxyPort too if you would like to have the proxy
     * relly used.
     * 
     * @param opts
     *            The FileSystem options.
     * @return proxyHost
     * @see #setProxyPort
     */
    public String getProxyHost(final FileSystemOptions opts)
    {
        return this.getString(opts, PROXY_HOST);
    }

    /**
     * Get the proxy-port to use for sftp the connection You have to set the ProxyHost too if you would like to have the
     * proxy relly used.
     * 
     * @param opts
     *            The FileSystem options.
     * @return proxyPort: the port number or 0 if it is not set
     * @see #setProxyHost
     */
    public int getProxyPort(final FileSystemOptions opts)
    {
        return this.getInteger(opts, PROXY_PORT, 0);
    }

    /**
     * Get the proxy type to use for sftp connection.
     * 
     * @param opts
     *            The FileSystem options.
     * @return The ProxyType.
     */
    public ProxyType getProxyType(final FileSystemOptions opts)
    {
        return (ProxyType) this.getParam(opts, PROXY_TYPE);
    }

    /**
     * @param opts
     *            The FileSystem options.
     * @return the option value The host key checking.
     * @see #setStrictHostKeyChecking(FileSystemOptions, String)
     */
    public String getStrictHostKeyChecking(final FileSystemOptions opts)
    {
        return this.getString(opts, STRICT_HOST_KEY_CHECKING, HOST_KEY_CHECK_NO);
    }

    /**
     * @param opts
     *            The FileSystem options.
     * @return The timeout value.
     * @see #setTimeout
     */
    public Integer getTimeout(final FileSystemOptions opts)
    {
        return this.getInteger(opts, TIMEOUT);
    }

    /**
     * @param opts
     *            The FileSystemOptions.
     * @return true if the user directory is the root.
     * @see #setUserDirIsRoot
     */
    public Boolean getUserDirIsRoot(final FileSystemOptions opts)
    {
        return this.getBoolean(opts, USER_DIR_IS_ROOT, Boolean.TRUE);
    }

    /**
     * @param opts
     *            The FileSystem options.
     * @return The UserInfo.
     * @see #setUserInfo
     */
    public UserInfo getUserInfo(final FileSystemOptions opts)
    {
        return (UserInfo) this.getParam(opts, UserInfo.class.getName());
    }

    /**
     * configure the compression to use.<br>
     * e.g. pass "zlib,none" to enable the compression.<br>
     * See the jsch documentation for details.
     * 
     * @param opts
     *            The FileSystem options.
     * @param compression
     *            The compression algorithm name.
     * @throws FileSystemException
     *             if an error occurs.
     */
    public void setCompression(final FileSystemOptions opts, final String compression) throws FileSystemException
    {
        this.setParam(opts, COMPRESSION, compression);
    }

    /**
     * Sets the identity files (your private key files).
     * <p>
     * We use java.io.File because JSch cannot deal with VFS FileObjects.
     * </p>
     * 
     * @param opts
     *            The FileSystem options.
     * @param identityFiles
     *            An array of identity Files.
     * @throws FileSystemException
     *             if an error occurs.
     */
    public void setIdentities(final FileSystemOptions opts, final File... identityFiles) throws FileSystemException
    {
        this.setParam(opts, IDENTITIES, identityFiles);
    }

    /**
     * Set the known_hosts file. e.g. /home/user/.ssh/known_hosts2<br>
     * Need to use a java.io.File as JSch cant deal with vfs FileObjects ;-)
     * 
     * @param opts
     *            The FileSystem options.
     * @param sshdir
     *            The known hosts directory.
     * @throws FileSystemException
     *             if an error occurs.
     */
    public void setKnownHosts(final FileSystemOptions opts, final File sshdir) throws FileSystemException
    {
        this.setParam(opts, KNOWN_HOSTS, sshdir);
    }

    /**
     * Configure authentication order.
     * 
     * @param opts
     *            The FileSystem options.
     * @param preferredAuthentications
     *            The authentication order.
     * @since 2.0
     */
    public void setPreferredAuthentications(final FileSystemOptions opts, final String preferredAuthentications)
    {
        this.setParam(opts, PREFERRED_AUTHENTICATIONS, preferredAuthentications);
    }

    /**
     * Set the proxy to use for sftp connection.<br>
     * You have to set the ProxyPort too if you would like to have the proxy relly used.
     * 
     * @param opts
     *            The FileSystem options.
     * @param proxyHost
     *            the host
     * @see #setProxyPort
     */
    public void setProxyHost(final FileSystemOptions opts, final String proxyHost)
    {
        this.setParam(opts, PROXY_HOST, proxyHost);
    }

    /**
     * Set the proxy-port to use for sftp connection. You have to set the ProxyHost too if you would like to have the
     * proxy relly used.
     * 
     * @param opts
     *            The FileSystem options.
     * @param proxyPort
     *            the port
     * @see #setProxyHost
     */
    public void setProxyPort(final FileSystemOptions opts, final int proxyPort)
    {
        this.setParam(opts, PROXY_PORT, Integer.valueOf(proxyPort));
    }

    /**
     * Set the proxy type to use for sftp connection.
     * 
     * @param opts
     *            The FileSystem options.
     * @param proxyType
     *            the type of the proxy to use.
     */
    public void setProxyType(final FileSystemOptions opts, final ProxyType proxyType)
    {
        this.setParam(opts, PROXY_TYPE, proxyType);
    }

    /**
     * configure the host key checking to use.<br>
     * valid arguments are only yes, no and ask.<br>
     * See the jsch documentation for details.
     * 
     * @param opts
     *            The FileSystem options.
     * @param hostKeyChecking
     *            The host key checking to use.
     * @throws FileSystemException
     *             if an error occurs.
     */
    public void setStrictHostKeyChecking(final FileSystemOptions opts, final String hostKeyChecking)
            throws FileSystemException
    {
        if (hostKeyChecking == null
                || (!hostKeyChecking.equals(HOST_KEY_CHECK_ASK) && !hostKeyChecking.equals(HOST_KEY_CHECK_NO) && !hostKeyChecking
                        .equals(HOST_KEY_CHECK_YES)))
        {
            throw new FileSystemException("vfs.provider.sftp/StrictHostKeyChecking-arg.error", hostKeyChecking);
        }

        this.setParam(opts, STRICT_HOST_KEY_CHECKING, hostKeyChecking);
    }

    /**
     * set the timeout value on jsch session.
     * 
     * @param opts
     *            The FileSystem options.
     * @param timeout
     *            The timeout.
     */
    public void setTimeout(final FileSystemOptions opts, final Integer timeout)
    {
        this.setParam(opts, TIMEOUT, timeout);
    }

    /**
     * use user directory as root (do not change to fs root).
     * 
     * @param opts
     *            The FileSystem options.
     * @param userDirIsRoot
     *            true if the user dir is the root directory.
     */
    public void setUserDirIsRoot(final FileSystemOptions opts, final boolean userDirIsRoot)
    {
        this.setParam(opts, USER_DIR_IS_ROOT, userDirIsRoot ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * Set the userinfo class to use if e.g. a password or a not known host will be contacted.
     * 
     * @param opts
     *            The FileSystem options.
     * @param info
     *            User information.
     */
    public void setUserInfo(final FileSystemOptions opts, final UserInfo info)
    {
        this.setParam(opts, UserInfo.class.getName(), info);
    }
}
