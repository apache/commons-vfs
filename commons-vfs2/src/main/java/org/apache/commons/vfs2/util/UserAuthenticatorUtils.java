/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.util;

import org.apache.commons.lang3.CharSequenceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.UserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;

/**
 * Helps with authentication.
 */
public final class UserAuthenticatorUtils {

    /**
     * Authenticates if there is an authenticator, else returns null.
     *
     * @param options The FileSystemOptions.
     * @param authenticatorTypes An array of types describing the data to be retrieved.
     * @return A UserAuthenticationData object containing the data requested.
     */
    public static UserAuthenticationData authenticate(final FileSystemOptions options, final UserAuthenticationData.Type[] authenticatorTypes) {
        return authenticate(DefaultFileSystemConfigBuilder.getInstance().getUserAuthenticator(options), authenticatorTypes);
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
        return auth != null ? auth.requestAuthentication(authenticatorTypes) : null;
    }

    /**
     * Cleans up the data in the UerAuthenticationData (null-safe).
     *
     * @param authData The UserAuthenticationDAta.
     */
    public static void cleanup(final UserAuthenticationData authData) {
        if (authData != null) {
            authData.cleanup();
        }
    }

    /**
     * Gets a copy of the data of a given type from the UserAuthenticationData or null if there is no data or data of this type available.
     *
     * @param data            The UserAuthenticationData.
     * @param type            The type of the element to retrieve.
     * @param overriddenValue The default value.
     * @return The data of the given type as a character array or null if the data is not available.
     */
    public static char[] getData(final UserAuthenticationData data, final UserAuthenticationData.Type type, final char[] overriddenValue) {
        if (overriddenValue != null) {
            return overriddenValue;
        }
        return data != null ? data.getData(type) : null;
    }

    /**
     * Converts a string to a char array (null-safe).
     *
     * @param string The String to convert.
     * @return A new character array.
     * @deprecated Use {@link CharSequenceUtils#toCharArray(CharSequence)}.
     */
    @Deprecated
    public static char[] toChar(final String string) {
        return CharSequenceUtils.toCharArray(string);
    }

    /**
     * Converts the given data to a string (null-safe).
     *
     * @param data A character array containing the data to convert to a String.
     * @return A new String.
     * @deprecated Use {@link StringUtils#valueOf(char[])}.
     */
    @Deprecated
    public static String toString(final char[] data) {
        return StringUtils.valueOf(data);
    }

    private UserAuthenticatorUtils() {
    }
}
