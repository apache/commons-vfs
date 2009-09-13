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
package org.apache.commons.vfs;

/**
 * Abstract class which has the right to fill FileSystemOptions.
 *
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 * @version $Revision$ $Date$
 */
public class DefaultFileSystemOptions extends FileSystemOptions
{
    /** Default prefix to use when resolving system properties */
    private static final String PREFIX = "vfs.";

    /** The root uri of the file system */
    private static final String ROOTURI = "rootURI";

    /** The prefix to use when resolving system properties */
    private final String prefix;

    /**
     *
     */
    protected DefaultFileSystemOptions()
    {
        this.prefix = PREFIX;
    }

    /**
     * @param component The component String;
     */
    protected DefaultFileSystemOptions(String component)
    {
        this.prefix = PREFIX + component;
    }

    /**
     * The root URI of the file system.
     * @param rootURI The creator name to be associated with the file.
     */
    public void setRootURI(String rootURI)
    {
        setParam(ROOTURI, rootURI);
    }

    /**
     * Return the root URI of the file system.
     * @return The root URI.
     */
    public String getRootURI()
    {
        return getString(ROOTURI);
    }

    /**
     * Sets the user authenticator to get authentication informations.
     * @param userAuthenticator The UserAuthenticator.
     * @throws FileSystemException if an error occurs setting the UserAuthenticator.
     */
    public void setUserAuthenticator(UserAuthenticator userAuthenticator)
            throws FileSystemException
    {
        setParam("userAuthenticator", userAuthenticator);
    }

    /**
     * @see #setUserAuthenticator
     * @return The UserAuthenticator.
     */
    public UserAuthenticator getUserAuthenticator()
    {
        return (UserAuthenticator) getParam("userAuthenticator");
    }

    protected void setParam(String name, Object value)
    {
        setOption(this.getClass(), name, value);
    }

    protected Object getParam(String name)
    {
        return getOption(this.getClass(), name);
    }

    protected boolean hasParam(String name)
    {
        return hasOption(this.getClass(), name);
    }

    protected boolean hasObject(String name)
    {
        return hasParam(name) || System.getProperties().containsKey(PREFIX + name);
    }

    protected Boolean getBoolean(String name)
    {
        return getBoolean(name, null);
    }

    protected boolean getBoolean(String name, boolean defaultValue)
    {
        return getBoolean(name, new Boolean(defaultValue)).booleanValue();
    }

    protected Boolean getBoolean(String name, Boolean defaultValue)
    {
        Boolean value = (Boolean) getParam(name);
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

    protected Byte getByte(String name)
    {
        return getByte(name, null);
    }

    protected byte getByte(String name, byte defaultValue)
    {
        return getByte(name, new Byte(defaultValue)).byteValue();
    }

    protected Byte getByte(String name, Byte defaultValue)
    {
        Byte value = (Byte) getParam(name);
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

    protected Character getCharacter(String name)
    {
        return getCharacter(name, null);
    }

    protected char getCharacter(String name, char defaultValue)
    {
        return getCharacter(name, new Character(defaultValue)).charValue();
    }

    protected Character getCharacter(String name, Character defaultValue)
    {
        Character value = (Character) getParam(name);
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

    protected Double getDouble(String name)
    {
        return getDouble(name, null);
    }

    protected double getDouble(String name, double defaultValue)
    {
        return getDouble(name, new Double(defaultValue)).doubleValue();
    }

    protected Double getDouble(String name, Double defaultValue)
    {
        Double value = (Double) getParam(name);
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

    protected Float getFloat(String name)
    {
        return getFloat(name, null);
    }

    protected float getFloat(String name, float defaultValue)
    {
        return getFloat(name, new Float(defaultValue)).floatValue();
    }

    protected Float getFloat(String name, Float defaultValue)
    {
        Float value = (Float) getParam(name);
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

    protected Integer getInteger(String name)
    {
        return getInteger(name, null);
    }

    protected int getInteger(String name, int defaultValue)
    {
        return getInteger(name, new Integer(defaultValue)).intValue();
    }

    protected Integer getInteger(String name, Integer defaultValue)
    {
        Integer value = (Integer) getParam(name);
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

    protected Long getLong(String name)
    {
        return getLong(name, null);
    }

    protected long getLong(String name, long defaultValue)
    {
        return getLong(name, new Long(defaultValue)).longValue();
    }

    protected Long getLong(String name, Long defaultValue)
    {
        Long value = (Long) getParam(name);
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

    protected Short getShort(String name)
    {
        return getShort(name, null);
    }

    protected short getShort(String name, short defaultValue)
    {
        return getShort(name, new Short(defaultValue)).shortValue();
    }

    protected Short getShort(String name, Short defaultValue)
    {
        Short value = (Short) getParam(name);
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

    protected String getString(String name)
    {
        return getString(name, null);
    }

    protected String getString(String name, String defaultValue)
    {
        String value = (String) getParam(name);
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
}