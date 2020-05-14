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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;

import com.jcraft.jsch.ConfigRepository;
import com.jcraft.jsch.UserInfo;

/**
 * The config builder for various SFTP configuration options.
 */
public final class SftpFileSystemConfigBuilder extends FileSystemConfigBuilder {

    /**
     * Proxy type.
     */
    public static final class ProxyType implements Serializable, Comparable<ProxyType> {
        /**
         * serialVersionUID format is YYYYMMDD for the date of the last binary change.
         */
        private static final long serialVersionUID = 20101208L;

        private final String proxyType;

        private ProxyType(final String proxyType) {
            this.proxyType = proxyType;
        }

        @Override
        public int compareTo(final ProxyType pType) {
            return this.proxyType.compareTo(pType.proxyType);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }

            final ProxyType pType = (ProxyType) obj;

            if (this.proxyType != null ? !this.proxyType.equals(pType.proxyType) : pType.proxyType != null) {
                return false;
            }

            return true;
        }

        /**
         * @return a hash code value for this object.
         * @since 2.0
         */
        @Override
        public int hashCode() {
            return this.proxyType.hashCode();
        }
    }
    private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 0;

    private static final int DEFAULT_SESSION_TIMEOUT_MILLIS = 0;

    private static final String _PREFIX = SftpFileSystemConfigBuilder.class.getName();
    private static final SftpFileSystemConfigBuilder BUILDER = new SftpFileSystemConfigBuilder();
    private static final String COMPRESSION = _PREFIX + "COMPRESSION";
    private static final String CONNECT_TIMEOUT_MILLIS = _PREFIX + ".CONNECT_TIMEOUT_MILLIS";
    private static final String ENCODING = _PREFIX + ".ENCODING";
    private static final String HOST_KEY_CHECK_ASK = "ask";
    private static final String HOST_KEY_CHECK_NO = "no";
    private static final String HOST_KEY_CHECK_YES = "yes";
    private static final String IDENTITIES = _PREFIX + ".IDENTITIES";
    private static final String IDENTITY_REPOSITORY_FACTORY = _PREFIX + "IDENTITY_REPOSITORY_FACTORY";
    private static final String CONFIG_REPOSITORY = _PREFIX + "CONFIG_REPOSITORY";
    private static final String KEY_EXCHANGE_ALGORITHM = _PREFIX + ".KEY_EXCHANGE_ALGORITHM";
    private static final String LOAD_OPENSSH_CONFIG = _PREFIX + "LOAD_OPENSSH_CONFIG";
    private static final String KNOWN_HOSTS = _PREFIX + ".KNOWN_HOSTS";
    private static final String PREFERRED_AUTHENTICATIONS = _PREFIX + ".PREFERRED_AUTHENTICATIONS";
    private static final String PROXY_COMMAND = _PREFIX + ".PROXY_COMMAND";
    private static final String PROXY_HOST = _PREFIX + ".PROXY_HOST";
    private static final String PROXY_OPTIONS = _PREFIX + ".PROXY_OPTIONS";
    private static final String PROXY_PASSWORD = _PREFIX + ".PROXY_PASSWORD";
    private static final String PROXY_PORT = _PREFIX + ".PROXY_PORT";
    private static final String DISABLE_DETECT_EXEC_CHANNEL = _PREFIX + ".DISABLE_DETECT_EXEC_CHANNEL";

    /** HTTP Proxy. */
    public static final ProxyType PROXY_HTTP = new ProxyType("http");

    /** SOCKS Proxy. */
    public static final ProxyType PROXY_SOCKS5 = new ProxyType("socks");

    /**
     * Connects to the SFTP server through a remote host reached by SSH.
     * <p>
     * On this proxy host, a command (e.g. {@linkplain SftpStreamProxy#NETCAT_COMMAND} or
     * {@linkplain SftpStreamProxy#NETCAT_COMMAND}) is run to forward input/output streams between the target host and
     * the VFS host.
     * </p>
     * <p>
     * When used, the proxy username ({@linkplain #setProxyUser}) and hostname ({@linkplain #setProxyHost}) <b>must</b>
     * be set. Optionnaly, the command ({@linkplain #setProxyCommand}), password ({@linkplain #setProxyPassword}) and
     * connection options ({@linkplain #setProxyOptions}) can be set.
     * </p>
     */
    public static final ProxyType PROXY_STREAM = new ProxyType("stream");

    private static final String PROXY_TYPE = _PREFIX + ".PROXY_TYPE";
    private static final String PROXY_USER = _PREFIX + ".PROXY_USER";
    private static final String SESSION_TIMEOUT_MILLIS = _PREFIX + ".TIMEOUT";
    private static final String STRICT_HOST_KEY_CHECKING = _PREFIX + ".STRICT_HOST_KEY_CHECKING";
    private static final String USER_DIR_IS_ROOT = _PREFIX + ".USER_DIR_IS_ROOT";

    /**
     * Gets the singleton builder.
     *
     * @return the singleton builder.
     */
    public static SftpFileSystemConfigBuilder getInstance() {
        return BUILDER;
    }

    private SftpFileSystemConfigBuilder() {
        super("sftp.");
    }

    /**
     * @param opts The FileSystem options.
     * @return The names of the compression algorithms, comma-separated.
     * @see #setCompression
     */
    public String getCompression(final FileSystemOptions opts) {
        return this.getString(opts, COMPRESSION);
    }

    @Override
    protected Class<? extends FileSystem> getConfigClass() {
        return SftpFileSystem.class;
    }

    /**
     * Gets the config repository.
     *
     * @param opts The FileSystem options.
     * @return the ConfigRepository
     */
    public ConfigRepository getConfigRepository(final FileSystemOptions opts) {
        return (ConfigRepository) this.getParam(opts, CONFIG_REPOSITORY);
    }

    /**
     * @param opts The FileSystem options.
     * @return The connect timeout value in milliseconds.
     * @see #setConnectTimeoutMillis
     * @since 2.3
     */
    public Integer getConnectTimeoutMillis(final FileSystemOptions opts) {
        return this.getInteger(opts, CONNECT_TIMEOUT_MILLIS, DEFAULT_CONNECT_TIMEOUT_MILLIS);
    }

    /**
     * Gets the file name encoding.
     *
     * @param opts The FileSystem options.
     * @return the file name encoding
     */
    public String getFileNameEncoding(final FileSystemOptions opts) {
        return this.getString(opts, ENCODING);
    }

    /**
     * Gets the identity files (your private key files).
     * <p>
     * We use java.io.File because JSch cannot deal with VFS FileObjects.
     * </p>
     *
     * @param opts The FileSystem options.
     * @return the array of identity Files.
     * @see #setIdentities
     * @deprecated As of 2.1 use {@link #getIdentityInfo(FileSystemOptions)}
     */
    @Deprecated
    public File[] getIdentities(final FileSystemOptions opts) {
        final IdentityInfo[] info = getIdentityInfo(opts);
        if (info != null) {
            final File[] files = new File[info.length];
            for (int i = 0; i < files.length; ++i) {
                files[i] = info[i].getPrivateKey();
            }
            return files;
        }
        return null;
    }

    /**
     * Gets the identity infos.
     *
     * @param opts The FileSystem options.
     * @return the array of identity info.
     * @see #setIdentityInfo
     */
    public IdentityInfo[] getIdentityInfo(final FileSystemOptions opts) {
        final IdentityProvider[] infos = getIdentityProvider(opts);
        if (infos != null) {
            final List<IdentityInfo> list = new ArrayList<>(infos.length);
            for (final IdentityProvider identityProvider : infos) {
                if (identityProvider instanceof IdentityInfo) {
                    list.add((IdentityInfo) identityProvider);
                }
            }
            return list.toArray(new IdentityInfo[list.size()]);
        }
        return null;
    }

    /**
     * Gets the identity providers.
     *
     * @param opts The FileSystem options.
     * @return the array of identity providers.
     * @see #setIdentityProvider
     * @since 2.4
     */
    public IdentityProvider[] getIdentityProvider(final FileSystemOptions opts) {
        return (IdentityProvider[]) this.getParam(opts, IDENTITIES);
    }

    /**
     * Get the identity repository factory.
     *
     * @param opts The FileSystem options.
     * @return the IdentityRepositoryFactory
     */
    public IdentityRepositoryFactory getIdentityRepositoryFactory(final FileSystemOptions opts) {
        return (IdentityRepositoryFactory) this.getParam(opts, IDENTITY_REPOSITORY_FACTORY);
    }

    /**
     * @param opts The FileSystem options.
     * @return the option value for specific key exchange algorithm
     * @see #setKeyExchangeAlgorithm(FileSystemOptions, String)
     * @since 2.4
     */
    public String getKeyExchangeAlgorithm(final FileSystemOptions opts) {
        return this.getString(opts, KEY_EXCHANGE_ALGORITHM);
    }


    /**
     * @param opts The FileSystem options.
     * @return the known hosts File.
     * @see #setKnownHosts
     */
    public File getKnownHosts(final FileSystemOptions opts) {
        return (File) this.getParam(opts, KNOWN_HOSTS);
    }

    /**
     * Gets authentication order.
     *
     * @param opts The FileSystem options.
     * @return The authentication order.
     * @since 2.0
     */
    public String getPreferredAuthentications(final FileSystemOptions opts) {
        return getString(opts, PREFERRED_AUTHENTICATIONS);
    }

    /**
     * Gets the command that will be run on the proxy host when using a {@linkplain SftpStreamProxy}. The command
     * defaults to {@linkplain SftpStreamProxy#NETCAT_COMMAND}.
     *
     * @param opts The FileSystem options.
     * @return proxyOptions
     * @see SftpStreamProxy
     * @see #setProxyOptions
     * @since 2.1
     */
    public String getProxyCommand(final FileSystemOptions opts) {
        return this.getString(opts, PROXY_COMMAND, SftpStreamProxy.NETCAT_COMMAND);
    }

    /**
     * Gets the proxy to use for the SFTP connection.
     *
     * @param opts The FileSystem options.
     * @return proxyHost
     * @see #getProxyPort
     * @see #setProxyHost
     */
    public String getProxyHost(final FileSystemOptions opts) {
        return this.getString(opts, PROXY_HOST);
    }

    /**
     * Gets the proxy options that are used to connect to the proxy host.
     *
     * @param opts The FileSystem options.
     * @return proxyOptions
     * @see SftpStreamProxy
     * @see #setProxyOptions
     * @since 2.1
     */
    public FileSystemOptions getProxyOptions(final FileSystemOptions opts) {
        return (FileSystemOptions) this.getParam(opts, PROXY_OPTIONS);
    }

    /**
     * Gets the proxy password that are used to connect to the proxy host.
     *
     * @param opts The FileSystem options.
     * @return proxyOptions
     * @see SftpStreamProxy
     * @see #setProxyPassword
     * @since 2.1
     */
    public String getProxyPassword(final FileSystemOptions opts) {
        return this.getString(opts, PROXY_PASSWORD);
    }

    /**
     * Gets the proxy-port to use for the SFTP the connection.
     *
     * @param opts The FileSystem options.
     * @return proxyPort: the port number or 0 if it is not set
     * @see #setProxyPort
     * @see #getProxyHost
     */
    public int getProxyPort(final FileSystemOptions opts) {
        return this.getInteger(opts, PROXY_PORT, 0);
    }

    /**
     * Gets the proxy type to use for the SFTP connection.
     *
     * @param opts The FileSystem options.
     * @return The ProxyType.
     */
    public ProxyType getProxyType(final FileSystemOptions opts) {
        return (ProxyType) this.getParam(opts, PROXY_TYPE);
    }

    /**
     * Gets the user name for the proxy used for the SFTP connection.
     *
     * @param opts The FileSystem options.
     * @return proxyUser
     * @see #setProxyUser
     * @since 2.1
     */
    public String getProxyUser(final FileSystemOptions opts) {
        return this.getString(opts, PROXY_USER);
    }

    /**
     * @param opts The FileSystem options.
     * @return The session timeout value in milliseconds.
     * @see #setSessionTimeoutMillis
     * @since 2.3
     */
    public Integer getSessionTimeoutMillis(final FileSystemOptions opts) {
        return this.getInteger(opts, SESSION_TIMEOUT_MILLIS, DEFAULT_SESSION_TIMEOUT_MILLIS);
    }

    /**
     * @param opts The FileSystem options.
     * @return the option value The host key checking.
     * @see #setStrictHostKeyChecking(FileSystemOptions, String)
     */
    public String getStrictHostKeyChecking(final FileSystemOptions opts) {
        return this.getString(opts, STRICT_HOST_KEY_CHECKING, HOST_KEY_CHECK_NO);
    }

    /**
     * @param opts The FileSystem options.
     * @return The timeout value in milliseconds.
     * @see #setTimeout
     * @deprecated Use {@link #getSessionTimeoutMillis(FileSystemOptions)}
     */
    @Deprecated
    public Integer getTimeout(final FileSystemOptions opts) {
        return this.getInteger(opts, SESSION_TIMEOUT_MILLIS);
    }

    /**
     * Returns {@link Boolean#TRUE} if VFS should treat the user directory as the root directory. Defaults to
     * {@code Boolean.TRUE} if the method {@link #setUserDirIsRoot(FileSystemOptions, boolean)} has not been
     * invoked.
     *
     * @param opts The FileSystemOptions.
     * @return {@code Boolean.TRUE} if VFS treats the user directory as the root directory.
     * @see #setUserDirIsRoot
     */
    public Boolean getUserDirIsRoot(final FileSystemOptions opts) {
        return this.getBoolean(opts, USER_DIR_IS_ROOT, Boolean.TRUE);
    }

    /**
     * @param opts The FileSystem options.
     * @return The UserInfo.
     * @see #setUserInfo
     */
    public UserInfo getUserInfo(final FileSystemOptions opts) {
        return (UserInfo) this.getParam(opts, UserInfo.class.getName());
    }

    /**
     * Returns {@code true} if the detection of the exec channel should be disabled.
     * Returns {@code false} if the detection of the exec channel should be enabled.
     * Defaults to {@code false} if the method {@link #setDisableDetectExecChannel(FileSystemOptions, boolean)} has not been invoked.
     *
     * @param opts The FileSystemOptions.
     * @return {@code true} if detection of exec channel should be disabled.
     *
     * @see #setDisableDetectExecChannel(FileSystemOptions, boolean)
     * @since 2.7.0
     */
    public boolean isDisableDetectExecChannel(final FileSystemOptions opts) {
        return this.getBoolean(opts, DISABLE_DETECT_EXEC_CHANNEL, Boolean.FALSE);
    }

    /**
     * Returns {@link Boolean#TRUE} if VFS should load the OpenSSH config. Defaults to {@code Boolean.FALSE} if the
     * method {@link #setLoadOpenSSHConfig(FileSystemOptions, boolean)} has not been invoked.
     *
     * @param opts The FileSystemOptions.
     * @return {@code Boolean.TRUE} if VFS should load the OpenSSH config.
     * @see #setLoadOpenSSHConfig
     */
    public boolean isLoadOpenSSHConfig(final FileSystemOptions opts) {
        return this.getBoolean(opts, LOAD_OPENSSH_CONFIG, Boolean.FALSE);
    }

    /**
     * Configures the compression algorithms to use.
     * <p>
     * For example, use {@code "zlib,none"} to enable compression.
     * </p>
     * <p>
     * See the Jsch documentation (in particular the README file) for details.
     * </p>
     *
     * @param opts        The FileSystem options.
     * @param compression The names of the compression algorithms, comma-separated.
     * @throws FileSystemException if an error occurs.
     */
    public void setCompression(final FileSystemOptions opts, final String compression) throws FileSystemException {
        this.setParam(opts, COMPRESSION, compression);
    }

    /**
     * Sets the config repository. e.g. {@code /home/user/.ssh/config}.
     * <p>
     * This is useful when you want to use OpenSSHConfig.
     * </p>
     *
     * @param opts             The FileSystem options.
     * @param configRepository An config repository.
     * @throws FileSystemException if an error occurs.
     * @see <a href="http://www.jcraft.com/jsch/examples/OpenSSHConfig.java.html">OpenSSHConfig</a>
     */
    public void setConfigRepository(final FileSystemOptions opts, final ConfigRepository configRepository)
            throws FileSystemException {
        this.setParam(opts, CONFIG_REPOSITORY, configRepository);
    }

    /**
     * Sets the timeout value to create a Jsch connection.
     *
     * @param opts    The FileSystem options.
     * @param timeout The connect timeout in milliseconds.
     * @since 2.3
     */
    public void setConnectTimeoutMillis(final FileSystemOptions opts, final Integer timeout) {
        this.setParam(opts, CONNECT_TIMEOUT_MILLIS, timeout);
    }

    /**
     * Sets whether detection of exec channel is disabled.
     * If this value is true the FileSystem will not test if the server allows to exec commands and disable the use of the exec channel.
     *
     * @param opts        The FileSystem options.
     * @param disableDetectExecChannel true if the detection of exec channel should be disabled.
     * @since 2.7.0
     */
    public void setDisableDetectExecChannel(final FileSystemOptions opts, final boolean disableDetectExecChannel) {
        this.setParam(opts, DISABLE_DETECT_EXEC_CHANNEL, toBooleanObject(disableDetectExecChannel));
    }

    /**
     * Sets the file name encoding.
     *
     * @param opts             The FileSystem options.
     * @param fileNameEncoding The name of the encoding to use for file names.
     */
    public void setFileNameEncoding(final FileSystemOptions opts, final String fileNameEncoding) {
        this.setParam(opts, ENCODING, fileNameEncoding);
    }

    /**
     * Sets the identity files (your private key files).
     * <p>
     * We use {@link java.io.File} because JSch cannot deal with VFS FileObjects.
     * </p>
     *
     * @param opts          The FileSystem options.
     * @param identityFiles An array of identity Files.
     * @throws FileSystemException if an error occurs.
     * @deprecated As of 2.1 use {@link #setIdentityInfo(FileSystemOptions, IdentityInfo...)}
     */
    @Deprecated
    public void setIdentities(final FileSystemOptions opts, final File... identityFiles) throws FileSystemException {
        IdentityProvider[] info = null;
        if (identityFiles != null) {
            info = new IdentityProvider[identityFiles.length];
            for (int i = 0; i < identityFiles.length; i++) {
                info[i] = new IdentityInfo(identityFiles[i]);
            }
        }
        this.setParam(opts, IDENTITIES, info);
    }

    /**
     * Sets the identity info (your private key files).
     *
     * @param opts      The FileSystem options.
     * @param identites An array of identity info.
     * @throws FileSystemException if an error occurs.
     * @since 2.1
     * @deprecated Use {@link #setIdentityProvider(FileSystemOptions,IdentityProvider...)}
     */
    @Deprecated
    public void setIdentityInfo(final FileSystemOptions opts, final IdentityInfo... identites)
            throws FileSystemException {
        this.setParam(opts, IDENTITIES, identites);
    }

    /**
     * Sets the identity info (your private key files).
     *
     * @param opts      The FileSystem options.
     * @param identites An array of identity info.
     * @throws FileSystemException if an error occurs.
     * @since 2.4
     */
    public void setIdentityProvider(final FileSystemOptions opts, final IdentityProvider... identites)
            throws FileSystemException {
        this.setParam(opts, IDENTITIES, identites);
    }

    /**
     * Sets the identity repository.
     * <p>
     * This is useful when you want to use e.g. an SSH agent as provided.
     * </p>
     *
     * @param opts    The FileSystem options.
     * @param factory An identity repository.
     * @throws FileSystemException if an error occurs.
     * @see <a href="http://www.jcraft.com/jsch-agent-proxy/">JSch agent proxy</a>
     */
    public void setIdentityRepositoryFactory(final FileSystemOptions opts, final IdentityRepositoryFactory factory)
            throws FileSystemException {
        this.setParam(opts, IDENTITY_REPOSITORY_FACTORY, factory);
    }

    /**
     * Configures Key exchange algorithm explicitly e.g diffie-hellman-group14-sha1,
     * diffie-hellman-group-exchange-sha256, diffie-hellman-group-exchange-sha1, diffie-hellman-group1-sha1
     *
     * @param opts                The FileSystem options.
     * @param keyExchangeAlgoritm The key exchange algorithm picked.
     * @since 2.4
     */
    public void setKeyExchangeAlgorithm(final FileSystemOptions opts, final String keyExchangeAlgoritm) {
        setParam(opts, KEY_EXCHANGE_ALGORITHM, keyExchangeAlgoritm);
    }

    /**
     * Sets the known_hosts file. e.g. {@code /home/user/.ssh/known_hosts2}.
     * <p>
     * We use {@link java.io.File} because JSch cannot deal with VFS FileObjects.
     * </p>
     *
     * @param opts       The FileSystem options.
     * @param knownHosts The known hosts file.
     * @throws FileSystemException if an error occurs.
     */
    public void setKnownHosts(final FileSystemOptions opts, final File knownHosts) throws FileSystemException {
        this.setParam(opts, KNOWN_HOSTS, knownHosts);
    }

    /**
     * Sets the whether to load OpenSSH config.
     *
     * @param opts              The FileSystem options.
     * @param loadOpenSSHConfig true if the OpenSSH config should be loaded.
     */
    public void setLoadOpenSSHConfig(final FileSystemOptions opts, final boolean loadOpenSSHConfig) {
        this.setParam(opts, LOAD_OPENSSH_CONFIG, toBooleanObject(loadOpenSSHConfig));
    }

    /**
     * Configures authentication order.
     *
     * @param opts                     The FileSystem options.
     * @param preferredAuthentications The authentication order.
     * @since 2.0
     */
    public void setPreferredAuthentications(final FileSystemOptions opts, final String preferredAuthentications) {
        this.setParam(opts, PREFERRED_AUTHENTICATIONS, preferredAuthentications);
    }

    /**
     * Sets the proxy username to use for the SFTP connection.
     *
     * @param opts         The FileSystem options.
     * @param proxyCommand the port
     * @see #getProxyOptions
     * @since 2.1
     */
    public void setProxyCommand(final FileSystemOptions opts, final String proxyCommand) {
        this.setParam(opts, PROXY_COMMAND, proxyCommand);
    }

    /**
     * Sets the proxy to use for the SFTP connection.
     *
     * You MUST also set the proxy port to use the proxy.
     *
     * @param opts      The FileSystem options.
     * @param proxyHost the host
     * @see #setProxyPort
     */
    public void setProxyHost(final FileSystemOptions opts, final String proxyHost) {
        this.setParam(opts, PROXY_HOST, proxyHost);
    }

    /**
     * Sets the proxy username to use for the SFTP connection.
     *
     * @param opts         The FileSystem options.
     * @param proxyOptions the options
     * @see #getProxyOptions
     * @since 2.1
     */
    public void setProxyOptions(final FileSystemOptions opts, final FileSystemOptions proxyOptions) {
        this.setParam(opts, PROXY_OPTIONS, proxyOptions);
    }

    /**
     * Sets the proxy password to use for the SFTP connection.
     *
     * @param opts          The FileSystem options.
     * @param proxyPassword the username used to connect to the proxy
     * @see #getProxyPassword
     * @since 2.1
     */
    public void setProxyPassword(final FileSystemOptions opts, final String proxyPassword) {
        this.setParam(opts, PROXY_PASSWORD, proxyPassword);
    }

    /**
     * Sets the proxy port to use for the SFTP connection.
     * <p>
     * You MUST also set the proxy host to use the proxy.
     * </p>
     *
     * @param opts      The FileSystem options.
     * @param proxyPort the port
     * @see #setProxyHost
     */
    public void setProxyPort(final FileSystemOptions opts, final int proxyPort) {
        this.setParam(opts, PROXY_PORT, Integer.valueOf(proxyPort));
    }

    /**
     * Sets the proxy type to use for the SFTP connection.
     * <p>
     * The possibles values are:
     * </p>
     * <ul>
     * <li>{@linkplain #PROXY_HTTP} connects using an HTTP proxy</li>
     * <li>{@linkplain #PROXY_SOCKS5} connects using an Socket5 proxy</li>
     * <li>{@linkplain #PROXY_STREAM} connects through a remote host stream command</li>
     * </ul>
     *
     * @param opts      The FileSystem options.
     * @param proxyType the type of the proxy to use.
     */
    public void setProxyType(final FileSystemOptions opts, final ProxyType proxyType) {
        this.setParam(opts, PROXY_TYPE, proxyType);
    }

    /**
     * Sets the proxy username to use for the SFTP connection.
     *
     * @param opts      The FileSystem options.
     * @param proxyUser the username used to connect to the proxy
     * @see #getProxyUser
     * @since 2.1
     */
    public void setProxyUser(final FileSystemOptions opts, final String proxyUser) {
        this.setParam(opts, PROXY_USER, proxyUser);
    }

    /**
     * Sets the timeout value on Jsch session.
     *
     * @param opts    The FileSystem options.
     * @param timeout The session timeout in milliseconds.
     * @since 2.3
     */
    public void setSessionTimeoutMillis(final FileSystemOptions opts, final Integer timeout) {
        this.setParam(opts, SESSION_TIMEOUT_MILLIS, timeout);
    }

    /**
     * Configures the host key checking to use.
     * <p>
     * Valid arguments are: {@code "yes"}, {@code "no"} and {@code "ask"}.
     * </p>
     * <p>
     * See the jsch documentation for details.
     * </p>
     *
     * @param opts            The FileSystem options.
     * @param hostKeyChecking The host key checking to use.
     * @throws FileSystemException if an error occurs.
     */
    public void setStrictHostKeyChecking(final FileSystemOptions opts, final String hostKeyChecking)
            throws FileSystemException {
        if (hostKeyChecking == null || (!hostKeyChecking.equals(HOST_KEY_CHECK_ASK)
                && !hostKeyChecking.equals(HOST_KEY_CHECK_NO) && !hostKeyChecking.equals(HOST_KEY_CHECK_YES))) {
            throw new FileSystemException("vfs.provider.sftp/StrictHostKeyChecking-arg.error", hostKeyChecking);
        }

        this.setParam(opts, STRICT_HOST_KEY_CHECKING, hostKeyChecking);
    }

    /**
     * Sets the timeout value on Jsch session.
     *
     * @param opts    The FileSystem options.
     * @param timeout The timeout in milliseconds.
     * @deprecated Use {@link #setSessionTimeoutMillis(FileSystemOptions, Integer)}
     */
    @Deprecated
    public void setTimeout(final FileSystemOptions opts, final Integer timeout) {
        this.setParam(opts, SESSION_TIMEOUT_MILLIS, timeout);
    }

    /**
     * Sets the whether to use the user directory as root (do not change to file system root).
     *
     * @param opts          The FileSystem options.
     * @param userDirIsRoot true if the user directory is the root directory.
     */
    public void setUserDirIsRoot(final FileSystemOptions opts, final boolean userDirIsRoot) {
        this.setParam(opts, USER_DIR_IS_ROOT, toBooleanObject(userDirIsRoot));
    }

    /**
     * Sets the Jsch UserInfo class to use.
     *
     * @param opts The FileSystem options.
     * @param info User information.
     */
    public void setUserInfo(final FileSystemOptions opts, final UserInfo info) {
        this.setParam(opts, UserInfo.class.getName(), info);
    }

}
