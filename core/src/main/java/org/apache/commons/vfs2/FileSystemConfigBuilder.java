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
 *
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 */
public abstract class FileSystemConfigBuilder
{
    /** Default prefix to use when resolving system properties */
    private static final String PREFIX = "vfs.";

    /** The root uri of the file system */
    private static final String ROOTURI = "rootURI";

    /** The prefix to use when resolving system properties */
    private final String prefix;

    protected FileSystemConfigBuilder()
    {
        this.prefix = PREFIX;
    }
    /** @since 2.0 */
    protected FileSystemConfigBuilder(String component)
    {
        this.prefix = PREFIX + component;
    }

    /**
     * The root URI of the file system.
     * @param opts The FileSystem options
     * @param rootURI The creator name to be associated with the file.
     * @since 2.0
     */
    public void setRootURI(FileSystemOptions opts, String rootURI)
    {
        setParam(opts, ROOTURI, rootURI);
    }

    /**
     * Return the root URI of the file system.
     * @param opts The FileSystem options
     * @return The root URI.
     * @since 2.0
     */
    public String getRootURI(FileSystemOptions opts)
    {
        return getString(opts, ROOTURI);
    }


    protected void setParam(FileSystemOptions opts, String name, Object value)
    {
        opts.setOption(getConfigClass(), name, value);
    }

    protected Object getParam(FileSystemOptions opts, String name)
    {
        if (opts == null)
        {
            return null;
        }

        return opts.getOption(getConfigClass(), name);
    }

    protected boolean hasParam(FileSystemOptions opts, String name)
    {
        return opts != null && opts.hasOption(getConfigClass(), name);
    }

    /** @since 2.0 */
    protected boolean hasObject(FileSystemOptions opts, String name)
    {
        return hasParam(opts, name) || System.getProperties().containsKey(PREFIX + name);
    }

    /** @since 2.0 */
    protected Boolean getBoolean(FileSystemOptions opts, String name)
    {
        return getBoolean(opts, name, null);
    }

    /** @since 2.0 */
    protected boolean getBoolean(FileSystemOptions opts, String name, boolean defaultValue)
    {
        return getBoolean(opts, name, new Boolean(defaultValue)).booleanValue();
    }

    /** @since 2.0 */
    protected Boolean getBoolean(FileSystemOptions opts, String name, Boolean defaultValue)
    {
        Boolean value = (Boolean) getParam(opts, name);
        if (value == null)
        {
            String str = System.getProperty(PREFIX + name);
            if (str == null)
            {
                return defaultValue;
            }
            value = Boolean.valueOf(str);
        }
        return value;
    }

    /** @since 2.0 */
    protected Byte getByte(FileSystemOptions opts, String name)
    {
        return getByte(opts, name, null);
    }

    /** @since 2.0 */
    protected byte getByte(FileSystemOptions opts, String name, byte defaultValue)
    {
        return getByte(opts, name, new Byte(defaultValue)).byteValue();
    }

    /** @since 2.0 */
    protected Byte getByte(FileSystemOptions opts, String name, Byte defaultValue)
    {
        Byte value = (Byte) getParam(opts, name);
        if (value == null)
        {
            String str = System.getProperty(this.prefix + name);
            if (str == null)
            {
                return defaultValue;
            }
            value = Byte.valueOf(str);
        }
        return value;
    }

    /** @since 2.0 */
    protected Character getCharacter(FileSystemOptions opts, String name)
    {
        return getCharacter(opts, name, null);
    }

    /** @since 2.0 */
    protected char getCharacter(FileSystemOptions opts, String name, char defaultValue)
    {
        return getCharacter(opts, name, new Character(defaultValue)).charValue();
    }

    /** @since 2.0 */
    protected Character getCharacter(FileSystemOptions opts, String name, Character defaultValue)
    {
        Character value = (Character) getParam(opts, name);
        if (value == null)
        {
            String str = System.getProperty(this.prefix + name);
            if (str == null || str.length() <= 0)
            {
                return defaultValue;
            }
            value = new Character(str.charAt(0));
        }
        return value;
    }

    /** @since 2.0 */
    protected Double getDouble(FileSystemOptions opts, String name)
    {
        return getDouble(opts, name, null);
    }

    /** @since 2.0 */
    protected double getDouble(FileSystemOptions opts, String name, double defaultValue)
    {
        return getDouble(opts, name, new Double(defaultValue)).doubleValue();
    }

    /** @since 2.0 */
    protected Double getDouble(FileSystemOptions opts, String name, Double defaultValue)
    {
        Double value = (Double) getParam(opts, name);
        if (value == null)
        {
            String str = System.getProperty(this.prefix + name);
            if (str == null || str.length() <= 0)
            {
                return defaultValue;
            }
            value = Double.valueOf(str);
        }
        return value;
    }

    /** @since 2.0 */
    protected Float getFloat(FileSystemOptions opts, String name)
    {
        return getFloat(opts, name, null);
    }

    /** @since 2.0 */
    protected float getFloat(FileSystemOptions opts, String name, float defaultValue)
    {
        return getFloat(opts, name, new Float(defaultValue)).floatValue();
    }

    /** @since 2.0 */
    protected Float getFloat(FileSystemOptions opts, String name, Float defaultValue)
    {
        Float value = (Float) getParam(opts, name);
        if (value == null)
        {
            String str = System.getProperty(this.prefix + name);
            if (str == null || str.length() <= 0)
            {
                return defaultValue;
            }
            value = Float.valueOf(str);
        }
        return value;
    }

    /** @since 2.0 */
    protected Integer getInteger(FileSystemOptions opts, String name)
    {
        return getInteger(opts, name, null);
    }

    /** @since 2.0 */
    protected int getInteger(FileSystemOptions opts, String name, int defaultValue)
    {
        return getInteger(opts, name, Integer.valueOf(defaultValue)).intValue();
    }

    /** @since 2.0 */
    protected Integer getInteger(FileSystemOptions opts, String name, Integer defaultValue)
    {
        Integer value = (Integer) getParam(opts, name);
        if (value == null)
        {
            String str = System.getProperty(this.prefix + name);
            if (str == null)
            {
                return defaultValue;
            }
            value = Integer.valueOf(str);
        }
        return value;
    }

    /** @since 2.0 */
    protected Long getLong(FileSystemOptions opts, String name)
    {
        return getLong(opts, name, null);
    }

    /** @since 2.0 */
    protected long getLong(FileSystemOptions opts, String name, long defaultValue)
    {
        return getLong(opts, name, Long.valueOf(defaultValue)).longValue();
    }

    /** @since 2.0 */
    protected Long getLong(FileSystemOptions opts, String name, Long defaultValue)
    {
        Long value = (Long) getParam(opts, name);
        if (value == null)
        {
            String str = System.getProperty(this.prefix + name);
            if (str == null)
            {
                return defaultValue;
            }
            value = Long.valueOf(str);
        }
        return value;
    }

    /** @since 2.0 */
    protected Short getShort(FileSystemOptions opts, String name)
    {
        return getShort(opts, name, null);
    }

    /** @since 2.0 */
    protected short getShort(FileSystemOptions opts, String name, short defaultValue)
    {
        return getShort(opts, name, new Short(defaultValue)).shortValue();
    }

    /** @since 2.0 */
    protected Short getShort(FileSystemOptions opts, String name, Short defaultValue)
    {
        Short value = (Short) getParam(opts, name);
        if (value == null)
        {
            String str = System.getProperty(this.prefix + name);
            if (str == null)
            {
                return defaultValue;
            }
            value = Short.valueOf(str);
        }
        return value;
    }

    /** @since 2.0 */
    protected String getString(FileSystemOptions opts, String name)
    {
        return getString(opts, name, null);
    }

    /** @since 2.0 */
    protected String getString(FileSystemOptions opts, String name, String defaultValue)
    {
        String value = (String) getParam(opts, name);
        if (value == null)
        {
            value = System.getProperty(this.prefix + name);
            if (value == null)
            {
                return defaultValue;
            }
        }
        return value;
    }

    protected abstract Class<? extends FileSystem> getConfigClass();
}
