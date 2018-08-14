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
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.vfs2.FileContentInfoFactory;
import org.apache.commons.vfs2.FileNotFoundException;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.GenericURLFileName;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.protocol.HTTP;

/**
 * A file object backed by Apache HttpComponents HttpClient.
 *
 * @param <FS> An {@link Http4FileSystem} subclass
 */
public class Http4FileObject<FS extends Http4FileSystem> extends AbstractFileObject<FS> {

    private final String urlCharset;

    private final URI internalURI;

    private HttpResponse lastHeadResponse;

    protected Http4FileObject(final AbstractFileName name, final FS fileSystem)
            throws FileSystemException, URISyntaxException {
        this(name, fileSystem, Http4FileSystemConfigBuilder.getInstance());
    }

    protected Http4FileObject(final AbstractFileName name, final FS fileSystem,
            final Http4FileSystemConfigBuilder builder) throws FileSystemException, URISyntaxException {
        super(name, fileSystem);
        final FileSystemOptions fileSystemOptions = fileSystem.getFileSystemOptions();
        urlCharset = builder.getUrlCharset(fileSystemOptions);
        final String pathEncoded = ((GenericURLFileName) name).getPathQueryEncoded(getUrlCharset());
        internalURI = URIUtils.resolve(fileSystem.getInternalBaseURI(), pathEncoded);
    }

    @Override
    protected FileType doGetType() throws Exception {
        final HttpHead headRequest = new HttpHead(getInternalURI());
        lastHeadResponse = executeHttpUriRequest(headRequest);
        final int status = lastHeadResponse.getStatusLine().getStatusCode();

        if (status == HttpStatus.SC_OK
                || status == HttpStatus.SC_METHOD_NOT_ALLOWED /* method is not allowed, but resource exist */) {
            return FileType.FILE;
        } else if (status == HttpStatus.SC_NOT_FOUND || status == HttpStatus.SC_GONE) {
            return FileType.IMAGINARY;
        } else {
            throw new FileSystemException("vfs.provider.http/head.error", getName(), Integer.valueOf(status));
        }
    }

    @Override
    protected long doGetContentSize() throws Exception {
        if (lastHeadResponse == null) {
            return 0L;
        }

        final Header header = lastHeadResponse.getFirstHeader(HTTP.CONTENT_LEN);

        if (header == null) {
            // Assume 0 content-length
            return 0;
        }

        return Long.parseLong(header.getValue());
    }

    @Override
    protected long doGetLastModifiedTime() throws Exception {
        if (lastHeadResponse == null) {
            throw new FileSystemException("vfs.provider.http/last-modified.error", getName());
        }

        final Header header = lastHeadResponse.getFirstHeader("Last-Modified");

        if (header == null) {
            throw new FileSystemException("vfs.provider.http/last-modified.error", getName());
        }

        return DateUtils.parseDate(header.getValue()).getTime();
    }


    @Override
    protected InputStream doGetInputStream() throws Exception {
        final HttpGet getRequest = new HttpGet(getInternalURI());
        final HttpResponse httpResponse = executeHttpUriRequest(getRequest);
        final int status = httpResponse.getStatusLine().getStatusCode();

        if (status == HttpStatus.SC_NOT_FOUND) {
            throw new FileNotFoundException(getName());
        }

        if (status != HttpStatus.SC_OK) {
            throw new FileSystemException("vfs.provider.http/get.error", getName(), Integer.valueOf(status));
        }

        return new MonitoredHttpResponseContentInputStream(httpResponse);
    }

    @Override
    protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception {
        return new Http4RandomAccessContent<>(this, mode);
    }

    @Override
    protected String[] doListChildren() throws Exception {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    protected boolean doIsWriteable() throws Exception {
        return false;
    }

    @Override
    protected FileContentInfoFactory getFileContentInfoFactory() {
        return new Http4FileContentInfoFactory();
    }

    @Override
    protected void doDetach() throws Exception {
        lastHeadResponse = null;
    }

    protected String getUrlCharset() {
        return urlCharset;
    }

    protected URI getInternalURI() throws FileSystemException {
        return internalURI;
    }

    protected HttpResponse executeHttpUriRequest(final HttpUriRequest httpRequest)
            throws ClientProtocolException, IOException {
        final HttpClient httpClient = getAbstractFileSystem().getHttpClient();
        final HttpClientContext httpClientContext = getAbstractFileSystem().getHttpClientContext();
        return httpClient.execute(httpRequest, httpClientContext);
    }

    HttpResponse getLastHeadResponse() {
        return lastHeadResponse;
    }
}
