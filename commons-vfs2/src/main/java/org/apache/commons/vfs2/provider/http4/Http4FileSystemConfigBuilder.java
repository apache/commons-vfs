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
package org.apache.commons.vfs2.provider.http4;

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticator;
import org.apache.http.cookie.Cookie;

/**
 * Configuration options builder utility for HTTP4 provider.
 */
public class Http4FileSystemConfigBuilder extends FileSystemConfigBuilder {

    private static final Http4FileSystemConfigBuilder BUILDER = new Http4FileSystemConfigBuilder();

    private static final String MAX_TOTAL_CONNECTIONS = "http.connection-manager.max-total";

    private static final String MAX_ROUTE_CONNECTIONS = "http.connection-manager.max-per-route";

    private static final String CONNECTION_TIMEOUT = "http.connection.timeout";

    private static final String SO_TIMEOUT = "http.socket.timeout";

    private static final String KEEP_ALIVE = "http.keepAlive";

    private static final String HOSTNAME_VERIFICATION_ENABLED = "http.hostname-verification.enabled";

    private static final String KEY_FOLLOW_REDIRECT = "followRedirect";

    private static final String KEY_USER_AGENT = "userAgent";

    private static final String KEY_PREEMPTIVE_AUTHENTICATION = "preemptiveAuth";

    private static final int DEFAULT_MAX_ROUTE_CONNECTIONS = 5;

    private static final int DEFAULT_MAX_CONNECTIONS = 50;

    private static final int DEFAULT_CONNECTION_TIMEOUT = 0;

    private static final int DEFAULT_SO_TIMEOUT = 0;

    private static final boolean DEFAULT_KEEP_ALIVE = true;

    private static final boolean DEFAULT_FOLLOW_REDIRECT = true;

    private static final String DEFAULT_USER_AGENT = "Apache-Commons-VFS";

    private static final boolean DEFAULT_HOSTNAME_VERIFICATION_ENABLED = true;

    /**
     * Creates new config builder.
     *
     * @param prefix String for properties of this file system.
     */
    protected Http4FileSystemConfigBuilder(final String prefix) {
        super(prefix);
    }

    private Http4FileSystemConfigBuilder() {
        super("http4.");
    }

    /**
     * Gets the singleton builder.
     *
     * @return the singleton builder.
     */
    public static Http4FileSystemConfigBuilder getInstance() {
        return BUILDER;
    }

    /**
     * Sets the charset used for url encoding.<br>
     *
     * @param opts The FileSystem options.
     * @param chaset the chaset
     */
    public void setUrlCharset(final FileSystemOptions opts, final String chaset) {
        setParam(opts, "urlCharset", chaset);
    }

    /**
     * Sets the charset used for url encoding.<br>
     *
     * @param opts The FileSystem options.
     * @return the chaset
     */
    public String getUrlCharset(final FileSystemOptions opts) {
        return getString(opts, "urlCharset");
    }

    /**
     * Sets the proxy to use for http connection.<br>
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
     * Sets the maximum number of connections allowed to any route.
     *
     * @param opts The FileSystem options.
     * @param maxRouteConnections The maximum number of connections to a route.
     */
    public void setMaxConnectionsPerRoute(final FileSystemOptions opts, final int maxRouteConnections) {
        setParam(opts, MAX_ROUTE_CONNECTIONS, Integer.valueOf(maxRouteConnections));
    }

    /**
     * Gets the maximum number of connections allowed per route.
     *
     * @param opts The FileSystemOptions.
     * @return The maximum number of connections allowed per route.
     */
    public int getMaxConnectionsPerRoute(final FileSystemOptions opts) {
        return getInteger(opts, MAX_ROUTE_CONNECTIONS, DEFAULT_MAX_ROUTE_CONNECTIONS);
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
     */
    public void setKeepAlive(final FileSystemOptions opts, boolean keepAlive) {
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
     * Sets if the hostname should be verified in SSL context.
     *
     * @param opts The FileSystemOptions.
     * @param hostnameVerificationEnabled whether hostname should be verified
     */
    public void setHostnameVerificationEnabled(final FileSystemOptions opts, boolean hostnameVerificationEnabled) {
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

    @Override
    protected Class<? extends FileSystem> getConfigClass() {
        return Http4FileSystem.class;
    }
}
