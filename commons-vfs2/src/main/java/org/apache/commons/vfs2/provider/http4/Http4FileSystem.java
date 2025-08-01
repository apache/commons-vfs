/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.provider.http4;

import java.io.Closeable;
import java.net.URI;
import java.util.Collection;

import org.apache.commons.io.function.Uncheck;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.HttpClientContext;

/**
 * http4 file system.
 *
 * @since 2.3
 * @deprecated Use {@link org.apache.commons.vfs2.provider.http5}.
 */
@Deprecated
public class Http4FileSystem extends AbstractFileSystem {

    /**
     * Internal base URI of this file system.
     */
    private final URI internalBaseURI;

    /**
     * Internal {@code HttpClient} instance of this file system.
     */
    private final HttpClient httpClient;

    /**
     * Internal {@code HttpClientContext} instance of this file system.
     */
    private final HttpClientContext httpClientContext;

    /**
     * Constructs a new instance.
     *
     * @param rootName root base name
     * @param fileSystemOptions file system options
     * @param httpClient {@link HttpClient} instance
     * @param httpClientContext {@link HttpClientContext} instance
     */
    protected Http4FileSystem(final FileName rootName, final FileSystemOptions fileSystemOptions, final HttpClient httpClient,
            final HttpClientContext httpClientContext) {
        super(rootName, null, fileSystemOptions);

        final String rootURI = getRootURI();
        final int offset = rootURI.indexOf(':');
        final char lastCharOfScheme = offset > 0 ? rootURI.charAt(offset - 1) : 0;

        // if scheme is 'http*s' or 'HTTP*S', then the internal base URI should be 'https'. 'http' otherwise.
        final String scheme  = lastCharOfScheme == 's' || lastCharOfScheme == 'S' ? "https" : "http";
        this.internalBaseURI = URI.create(scheme + rootURI.substring(offset));
        this.httpClient = httpClient;
        this.httpClientContext = httpClientContext;
    }

    @Override
    protected void addCapabilities(final Collection<Capability> caps) {
        caps.addAll(Http4FileProvider.CAPABILITIES);
    }

    @Override
    protected FileObject createFile(final AbstractFileName name) throws Exception {
        return new Http4FileObject<>(name, this);
    }

    @Override
    protected void doCloseCommunicationLink() {
        if (httpClient instanceof Closeable) {
            Uncheck.run(() -> ((Closeable) httpClient).close(), () -> "Error closing HttpClient");
        }
    }

    /**
     * Gets the internal {@link HttpClient} instance.
     *
     * @return the internal {@link HttpClient} instance
     */
    protected HttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Gets the internal {@link HttpClientContext} instance.
     *
     * @return the internal {@link HttpClientContext} instance
     */
    protected HttpClientContext getHttpClientContext() {
        return httpClientContext;
    }

    /**
     * Gets the internal base {@code URI} instance.
     *
     * @return the internal base {@code URI} instance
     */
    protected URI getInternalBaseURI() {
        return internalBaseURI;
    }
}
