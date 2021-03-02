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
package org.apache.commons.vfs2.provider.http;

import java.time.Duration;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticator;

/**
 * Configuration options for HTTP.
 */
public class HttpFileSystemConfigBuilder extends FileSystemConfigBuilder {

    protected static final String KEY_FOLLOW_REDIRECT = "followRedirect";

    protected static final String KEY_USER_AGENT = "userAgent";

    private static final HttpFileSystemConfigBuilder BUILDER = new HttpFileSystemConfigBuilder();

    private static final int DEFAULT_MAX_HOST_CONNECTIONS = 5;

    private static final int DEFAULT_MAX_CONNECTIONS = 50;

    private static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ZERO;

    private static final Duration DEFAULT_SO_TIMEOUT = Duration.ZERO;

    private static final boolean DEFAULT_FOLLOW_REDIRECT = true;

    private static final String DEFAULT_USER_AGENT = "Jakarta-Commons-VFS";

    private static final String KEY_PREEMPTIVE_AUTHENTICATION = "preemptiveAuth";

    /**
     * Gets the singleton builder.
     *
     * @return the singleton builder.
     */
    public static HttpFileSystemConfigBuilder getInstance() {
        return BUILDER;
    }

    private HttpFileSystemConfigBuilder() {
        super("http.");
    }

    /**
     * Creates new config builder.
     *
     * @param prefix String for properties of this file system.
     * @since 2.0
     */
    protected HttpFileSystemConfigBuilder(final String prefix) {
        super(prefix);
    }

    @Override
    protected Class<? extends FileSystem> getConfigClass() {
        return HttpFileSystem.class;
    }

    /**
     * Gets the connection timeout.
     *
     * @param opts The FileSystem options.
     * @return The connection timeout.
     * @since 2.1
     * @deprecated Use {@link #getConnectionTimeoutDuration(FileSystemOptions)}.
     */
    @Deprecated
    public int getConnectionTimeout(final FileSystemOptions opts) {
        return getDurationInteger(opts, HttpConnectionParams.CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
    }

    /**
     * Gets the connection timeout.
     *
     * @param opts The FileSystem options.
     * @return The connection timeout.
     * @since 2.8.0
     */
    public Duration getConnectionTimeoutDuration(final FileSystemOptions opts) {
        return getDuration(opts, HttpConnectionParams.CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
    }

    /**
     * Gets the cookies to add to the request.
     *
     * @param opts The FileSystem options.
     * @return the Cookie array.
     */
    public Cookie[] getCookies(final FileSystemOptions opts) {
        return getParam(opts, "cookies");
    }

    /**
     * Gets whether to follow redirects for the connection.
     *
     * @param opts The FileSystem options.
     * @return {@code true} to follow redirects, {@code false} not to.
     * @see #setFollowRedirect
     * @since 2.1
     */
    public boolean getFollowRedirect(final FileSystemOptions opts) {
        return getBoolean(opts, KEY_FOLLOW_REDIRECT, DEFAULT_FOLLOW_REDIRECT);
    }

    /**
     * Gets the maximum number of connections allowed per host.
     *
     * @param opts The FileSystemOptions.
     * @return The maximum number of connections allowed per host.
     * @since 2.0
     */
    public int getMaxConnectionsPerHost(final FileSystemOptions opts) {
        return getInteger(opts, HttpConnectionManagerParams.MAX_HOST_CONNECTIONS, DEFAULT_MAX_HOST_CONNECTIONS);
    }

    /**
     * Gets the maximum number of connections allowed.
     *
     * @param opts The FileSystemOptions.
     * @return The maximum number of connections allowed.
     * @since 2.0
     */
    public int getMaxTotalConnections(final FileSystemOptions opts) {
        return getInteger(opts, HttpConnectionManagerParams.MAX_TOTAL_CONNECTIONS, DEFAULT_MAX_CONNECTIONS);
    }

    /**
     * Gets the proxy authenticator where the system should get the credentials from.
     *
     * @param opts The FileSystem options.
     * @return The UserAuthenticator.
     */
    public UserAuthenticator getProxyAuthenticator(final FileSystemOptions opts) {
        return getParam(opts, "proxyAuthenticator");
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
     * Gets the socket timeout.
     *
     * @param opts The FileSystemOptions.
     * @return The socket timeout.
     * @since 2.1
     * @deprecated Use {@link #getSoTimeoutDuration(FileSystemOptions)}.
     */
    @Deprecated
    public int getSoTimeout(final FileSystemOptions opts) {
        return getDurationInteger(opts, HttpConnectionParams.SO_TIMEOUT, DEFAULT_SO_TIMEOUT);
    }

    /**
     * Gets the socket timeout.
     *
     * @param opts The FileSystemOptions.
     * @return The socket timeout.
     * @since 2.8.0
     */
    public Duration getSoTimeoutDuration(final FileSystemOptions opts) {
        return getDuration(opts, HttpConnectionParams.SO_TIMEOUT, DEFAULT_SO_TIMEOUT);
    }

    /**
     * Gets the charset used for url encoding.
     *
     * @param opts The FileSystem options.
     * @return the charset name.
     */
    public String getUrlCharset(final FileSystemOptions opts) {
        return getString(opts, "urlCharset");
    }

    /**
     * Gets the user agent string
     *
     * @param opts the file system options to modify
     * @return User provided User-Agent string, otherwise default of: Commons-VFS
     */
    public String getUserAgent(final FileSystemOptions opts) {
        final String userAgent = getParam(opts, KEY_USER_AGENT);
        return userAgent != null ? userAgent : DEFAULT_USER_AGENT;
    }

    /**
     * Determines if the FileSystemOptions indicate that preemptive authentication is requested.
     *
     * @param opts The FileSystemOptions.
     * @return true if preemptiveAuth is requested.
     * @since 2.0
     */
    public boolean isPreemptiveAuth(final FileSystemOptions opts) {
        return getBoolean(opts, KEY_PREEMPTIVE_AUTHENTICATION, Boolean.FALSE).booleanValue();
    }

    /**
     * The connection timeout.
     *
     * @param opts The FileSystem options.
     * @param timeout The connection timeout.
     * @since 2.8.0
     */
    public void setConnectionTimeout(final FileSystemOptions opts, final Duration timeout) {
        setParam(opts, HttpConnectionParams.CONNECTION_TIMEOUT, timeout);
    }

    /**
     * The connection timeout.
     *
     * @param opts The FileSystem options.
     * @param timeout The connection timeout.
     * @since 2.1
     * @deprecated Use {@link #setConnectionTimeout(FileSystemOptions, Duration)}.
     */
    @Deprecated
    public void setConnectionTimeout(final FileSystemOptions opts, final int timeout) {
        setConnectionTimeout(opts, Duration.ofMillis(timeout));
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
     * @since 2.1
     */
    public void setFollowRedirect(final FileSystemOptions opts, final boolean redirect) {
        setParam(opts, KEY_FOLLOW_REDIRECT, redirect);
    }

    /**
     * Sets the maximum number of connections allowed to any host.
     *
     * @param opts The FileSystem options.
     * @param maxHostConnections The maximum number of connections to a host.
     * @since 2.0
     */
    public void setMaxConnectionsPerHost(final FileSystemOptions opts, final int maxHostConnections) {
        setParam(opts, HttpConnectionManagerParams.MAX_HOST_CONNECTIONS, Integer.valueOf(maxHostConnections));
    }

    /**
     * Sets the maximum number of connections allowed.
     *
     * @param opts The FileSystem options.
     * @param maxTotalConnections The maximum number of connections.
     * @since 2.0
     */
    public void setMaxTotalConnections(final FileSystemOptions opts, final int maxTotalConnections) {
        setParam(opts, HttpConnectionManagerParams.MAX_TOTAL_CONNECTIONS, Integer.valueOf(maxTotalConnections));
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
     * Sets the proxy authenticator where the system should get the credentials from.
     *
     * @param opts The FileSystem options.
     * @param authenticator The UserAuthenticator.
     */
    public void setProxyAuthenticator(final FileSystemOptions opts, final UserAuthenticator authenticator) {
        setParam(opts, "proxyAuthenticator", authenticator);
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
     * The socket timeout.
     *
     * @param opts The FileSystem options.
     * @param timeout socket timeout.
     * @since 2.8.0
     */
    public void setSoTimeout(final FileSystemOptions opts, final Duration timeout) {
        setParam(opts, HttpConnectionParams.SO_TIMEOUT, timeout);
    }

    /**
     * The socket timeout.
     *
     * @param opts The FileSystem options.
     * @param timeout socket timeout.
     * @since 2.1
     * @deprecated Use {@link #setSoTimeout(FileSystemOptions, Duration)}.
     */
    @Deprecated
    public void setSoTimeout(final FileSystemOptions opts, final int timeout) {
        setSoTimeout(opts, Duration.ofMillis(timeout));
    }

    /**
     * Sets the charset used for url encoding.
     *
     * @param opts The FileSystem options.
     * @param charset the charset name.
     */
    public void setUrlCharset(final FileSystemOptions opts, final String charset) {
        setParam(opts, "urlCharset", charset);
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
}
