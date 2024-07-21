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
package org.apache.commons.vfs2.provider.webdav4;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.GenericURLFileName;
import org.apache.commons.vfs2.util.URIUtils;

/**
 * WebDAV 4 file name that represents a URL.
 *
 * @since 2.10.0
 */
public class Webdav4FileName extends GenericURLFileName {
    private static final int BUFFER_SIZE = 250;

    private final boolean appendTrailingSlash;

    /**
     * Constructs a new instance.
     *
     * @param scheme Host scheme.
     * @param hostName Host name or IP address.
     * @param port Host port.
     * @param defaultPort Default host port.
     * @param userName user name.
     * @param password user password.
     * @param path Path on the host.
     * @param type File type on the host.
     * @param queryString Query string for the path.
     */
    public Webdav4FileName(final String scheme, final String hostName, final int port, final int defaultPort,
                           final String userName, final String password, final String path, final FileType type,
                           final String queryString) {
        this(scheme, hostName, port, defaultPort, userName, password, path, type, queryString, false);
    }

    /**
     * Constructs a new instance.
     *
     * @param scheme Host scheme.
     * @param hostName Host name or IP address.
     * @param port Host port.
     * @param defaultPort Default host port.
     * @param userName user name.
     * @param password user password.
     * @param path Path on the host.
     * @param type File type on the host.
     * @param queryString Query string for the path.
     * @param appendTrailingSlash Append trailing slash to path.
     */
    public Webdav4FileName(final String scheme, final String hostName, final int port, final int defaultPort,
                           final String userName, final String password, final String path, final FileType type,
                           final String queryString, final boolean appendTrailingSlash) {
        super(scheme, hostName, port, defaultPort, userName, password, path, type, queryString);
        this.appendTrailingSlash = appendTrailingSlash;
    }

    /**
     * Gets the path encoded suitable for url like file system e.g. (http, webdav).
     * Reappend the trailing slash ( / ) if this FileName is a directory and not ROOT
     * because many WEBDav-Servers require the trailing slash if the request access a directory
     *
     * @param charset the charset used for the path encoding
     * @return The encoded path.
     * @throws FileSystemException If some other error occurs.
     */
    @Override
    public String getPathQueryEncoded(final String charset) throws FileSystemException {
        String pathDecoded = getPathDecoded();

        if (appendTrailingSlash && getType() == FileType.FOLDER && getPath().length() > 1) {
            pathDecoded += SEPARATOR;
        }

        if (getQueryString() == null || getQueryString().isEmpty()) {
            if (charset != null) {
                return URIUtils.encodePath(pathDecoded, charset);
            }
            return URIUtils.encodePath(pathDecoded);
        }

        final StringBuilder sb = new StringBuilder(BUFFER_SIZE);
        if (charset != null) {
            sb.append(URIUtils.encodePath(pathDecoded, charset));
        } else {
            sb.append(URIUtils.encodePath(pathDecoded));
        }
        sb.append("?");
        sb.append(getQueryString());
        return sb.toString();
    }

}
