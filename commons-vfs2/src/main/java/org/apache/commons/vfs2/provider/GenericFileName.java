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
import org.apache.commons.vfs2.FileType;

/**
 * A file name that represents a 'generic' URI, as per RFC 2396. Consists of a scheme, userinfo (typically username and
 * password), hostname, port, and path.
 */
public class GenericFileName extends AbstractFileName {
    private static final char[] USERNAME_RESERVED = { ':', '@', '/' };
    private static final char[] PASSWORD_RESERVED = { '@', '/', '?' };
    private final String userName;
    private final String hostName;
    private final int defaultPort;
    private final String password;
    private final int port;

    protected GenericFileName(final String scheme, final String hostName, final int port, final int defaultPort,
            final String userName, final String password, final String path, final FileType type) {
        super(scheme, path, type);
        this.hostName = hostName;
        this.defaultPort = defaultPort;
        this.password = password;
        this.userName = userName;
        if (port > 0) {
            this.port = port;
        } else {
            this.port = getDefaultPort();
        }
    }

    /**
     * Returns the user name part of this name.
     *
     * @return The user name.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Returns the password part of this name.
     *
     * @return The password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the host name part of this name.
     *
     * @return The host name.
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * Returns the port part of this name.
     *
     * @return The port number.
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the default port for this file name.
     *
     * @return The default port number.
     */
    public int getDefaultPort() {
        return defaultPort;
    }

    /**
     * Create a FileName.
     *
     * @param absPath The absolute path.
     * @param type The FileType.
     * @return The created FileName.
     */
    @Override
    public FileName createName(final String absPath, final FileType type) {
        return new GenericFileName(getScheme(), hostName, port, defaultPort, userName, password, absPath, type);
    }

    /**
     * Builds the root URI for this file name.
     */
    @Override
    protected void appendRootUri(final StringBuilder buffer, final boolean addPassword) {
        buffer.append(getScheme());
        buffer.append("://");
        appendCredentials(buffer, addPassword);
        buffer.append(hostName);
        if (port != getDefaultPort()) {
            buffer.append(':');
            buffer.append(port);
        }
    }

    /**
     * Append the user credentials.
     * <p>
     * If anything was added, it will be '@' terminated.
     *
     * @param buffer the string buffer to modify.
     * @param addPassword flag if password should be added or replaced with placeholder (false).
     */
    protected void appendCredentials(final StringBuilder buffer, final boolean addPassword) {
        if (userName != null && userName.length() != 0) {
            UriParser.appendEncoded(buffer, userName, USERNAME_RESERVED);
            if (password != null && password.length() != 0) {
                buffer.append(':');
                if (addPassword) {
                    UriParser.appendEncoded(buffer, password, PASSWORD_RESERVED);
                } else {
                    buffer.append("***");
                }
            }
            buffer.append('@');
        }
    }
}
