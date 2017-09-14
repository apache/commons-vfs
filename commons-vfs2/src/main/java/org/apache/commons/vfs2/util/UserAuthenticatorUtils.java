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
package org.apache.commons.vfs2.util;

import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.UserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;

/**
 * Helps with authentication.
 */
public final class UserAuthenticatorUtils {
    private UserAuthenticatorUtils() {
    }

    /**
     * Gets data of given type from the UserAuthenticationData or null if there is no data or data of this type
     * available.
     *
     * @param data The UserAuthenticationData.
     * @param type The type of the element to retrieve.
     * @param overriddenValue The default value.
     * @return The data of the given type as a character array or null if the data is not available.
     */
    public static char[] getData(final UserAuthenticationData data, final UserAuthenticationData.Type type,
            final char[] overriddenValue) {
        if (overriddenValue != null) {
            return overriddenValue;
        }

        if (data == null) {
            return null;
        }

        return data.getData(type);
    }

    /**
     * Authenticates if there is an authenticator, else returns null.
     *
     * @param opts The FileSystemOptions.
     * @param authenticatorTypes An array of types describing the data to be retrieved.
     * @return A UserAuthenticationData object containing the data requested.
     */
    public static UserAuthenticationData authenticate(final FileSystemOptions opts,
            final UserAuthenticationData.Type[] authenticatorTypes) {
        final UserAuthenticator auth = DefaultFileSystemConfigBuilder.getInstance().getUserAuthenticator(opts);
        return authenticate(auth, authenticatorTypes);
    }

    /**
     * Authenticates if there is an authenticator, else returns null.
     *
     * @param auth The UserAuthenticator.
     * @param authenticatorTypes An array of types describing the data to be retrieved.
     * @return A UserAuthenticationData object containing the data requested.
     */
    public static UserAuthenticationData authenticate(final UserAuthenticator auth,
            final UserAuthenticationData.Type[] authenticatorTypes) {
        if (auth == null) {
            return null;
        }

        return auth.requestAuthentication(authenticatorTypes);
    }

    /**
     * Converts a string to a char array (null-safe).
     *
     * @param string The String to convert.
     * @return The character array.
     */
    public static char[] toChar(final String string) {
        if (string == null) {
            return null;
        }

        return string.toCharArray();
    }

    /**
     * Cleans up the data in the UerAuthenticationData (null-safe).
     *
     * @param authData The UserAuthenticationDAta.
     */
    public static void cleanup(final UserAuthenticationData authData) {
        if (authData == null) {
            return;
        }

        authData.cleanup();
    }

    /**
     * Converts the given data to a string (null-safe).
     *
     * @param data A character array containing the data to convert to a String.
     * @return The String.
     */
    public static String toString(final char[] data) {
        if (data == null) {
            return null;
        }

        return new String(data);
    }
}
