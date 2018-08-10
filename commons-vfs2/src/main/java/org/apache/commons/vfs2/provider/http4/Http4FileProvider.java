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
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;
import org.apache.http.Header;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;

/**
 * An HTTP provider that uses HttpComponents HttpClient.
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
            authData = UserAuthenticatorUtils.authenticate(fileSystemOptions, AUTHENTICATOR_TYPES);
            httpClientContext = createHttpClientContext(rootName, authData);
            httpClient = createHttpClient(rootName, fileSystemOptions);
        } finally {
            UserAuthenticatorUtils.cleanup(authData);
        }

        return new Http4FileSystem(rootName, fileSystemOptions, httpClient, httpClientContext);
    }

    protected HttpClient createHttpClient(final GenericFileName rootName, final FileSystemOptions fileSystemOptions) {
        return createHttpClient(Http4FileSystemConfigBuilder.getInstance(), rootName, fileSystemOptions);
    }

    protected HttpClient createHttpClient(final Http4FileSystemConfigBuilder builder, final GenericFileName rootName,
            final FileSystemOptions fileSystemOptions) {

        final List<Header> defaultHeaders = new ArrayList<>();
        defaultHeaders.add(new BasicHeader("User-Agent", builder.getUserAgent(fileSystemOptions)));

        final HttpClientBuilder httpClientBuilder =
                HttpClients.custom()
                .setConnectionManager(createConnectionManager(builder, fileSystemOptions))
                .setDefaultRequestConfig(createDefaultRequestConfig(builder, fileSystemOptions))
                .setDefaultHeaders(defaultHeaders);

        if (builder.getFollowRedirect(fileSystemOptions)) {
            httpClientBuilder.disableRedirectHandling();
        }

        return httpClientBuilder.build();
    }

    protected HttpClientContext createHttpClientContext(final GenericFileName rootName,
            final UserAuthenticationData authData) {
        final String username = UserAuthenticatorUtils.toString(UserAuthenticatorUtils.getData(authData,
                UserAuthenticationData.USERNAME, UserAuthenticatorUtils.toChar(rootName.getUserName())));
        final String password = UserAuthenticatorUtils.toString(UserAuthenticatorUtils.getData(authData,
                UserAuthenticationData.PASSWORD, UserAuthenticatorUtils.toChar(rootName.getPassword())));

        final CredentialsProvider credsProvider = new BasicCredentialsProvider();

        if (username != null && !username.isEmpty()) {
            credsProvider.setCredentials(new AuthScope(rootName.getHostName(), rootName.getPort()),
                    new UsernamePasswordCredentials(username, password));
        }

        final HttpClientContext clientContext = HttpClientContext.create();
        clientContext.setCredentialsProvider(credsProvider);

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
}
