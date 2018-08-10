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

import java.net.ProxySelector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.UserAuthenticator;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.message.BasicHeader;

/**
 * HTTP4 provider that uses HttpComponents HttpClient.
 */
public class Http4FileProvider extends AbstractOriginatingFileProvider {

    /** Authenticator information. */
    static final UserAuthenticationData.Type[] AUTHENTICATOR_TYPES =
            new UserAuthenticationData.Type[] {
                    UserAuthenticationData.USERNAME,
                    UserAuthenticationData.PASSWORD
                    };

    static final Collection<Capability> capabilities =
            Collections.unmodifiableCollection(
                    Arrays.asList(
                            Capability.GET_TYPE,
                            Capability.READ_CONTENT,
                            Capability.URI,
                            Capability.GET_LAST_MODIFIED,
                            Capability.ATTRIBUTES,
                            Capability.RANDOM_ACCESS_READ,
                            Capability.DIRECTORY_READ_CONTENT
                            )
                    );

    /**
     * Constructs a new provider.
     */
    public Http4FileProvider() {
        super();
        setFileNameParser(Http4FileNameParser.getInstance());
    }

    @Override
    public FileSystemConfigBuilder getConfigBuilder() {
        return Http4FileSystemConfigBuilder.getInstance();
    }

    @Override
    public Collection<Capability> getCapabilities() {
        return capabilities;
    }

    @Override
    protected FileSystem doCreateFileSystem(FileName name, FileSystemOptions fileSystemOptions)
            throws FileSystemException {
        final GenericFileName rootName = (GenericFileName) name;

        UserAuthenticationData authData = null;
        HttpClient httpClient = null;
        HttpClientContext httpClientContext = null;

        try {
            final Http4FileSystemConfigBuilder builder = Http4FileSystemConfigBuilder.getInstance();
            authData = UserAuthenticatorUtils.authenticate(fileSystemOptions, AUTHENTICATOR_TYPES);
            httpClientContext = createHttpClientContext(builder, rootName, fileSystemOptions, authData);
            httpClient = createHttpClient(builder, rootName, fileSystemOptions);
        } finally {
            UserAuthenticatorUtils.cleanup(authData);
        }

        return new Http4FileSystem(rootName, fileSystemOptions, httpClient, httpClientContext);
    }

    /**
     * Create an {@link HttpClient} object.
     * @param builder Configuration options builder for HTTP4 provider
     * @param rootName The root path.
     * @param fileSystemOptions The FileSystem options.
     * @return an {@link HttpClient} object
     */
    protected HttpClient createHttpClient(final Http4FileSystemConfigBuilder builder, final GenericFileName rootName,
            final FileSystemOptions fileSystemOptions) {

        final List<Header> defaultHeaders = new ArrayList<>();
        defaultHeaders.add(new BasicHeader("User-Agent", builder.getUserAgent(fileSystemOptions)));

        final HttpClientBuilder httpClientBuilder =
                HttpClients.custom()
                .setRoutePlanner(createHttpRoutePlanner(builder, fileSystemOptions))
                .setConnectionManager(createConnectionManager(builder, fileSystemOptions))
                .setDefaultRequestConfig(createDefaultRequestConfig(builder, fileSystemOptions))
                .setDefaultHeaders(defaultHeaders)
                .setDefaultCookieStore(createDefaultCookieStore(builder, fileSystemOptions));

        if (builder.getFollowRedirect(fileSystemOptions)) {
            httpClientBuilder.disableRedirectHandling();
        }

        return httpClientBuilder.build();
    }

    /**
     * Create an {@link HttpClientContext} object.
     * @param builder Configuration options builder for HTTP4 provider
     * @param rootName The root path.
     * @param fileSystemOptions The FileSystem options.
     * @param authData The UserAuthentiationData
     * @return an {@link HttpClientContext} object
     */
    protected HttpClientContext createHttpClientContext(final Http4FileSystemConfigBuilder builder,
            final GenericFileName rootName, final FileSystemOptions fileSystemOptions,
            final UserAuthenticationData authData) {
        final CredentialsProvider credsProvider = new BasicCredentialsProvider();

        final String username = UserAuthenticatorUtils.toString(UserAuthenticatorUtils.getData(authData,
                UserAuthenticationData.USERNAME, UserAuthenticatorUtils.toChar(rootName.getUserName())));
        final String password = UserAuthenticatorUtils.toString(UserAuthenticatorUtils.getData(authData,
                UserAuthenticationData.PASSWORD, UserAuthenticatorUtils.toChar(rootName.getPassword())));

        if (username != null && !username.isEmpty()) {
            credsProvider.setCredentials(new AuthScope(rootName.getHostName(), AuthScope.ANY_PORT),
                    new UsernamePasswordCredentials(username, password));
        }

        final HttpHost proxyHost = getProxyHttpHost(builder, fileSystemOptions);

        if (proxyHost != null) {
            final UserAuthenticator proxyAuth = builder.getProxyAuthenticator(fileSystemOptions);

            if (proxyAuth != null) {
                final UserAuthenticationData proxyAuthData = UserAuthenticatorUtils.authenticate(proxyAuth,
                        new UserAuthenticationData.Type[] { UserAuthenticationData.USERNAME,
                                UserAuthenticationData.PASSWORD });

                if (proxyAuthData != null) {
                    final UsernamePasswordCredentials proxyCreds = new UsernamePasswordCredentials(
                            UserAuthenticatorUtils.toString(
                                    UserAuthenticatorUtils.getData(authData, UserAuthenticationData.USERNAME, null)),
                            UserAuthenticatorUtils.toString(
                                    UserAuthenticatorUtils.getData(authData, UserAuthenticationData.PASSWORD, null)));

                    credsProvider.setCredentials(new AuthScope(proxyHost.getHostName(), AuthScope.ANY_PORT),
                            proxyCreds);
                }
            }
        }

        final HttpClientContext clientContext = HttpClientContext.create();
        clientContext.setCredentialsProvider(credsProvider);

        if (builder.isPreemptiveAuth(fileSystemOptions)) {
            // Create AuthCache instance
            final AuthCache authCache = new BasicAuthCache();
            // Generate BASIC scheme object and add it to the local auth cache
            final BasicScheme basicAuth = new BasicScheme();
            authCache.put(proxyHost, basicAuth);
            // Add AuthCache to the execution context
            clientContext.setAuthCache(authCache);
        }

        return clientContext;
    }

    private HttpClientConnectionManager createConnectionManager(final Http4FileSystemConfigBuilder builder,
            final FileSystemOptions fileSystemOptions) {
        final PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(builder.getMaxTotalConnections(fileSystemOptions));
        connManager.setDefaultMaxPerRoute(builder.getMaxConnectionsPerRoute(fileSystemOptions));
        return connManager;
    }

    private RequestConfig createDefaultRequestConfig(final Http4FileSystemConfigBuilder builder,
            final FileSystemOptions fileSystemOptions) {
        return RequestConfig.custom()
                .setSocketTimeout(builder.getSoTimeout(fileSystemOptions))
                .setConnectTimeout(builder.getConnectionTimeout(fileSystemOptions))
                .build();
    }

    private HttpRoutePlanner createHttpRoutePlanner(final Http4FileSystemConfigBuilder builder,
            final FileSystemOptions fileSystemOptions) {
        final HttpHost proxyHost = getProxyHttpHost(builder, fileSystemOptions);

        if (proxyHost != null) {
            return new DefaultProxyRoutePlanner(proxyHost);
        }

        return new SystemDefaultRoutePlanner(ProxySelector.getDefault());
    }

    private HttpHost getProxyHttpHost(final Http4FileSystemConfigBuilder builder,
            final FileSystemOptions fileSystemOptions) {
        final String proxyHost = builder.getProxyHost(fileSystemOptions);
        final int proxyPort = builder.getProxyPort(fileSystemOptions);

        if (proxyHost != null && proxyHost.length() > 0 && proxyPort > 0) {
            return new HttpHost(proxyHost, proxyPort);
        }

        return null;
    }

    private CookieStore createDefaultCookieStore(final Http4FileSystemConfigBuilder builder,
            final FileSystemOptions fileSystemOptions) {
        final CookieStore cookieStore = new BasicCookieStore();
        final Cookie[] cookies = builder.getCookies(fileSystemOptions);

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookieStore.addCookie(cookie);
            }
        }

        return cookieStore;
    }
}
