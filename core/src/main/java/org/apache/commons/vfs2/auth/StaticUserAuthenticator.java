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

import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.UserAuthenticator;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;

/**
 * Provides always the same credentials data passed in with the constructor.
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 */
public class StaticUserAuthenticator implements UserAuthenticator, Comparable<StaticUserAuthenticator>
{
    /** The user name */
    private final String username;

    /** The password */
    private final String password;

    /** The user's domain */
    private final String domain;

    public StaticUserAuthenticator(String domain, String username, String password)
    {
        this.username = username;
        this.password = password;
        this.domain = domain;
    }

    public UserAuthenticationData requestAuthentication(UserAuthenticationData.Type[] types)
    {
        UserAuthenticationData data = new UserAuthenticationData();
        data.setData(UserAuthenticationData.DOMAIN, UserAuthenticatorUtils.toChar(domain));
        data.setData(UserAuthenticationData.USERNAME, UserAuthenticatorUtils.toChar(username));
        data.setData(UserAuthenticationData.PASSWORD, UserAuthenticatorUtils.toChar(password));
        return data;
    }

    /**
     * {@inheritDoc}
     * @since 2.0
     */
    @Override
    public int hashCode()
    {
        final int prime = 37;
        int result = 1;
        result = prime * result + ((domain == null) ? 0 : domain.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());

        return result;
    }

    /**
     * {@inheritDoc}
     * @since 2.0
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }

        if (obj == null)
        {
            return false;
        }

        if (getClass() != obj.getClass())
        {
            return false;
        }

        StaticUserAuthenticator other = (StaticUserAuthenticator) obj;
        return equalsNullsafe(domain, other.domain)
                && equalsNullsafe(username, other.username)
                && equalsNullsafe(password, other.password);
    }

    private boolean equalsNullsafe(final String thisString, final String otherString)
    {
        if (thisString == null)
        {
            if (otherString != null)
            {
                return false;
            }
        }
        else if (!thisString.equals(otherString))
        {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     * @since 2.0
     */
    public int compareTo(final StaticUserAuthenticator other)
    {
        int result = compareStringOrNull(domain, other.domain);
        result = result == 0 ? compareStringOrNull(username, other.username) : result;
        result = result == 0 ? compareStringOrNull(password, other.password) : result;

        return result;
    }

    private int compareStringOrNull(final String thisString, final String otherString)
    {
        if (thisString == null)
        {
            if (otherString != null)
            {
                return -1;
            }
        }
        else
        {
            if (otherString == null)
            {
                return 1;
            }

            final int result = thisString.compareTo(otherString);
            if (result != 0)
            {
                return result;
            }
        }

        return 0;
    }

    /**
     * {@inheritDoc}
     * @since 2.0
     */
    @Override
    public String toString()
    {
        StringBuilder buffer = new StringBuilder();
        if (domain != null)
        {
            buffer.append(domain).append('\\');
        }
        if (username != null)
        {
            buffer.append(username);
        }
        else
        {
            buffer.append("(null)");
        }
        if (password != null)
        {
            buffer.append(":***");
        }
        return buffer.toString();
    }
}
