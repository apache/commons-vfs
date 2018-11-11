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

/**
 * Abstract class which has the right to fill FileSystemOptions.
 */
public abstract class FileSystemConfigBuilder {
    /** Default prefix to use when resolving system properties */
    private static final String PREFIX = "vfs.";

    /** The root uri of the file system */
    private static final String ROOTURI = "rootURI";

    /** The prefix to use when resolving system properties */
    private final String prefix;

    /**
     * Construct builder with default prefix.
     *
     * @since 1.0
     */
    protected FileSystemConfigBuilder() {
        this.prefix = PREFIX;
    }

    /**
     * Construct builder with specified component name.
     *
     * @param component component name to be used in prefix
     *
     * @since 2.0
     */
    protected FileSystemConfigBuilder(final String component) {
        this.prefix = PREFIX + component;
    }

    /**
     * The root URI of the file system.
     *
     * @param opts the file system options to modify
     * @param rootURI The creator name to be associated with the file.
     *
     * @since 2.0
     */
    public void setRootURI(final FileSystemOptions opts, final String rootURI) {
        setParam(opts, ROOTURI, rootURI);
    }

    /**
     * Return the root URI of the file system.
     *
     * @param opts file system options to work with
     * @return The root URI
     *
     * @since 2.0
     */
    public String getRootURI(final FileSystemOptions opts) {
        return getString(opts, ROOTURI);
    }

    /**
     * Set named parameter.
     *
     * @param opts the file system options to modify
     * @param name set option with this name
     * @param value boolean value to set
     *
     * @since 2.1
     */
    protected void setParam(final FileSystemOptions opts, final String name, final boolean value) {
        setParam(opts, name, Boolean.valueOf(value));
    }

    /**
     * Set named parameter.
     *
     * @param opts the file system options to modify
     * @param name set option with this name
     * @param value object value to set
     *
     * @since 1.0
     */
    protected void setParam(final FileSystemOptions opts, final String name, final Object value) {
        opts.setOption(getConfigClass(), name, value);
    }

    /**
     * Get named parameter.
     *
     * @param opts file system options to work with
     * @param name get option with this name
     * @return the named option or null
     *
     * @since 1.0
     */
    protected Object getParam(final FileSystemOptions opts, final String name) {
        if (opts == null) {
            return null;
        }

        return opts.getOption(getConfigClass(), name);
    }

    /**
     * Check if option exists.
     *
     * @param opts file system options to work with
     * @param name the name to look up in {@code opts}
     * @return true if opts have the named parameter
     *
     * @since 1.0
     */
    protected boolean hasParam(final FileSystemOptions opts, final String name) {
        return opts != null && opts.hasOption(getConfigClass(), name);
    }

    /**
     * Is named setting specified.
     *
     * @param opts file system options to work with
     * @param name the option to check in {@code opts} or system properties
     * @return true if option exists
     *
     * @since 2.0
     */
    protected boolean hasObject(final FileSystemOptions opts, final String name) {
        return hasParam(opts, name) || System.getProperties().containsKey(toPropertyKey(name));
    }

    /**
     * Get named option as boolean.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @return the option in {@code opts} or system properties, otherwise null
     * @see #getBoolean(FileSystemOptions, String, Boolean)
     *
     * @since 2.0
     */
    protected Boolean getBoolean(final FileSystemOptions opts, final String name) {
        return getBoolean(opts, name, null);
    }

    /**
     * Get named option as boolean.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @see #getBoolean(FileSystemOptions, String, Boolean)
     *
     * @since 2.0
     */
    protected boolean getBoolean(final FileSystemOptions opts, final String name, final boolean defaultValue) {
        return getBoolean(opts, name, Boolean.valueOf(defaultValue)).booleanValue();
    }

    /**
     * Get named option as boolean.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @see #getBoolean(FileSystemOptions, String, Boolean)
     *
     * @since 2.0
     */
    protected Boolean getBoolean(final FileSystemOptions opts, final String name, final Boolean defaultValue) {
        Boolean value = (Boolean) getParam(opts, name);
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
     * Get named option as byte.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @return the option in {@code opts} or system properties, otherwise null
     * @see #getByte(FileSystemOptions, String, Byte)
     *
     * @since 2.0
     */
    protected Byte getByte(final FileSystemOptions opts, final String name) {
        return getByte(opts, name, null);
    }

    /**
     * Get named option as byte.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @see #getByte(FileSystemOptions, String, Byte)
     *
     * @since 2.0
     */
    protected byte getByte(final FileSystemOptions opts, final String name, final byte defaultValue) {
        return getByte(opts, name, Byte.valueOf(defaultValue)).byteValue();
    }

    /**
     * Get named option as byte.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     *
     * @since 2.0
     */
    protected Byte getByte(final FileSystemOptions opts, final String name, final Byte defaultValue) {
        Byte value = (Byte) getParam(opts, name);
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
     * Get named option as character.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @return the option in {@code opts} or system properties, otherwise null
     * @see #getCharacter(FileSystemOptions, String, Character)
     *
     * @since 2.0
     */
    protected Character getCharacter(final FileSystemOptions opts, final String name) {
        return getCharacter(opts, name, null);
    }

    /**
     * Get named option as character.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @see #getCharacter(FileSystemOptions, String, Character)
     *
     * @since 2.0
     */
    protected char getCharacter(final FileSystemOptions opts, final String name, final char defaultValue) {
        return getCharacter(opts, name, new Character(defaultValue)).charValue();
    }

    /**
     * Get named option as character.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     *
     * @since 2.0
     */
    protected Character getCharacter(final FileSystemOptions opts, final String name, final Character defaultValue) {
        Character value = (Character) getParam(opts, name);
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
     * Get named option as double.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @return the option in {@code opts} or system properties, otherwise null
     * @see #getDouble(FileSystemOptions, String, Double)
     *
     * @since 2.0
     */
    protected Double getDouble(final FileSystemOptions opts, final String name) {
        return getDouble(opts, name, null);
    }

    /**
     * Get named option as double.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @see #getDouble(FileSystemOptions, String, Double)
     *
     * @since 2.0
     */
    protected double getDouble(final FileSystemOptions opts, final String name, final double defaultValue) {
        return getDouble(opts, name, new Double(defaultValue)).doubleValue();
    }

    /**
     * Get named option as double.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     *
     * @since 2.0
     */
    protected Double getDouble(final FileSystemOptions opts, final String name, final Double defaultValue) {
        Double value = (Double) getParam(opts, name);
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
     * Get named option as enumeration.
     *
     * @param <E> enumeration type
     * @param enumClass class of enumeration type
     * @param opts file system options to work with
     * @param name the option name *
     * @return the option in {@code opts} or system properties, otherwise null
     * @see #getEnum(Class, FileSystemOptions, String, Enum)
     * @throws IllegalArgumentException if option value is not a known enumeration.
     *
     * @since 2.1
     */
    protected <E extends Enum<E>> E getEnum(final Class<E> enumClass, final FileSystemOptions opts, final String name) {
        return this.<E>getEnum(enumClass, opts, name, null);
    }

    /**
     * Get named option as enumeration.
     *
     * @param <E> enumeration type
     * @param enumClass class of enumeration type
     * @param opts file system options to work with
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @see #getEnum(Class, FileSystemOptions, String, Enum)
     * @throws IllegalArgumentException if option value is not a known enumeration.
     *
     * @since 2.1
     */
    protected <E extends Enum<E>> E getEnum(final Class<E> enumClass, final FileSystemOptions opts, final String name,
            final E defaultValue) {
        @SuppressWarnings("unchecked")
        E value = (E) getParam(opts, name);
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
     * Get named option as float.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @return the option in {@code opts} or system properties, otherwise null
     * @see #getFloat(FileSystemOptions, String, Float)
     * @throws NumberFormatException if option value is not a valid float.
     *
     * @since 2.0
     */
    protected Float getFloat(final FileSystemOptions opts, final String name) {
        return getFloat(opts, name, null);
    }

    /**
     * Get named option as float.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @see #getFloat(FileSystemOptions, String, Float)
     * @throws NumberFormatException if option value is not a valid float.
     *
     * @since 2.0
     */
    protected float getFloat(final FileSystemOptions opts, final String name, final float defaultValue) {
        return getFloat(opts, name, new Float(defaultValue)).floatValue();
    }

    /**
     * Get named option as float.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @throws NumberFormatException if option value is not a valid float.
     *
     * @since 2.0
     */
    protected Float getFloat(final FileSystemOptions opts, final String name, final Float defaultValue) {
        Float value = (Float) getParam(opts, name);
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
     * Get named option as integer.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @return the option in {@code opts} or system properties, otherwise null
     * @see #getInteger(FileSystemOptions, String, Integer)
     * @throws NumberFormatException if option value is not a valid integer.
     *
     * @since 2.0
     */
    protected Integer getInteger(final FileSystemOptions opts, final String name) {
        return getInteger(opts, name, null);
    }

    /**
     * Get named option as integer.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @see #getInteger(FileSystemOptions, String, Integer)
     * @throws NumberFormatException if option value is not a valid integer.
     *
     * @since 2.0
     */
    protected int getInteger(final FileSystemOptions opts, final String name, final int defaultValue) {
        return getInteger(opts, name, Integer.valueOf(defaultValue)).intValue();
    }

    /**
     * Get named option as integer.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @throws NumberFormatException if option value is not a valid integer.
     *
     * @since 2.0
     */
    protected Integer getInteger(final FileSystemOptions opts, final String name, final Integer defaultValue) {
        Integer value = (Integer) getParam(opts, name);
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
     * Get named option as long.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @return the option in {@code opts} or system properties, otherwise null
     * @see #getLong(FileSystemOptions, String, Long)
     * @throws NumberFormatException if option value is not a valid long.
     *
     * @since 2.0
     */
    protected Long getLong(final FileSystemOptions opts, final String name) {
        return getLong(opts, name, null);
    }

    /**
     * Get named option as long.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @see #getLong(FileSystemOptions, String, Long)
     * @throws NumberFormatException if option value is not a valid long.
     *
     * @since 2.0
     */
    protected long getLong(final FileSystemOptions opts, final String name, final long defaultValue) {
        return getLong(opts, name, Long.valueOf(defaultValue)).longValue();
    }

    /**
     * Get named option as long.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @throws NumberFormatException if option value is not a valid long.
     *
     * @since 2.0
     */
    protected Long getLong(final FileSystemOptions opts, final String name, final Long defaultValue) {
        Long value = (Long) getParam(opts, name);
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
     * Get named option as short.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @return the option in {@code opts} or system properties, otherwise null
     * @see #getShort(FileSystemOptions, String, Short)
     * @throws NumberFormatException if option value is not a valid short.
     *
     * @since 2.0
     */
    protected Short getShort(final FileSystemOptions opts, final String name) {
        return getShort(opts, name, null);
    }

    /**
     * Get named option as short.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @see #getShort(FileSystemOptions, String, Short)
     * @throws NumberFormatException if option value is not a valid short
     *
     * @since 2.0
     */
    protected short getShort(final FileSystemOptions opts, final String name, final short defaultValue) {
        return getShort(opts, name, Short.valueOf(defaultValue)).shortValue();
    }

    /**
     * Get named option as short.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     * @throws NumberFormatException if option value is not a valid short
     *
     * @since 2.0
     */
    protected Short getShort(final FileSystemOptions opts, final String name, final Short defaultValue) {
        Short value = (Short) getParam(opts, name);
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
     * Get named option as String.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @return the option in {@code opts} or system properties, otherwise null
     * @see #getString(FileSystemOptions, String, String)
     *
     * @since 2.0
     */
    protected String getString(final FileSystemOptions opts, final String name) {
        return getString(opts, name, null);
    }

    /**
     * Get named option as String.
     *
     * @param opts file system options to work with
     * @param name the option name
     * @param defaultValue value to return if option is not present
     * @return the option in {@code opts} or system properties, otherwise {@code defaultValue}
     *
     * @since 2.0
     */
    protected String getString(final FileSystemOptions opts, final String name, final String defaultValue) {
        String value = (String) getParam(opts, name);
        if (value == null) {
            value = getProperty(name);
            if (value == null) {
                return defaultValue;
            }
        }
        return value;
    }

    /**
     * Get the target of this configuration.
     *
     * @return the specific file system class
     *
     * @since 1.0
     */
    protected abstract Class<? extends FileSystem> getConfigClass();

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

    /**
     * Get the system property for the given name.
     *
     * @param name The name to lookup combined with the prefix.
     * @return a system property or null
     *
     * @since 2.1
     */
    private String getProperty(final String name) {
        return System.getProperty(toPropertyKey(name));
    }

}
