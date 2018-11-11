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
import org.apache.commons.vfs2.util.Cryptor;
import org.apache.commons.vfs2.util.CryptorFactory;

/**
 * Implementation for any url based filesystem.
 * <p>
 * Parses the url into user/password/host/port/path. Does not handle a query string (after ?)
 *
 * @see URLFileNameParser URLFileNameParser for the implementation which also handles the query string too
 */
public class HostFileNameParser extends AbstractFileNameParser {
    private final int defaultPort;

    public HostFileNameParser(final int defaultPort) {
        this.defaultPort = defaultPort;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    @Override
    public FileName parseUri(final VfsComponentContext context, final FileName base, final String filename)
            throws FileSystemException {
        // FTP URI are generic URI (as per RFC 2396)
        final StringBuilder name = new StringBuilder();

        // Extract the scheme and authority parts
        final Authority auth = extractToPath(filename, name);

        // Decode and normalise the file name
        UriParser.canonicalizePath(name, 0, name.length(), this);
        UriParser.fixSeparators(name);
        final FileType fileType = UriParser.normalisePath(name);
        final String path = name.toString();

        return new GenericFileName(auth.scheme, auth.hostName, auth.port, defaultPort, auth.userName, auth.password,
                path, fileType);
    }

    /**
     * Extracts the scheme, userinfo, hostname and port components of a generic URI.
     *
     * @param uri The absolute URI to parse.
     * @param name Used to return the remainder of the URI.
     * @return Authority extracted host authority, never null.
     * @throws FileSystemException if authority cannot be extracted.
     */
    protected Authority extractToPath(final String uri, final StringBuilder name) throws FileSystemException {
        final Authority auth = new Authority();

        // Extract the scheme
        auth.scheme = UriParser.extractScheme(uri, name);

        // Expecting "//"
        if (name.length() < 2 || name.charAt(0) != '/' || name.charAt(1) != '/') {
            throw new FileSystemException("vfs.provider/missing-double-slashes.error", uri);
        }
        name.delete(0, 2);

        // Extract userinfo, and split into username and password
        final String userInfo = extractUserInfo(name);
        final String userName;
        final String password;
        if (userInfo != null) {
            final int idx = userInfo.indexOf(':');
            if (idx == -1) {
                userName = userInfo;
                password = null;
            } else {
                userName = userInfo.substring(0, idx);
                password = userInfo.substring(idx + 1);
            }
        } else {
            userName = null;
            password = null;
        }
        auth.userName = UriParser.decode(userName);
        auth.password = UriParser.decode(password);

        if (auth.password != null && auth.password.startsWith("{") && auth.password.endsWith("}")) {
            try {
                final Cryptor cryptor = CryptorFactory.getCryptor();
                auth.password = cryptor.decrypt(auth.password.substring(1, auth.password.length() - 1));
            } catch (final Exception ex) {
                throw new FileSystemException("Unable to decrypt password", ex);
            }
        }

        // Extract hostname, and normalise (lowercase)
        final String hostName = extractHostName(name);
        if (hostName == null) {
            throw new FileSystemException("vfs.provider/missing-hostname.error", uri);
        }
        auth.hostName = hostName.toLowerCase();

        // Extract port
        auth.port = extractPort(name, uri);

        // Expecting '/' or empty name
        if (name.length() > 0 && name.charAt(0) != '/') {
            throw new FileSystemException("vfs.provider/missing-hostname-path-sep.error", uri);
        }

        return auth;
    }

    /**
     * Extracts the user info from a URI.
     *
     * @param name string buffer with the "scheme://" part has been removed already. Will be modified.
     * @return the user information up to the '@' or null.
     */
    protected String extractUserInfo(final StringBuilder name) {
        final int maxlen = name.length();
        for (int pos = 0; pos < maxlen; pos++) {
            final char ch = name.charAt(pos);
            if (ch == '@') {
                // Found the end of the user info
                final String userInfo = name.substring(0, pos);
                name.delete(0, pos + 1);
                return userInfo;
            }
            if (ch == '/' || ch == '?') {
                // Not allowed in user info
                break;
            }
        }

        // Not found
        return null;
    }

    /**
     * Extracts the hostname from a URI.
     *
     * @param name string buffer with the "scheme://[userinfo@]" part has been removed already. Will be modified.
     * @return the host name or null.
     */
    protected String extractHostName(final StringBuilder name) {
        final int maxlen = name.length();
        int pos = 0;
        for (; pos < maxlen; pos++) {
            final char ch = name.charAt(pos);
            if (ch == '/' || ch == ';' || ch == '?' || ch == ':' || ch == '@' || ch == '&' || ch == '=' || ch == '+'
                    || ch == '$' || ch == ',') {
                break;
            }
        }
        if (pos == 0) {
            return null;
        }

        final String hostname = name.substring(0, pos);
        name.delete(0, pos);
        return hostname;
    }

    /**
     * Extracts the port from a URI.
     *
     * @param name string buffer with the "scheme://[userinfo@]hostname" part has been removed already. Will be
     *            modified.
     * @param uri full URI for error reporting.
     * @return The port, or -1 if the URI does not contain a port.
     * @throws FileSystemException if URI is malformed.
     * @throws NumberFormatException if port number cannot be parsed.
     */
    protected int extractPort(final StringBuilder name, final String uri) throws FileSystemException {
        if (name.length() < 1 || name.charAt(0) != ':') {
            return -1;
        }

        final int maxlen = name.length();
        int pos = 1;
        for (; pos < maxlen; pos++) {
            final char ch = name.charAt(pos);
            if (ch < '0' || ch > '9') {
                break;
            }
        }

        final String port = name.substring(1, pos);
        name.delete(0, pos);
        if (port.length() == 0) {
            throw new FileSystemException("vfs.provider/missing-port.error", uri);
        }

        return Integer.parseInt(port);
    }

    /**
     * Parsed authority info (scheme, hostname, username/password, port).
     */
    protected static class Authority {
        private String scheme;
        private String hostName;
        private String userName;
        private String password;
        private int port;

        /**
         * Get the connection schema.
         *
         * @return the connection scheme.
         * @since 2.0
         */
        public String getScheme() {
            return scheme;
        }

        /**
         * Set the connection schema.
         *
         * @param scheme the connection scheme.
         * @since 2.0
         */
        public void setScheme(final String scheme) {
            this.scheme = scheme;
        }

        /**
         * Get the host name.
         *
         * @return the host name.
         * @since 2.0
         */
        public String getHostName() {
            return hostName;
        }

        /**
         * Set the host name.
         *
         * @param hostName the host name.
         * @since 2.0
         */
        public void setHostName(final String hostName) {
            this.hostName = hostName;
        }

        /**
         * Get the user name.
         *
         * @return the user name or null.
         * @since 2.0
         */
        public String getUserName() {
            return userName;
        }

        /**
         * Set the user name.
         *
         * @param userName the user name.
         * @since 2.0
         */
        public void setUserName(final String userName) {
            this.userName = userName;
        }

        /**
         * Get the user password.
         *
         * @return the password or null.
         * @since 2.0
         */
        public String getPassword() {
            return password;
        }

        /**
         * Set the user password.
         *
         * @param password the user password.
         * @since 2.0
         */
        public void setPassword(final String password) {
            this.password = password;
        }

        /**
         * Get the port.
         *
         * @return the port or -1.
         * @since 2.0
         */
        public int getPort() {
            return port;
        }

        /**
         * Set the connection port.
         *
         * @param port the port number or -1.
         * @since 2.0
         */
        public void setPort(final int port) {
            this.port = port;
        }
    }
}
