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
package org.apache.commons.vfs2.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.UserAuthenticator;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;

/**
 * Provides always the same credentials data passed in with the constructor.
 */
public class StaticUserAuthenticator implements UserAuthenticator, Comparable<StaticUserAuthenticator> {
    private static final Log LOG = LogFactory.getLog(StaticUserAuthenticator.class);

    /** The user name */
    private final String username;

    /** The password */
    private final String password;

    /** The user's domain */
    private final String domain;

    public StaticUserAuthenticator(final String domain, final String username, final String password) {
        this.username = username;
        this.password = password;
        this.domain = domain;
    }

    @Override
    public UserAuthenticationData requestAuthentication(final UserAuthenticationData.Type[] types) {
        final UserAuthenticationData data = new UserAuthenticationData();
        for (final UserAuthenticationData.Type type : types) {
            if (type == UserAuthenticationData.DOMAIN) {
                data.setData(UserAuthenticationData.DOMAIN, UserAuthenticatorUtils.toChar(domain));
            } else if (type == UserAuthenticationData.USERNAME) {
                data.setData(UserAuthenticationData.USERNAME, UserAuthenticatorUtils.toChar(username));
            } else if (type == UserAuthenticationData.PASSWORD) {
                data.setData(UserAuthenticationData.PASSWORD, UserAuthenticatorUtils.toChar(password));
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(StaticUserAuthenticator.class.getSimpleName()
                            + " does not support authentication data type '" + type
                            + "'; authentication request for this type ignored.");
                }
            }
        }
        return data;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Override
    public int hashCode() {
        final int prime = 37;
        int result = 1;
        result = prime * result + (domain == null ? 0 : domain.hashCode());
        result = prime * result + (password == null ? 0 : password.hashCode());
        result = prime * result + (username == null ? 0 : username.hashCode());

        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final StaticUserAuthenticator other = (StaticUserAuthenticator) obj;
        return equalsNullsafe(domain, other.domain) && equalsNullsafe(username, other.username)
                && equalsNullsafe(password, other.password);
    }

    private boolean equalsNullsafe(final String thisString, final String otherString) {
        if (thisString == null) {
            if (otherString != null) {
                return false;
            }
        } else if (!thisString.equals(otherString)) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Override
    public int compareTo(final StaticUserAuthenticator other) {
        int result = compareStringOrNull(domain, other.domain);
        result = result == 0 ? compareStringOrNull(username, other.username) : result;
        result = result == 0 ? compareStringOrNull(password, other.password) : result;

        return result;
    }

    private int compareStringOrNull(final String thisString, final String otherString) {
        if (thisString == null) {
            if (otherString != null) {
                return -1;
            }
        } else {
            if (otherString == null) {
                return 1;
            }

            final int result = thisString.compareTo(otherString);
            if (result != 0) {
                return result;
            }
        }

        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.0
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        if (domain != null) {
            buffer.append(domain).append('\\');
        }
        if (username != null) {
            buffer.append(username);
        } else {
            buffer.append("(null)");
        }
        if (password != null) {
            buffer.append(":***");
        }
        return buffer.toString();
    }
}
