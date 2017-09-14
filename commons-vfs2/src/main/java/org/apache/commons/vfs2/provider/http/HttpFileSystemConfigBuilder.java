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

    private static final int DEFAULT_CONNECTION_TIMEOUT = 0;

    private static final int DEFAULT_SO_TIMEOUT = 0;

    private static final boolean DEFAULT_FOLLOW_REDIRECT = true;

    private static final String DEFAULT_USER_AGENT = "Jakarta-Commons-VFS";

    private static final String KEY_PREEMPTIVE_AUTHENTICATION = "preemptiveAuth";

    /**
     * Create new config builder.
     *
     * @param prefix String for properties of this file system.
     * @since 2.0
     */
    protected HttpFileSystemConfigBuilder(final String prefix) {
        super(prefix);
    }

    private HttpFileSystemConfigBuilder() {
        super("http.");
    }

    /**
     * Gets the singleton builder.
     *
     * @return the singleton builder.
     */
    public static HttpFileSystemConfigBuilder getInstance() {
        return BUILDER;
    }

    /**
     * Set the charset used for url encoding.<br>
     *
     * @param opts The FileSystem options.
     * @param chaset the chaset
     */
    public void setUrlCharset(final FileSystemOptions opts, final String chaset) {
        setParam(opts, "urlCharset", chaset);
    }

    /**
     * Set the charset used for url encoding.<br>
     *
     * @param opts The FileSystem options.
     * @return the chaset
     */
    public String getUrlCharset(final FileSystemOptions opts) {
        return getString(opts, "urlCharset");
    }

    /**
     * Set the proxy to use for http connection.<br>
     * You have to set the ProxyPort too if you would like to have the proxy really used.
     *
     * @param opts The FileSystem options.
     * @param proxyHost the host
     * @see #setProxyPort
     */
    public void setProxyHost(final FileSystemOptions opts, final String proxyHost) {
        setParam(opts, "proxyHost", proxyHost);
    }

    /**
     * Set the proxy-port to use for http connection. You have to set the ProxyHost too if you would like to have the
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
     * Get the proxy to use for http connection. You have to set the ProxyPort too if you would like to have the proxy
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
     * Get the proxy-port to use for http the connection. You have to set the ProxyHost too if you would like to have
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
     * Set the proxy authenticator where the system should get the credentials from.
     *
     * @param opts The FileSystem options.
     * @param authenticator The UserAuthenticator.
     */
    public void setProxyAuthenticator(final FileSystemOptions opts, final UserAuthenticator authenticator) {
        setParam(opts, "proxyAuthenticator", authenticator);
    }

    /**
     * Get the proxy authenticator where the system should get the credentials from.
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
     * @since 2.1
     */
    public void setFollowRedirect(final FileSystemOptions opts, final boolean redirect) {
        setParam(opts, KEY_FOLLOW_REDIRECT, redirect);
    }

    /**
     * The cookies to add to the request.
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
     * @since 2.1
     */
    public boolean getFollowRedirect(final FileSystemOptions opts) {
        return getBoolean(opts, KEY_FOLLOW_REDIRECT, DEFAULT_FOLLOW_REDIRECT);
    }

    /**
     * The maximum number of connections allowed.
     *
     * @param opts The FileSystem options.
     * @param maxTotalConnections The maximum number of connections.
     * @since 2.0
     */
    public void setMaxTotalConnections(final FileSystemOptions opts, final int maxTotalConnections) {
        setParam(opts, HttpConnectionManagerParams.MAX_TOTAL_CONNECTIONS, Integer.valueOf(maxTotalConnections));
    }

    /**
     * Retrieve the maximum number of connections allowed.
     *
     * @param opts The FileSystemOptions.
     * @return The maximum number of connections allowed.
     * @since 2.0
     */
    public int getMaxTotalConnections(final FileSystemOptions opts) {
        return getInteger(opts, HttpConnectionManagerParams.MAX_TOTAL_CONNECTIONS, DEFAULT_MAX_CONNECTIONS);
    }

    /**
     * The maximum number of connections allowed to any host.
     *
     * @param opts The FileSystem options.
     * @param maxHostConnections The maximum number of connections to a host.
     * @since 2.0
     */
    public void setMaxConnectionsPerHost(final FileSystemOptions opts, final int maxHostConnections) {
        setParam(opts, HttpConnectionManagerParams.MAX_HOST_CONNECTIONS, Integer.valueOf(maxHostConnections));
    }

    /**
     * Retrieve the maximum number of connections allowed per host.
     *
     * @param opts The FileSystemOptions.
     * @return The maximum number of connections allowed per host.
     * @since 2.0
     */
    public int getMaxConnectionsPerHost(final FileSystemOptions opts) {
        return getInteger(opts, HttpConnectionManagerParams.MAX_HOST_CONNECTIONS, DEFAULT_MAX_HOST_CONNECTIONS);
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
     * @since 2.1
     */
    public void setConnectionTimeout(final FileSystemOptions opts, final int connectionTimeout) {
        setParam(opts, HttpConnectionParams.CONNECTION_TIMEOUT, Integer.valueOf(connectionTimeout));
    }

    /**
     * Retrieve the connection timeout.
     *
     * @param opts The FileSystem options.
     * @return The connection timeout.
     * @since 2.1
     */
    public int getConnectionTimeout(final FileSystemOptions opts) {
        return getInteger(opts, HttpConnectionParams.CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
    }

    /**
     * The socket timeout.
     *
     * @param opts The FileSystem options.
     * @param soTimeout socket timeout.
     * @since 2.1
     */
    public void setSoTimeout(final FileSystemOptions opts, final int soTimeout) {
        setParam(opts, HttpConnectionParams.SO_TIMEOUT, Integer.valueOf(soTimeout));
    }

    /**
     * Retrieve the socket timeout.
     *
     * @param opts The FileSystemOptions.
     * @return The socket timeout.
     * @since 2.1
     */
    public int getSoTimeout(final FileSystemOptions opts) {
        return getInteger(opts, HttpConnectionParams.SO_TIMEOUT, DEFAULT_SO_TIMEOUT);
    }

    /**
     * Assign the user agent to attach to the outgoing http methods
     *
     * @param userAgent User Agent String
     */
    public void setUserAgent(final FileSystemOptions opts, final String userAgent) {
        setParam(opts, "userAgent", userAgent);
    }

    /**
     * Return the user agent string
     *
     * @return User provided User-Agent string, otherwise default of: Jakarta-Commons-VFS
     */
    public String getUserAgent(final FileSystemOptions opts) {
        final String userAgent = (String) getParam(opts, KEY_USER_AGENT);
        return userAgent != null ? userAgent : DEFAULT_USER_AGENT;
    }

    @Override
    protected Class<? extends FileSystem> getConfigClass() {
        return HttpFileSystem.class;
    }
}
