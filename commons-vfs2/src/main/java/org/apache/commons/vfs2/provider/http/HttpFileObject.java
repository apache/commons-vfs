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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.vfs2.FileContentInfoFactory;
import org.apache.commons.vfs2.FileNotFoundException;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.URLFileName;
import org.apache.commons.vfs2.util.MonitorInputStream;
import org.apache.commons.vfs2.util.RandomAccessMode;

/**
 * A file object backed by Apache Commons HttpClient.
 * <p>
 * TODO - status codes.
 *
 * @param <FS> An {@link HttpFileSystem} subclass
 */
public class HttpFileObject<FS extends HttpFileSystem> extends AbstractFileObject<FS> {
    /**
     * An InputStream that cleans up the HTTP connection on close.
     */
    static class HttpInputStream extends MonitorInputStream {
        private final GetMethod method;

        public HttpInputStream(final GetMethod method) throws IOException {
            super(method.getResponseBodyAsStream());
            this.method = method;
        }

        /**
         * Called after the stream has been closed.
         */
        @Override
        protected void onClose() throws IOException {
            method.releaseConnection();
        }
    }

    private final String urlCharset;
    private final String userAgent;
    private final boolean followRedirect;

    private HeadMethod method;

    protected HttpFileObject(final AbstractFileName name, final FS fileSystem) {
        this(name, fileSystem, HttpFileSystemConfigBuilder.getInstance());
    }

    protected HttpFileObject(final AbstractFileName name, final FS fileSystem,
            final HttpFileSystemConfigBuilder builder) {
        super(name, fileSystem);
        final FileSystemOptions fileSystemOptions = fileSystem.getFileSystemOptions();
        urlCharset = builder.getUrlCharset(fileSystemOptions);
        userAgent = builder.getUserAgent(fileSystemOptions);
        followRedirect = builder.getFollowRedirect(fileSystemOptions);
    }

    /**
     * Detaches this file object from its file resource.
     */
    @Override
    protected void doDetach() throws Exception {
        method = null;
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    @Override
    protected long doGetContentSize() throws Exception {
        final Header header = method.getResponseHeader("content-length");
        if (header == null) {
            // Assume 0 content-length
            return 0;
        }
        return Long.parseLong(header.getValue());
    }

    /**
     * Creates an input stream to read the file content from. Is only called if {@link #doGetType} returns
     * {@link FileType#FILE}.
     * <p>
     * It is guaranteed that there are no open output streams for this file when this method is called.
     * <p>
     * The returned stream does not have to be buffered.
     */
    @Override
    protected InputStream doGetInputStream() throws Exception {
        final GetMethod getMethod = new GetMethod();
        setupMethod(getMethod);
        final int status = getAbstractFileSystem().getClient().executeMethod(getMethod);
        if (status == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new FileNotFoundException(getName());
        }
        if (status != HttpURLConnection.HTTP_OK) {
            throw new FileSystemException("vfs.provider.http/get.error", getName(), Integer.valueOf(status));
        }

        return new HttpInputStream(getMethod);
    }

    /**
     * Returns the last modified time of this file.
     * <p>
     * This implementation throws an exception.
     */
    @Override
    protected long doGetLastModifiedTime() throws Exception {
        final Header header = method.getResponseHeader("last-modified");
        if (header == null) {
            throw new FileSystemException("vfs.provider.http/last-modified.error", getName());
        }
        return DateUtil.parseDate(header.getValue()).getTime();
    }

    @Override
    protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception {
        return new HttpRandomAccessContent(this, mode);
    }

    /**
     * Determines the type of this file. Must not return null. The return value of this method is cached, so the
     * implementation can be expensive.
     */
    @Override
    protected FileType doGetType() throws Exception {
        // Use the HEAD method to probe the file.
        final int status = this.getHeadMethod().getStatusCode();
        if (status == HttpURLConnection.HTTP_OK
                || status == HttpURLConnection.HTTP_BAD_METHOD /* method is bad, but resource exist */) {
            return FileType.FILE;
        } else if (status == HttpURLConnection.HTTP_NOT_FOUND || status == HttpURLConnection.HTTP_GONE) {
            return FileType.IMAGINARY;
        } else {
            throw new FileSystemException("vfs.provider.http/head.error", getName(), Integer.valueOf(status));
        }
    }

    @Override
    protected boolean doIsWriteable() throws Exception {
        return false;
    }

    /**
     * Lists the children of this file.
     */
    @Override
    protected String[] doListChildren() throws Exception {
        throw new Exception("Not implemented.");
    }

    protected String encodePath(final String decodedPath) throws URIException {
        return URIUtil.encodePath(decodedPath);
    }

    @Override
    protected FileContentInfoFactory getFileContentInfoFactory() {
        return new HttpFileContentInfoFactory();
    }

    protected boolean getFollowRedirect() {
        return followRedirect;
    }

    protected String getUserAgent() {
        return userAgent;
    }

    HeadMethod getHeadMethod() throws IOException {
        if (method != null) {
            return method;
        }
        method = new HeadMethod();
        setupMethod(method);
        final HttpClient client = getAbstractFileSystem().getClient();
        client.executeMethod(method);
        method.releaseConnection();
        return method;
    }

    protected String getUrlCharset() {
        return urlCharset;
    }

    /**
     * Prepares a HttpMethod object.
     *
     * @param method The object which gets prepared to access the file object.
     * @throws FileSystemException if an error occurs.
     * @throws URIException if path cannot be represented.
     * @since 2.0 (was package)
     */
    protected void setupMethod(final HttpMethod method) throws FileSystemException, URIException {
        final String pathEncoded = ((URLFileName) getName()).getPathQueryEncoded(this.getUrlCharset());
        method.setPath(pathEncoded);
        method.setFollowRedirects(this.getFollowRedirect());
        method.setRequestHeader("User-Agent", this.getUserAgent());
    }

    /*
     * protected Map doGetAttributes() throws Exception { TreeMap map = new TreeMap();
     *
     * Header contentType = method.getResponseHeader("content-type"); if (contentType != null) { HeaderElement[] element
     * = contentType.getValues(); if (element != null && element.length > 0) { map.put("content-type",
     * element[0].getName()); } }
     *
     * map.put("content-encoding", method.getResponseCharSet()); return map; }
     */
}
