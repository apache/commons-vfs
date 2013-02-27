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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains various authentication data.
 */
public class UserAuthenticationData
{
    /**
     * Represents a user authentication item.
     */
    public static class Type implements Comparable<Type>
    {
        /** The type name */
        private final String type;
        private final Class<?> clazz;

        /**
         * Creates a new Type.
         *
         * @param type the type name
         * @deprecated As of 2.1 use {@link #Type(String, Class)}
         */
        @Deprecated
        public Type(final String type)
        {
            this(type, char[].class);
        }

        /**
         * Creates a new Type.
         *
         * @param type the type name
         * @param clazz the class type
         * @since 2.1
         */
        public Type(final String type, final Class<?> clazz)
        {
            this.type = type;
            this.clazz = clazz;
        }

        /**
         * Test the provided class type for compatibility.
         *  
         * @param clz the class type to check
         * @return {@code true} if class type is assignable
         * @since 2.1
         */
        public final boolean isAssignable(final Class<?> clz)
        {
            return clazz.isAssignableFrom(clz);
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final Type other = (Type) o;
            if (type != null ? !type.equals(other.type) : other.type != null)
            {
                return false;
            }
            if (clazz != null ? (other.clazz == null || !clazz.getName().equals(other.clazz.getName())) : other.clazz != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int compareTo(final Type o)
        {
            return type.compareTo(o.type);
        }

        /**
         * @return The hash code.
         * @since 2.0
         * */
        @Override
        public int hashCode()
        {
            final int prime = 7;
            int result = 1;
            result = prime * result + ((clazz == null) ? 0 : clazz.getName().hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

        /**
         * @return The type.
         * @since 2.0
         * */
        @Override
        public String toString()
        {
            return type;
        }
    }

    /** The user name. */
    public static final Type USERNAME = new Type("username", char[].class);

    /** The password. */
    public static final Type PASSWORD = new Type("password", char[].class);

    /** The user's domain. */
    public static final Type DOMAIN = new Type("domain", char[].class);

    /** The authentication data. */
    private final Map<Type, Object> authenticationData = new HashMap<Type, Object>();

    /**
     * Creates a new uninitialized instance.
     */
    public UserAuthenticationData()
    {
        // do nothing
    }

    /**
     * Sets a data to this collection.
     * @param type The Type to add
     * @param data The data associated with the Type
     * @deprecated As of 2.1 use {@link #setAuthData(Type, Object)}
     */
    @Deprecated
    public void setData(final Type type, final char[] data)
    {
        setAuthData(type, data);
    }

    /**
     * Gets a data from the collection.
     * @param type The Type to retrieve.
     * @return a character array containing the data associated with the type.
     * @deprecated As of 2.1 use {@link #getAuthData(Type)}
     */
    @Deprecated
    public char[] getData(final Type type)
    {
        return getAuthData(type);
    }

    /**
     * Sets a data to this collection.
     * @param type The Type to add
     * @param data The data associated with the Type
     * @return {@code true} if the provided data is compatible with the Type
     * @since 2.1
     */
    public <T> boolean setAuthData(final Type type, final T data)
    {
        if (data == null || type.isAssignable(data.getClass()))
        {
            authenticationData.put(type, data);
            return true;
        }
        return false;
    }

    /**
     * Gets a data from the collection.
     * @param type The Type to retrieve.
     * @return the data associated with the Type.
     * @since 2.1
     */
    public <T> T getAuthData(final Type type)
    {
        final T result;
        Object data = authenticationData.get(type);
        if (data != null && type.isAssignable(data.getClass()))
        {
            @SuppressWarnings("unchecked")
            T checked = (T) data;
            result = checked;
        }
        else
        {
            result = null;
        }
        
        return result;
    }

    /**
     * Deletes all data stored within this authenticator.
     */
    public void cleanup()
    {
        for(Object data : authenticationData.values())
        {
            if (data == null)
            {
                continue;
            }
            
            // step 1: nullify arrays
            if (data.getClass().isArray())
            {
                int length = Array.getLength(data);
                if (length == 0)
                {
                    continue;
                }
                if (data.getClass().getComponentType().isPrimitive())
                {
                    if (data.getClass() == char[].class)
                    {
                        Arrays.fill((char[])data, '\0');
                    }
                    else if (data.getClass() == byte[].class)
                    {
                        Arrays.fill((byte[])data, (byte)0);
                    }
                    else if (data.getClass() == short[].class)
                    {
                        Arrays.fill((short[])data, (short)0);
                    }
                    else if (data.getClass() == int[].class)
                    {
                        Arrays.fill((int[])data, 0);
                    }
                    else if (data.getClass() == long[].class)
                    {
                        Arrays.fill((long[])data, 0L);
                    }
                    else if (data.getClass() == float[].class)
                    {
                        Arrays.fill((float[])data, 0.0f);
                    }
                    else if (data.getClass() == double[].class)
                    {
                        Arrays.fill((double[])data, 0.0);
                    }
                    else if (data.getClass() == boolean[].class)
                    {
                        Arrays.fill((boolean[])data, false);
                    }
                }
                else
                {
                    Arrays.fill((Object[])data, null);
                }
            }
        }

        // step 2: allow data itself to gc
        authenticationData.clear();
    }
}
