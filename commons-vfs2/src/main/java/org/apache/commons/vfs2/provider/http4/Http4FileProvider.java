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

import java.io.File;
import java.io.IOException;
import java.net.ProxySelector;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

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
import org.apache.http.ConnectionReuseStrategy;
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
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.NoConnectionReuseStrategy;
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
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;

/**
 * {@code FileProvider} implementation using HttpComponents HttpClient library.
 *
 * @since 2.3
 */
public class Http4FileProvider extends AbstractOriginatingFileProvider {

    /** Authenticator information. */
    static final UserAuthenticationData.Type[] AUTHENTICATOR_TYPES =
            new UserAuthenticationData.Type[] {
                    UserAuthenticationData.USERNAME,
                    UserAuthenticationData.PASSWORD
                    };

    /** FileProvider capabilities */
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
    protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions)
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
     * Create an {@link HttpClient} object for an http4 file system.
     *
     * @param builder Configuration options builder for http4 provider
     * @param rootName The root path
     * @param fileSystemOptions The file system options
     * @return an {@link HttpClient} object
     * @throws FileSystemException if an error occurs.
     */
    protected HttpClient createHttpClient(final Http4FileSystemConfigBuilder builder, final GenericFileName rootName,
            final FileSystemOptions fileSystemOptions) throws FileSystemException {
        return createHttpClientBuilder(builder, rootName, fileSystemOptions).build();
    }

    /**
     * Create an {@link HttpClientBuilder} object. Invoked by {@link #createHttpClient(Http4FileSystemConfigBuilder, GenericFileName, FileSystemOptions)}.
     *
     * @param builder Configuration options builder for HTTP4 provider
     * @param rootName The root path
     * @param fileSystemOptions The FileSystem options
     * @return an {@link HttpClientBuilder} object
     * @throws FileSystemException if an error occurs
     */
    protected HttpClientBuilder createHttpClientBuilder(final Http4FileSystemConfigBuilder builder, final GenericFileName rootName,
            final FileSystemOptions fileSystemOptions) throws FileSystemException {
        final List<Header> defaultHeaders = new ArrayList<>();
        defaultHeaders.add(new BasicHeader(HTTP.USER_AGENT, builder.getUserAgent(fileSystemOptions)));

        final ConnectionReuseStrategy connectionReuseStrategy = builder.isKeepAlive(fileSystemOptions)
                ? DefaultConnectionReuseStrategy.INSTANCE
                : NoConnectionReuseStrategy.INSTANCE;

        final HttpClientBuilder httpClientBuilder =
                HttpClients.custom()
                .setRoutePlanner(createHttpRoutePlanner(builder, fileSystemOptions))
                .setConnectionManager(createConnectionManager(builder, fileSystemOptions))
                .setSSLContext(createSSLContext(builder, fileSystemOptions))
                .setSSLHostnameVerifier(createHostnameVerifier(builder, fileSystemOptions))
                .setConnectionReuseStrategy(connectionReuseStrategy)
                .setDefaultRequestConfig(createDefaultRequestConfig(builder, fileSystemOptions))
                .setDefaultHeaders(defaultHeaders)
                .setDefaultCookieStore(createDefaultCookieStore(builder, fileSystemOptions));

        if (!builder.getFollowRedirect(fileSystemOptions)) {
            httpClientBuilder.disableRedirectHandling();
        }

        return httpClientBuilder;
    }

    /**
     * Create {@link SSLContext} for HttpClient. Invoked by {@link #createHttpClientBuilder(Http4FileSystemConfigBuilder, GenericFileName, FileSystemOptions)}.
     *
     * @param builder Configuration options builder for HTTP4 provider
     * @param fileSystemOptions The FileSystem options
     * @return a {@link SSLContext} for HttpClient
     * @throws FileSystemException if an error occurs
     */
    protected SSLContext createSSLContext(final Http4FileSystemConfigBuilder builder,
            final FileSystemOptions fileSystemOptions) throws FileSystemException {
        try {
            final SSLContextBuilder sslContextBuilder = new SSLContextBuilder();

            File keystoreFileObject = null;
            final String keystoreFile = builder.getKeyStoreFile(fileSystemOptions);

            if (keystoreFile != null && !keystoreFile.isEmpty()) {
                keystoreFileObject = new File(keystoreFile);
            }

            if (keystoreFileObject != null && keystoreFileObject.exists()) {
                final String keystorePass = builder.getKeyStorePass(fileSystemOptions);
                final char[] keystorePassChars = (keystorePass != null) ? keystorePass.toCharArray() : null;
                sslContextBuilder.loadTrustMaterial(keystoreFileObject, keystorePassChars, TrustAllStrategy.INSTANCE);
            } else {
                sslContextBuilder.loadTrustMaterial(TrustAllStrategy.INSTANCE);
            }

            return sslContextBuilder.build();
        } catch (final KeyStoreException e) {
            throw new FileSystemException("Keystore error. " + e.getMessage(), e);
        } catch (final KeyManagementException e) {
            throw new FileSystemException("Cannot retrieve keys. " + e.getMessage(), e);
        } catch (final NoSuchAlgorithmException e) {
            throw new FileSystemException("Algorithm error. " + e.getMessage(), e);
        } catch (final CertificateException e) {
            throw new FileSystemException("Certificate error. " + e.getMessage(), e);
        } catch (final IOException e) {
            throw new FileSystemException("Cannot open key file. " + e.getMessage(), e);
        }
    }

    /**
     * Create an {@link HttpClientContext} object for an http4 file system.
     *
     * @param builder Configuration options builder for http4 provider
     * @param rootName The root path
     * @param fileSystemOptions The FileSystem options
     * @param authData The {@code UserAuthentiationData} object
     * @return an {@link HttpClientContext} object
     * @throws FileSystemException if an error occurs
     */
    protected HttpClientContext createHttpClientContext(final Http4FileSystemConfigBuilder builder,
            final GenericFileName rootName, final FileSystemOptions fileSystemOptions,
            final UserAuthenticationData authData) throws FileSystemException {

        final HttpClientContext clientContext = HttpClientContext.create();
        final CredentialsProvider credsProvider = new BasicCredentialsProvider();
        clientContext.setCredentialsProvider(credsProvider);

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

                if (builder.isPreemptiveAuth(fileSystemOptions)) {
                    final AuthCache authCache = new BasicAuthCache();
                    final BasicScheme basicAuth = new BasicScheme();
                    authCache.put(proxyHost, basicAuth);
                    clientContext.setAuthCache(authCache);
                }
            }
        }

        return clientContext;
    }

    private HttpClientConnectionManager createConnectionManager(final Http4FileSystemConfigBuilder builder,
            final FileSystemOptions fileSystemOptions) throws FileSystemException {
        final PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(builder.getMaxTotalConnections(fileSystemOptions));
        connManager.setDefaultMaxPerRoute(builder.getMaxConnectionsPerHost(fileSystemOptions));

        final SocketConfig socketConfig =
                SocketConfig
                .custom()
                .setSoTimeout(builder.getSoTimeout(fileSystemOptions))
                .build();

        connManager.setDefaultSocketConfig(socketConfig);

        return connManager;
    }

    private RequestConfig createDefaultRequestConfig(final Http4FileSystemConfigBuilder builder,
            final FileSystemOptions fileSystemOptions) {
        return RequestConfig.custom()
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
            for (final Cookie cookie : cookies) {
                cookieStore.addCookie(cookie);
            }
        }

        return cookieStore;
    }

    private HostnameVerifier createHostnameVerifier(final Http4FileSystemConfigBuilder builder,
            final FileSystemOptions fileSystemOptions) throws FileSystemException {
        if (!builder.isHostnameVerificationEnabled(fileSystemOptions)) {
            return NoopHostnameVerifier.INSTANCE;
        }

        return new DefaultHostnameVerifier();
    }

}
