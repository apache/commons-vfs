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

import java.util.Objects;

/**
 * Abstract class which has the right to fill FileSystemOptions.
 */
public abstract class FileSystemConfigBuilder {

    /** Default prefix to use when resolving system properties */
    private static final String PREFIX = "vfs.";

    /** The root URI of the file system */
    private static final String ROOTURI = "rootURI";

    /** The prefix to use when resolving system properties */
    private final String prefix;

    /**
     * Constructs builder with default prefix.
     *
     * @since 1.0
     */
    protected FileSystemConfigBuilder() {
        this.prefix = PREFIX;
    }

    /**
     * Constructs builder with specified component name.
     *
     * @param component component name to be used in prefix
     *
     * @since 2.0
     */
    protected FileSystemConfigBuilder(final String component) {
        this.prefix = PREFIX + component;
    }

    /**
     * Gets named option as boolean.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @return the option in {@code opts} or system properties, otherwise null
     * @see #getBoolean(FileSystemOptions, String, Boolean)
     *
     * @since 2.0
     */
    protected Boolean getBoolean(final FileSystemOptions fileSystemOptions, final String name) {
        return getBoolean(fileSystemOptions, name, null);
    }

    /**
     * Gets named option as boolean.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @see #getBoolean(FileSystemOptions, String, Boolean)
     *
     * @since 2.0
     */
    protected boolean getBoolean(final FileSystemOptions fileSystemOptions, final String name,
        final boolean defaultValue) {
        return getBoolean(fileSystemOptions, name, Boolean.valueOf(defaultValue)).booleanValue();
    }

    /**
     * Gets named option as boolean.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @see #getBoolean(FileSystemOptions, String, Boolean)
     *
     * @since 2.0
     */
    protected Boolean getBoolean(final FileSystemOptions fileSystemOptions, final String name,
        final Boolean defaultValue) {
        Boolean value = (Boolean) getParam(fileSystemOptions, name);
        if (value == null) {
            final String str = getProperty(name);
            if (str == null) {
                return defaultValue;
            }
            value = Boolean.valueOf(str);
        }
        return value;
    }

    /**
     * Gets named option as byte.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @return the option in {@code opts} or system properties, otherwise null
     * @see #getByte(FileSystemOptions, String, Byte)
     *
     * @since 2.0
     */
    protected Byte getByte(final FileSystemOptions fileSystemOptions, final String name) {
        return getByte(fileSystemOptions, name, null);
    }

    /**
     * Gets named option as byte.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @see #getByte(FileSystemOptions, String, Byte)
     *
     * @since 2.0
     */
    protected byte getByte(final FileSystemOptions fileSystemOptions, final String name, final byte defaultValue) {
        return getByte(fileSystemOptions, name, Byte.valueOf(defaultValue)).byteValue();
    }

    /**
     * Gets named option as byte.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     *
     * @since 2.0
     */
    protected Byte getByte(final FileSystemOptions fileSystemOptions, final String name, final Byte defaultValue) {
        Byte value = (Byte) getParam(fileSystemOptions, name);
        if (value == null) {
            final String str = getProperty(name);
            if (str == null) {
                return defaultValue;
            }
            value = Byte.valueOf(str);
        }
        return value;
    }

    /**
     * Gets named option as character.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @return the option in {@code opts} or system properties, otherwise null
     * @see #getCharacter(FileSystemOptions, String, Character)
     *
     * @since 2.0
     */
    protected Character getCharacter(final FileSystemOptions fileSystemOptions, final String name) {
        return getCharacter(fileSystemOptions, name, null);
    }

    /**
     * Gets named option as character.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @see #getCharacter(FileSystemOptions, String, Character)
     *
     * @since 2.0
     */
    protected char getCharacter(final FileSystemOptions fileSystemOptions, final String name, final char defaultValue) {
        return getCharacter(fileSystemOptions, name, new Character(defaultValue)).charValue();
    }

    /**
     * Gets named option as character.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     *
     * @since 2.0
     */
    protected Character getCharacter(final FileSystemOptions fileSystemOptions, final String name,
        final Character defaultValue) {
        Character value = (Character) getParam(fileSystemOptions, name);
        if (value == null) {
            final String str = getProperty(name);
            if (str == null || str.length() <= 0) {
                return defaultValue;
            }
            value = new Character(str.charAt(0));
        }
        return value;
    }

    /**
     * Gets the target of this configuration.
     *
     * @return the specific file system class
     *
     * @since 1.0
     */
    protected abstract Class<? extends FileSystem> getConfigClass();

    /**
     * Gets named option as double.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @return the option in {@code opts} or system properties, otherwise null
     * @see #getDouble(FileSystemOptions, String, Double)
     *
     * @since 2.0
     */
    protected Double getDouble(final FileSystemOptions fileSystemOptions, final String name) {
        return getDouble(fileSystemOptions, name, null);
    }

    /**
     * Gets named option as double.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @see #getDouble(FileSystemOptions, String, Double)
     *
     * @since 2.0
     */
    protected double getDouble(final FileSystemOptions fileSystemOptions, final String name,
        final double defaultValue) {
        return getDouble(fileSystemOptions, name, new Double(defaultValue)).doubleValue();
    }

    /**
     * Gets named option as double.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     *
     * @since 2.0
     */
    protected Double getDouble(final FileSystemOptions fileSystemOptions, final String name,
        final Double defaultValue) {
        Double value = (Double) getParam(fileSystemOptions, name);
        if (value == null) {
            final String str = getProperty(name);
            if (str == null || str.length() <= 0) {
                return defaultValue;
            }
            value = Double.valueOf(str);
        }
        return value;
    }

    /**
     * Gets named option as enumeration.
     *
     * @param <E> enumeration type
     * @param enumClass class of enumeration type
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name *
     * @return the option in {@code opts} or system properties, otherwise null
     * @see #getEnum(Class, FileSystemOptions, String, Enum)
     * @throws IllegalArgumentException if option value is not a known enumeration.
     *
     * @since 2.1
     */
    protected <E extends Enum<E>> E getEnum(final Class<E> enumClass, final FileSystemOptions fileSystemOptions,
        final String name) {
        return this.<E>getEnum(enumClass, fileSystemOptions, name, null);
    }

    /**
     * Gets named option as enumeration.
     *
     * @param <E> enumeration type
     * @param enumClass class of enumeration type
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @see #getEnum(Class, FileSystemOptions, String, Enum)
     * @throws IllegalArgumentException if option value is not a known enumeration.
     *
     * @since 2.1
     */
    protected <E extends Enum<E>> E getEnum(final Class<E> enumClass, final FileSystemOptions fileSystemOptions,
        final String name, final E defaultValue) {
        @SuppressWarnings("unchecked")
        E value = (E) getParam(fileSystemOptions, name);
        if (value == null) {
            final String str = getProperty(name);
            if (str == null) {
                return defaultValue;
            }
            value = Enum.valueOf(enumClass, str);
        }
        return value;
    }

    /**
     * Gets named option as float.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @return the option in {@code opts} or system properties, otherwise null
     * @see #getFloat(FileSystemOptions, String, Float)
     * @throws NumberFormatException if option value is not a valid float.
     *
     * @since 2.0
     */
    protected Float getFloat(final FileSystemOptions fileSystemOptions, final String name) {
        return getFloat(fileSystemOptions, name, null);
    }

    /**
     * Gets named option as float.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @see #getFloat(FileSystemOptions, String, Float)
     * @throws NumberFormatException if option value is not a valid float.
     *
     * @since 2.0
     */
    protected float getFloat(final FileSystemOptions fileSystemOptions, final String name, final float defaultValue) {
        return getFloat(fileSystemOptions, name, new Float(defaultValue)).floatValue();
    }

    /**
     * Gets named option as float.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @throws NumberFormatException if option value is not a valid float.
     *
     * @since 2.0
     */
    protected Float getFloat(final FileSystemOptions fileSystemOptions, final String name, final Float defaultValue) {
        Float value = (Float) getParam(fileSystemOptions, name);
        if (value == null) {
            final String str = getProperty(name);
            if (str == null || str.length() <= 0) {
                return defaultValue;
            }
            value = Float.valueOf(str);
        }
        return value;
    }

    /**
     * Gets named option as integer.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @return the option in {@code opts} or system properties, otherwise null
     * @see #getInteger(FileSystemOptions, String, Integer)
     * @throws NumberFormatException if option value is not a valid integer.
     *
     * @since 2.0
     */
    protected Integer getInteger(final FileSystemOptions fileSystemOptions, final String name) {
        return getInteger(fileSystemOptions, name, null);
    }

    /**
     * Gets named option as integer.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @see #getInteger(FileSystemOptions, String, Integer)
     * @throws NumberFormatException if option value is not a valid integer.
     *
     * @since 2.0
     */
    protected int getInteger(final FileSystemOptions fileSystemOptions, final String name, final int defaultValue) {
        return getInteger(fileSystemOptions, name, Integer.valueOf(defaultValue)).intValue();
    }

    /**
     * Gets named option as integer.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @throws NumberFormatException if option value is not a valid integer.
     *
     * @since 2.0
     */
    protected Integer getInteger(final FileSystemOptions fileSystemOptions, final String name,
        final Integer defaultValue) {
        Integer value = (Integer) getParam(fileSystemOptions, name);
        if (value == null) {
            final String str = getProperty(name);
            if (str == null) {
                return defaultValue;
            }
            value = Integer.valueOf(str);
        }
        return value;
    }

    /**
     * Gets named option as long.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @return the option in {@code opts} or system properties, otherwise null
     * @see #getLong(FileSystemOptions, String, Long)
     * @throws NumberFormatException if option value is not a valid long.
     *
     * @since 2.0
     */
    protected Long getLong(final FileSystemOptions fileSystemOptions, final String name) {
        return getLong(fileSystemOptions, name, null);
    }

    /**
     * Gets named option as long.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @see #getLong(FileSystemOptions, String, Long)
     * @throws NumberFormatException if option value is not a valid long.
     *
     * @since 2.0
     */
    protected long getLong(final FileSystemOptions fileSystemOptions, final String name, final long defaultValue) {
        return getLong(fileSystemOptions, name, Long.valueOf(defaultValue)).longValue();
    }

    /**
     * Gets named option as long.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @throws NumberFormatException if option value is not a valid long.
     *
     * @since 2.0
     */
    protected Long getLong(final FileSystemOptions fileSystemOptions, final String name, final Long defaultValue) {
        Long value = (Long) getParam(fileSystemOptions, name);
        if (value == null) {
            final String str = getProperty(name);
            if (str == null) {
                return defaultValue;
            }
            value = Long.valueOf(str);
        }
        return value;
    }

    /**
     * Gets named parameter.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name get option with this name
     * @return the named option or null
     *
     * @since 1.0
     */
    protected Object getParam(final FileSystemOptions fileSystemOptions, final String name) {
        return fileSystemOptions == null ? null : fileSystemOptions.getOption(getConfigClass(), name);
    }

    /**
     * Gets the system property for the given name.
     *
     * @param name The name to lookup combined with the prefix.
     * @return a system property or null
     *
     * @since 2.1
     */
    private String getProperty(final String name) {
        return System.getProperty(toPropertyKey(name));
    }

    /**
     * Gets the root URI of the file system.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @return The root URI
     *
     * @since 2.0
     */
    public String getRootURI(final FileSystemOptions fileSystemOptions) {
        return getString(fileSystemOptions, ROOTURI);
    }

    /**
     * Gets named option as short.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @return the option in {@code opts} or system properties, otherwise null
     * @see #getShort(FileSystemOptions, String, Short)
     * @throws NumberFormatException if option value is not a valid short.
     *
     * @since 2.0
     */
    protected Short getShort(final FileSystemOptions fileSystemOptions, final String name) {
        return getShort(fileSystemOptions, name, null);
    }

    /**
     * Gets named option as short.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @see #getShort(FileSystemOptions, String, Short)
     * @throws NumberFormatException if option value is not a valid short
     *
     * @since 2.0
     */
    protected short getShort(final FileSystemOptions fileSystemOptions, final String name, final short defaultValue) {
        return getShort(fileSystemOptions, name, Short.valueOf(defaultValue)).shortValue();
    }

    /**
     * Gets named option as short.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @throws NumberFormatException if option value is not a valid short
     *
     * @since 2.0
     */
    protected Short getShort(final FileSystemOptions fileSystemOptions, final String name, final Short defaultValue) {
        Short value = (Short) getParam(fileSystemOptions, name);
        if (value == null) {
            final String str = getProperty(name);
            if (str == null) {
                return defaultValue;
            }
            value = Short.valueOf(str);
        }
        return value;
    }

    /**
     * Gets named option as String.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @return the option in {@code opts} or system properties, otherwise null
     * @see #getString(FileSystemOptions, String, String)
     *
     * @since 2.0
     */
    protected String getString(final FileSystemOptions fileSystemOptions, final String name) {
        return getString(fileSystemOptions, name, null);
    }

    /**
     * Gets named option as String.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     *
     * @since 2.0
     */
    protected String getString(final FileSystemOptions fileSystemOptions, final String name,
        final String defaultValue) {
        String value = (String) getParam(fileSystemOptions, name);
        if (value == null) {
            value = getProperty(name);
            if (value == null) {
                return defaultValue;
            }
        }
        return value;
    }

    /**
     * Checks the named setting specified.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the option to check in {@code opts} or system properties
     * @return true if option exists
     *
     * @since 2.0
     */
    protected boolean hasObject(final FileSystemOptions fileSystemOptions, final String name) {
        return hasParam(fileSystemOptions, name) || System.getProperties().containsKey(toPropertyKey(name));
    }

    /**
     * Checks if option exists.
     *
     * @param fileSystemOptions file system options to query, may be null.
     * @param name the name to look up in {@code opts}
     * @return true if opts have the named parameter
     *
     * @since 1.0
     */
    protected boolean hasParam(final FileSystemOptions fileSystemOptions, final String name) {
        return fileSystemOptions != null && fileSystemOptions.hasOption(getConfigClass(), name);
    }

    /**
     * Sets the named parameter.
     *
     * @param fileSystemOptions the file system options to modify
     * @param name set option with this name
     * @param value boolean value to set
     *
     * @since 2.1
     */
    protected void setParam(final FileSystemOptions fileSystemOptions, final String name, final boolean value) {
        setParam(fileSystemOptions, name, Boolean.valueOf(value));
    }

    /**
     * Sets the named parameter.
     *
     * @param fileSystemOptions the file system options to modify
     * @param name set option with this name
     * @param value object value to set
     *
     * @since 1.0
     */
    protected void setParam(final FileSystemOptions fileSystemOptions, final String name, final Object value) {
        Objects.requireNonNull(fileSystemOptions, "fileSystemOptions").setOption(getConfigClass(), name, value);
    }

    /**
     * Sets the root URI of the file system.
     *
     * @param fileSystemOptions the file system options to modify
     * @param rootURI The creator name to be associated with the file.
     *
     * @since 2.0
     */
    public void setRootURI(final FileSystemOptions fileSystemOptions, final String rootURI) {
        setParam(fileSystemOptions, ROOTURI, rootURI);
    }

    /**
     * Converts the given primitive boolean to a Boolean object.
     *
     * @param value a primitive boolean.
     * @return the given primitive boolean as Boolean object.
     * @since 2.7.0
     */
    protected Boolean toBooleanObject(final boolean value) {
        return value ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Converts the given name into a System property key.
     *
     * @param name a name to combine with the builder prefix
     * @return name of system property
     *
     * @since 2.1
     */
    private String toPropertyKey(final String name) {
        return this.prefix + name;
    }

}
