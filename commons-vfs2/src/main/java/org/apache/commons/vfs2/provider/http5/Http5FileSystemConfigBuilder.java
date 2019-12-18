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
package org.apache.commons.vfs2.provider.http5;

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticator;
import org.apache.hc.client5.http.cookie.Cookie;

/**
 * Configuration options builder utility for http5 provider.
 *
 * @since 2.5.0
 */
public class Http5FileSystemConfigBuilder extends FileSystemConfigBuilder {

    private static final Http5FileSystemConfigBuilder BUILDER = new Http5FileSystemConfigBuilder();

    /**
     * Defines the maximum number of connections allowed overall. This value only applies
     * to the number of connections from a particular instance of HTTP connection manager.
     * <p>
     * This parameter expects a value of type {@link Integer}.
     * </p>
     */
    private static final String MAX_TOTAL_CONNECTIONS = "http.connection-manager.max-total";

    /**
     * Defines the maximum number of connections allowed per host configuration.
     * These values only apply to the number of connections from a particular instance
     * of HTTP connection manager.
     */
    private static final String MAX_HOST_CONNECTIONS = "http.connection-manager.max-per-host";

    /**
     * Defines the connection timeout of an HTTP request.
     * <p>
     * This parameter expects a value of type {@link Integer}.
     * </p>
     */
    private static final String CONNECTION_TIMEOUT = "http.connection.timeout";

    /**
     * Defines the socket timeout of an HTTP request.
     * <p>
     * This parameter expects a value of type {@link Integer}.
     * </p>
     */
    private static final String SO_TIMEOUT = "http.socket.timeout";

    /**
     * Defines whether Keep-Alive option is used or not.
     * <p>
     * This parameter expects a value of type {@link Boolean}.
     * </p>
     */
    private static final String KEEP_ALIVE = "http.keepAlive";

    /**
     * Defines the keystore file path for SSL connections.
     * <p>
     * This parameter expects a value of type {@link String}.
     * </p>
     */
    private static final String KEYSTORE_FILE = "http.keystoreFile";

    /**
     * Defines the keystore pass phrase for SSL connections.
     * <p>
     * This parameter expects a value of type {@link String}.
     * </p>
     */
    private static final String KEYSTORE_PASS = "http.keystorePass";

    /**
     * Defines whether the host name should be verified or not in SSL connections.
     * <p>
     * This parameter expects a value of type {@link Boolean}.
     * </p>
     */
    private static final String HOSTNAME_VERIFICATION_ENABLED = "http.hostname-verification.enabled";

    /**
     * Defines whether the HttpClient should follow redirections from the responses.
     * <p>
     * This parameter expects a value of type {@link Boolean}.
     * </p>
     */
    protected static final String KEY_FOLLOW_REDIRECT = "followRedirect";

    /**
     * Defines the User-Agent request header string of the underlying HttpClient.
     * <p>
     * This parameter expects a value of type {@link String}.
     * </p>
     */
    private static final String KEY_USER_AGENT = "userAgent";

    /**
     * Defines whether the preemptive authentication should be enabled or not.
     * <p>
     * This parameter expects a value of type {@link Boolean}.
     * </p>
     */
    private static final String KEY_PREEMPTIVE_AUTHENTICATION = "preemptiveAuth";

    /**
     * Defines the enabled TLS versions for the underlying HttpClient.
     * <p>
     * This parameter expects a value of type {@link String} as a comma separated string, each token of
     * which is the name of {@code org.apache.hc.core5.http.ssl.TLS} enum. e.g, "V_1_2, V_1_3".
     * </p>
     */
    private static final String KEY_TLS_VERSIONS = "tlsVersions";

    /**
     * The default value for {@link #MAX_TOTAL_CONNECTIONS} configuration.
     */
    private static final int DEFAULT_MAX_CONNECTIONS = 50;

    /**
     * The default value for {@link #MAX_HOST_CONNECTIONS} configuration.
     */
    private static final int DEFAULT_MAX_HOST_CONNECTIONS = 5;

    /**
     * The default value for {@link #CONNECTION_TIMEOUT} configuration.
     */
    private static final int DEFAULT_CONNECTION_TIMEOUT = 0;

    /**
     * The default value for {@link #SO_TIMEOUT} configuration.
     */
    private static final int DEFAULT_SO_TIMEOUT = 0;

    /**
     * The default value for {@link #KEEP_ALIVE} configuration.
     */
    private static final boolean DEFAULT_KEEP_ALIVE = true;

    /**
     * The default value for {@link #KEY_FOLLOW_REDIRECT} configuration.
     */
    private static final boolean DEFAULT_FOLLOW_REDIRECT = true;

    /**
     * The default value for {@link #KEY_USER_AGENT} configuration.
     */
    private static final String DEFAULT_USER_AGENT = "Jakarta-Commons-VFS";

    /**
     * The default value for {@link #HOSTNAME_VERIFICATION_ENABLED} configuration.
     */
    private static final boolean DEFAULT_HOSTNAME_VERIFICATION_ENABLED = true;

    /**
     * The default value for {@link #KEY_TLS_VERSIONS} configuration as a comma separated string, each token of
     * which is the name of {@code org.apache.hc.core5.http.ssl.TLS} enum. e.g, "V_1_2, V_1_3".
     */
    private static final String DEFAULT_TLS_VERSIONS = "V_1_2";

    /**
     * Construct an {@code Http4FileSystemConfigBuilder}.
     *
     * @param prefix String for properties of this file system.
     */
    protected Http5FileSystemConfigBuilder(final String prefix) {
        super(prefix);
    }

    private Http5FileSystemConfigBuilder() {
        super("http.");
    }

    /**
     * Gets the singleton builder.
     *
     * @return the singleton builder.
     */
    public static Http5FileSystemConfigBuilder getInstance() {
        return BUILDER;
    }

    /**
     * Sets the charset used for url encoding.
     *
     * @param opts The FileSystem options.
     * @param chaset the chaset
     */
    public void setUrlCharset(final FileSystemOptions opts, final String chaset) {
        setParam(opts, "urlCharset", chaset);
    }

    /**
     * Sets the charset used for url encoding.
     *
     * @param opts The FileSystem options.
     * @return the chaset
     */
    public String getUrlCharset(final FileSystemOptions opts) {
        return getString(opts, "urlCharset");
    }

    /**
     * Sets the proxy to use for http connection.
     * <p>
     * You have to set the ProxyPort too if you would like to have the proxy really used.
     * </p>
     *
     * @param opts The FileSystem options.
     * @param proxyHost the host
     * @see #setProxyPort
     */
    public void setProxyHost(final FileSystemOptions opts, final String proxyHost) {
        setParam(opts, "proxyHost", proxyHost);
    }

    /**
     * Sets the proxy-port to use for http connection. You have to set the ProxyHost too if you would like to have the
     * proxy really used.
     *
     * @param opts The FileSystem options.
     * @param proxyPort the port
     * @see #setProxyHost
     */
    public void setProxyPort(final FileSystemOptions opts, final int proxyPort) {
        setParam(opts, "proxyPort", Integer.valueOf(proxyPort));
    }

    /**
     * Gets the proxy to use for http connection. You have to set the ProxyPort too if you would like to have the proxy
     * really used.
     *
     * @param opts The FileSystem options.
     * @return proxyHost
     * @see #setProxyPort
     */
    public String getProxyHost(final FileSystemOptions opts) {
        return getString(opts, "proxyHost");
    }

    /**
     * Gets the proxy-port to use for http the connection. You have to set the ProxyHost too if you would like to have
     * the proxy really used.
     *
     * @param opts The FileSystem options.
     * @return proxyPort: the port number or 0 if it is not set
     * @see #setProxyHost
     */
    public int getProxyPort(final FileSystemOptions opts) {
        return getInteger(opts, "proxyPort", 0);
    }

    /**
     * Sets the proxy authenticator where the system should get the credentials from.
     *
     * @param opts The FileSystem options.
     * @param authenticator The UserAuthenticator.
     */
    public void setProxyAuthenticator(final FileSystemOptions opts, final UserAuthenticator authenticator) {
        setParam(opts, "proxyAuthenticator", authenticator);
    }

    /**
     * Gets the proxy authenticator where the system should get the credentials from.
     *
     * @param opts The FileSystem options.
     * @return The UserAuthenticator.
     */
    public UserAuthenticator getProxyAuthenticator(final FileSystemOptions opts) {
        return (UserAuthenticator) getParam(opts, "proxyAuthenticator");
    }

    /**
     * The cookies to add to the request.
     *
     * @param opts The FileSystem options.
     * @param cookies An array of Cookies.
     */
    public void setCookies(final FileSystemOptions opts, final Cookie[] cookies) {
        setParam(opts, "cookies", cookies);
    }

    /**
     * Sets whether to follow redirects for the connection.
     *
     * @param opts The FileSystem options.
     * @param redirect {@code true} to follow redirects, {@code false} not to.
     * @see #setFollowRedirect
     */
    public void setFollowRedirect(final FileSystemOptions opts, final boolean redirect) {
        setParam(opts, KEY_FOLLOW_REDIRECT, redirect);
    }

    /**
     * Gets the cookies to add to the request.
     *
     * @param opts The FileSystem options.
     * @return the Cookie array.
     */
    public Cookie[] getCookies(final FileSystemOptions opts) {
        return (Cookie[]) getParam(opts, "cookies");
    }

    /**
     * Gets whether to follow redirects for the connection.
     *
     * @param opts The FileSystem options.
     * @return {@code true} to follow redirects, {@code false} not to.
     * @see #setFollowRedirect
     */
    public boolean getFollowRedirect(final FileSystemOptions opts) {
        return getBoolean(opts, KEY_FOLLOW_REDIRECT, DEFAULT_FOLLOW_REDIRECT);
    }

    /**
     * Sets the maximum number of connections allowed.
     *
     * @param opts The FileSystem options.
     * @param maxTotalConnections The maximum number of connections.
     */
    public void setMaxTotalConnections(final FileSystemOptions opts, final int maxTotalConnections) {
        setParam(opts, MAX_TOTAL_CONNECTIONS, Integer.valueOf(maxTotalConnections));
    }

    /**
     * Gets the maximum number of connections allowed.
     *
     * @param opts The FileSystemOptions.
     * @return The maximum number of connections allowed.
     */
    public int getMaxTotalConnections(final FileSystemOptions opts) {
        return getInteger(opts, MAX_TOTAL_CONNECTIONS, DEFAULT_MAX_CONNECTIONS);
    }

    /**
     * Sets the maximum number of connections allowed to any host.
     *
     * @param opts The FileSystem options.
     * @param maxHostConnections The maximum number of connections to a host.
     */
    public void setMaxConnectionsPerHost(final FileSystemOptions opts, final int maxHostConnections) {
        setParam(opts, MAX_HOST_CONNECTIONS, Integer.valueOf(maxHostConnections));
    }

    /**
     * Gets the maximum number of connections allowed per host.
     *
     * @param opts The FileSystemOptions.
     * @return The maximum number of connections allowed per host.
     */
    public int getMaxConnectionsPerHost(final FileSystemOptions opts) {
        return getInteger(opts, MAX_HOST_CONNECTIONS, DEFAULT_MAX_HOST_CONNECTIONS);
    }

    /**
     * Determines if the FileSystemOptions indicate that preemptive authentication is requested.
     *
     * @param opts The FileSystemOptions.
     * @return true if preemptiveAuth is requested.
     */
    public boolean isPreemptiveAuth(final FileSystemOptions opts) {
        return getBoolean(opts, KEY_PREEMPTIVE_AUTHENTICATION, Boolean.FALSE).booleanValue();
    }

    /**
     * Sets the given value for preemptive HTTP authentication (using BASIC) on the given FileSystemOptions object.
     * Defaults to false if not set. It may be appropriate to set to true in cases when the resulting chattiness of the
     * conversation outweighs any architectural desire to use a stronger authentication scheme than basic/preemptive.
     *
     * @param opts The FileSystemOptions.
     * @param preemptiveAuth the desired setting; true=enabled and false=disabled.
     */
    public void setPreemptiveAuth(final FileSystemOptions opts, final boolean preemptiveAuth) {
        setParam(opts, KEY_PREEMPTIVE_AUTHENTICATION, Boolean.valueOf(preemptiveAuth));
    }

    /**
     * The connection timeout.
     *
     * @param opts The FileSystem options.
     * @param connectionTimeout The connection timeout.
     */
    public void setConnectionTimeout(final FileSystemOptions opts, final int connectionTimeout) {
        setParam(opts, CONNECTION_TIMEOUT, Integer.valueOf(connectionTimeout));
    }

    /**
     * Gets the connection timeout.
     *
     * @param opts The FileSystem options.
     * @return The connection timeout.
     */
    public int getConnectionTimeout(final FileSystemOptions opts) {
        return getInteger(opts, CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
    }

    /**
     * The socket timeout.
     *
     * @param opts The FileSystem options.
     * @param soTimeout socket timeout.
     */
    public void setSoTimeout(final FileSystemOptions opts, final int soTimeout) {
        setParam(opts, SO_TIMEOUT, Integer.valueOf(soTimeout));
    }

    /**
     * Gets the socket timeout.
     *
     * @param opts The FileSystemOptions.
     * @return The socket timeout.
     */
    public int getSoTimeout(final FileSystemOptions opts) {
        return getInteger(opts, SO_TIMEOUT, DEFAULT_SO_TIMEOUT);
    }

    /**
     * Sets if the FileSystemOptions indicate that HTTP Keep-Alive is respected.
     *
     * @param opts The FileSystemOptions.
     * @param keepAlive whether the FileSystemOptions indicate that HTTP Keep-Alive is respected or not.
     */
    public void setKeepAlive(final FileSystemOptions opts, final boolean keepAlive) {
        setParam(opts, KEEP_ALIVE, Boolean.valueOf(keepAlive));
    }

    /**
     * Determines if the FileSystemOptions indicate that HTTP Keep-Alive is respected.
     *
     * @param opts The FileSystemOptions.
     * @return true if if the FileSystemOptions indicate that HTTP Keep-Alive is respected.
     */
    public boolean isKeepAlive(final FileSystemOptions opts) {
        return getBoolean(opts, KEEP_ALIVE, DEFAULT_KEEP_ALIVE);
    }

    /**
     * Sets the user agent to attach to the outgoing http methods
     *
     * @param opts the file system options to modify
     * @param userAgent User Agent String
     */
    public void setUserAgent(final FileSystemOptions opts, final String userAgent) {
        setParam(opts, "userAgent", userAgent);
    }

    /**
     * Gets the user agent string
     *
     * @param opts the file system options to modify
     * @return User provided User-Agent string, otherwise default of: Commons-VFS
     */
    public String getUserAgent(final FileSystemOptions opts) {
        final String userAgent = (String) getParam(opts, KEY_USER_AGENT);
        return userAgent != null ? userAgent : DEFAULT_USER_AGENT;
    }

    /**
     * Set keystore file path for SSL connections.
     * @param opts the file system options to modify
     * @param keyStoreFile keystore file path
     */
    public void setKeyStoreFile(final FileSystemOptions opts, final String keyStoreFile) {
        setParam(opts, KEYSTORE_FILE, keyStoreFile);
    }

    /**
     * Return keystore file path to be used in SSL connections.
     * @param opts the file system options to modify
     * @return keystore file path to be used in SSL connections
     */
    public String getKeyStoreFile(final FileSystemOptions opts) {
        return (String) getParam(opts, KEYSTORE_FILE);
    }

    /**
     * Set keystore pass phrase for SSL connecdtions.
     * @param opts the file system options to modify
     * @param keyStorePass keystore pass phrase for SSL connecdtions
     */
    public void setKeyStorePass(final FileSystemOptions opts, final String keyStorePass) {
        setParam(opts, KEYSTORE_PASS, keyStorePass);
    }

    /**
     * Return keystore pass phrase for SSL connections.
     * @param opts the file system options to modify
     * @return keystore pass phrase for SSL connections
     */
    String getKeyStorePass(final FileSystemOptions opts) {
        return (String) getParam(opts, KEYSTORE_PASS);
    }

    /**
     * Sets if the hostname should be verified in SSL context.
     *
     * @param opts The FileSystemOptions.
     * @param hostnameVerificationEnabled whether hostname should be verified
     */
    public void setHostnameVerificationEnabled(final FileSystemOptions opts, final boolean hostnameVerificationEnabled) {
        setParam(opts, HOSTNAME_VERIFICATION_ENABLED, Boolean.valueOf(hostnameVerificationEnabled));
    }

    /**
     * Determines if the hostname should be verified in SSL context.
     *
     * @param opts The FileSystemOptions.
     * @return true if if the FileSystemOptions indicate that HTTP Keep-Alive is respected.
     */
    public boolean isHostnameVerificationEnabled(final FileSystemOptions opts) {
        return getBoolean(opts, HOSTNAME_VERIFICATION_ENABLED, DEFAULT_HOSTNAME_VERIFICATION_ENABLED);
    }

    /**
     * Sets the enabled TLS versions as a comma separated string, each token of which is the name of
     * {@code org.apache.hc.core5.http.ssl.TLS} enum. e.g, "V_1_2, V_1_3".
     *
     * @param opts the file system options to modify
     * @param tlsVersions enabled TLS versions
     */
    public void setTlsVersions(final FileSystemOptions opts, final String tlsVersions) {
        setParam(opts, KEY_TLS_VERSIONS, tlsVersions);
    }

    /**
     * Gets the enabled TLS versions as a comma separated string, each token of which is the name of
     * {@code org.apache.hc.core5.http.ssl.TLS} enum. e.g, "V_1_2, V_1_3".
     *
     * @param opts the file system options to modify
     * @return enabled TLS versions
     */
    public String getTlsVersions(final FileSystemOptions opts) {
        final String tlsVersions = (String) getParam(opts, KEY_TLS_VERSIONS);
        return tlsVersions != null ? tlsVersions : DEFAULT_TLS_VERSIONS;
    }

    @Override
    protected Class<? extends FileSystem> getConfigClass() {
        return Http5FileSystem.class;
    }
}
