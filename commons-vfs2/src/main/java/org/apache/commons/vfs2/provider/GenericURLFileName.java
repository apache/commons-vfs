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
package org.apache.commons.vfs2.provider;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.util.URIUtils;

/**
 * Generic file name that represents a URL.
 */
public class GenericURLFileName extends GenericFileName {

    private static final int BUFFER_SIZE = 250;

    private final String queryString;

    public GenericURLFileName(final String scheme, final String hostName, final int port, final int defaultPort,
            final String userName, final String password, final String path, final FileType type,
            final String queryString) {
        super(scheme, hostName, port, defaultPort, userName, password, path, type);
        this.queryString = queryString;
    }

    /**
     * Gets the query string.
     *
     * @return the query string part of the file name
     */
    public String getQueryString() {
        return queryString;
    }

    /**
     * Gets the path and query string e.g. /path/servlet?param1=true.
     *
     * @return the path and its query string
     */
    public String getPathQuery() {
        final StringBuilder sb = new StringBuilder(BUFFER_SIZE);
        sb.append(getPath());
        sb.append("?");
        sb.append(getQueryString());

        return sb.toString();
    }

    /**
     * Gets the path encoded suitable for url like file system e.g. (http, webdav).
     *
     * @param charset the charset used for the path encoding
     * @return The encoded path.
     * @throws FileSystemException If some other error occurs.
     */
    public String getPathQueryEncoded(final String charset) throws FileSystemException {
        if (getQueryString() == null) {
            if (charset != null) {
                return URIUtils.encodePath(getPathDecoded(), charset);
            }
            return URIUtils.encodePath(getPathDecoded());
        }

        final StringBuilder sb = new StringBuilder(BUFFER_SIZE);
        if (charset != null) {
            sb.append(URIUtils.encodePath(getPathDecoded(), charset));
        } else {
            sb.append(URIUtils.encodePath(getPathDecoded()));
        }
        sb.append("?");
        sb.append(getQueryString());
        return sb.toString();
    }

    /**
     * Creates a FileName.
     *
     * @param absPath The absolute path.
     * @param type The FileType.
     * @return The FileName
     */
    @Override
    public FileName createName(final String absPath, final FileType type) {
        return new GenericURLFileName(getScheme(), getHostName(), getPort(), getDefaultPort(), getUserName(), getPassword(),
                absPath, type, getQueryString());
    }

    /**
     * Appends query string to the uri.
     *
     * @return the uri
     */
    @Override
    protected String createURI() {
        if (getQueryString() != null) {
            final StringBuilder sb = new StringBuilder(BUFFER_SIZE);
            sb.append(super.createURI());
            sb.append("?");
            sb.append(getQueryString());

            return sb.toString();
        }

        return super.createURI();
    }

    /**
     * Encodes a URI.
     *
     * @param charset The character set.
     * @return The encoded URI
     * @throws FileSystemException if some other exception occurs.
     */
    public String getURIEncoded(final String charset) throws FileSystemException {
        final StringBuilder sb = new StringBuilder(BUFFER_SIZE);
        appendRootUri(sb, true);
        sb.append(getPathQueryEncoded(charset));
        return sb.toString();
    }
}
