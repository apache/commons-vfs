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
package org.apache.commons.vfs2;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Contains various authentication data.
 */
public class UserAuthenticationData {
    /**
     * Represents a user authentication item.
     */
    public static class Type implements Comparable<Type> {
        /** The type name */
        private final String type;

        /**
         * Creates a new Type.
         *
         * @param type the type
         */
        public Type(final String type) {
            this.type = type;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final Type type1 = (Type) o;

            if (type != null ? !type.equals(type1.type) : type1.type != null) {
                return false;
            }

            return true;
        }

        @Override
        public int compareTo(final Type o) {
            return type.compareTo(o.type);
        }

        /**
         * @return The hash code.
         * @since 2.0
         */
        @Override
        public int hashCode() {
            return type != null ? type.hashCode() : 0;
        }

        /**
         * @return The type.
         * @since 2.0
         */
        @Override
        public String toString() {
            return type;
        }
    }

    /** The user name. */
    public static final Type USERNAME = new Type("username");

    /** The password. */
    public static final Type PASSWORD = new Type("password");

    /** The user's domain. */
    public static final Type DOMAIN = new Type("domain");

    /** The authentication data. */
    private final Map<Type, char[]> authenticationData = new TreeMap<>();

    /**
     * Creates a new uninitialized instance.
     */
    public UserAuthenticationData() {
        // do nothing
    }

    /**
     * Sets a data to this collection.
     *
     * @param type The Type to add
     * @param data The data associated with the Type
     */
    public void setData(final Type type, final char[] data) {
        authenticationData.put(type, data);
    }

    /**
     * Gets a data from the collection.
     *
     * @param type The Type to retrieve.
     * @return a character array containing the data associated with the type.
     */
    public char[] getData(final Type type) {
        return authenticationData.get(type);
    }

    /**
     * Deletes all data stored within this authenticator.
     */
    public void cleanup() {
        // step 1: nullify character buffers
        final Iterator<char[]> iterAuthenticationData = authenticationData.values().iterator();
        while (iterAuthenticationData.hasNext()) {
            final char[] data = iterAuthenticationData.next();
            if (data == null || data.length < 0) {
                continue;
            }

            for (int i = 0; i < data.length; i++) {
                data[i] = 0;
            }
        }
        // step 2: allow data itself to gc
        authenticationData.clear();
    }
}
