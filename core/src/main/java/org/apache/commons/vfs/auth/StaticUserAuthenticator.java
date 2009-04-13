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
package org.apache.commons.vfs.auth;

import org.apache.commons.vfs.UserAuthenticationData;
import org.apache.commons.vfs.UserAuthenticator;
import org.apache.commons.vfs.util.UserAuthenticatorUtils;

/**
 * provides always the same credential data passed in with the constructor.
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 */
public class StaticUserAuthenticator implements UserAuthenticator
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
}
