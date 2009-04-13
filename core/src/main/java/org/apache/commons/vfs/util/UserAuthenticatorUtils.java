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
package org.apache.commons.vfs.util;

import org.apache.commons.vfs.UserAuthenticator;
import org.apache.commons.vfs.UserAuthenticationData;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;

/**
 * some helper
 */
public class UserAuthenticatorUtils
{
    /**
     * gets data of given type from the UserAuthenticationData or null if there is no data or data of this type available
     */
    public static char[] getData(UserAuthenticationData data, UserAuthenticationData.Type type, char[] overwriddenValue)
    {
        if (overwriddenValue != null)
        {
            return overwriddenValue;
        }

        if (data == null)
        {
            return null;
        }

        return data.getData(type);
    }

    /**
     * if there is a authenticator the authentication will take place, else null will be reutrned
     */
    public static UserAuthenticationData authenticate(FileSystemOptions opts, UserAuthenticationData.Type[] authenticatorTypes)
    {
        UserAuthenticator auth = DefaultFileSystemConfigBuilder.getInstance().getUserAuthenticator(opts);
        return authenticate(auth, authenticatorTypes);
    }

    /**
     * if there is a authenticator the authentication will take place, else null will be reutrned
     */
    public static UserAuthenticationData authenticate(UserAuthenticator auth, UserAuthenticationData.Type[] authenticatorTypes)
    {
        if (auth == null)
        {
            return null;
        }

        return auth.requestAuthentication(authenticatorTypes);
    }

    /**
     * converts a string to a char array (null safe)
     */
    public static char[] toChar(String string)
    {
        if (string == null)
        {
            return null;
        }

        return string.toCharArray();
    }

    /**
     * cleanup the data in the UerAuthenticationData (null safe)
     */
    public static void cleanup(UserAuthenticationData authData)
    {
        if (authData == null)
        {
            return;
        }

        authData.cleanup();
    }

    /**
     * converts the given data to a string (null safe)
     */
    public static String toString(char[] data)
    {
        if (data == null)
        {
            return null;
        }

        return new String(data);
    }
}
