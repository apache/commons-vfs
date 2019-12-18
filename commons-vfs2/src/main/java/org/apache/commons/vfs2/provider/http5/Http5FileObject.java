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
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.utils.DateUtils;
import org.apache.hc.client5.http.utils.URIUtils;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;

/**
 * A file object backed by Apache HttpComponents HttpClient v5.
 *
 * @param <FS> An {@link Http5FileSystem} subclass
 *
 * @since 2.5.0
 */
public class Http5FileObject<FS extends Http5FileSystem> extends AbstractFileObject<FS> {

    /**
     * URL charset string.
     */
    private final String urlCharset;

    /**
     * Internal URI mapped to this {@code FileObject}.
     * For example, the internal URI of {@code http4://example.com/a.txt} is {@code http://example.com/a.txt}.
     */
    private final URI internalURI;

    /**
     * The last executed HEAD {@code HttpResponse} object.
     */
    private HttpResponse lastHeadResponse;

    /**
     * Construct {@code Http4FileObject}.
     *
     * @param name file name
     * @param fileSystem file system
     * @throws FileSystemException if any error occurs
     * @throws URISyntaxException if given file name cannot be converted to a URI due to URI syntax error
     */
    protected Http5FileObject(final AbstractFileName name, final FS fileSystem)
            throws FileSystemException, URISyntaxException {
        this(name, fileSystem, Http5FileSystemConfigBuilder.getInstance());
    }

    /**
     * Construct {@code Http4FileObject}.
     *
     * @param name file name
     * @param fileSystem file system
     * @param builder {@code Http4FileSystemConfigBuilder} object
     * @throws FileSystemException if any error occurs
     * @throws URISyntaxException if given file name cannot be converted to a URI due to URI syntax error
     */
    protected Http5FileObject(final AbstractFileName name, final FS fileSystem,
            final Http5FileSystemConfigBuilder builder) throws FileSystemException, URISyntaxException {
        super(name, fileSystem);
        final FileSystemOptions fileSystemOptions = fileSystem.getFileSystemOptions();
        urlCharset = builder.getUrlCharset(fileSystemOptions);
        final String pathEncoded = ((GenericURLFileName) name).getPathQueryEncoded(getUrlCharset());
        internalURI = URIUtils.resolve(fileSystem.getInternalBaseURI(), pathEncoded);
    }

    @Override
    protected FileType doGetType() throws Exception {
        lastHeadResponse = executeHttpUriRequest(new HttpHead(getInternalURI()));
        final int status = lastHeadResponse.getCode();

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

        final Header header = lastHeadResponse.getFirstHeader(HttpHeaders.CONTENT_LENGTH);

        if (header == null) {
            // Assume 0 content-length
            return 0;
        }

        return Long.parseLong(header.getValue());
    }

    @Override
    protected long doGetLastModifiedTime() throws Exception {
        FileSystemException.requireNonNull(lastHeadResponse, "vfs.provider.http/last-modified.error", getName());

        final Header header = lastHeadResponse.getFirstHeader("Last-Modified");

        FileSystemException.requireNonNull(header, "vfs.provider.http/last-modified.error", getName());

        return DateUtils.parseDate(header.getValue()).getTime();
    }


    @Override
    protected InputStream doGetInputStream(final int bufferSize) throws Exception {
        final HttpGet getRequest = new HttpGet(getInternalURI());
        final ClassicHttpResponse httpResponse = executeHttpUriRequest(getRequest);
        final int status = httpResponse.getCode();

        if (status == HttpStatus.SC_NOT_FOUND) {
            throw new FileNotFoundException(getName());
        }

        if (status != HttpStatus.SC_OK) {
            throw new FileSystemException("vfs.provider.http/get.error", getName(), Integer.valueOf(status));
        }

        return new MonitoredHttpResponseContentInputStream(httpResponse, bufferSize);
    }

    @Override
    protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception {
        return new Http5RandomAccessContent<>(this, mode);
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
        return new Http5FileContentInfoFactory();
    }

    @Override
    protected void doDetach() throws Exception {
        lastHeadResponse = null;
    }

    /**
     * Return URL charset string.
     * @return URL charset string
     */
    protected String getUrlCharset() {
        return urlCharset;
    }

    /**
     * Return the internal {@code URI} object mapped to this file object.
     *
     * @return the internal {@code URI} object mapped to this file object
     * @throws FileSystemException if any error occurs
     */
    protected URI getInternalURI() throws FileSystemException {
        return internalURI;
    }

    /**
     * Return the last executed HEAD {@code HttpResponse} object.
     *
     * @return the last executed HEAD {@code HttpResponse} object
     * @throws IOException if IO error occurs
     */
    HttpResponse getLastHeadResponse() throws IOException {
        if (lastHeadResponse != null) {
            return lastHeadResponse;
        }

        return executeHttpUriRequest(new HttpHead(getInternalURI()));
    }

    /**
     * Execute the request using the given {@code httpRequest} and return a {@code ClassicHttpResponse} from the execution.
     *
     * @param httpRequest {@code HttpUriRequest} object
     * @return {@code ClassicHttpResponse} from the execution
     * @throws IOException if IO error occurs
     */
    protected ClassicHttpResponse executeHttpUriRequest(final HttpUriRequest httpRequest) throws IOException {
        final CloseableHttpClient httpClient = (CloseableHttpClient) getAbstractFileSystem().getHttpClient();
        final HttpClientContext httpClientContext = getAbstractFileSystem().getHttpClientContext();
        return httpClient.execute(httpRequest, httpClientContext);
    }

}
