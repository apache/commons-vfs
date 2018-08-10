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

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * An HTTP4 file system.
 */
public class Http4FileSystem extends AbstractFileSystem {

    private final URI baseURI;
    private final HttpClient httpClient;
    private final HttpClientContext httpClientContext;

    protected Http4FileSystem(FileName rootName, FileSystemOptions fileSystemOptions, HttpClient httpClient,
            HttpClientContext httpClientContext) {
        super(rootName, null, fileSystemOptions);

        final String rootURI = getRootURI();
        final int offset = rootURI.indexOf(':');
        final String scheme = rootURI.substring(0, offset);

        if ("http4s".equals(scheme)) {
            this.baseURI = URI.create("https" + rootURI.substring(offset));
        } else {
            this.baseURI = URI.create("http" + rootURI.substring(offset));
        }

        this.httpClient = httpClient;
        this.httpClientContext = httpClientContext;
    }

    @Override
    protected FileObject createFile(AbstractFileName name) throws Exception {
        return new Http4FileObject<>(name, this);
    }

    @Override
    protected void addCapabilities(Collection<Capability> caps) {
        caps.addAll(Http4FileProvider.capabilities);
    }

    @Override
    protected void doCloseCommunicationLink() {
        if (httpClient instanceof CloseableHttpClient) {
            try {
                ((CloseableHttpClient) httpClient).close();
            } catch (IOException e) {
                throw new RuntimeException("Error closing HttpClient", e);
            }
        }
    }

    protected HttpClient getHttpClient() {
        return httpClient;
    }

    protected HttpClientContext getHttpClientContext() {
        return httpClientContext;
    }

    protected URI getBaseURI() {
        return baseURI;
    }
}
