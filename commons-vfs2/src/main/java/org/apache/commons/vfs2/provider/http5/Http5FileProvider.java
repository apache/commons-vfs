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
import java.util.concurrent.TimeUnit;

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
import org.apache.hc.client5.http.auth.AuthCache;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.auth.BasicAuthCache;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.ssl.SSLContextBuilder;

/**
 * {@code FileProvider} implementation using HttpComponents HttpClient v5 library.
 *
 * @since 2.5.0
 */
public class Http5FileProvider extends AbstractOriginatingFileProvider {

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
    public Http5FileProvider() {
        super();
        setFileNameParser(Http5FileNameParser.getInstance());
    }

    @Override
    public FileSystemConfigBuilder getConfigBuilder() {
        return Http5FileSystemConfigBuilder.getInstance();
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
            final Http5FileSystemConfigBuilder builder = Http5FileSystemConfigBuilder.getInstance();
            authData = UserAuthenticatorUtils.authenticate(fileSystemOptions, AUTHENTICATOR_TYPES);
            httpClientContext = createHttpClientContext(builder, rootName, fileSystemOptions, authData);
            httpClient = createHttpClient(builder, rootName, fileSystemOptions);
        } finally {
            UserAuthenticatorUtils.cleanup(authData);
        }

        return new Http5FileSystem(rootName, fileSystemOptions, httpClient, httpClientContext);
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
    protected HttpClient createHttpClient(final Http5FileSystemConfigBuilder builder, final GenericFileName rootName,
            final FileSystemOptions fileSystemOptions) throws FileSystemException {
        return createHttpClientBuilder(builder, rootName, fileSystemOptions).build();
    }

    /**
     * Create an {@link HttpClientBuilder} object. Invoked by {@link #createHttpClient(Http5FileSystemConfigBuilder, GenericFileName, FileSystemOptions)}.
     *
     * @param builder Configuration options builder for HTTP4 provider
     * @param rootName The root path
     * @param fileSystemOptions The FileSystem options
     * @return an {@link HttpClientBuilder} object
     * @throws FileSystemException if an error occurs
     */
    protected HttpClientBuilder createHttpClientBuilder(final Http5FileSystemConfigBuilder builder, final GenericFileName rootName,
            final FileSystemOptions fileSystemOptions) throws FileSystemException {
        final List<Header> defaultHeaders = new ArrayList<>();
        defaultHeaders.add(new BasicHeader(HttpHeaders.USER_AGENT, builder.getUserAgent(fileSystemOptions)));

        final ConnectionReuseStrategy connectionReuseStrategy = builder.isKeepAlive(fileSystemOptions)
                ? DefaultConnectionReuseStrategy.INSTANCE
                : (request, response, context) -> false;

        final HttpClientBuilder httpClientBuilder =
                HttpClients.custom()
                .setRoutePlanner(createHttpRoutePlanner(builder, fileSystemOptions))
                .setConnectionManager(createConnectionManager(builder, fileSystemOptions))
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
     * Create {@link SSLContext} for HttpClient. Invoked by {@link #createHttpClientBuilder(Http5FileSystemConfigBuilder, GenericFileName, FileSystemOptions)}.
     *
     * @param builder Configuration options builder for HTTP4 provider
     * @param fileSystemOptions The FileSystem options
     * @return a {@link SSLContext} for HttpClient
     * @throws FileSystemException if an error occurs
     */
    protected SSLContext createSSLContext(final Http5FileSystemConfigBuilder builder,
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
    protected HttpClientContext createHttpClientContext(final Http5FileSystemConfigBuilder builder,
            final GenericFileName rootName, final FileSystemOptions fileSystemOptions,
            final UserAuthenticationData authData) throws FileSystemException {

        final HttpClientContext clientContext = HttpClientContext.create();
        final BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        clientContext.setCredentialsProvider(credsProvider);

        final String username = UserAuthenticatorUtils.toString(UserAuthenticatorUtils.getData(authData,
                UserAuthenticationData.USERNAME, UserAuthenticatorUtils.toChar(rootName.getUserName())));
        final char[] password = UserAuthenticatorUtils.getData(authData,
                UserAuthenticationData.PASSWORD, UserAuthenticatorUtils.toChar(rootName.getPassword()));

        if (username != null && !username.isEmpty()) {
            // -1 for any port
            credsProvider.setCredentials(new AuthScope(rootName.getHostName(), -1),
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
                            UserAuthenticatorUtils.getData(authData, UserAuthenticationData.PASSWORD, null));

                    // -1 for any port
                    credsProvider.setCredentials(new AuthScope(proxyHost.getHostName(), -1),
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

    private HttpClientConnectionManager createConnectionManager(final Http5FileSystemConfigBuilder builder,
            final FileSystemOptions fileSystemOptions) throws FileSystemException {

        final SocketConfig socketConfig =
                SocketConfig
                .custom()
                .setSoTimeout(builder.getSoTimeout(fileSystemOptions), TimeUnit.MILLISECONDS)
                .build();

        final String[] tlsVersions = builder.getTlsVersions(fileSystemOptions).split("\\s*,\\s*");
        final TLS[] tlsArray = new TLS[tlsVersions.length];

        for (int i = 0; i < tlsVersions.length; i++) {
            tlsArray[i] = TLS.valueOf(tlsVersions[i]);
        }

        final SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactoryBuilder.create()
                .setSslContext(createSSLContext(builder, fileSystemOptions))
                .setHostnameVerifier(createHostnameVerifier(builder, fileSystemOptions))
                .setTlsVersions(tlsArray)
                .build();

        return PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .setMaxConnTotal(builder.getMaxTotalConnections(fileSystemOptions))
                .setMaxConnPerRoute(builder.getMaxConnectionsPerHost(fileSystemOptions))
                .setDefaultSocketConfig(socketConfig)
                .build();
    }

    private RequestConfig createDefaultRequestConfig(final Http5FileSystemConfigBuilder builder,
            final FileSystemOptions fileSystemOptions) {
        return RequestConfig.custom()
                .setConnectTimeout(builder.getConnectionTimeout(fileSystemOptions), TimeUnit.MILLISECONDS)
                .build();
    }

    private HttpRoutePlanner createHttpRoutePlanner(final Http5FileSystemConfigBuilder builder,
            final FileSystemOptions fileSystemOptions) {
        final HttpHost proxyHost = getProxyHttpHost(builder, fileSystemOptions);

        if (proxyHost != null) {
            return new DefaultProxyRoutePlanner(proxyHost);
        }

        return new SystemDefaultRoutePlanner(ProxySelector.getDefault());
    }

    private HttpHost getProxyHttpHost(final Http5FileSystemConfigBuilder builder,
            final FileSystemOptions fileSystemOptions) {
        final String proxyHost = builder.getProxyHost(fileSystemOptions);
        final int proxyPort = builder.getProxyPort(fileSystemOptions);

        if (proxyHost != null && proxyHost.length() > 0 && proxyPort > 0) {
            return new HttpHost(proxyHost, proxyPort);
        }

        return null;
    }

    private CookieStore createDefaultCookieStore(final Http5FileSystemConfigBuilder builder,
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

    private HostnameVerifier createHostnameVerifier(final Http5FileSystemConfigBuilder builder,
            final FileSystemOptions fileSystemOptions) throws FileSystemException {
        if (!builder.isHostnameVerificationEnabled(fileSystemOptions)) {
            return NoopHostnameVerifier.INSTANCE;
        }

        return new DefaultHostnameVerifier();
    }

}
