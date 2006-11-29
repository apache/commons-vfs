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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class use reflection to set a configuration value using the fileSystemConfigBuilder
 * associated the a scheme.<br><br>
 * Example:<br>
 * <pre>
 * FileSystemOptions fso = new FileSystemOptions();
 * DelegatingFileSystemOptionsBuilder delegate = new DelegatingFileSystemOptionsBuilder(VFS.getManager());
 * delegate.setConfigString(fso, "sftp", "identities", "c:/tmp/test.ident");
 * delegate.setConfigString(fso, "http", "proxyPort", "8080");
 * delegate.setConfigClass(fso, "sftp", "userinfo", TrustEveryoneUserInfo.class);
 * </pre>
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date$
 */
public class DelegatingFileSystemOptionsBuilder
{
    private Log log = LogFactory.getLog(DelegatingFileSystemOptionsBuilder.class);

    private final static Class[] STRING_PARAM = new Class[]{String.class};

    private final FileSystemManager manager;

    private final Map beanMethods = new TreeMap();

    private final static Map primitiveToObject = new TreeMap();

    static
    {
        primitiveToObject.put(Void.TYPE.getName(), Void.class);
        primitiveToObject.put(Boolean.TYPE.getName(), Boolean.class);
        primitiveToObject.put(Byte.TYPE.getName(), Byte.class);
        primitiveToObject.put(Character.TYPE.getName(), Character.class);
        primitiveToObject.put(Short.TYPE.getName(), Short.class);
        primitiveToObject.put(Integer.TYPE.getName(), Integer.class);
        primitiveToObject.put(Long.TYPE.getName(), Long.class);
        primitiveToObject.put(Double.TYPE.getName(), Double.class);
        primitiveToObject.put(Float.TYPE.getName(), Float.class);
    }

    private static class Context
    {
        private final FileSystemOptions fso;
        private final String scheme;
        private final String name;
        private final Object[] values;

        private List configSetters;
        private FileSystemConfigBuilder fileSystemConfigBuilder;

        private Context(final FileSystemOptions fso, final String scheme, final String name, final Object[] values)
        {
            this.fso = fso;
            this.scheme = scheme;
            this.name = name;
            this.values = values;
        }
    }

    /**
     * Constructor.<br>
     * Pass in your fileSystemManager instance.
     *
     * @param manager the manager to use to get the fileSystemConfigBuilder assocated to a scheme
     */
    public DelegatingFileSystemOptionsBuilder(final FileSystemManager manager)
    {
        this.manager = manager;
    }

    protected FileSystemManager getManager()
    {
        return manager;
    }

    /**
     * Set a single string value.
     *
     * @param fso    FileSystemOptions
     * @param scheme scheme
     * @param name   name
     * @param value  value
     */
    public void setConfigString(final FileSystemOptions fso, final String scheme, final String name, final String value) throws FileSystemException
    {
        setConfigStrings(fso, scheme, name, new String[]{value});
    }

    /**
     * Set an array of string value.
     *
     * @param fso    FileSystemOptions
     * @param scheme scheme
     * @param name   name
     * @param values values
     */
    public void setConfigStrings(final FileSystemOptions fso, final String scheme, final String name, final String[] values) throws FileSystemException
    {
        Context ctx = new Context(fso, scheme, name, values);

        setValues(ctx);
    }

    /**
     * Set a single class value.<br>
     * The class has to implement a no-args constructor, else the instantiation might fail.
     *
     * @param fso       FileSystemOptions
     * @param scheme    scheme
     * @param name      name
     * @param className className
     */
    public void setConfigClass(final FileSystemOptions fso, final String scheme, final String name, final Class className) throws FileSystemException, IllegalAccessException, InstantiationException
    {
        setConfigClasses(fso, scheme, name, new Class[]{className});
    }

    /**
     * Set an array of class values.<br>
     * The class has to implement a no-args constructor, else the instantiation might fail.
     *
     * @param fso        FileSystemOptions
     * @param scheme     scheme
     * @param name       name
     * @param classNames classNames
     */
    public void setConfigClasses(final FileSystemOptions fso, final String scheme, final String name, final Class[] classNames) throws FileSystemException, IllegalAccessException, InstantiationException
    {
        Object values[] = new Object[classNames.length];
        for (int iterClassNames = 0; iterClassNames < values.length; iterClassNames++)
        {
            values[iterClassNames] = classNames[iterClassNames].newInstance();
        }

        Context ctx = new Context(fso, scheme, name, values);

        setValues(ctx);
    }

    /**
     * sets the values using the informations of the given context.<br>
     */
    private void setValues(Context ctx) throws FileSystemException
    {
        // find all setter methods suitable for the given "name"
        if (!fillConfigSetters(ctx))
        {
            throw new FileSystemException("vfs.provider/config-key-invalid.error", new String[]
            {
                ctx.scheme,
                ctx.name
            });
        }

        // get the fileSystemConfigBuilder
        ctx.fileSystemConfigBuilder = getManager().getFileSystemConfigBuilder(ctx.scheme);

        // try to find a setter which could accept the value
        Iterator iterConfigSetters = ctx.configSetters.iterator();
        while (iterConfigSetters.hasNext())
        {
            Method configSetter = (Method) iterConfigSetters.next();
            if (convertValuesAndInvoke(configSetter, ctx))
            {
                return;
            }
        }

        throw new FileSystemException("vfs.provider/config-value-invalid.error", new Object[]
        {
            ctx.scheme,
            ctx.name,
            ctx.values
        });
    }

    /**
     * tries to convert the value and pass it to the given method
     */
    private boolean convertValuesAndInvoke(final Method configSetter, final Context ctx) throws FileSystemException
    {
        Class parameters[] = configSetter.getParameterTypes();
        if (parameters.length < 2)
        {
            return false;
        }
        if (!parameters[0].isAssignableFrom(FileSystemOptions.class))
        {
            return false;
        }

        Class valueParameter = parameters[1];
        Class type;
        if (valueParameter.isArray())
        {
            type = valueParameter.getComponentType();
        }
        else
        {
            if (ctx.values.length > 1)
            {
                return false;
            }

            type = valueParameter;
        }

        if (type.isPrimitive())
        {
            Class objectType = (Class) primitiveToObject.get(type.getName());
            if (objectType == null)
            {
                log.warn(Messages.getString("vfs.provider/config-unexpected-primitive.error", type.getName()));
                return false;
            }
            type = objectType;
        }

        Class valueClass = ctx.values[0].getClass();
        if (type.isAssignableFrom(valueClass))
        {
            // can set value directly
            invokeSetter(valueParameter, ctx, configSetter, ctx.values);
            return true;
        }
        if (valueClass != String.class)
        {
            log.warn(Messages.getString("vfs.provider/config-unexpected-value-class.error", new String[]
            {
                valueClass.getName(),
                ctx.scheme,
                ctx.name
            }));
            return false;
        }

        Object convertedValues = java.lang.reflect.Array.newInstance(type, ctx.values.length);

        Constructor valueConstructor;
        try
        {
            valueConstructor = type.getConstructor(STRING_PARAM);
        }
        catch (NoSuchMethodException e)
        {
            valueConstructor = null;
        }
        if (valueConstructor != null)
        {
            // can convert using constructor
            for (int iterValues = 0; iterValues < ctx.values.length; iterValues++)
            {
                try
                {
                    Array.set(convertedValues, iterValues, valueConstructor.newInstance(new Object[]{ctx.values[iterValues]}));
                }
                catch (InstantiationException e)
                {
                    throw new FileSystemException(e);
                }
                catch (IllegalAccessException e)
                {
                    throw new FileSystemException(e);
                }
                catch (InvocationTargetException e)
                {
                    throw new FileSystemException(e);
                }
            }

            invokeSetter(valueParameter, ctx, configSetter, convertedValues);
            return true;
        }

        Method valueFactory;
        try
        {
            valueFactory = type.getMethod("valueOf", STRING_PARAM);
            if (!Modifier.isStatic(valueFactory.getModifiers()))
            {
                valueFactory = null;
            }
        }
        catch (NoSuchMethodException e)
        {
            valueFactory = null;
        }

        if (valueFactory != null)
        {
            // can convert using factory method (valueOf)
            for (int iterValues = 0; iterValues < ctx.values.length; iterValues++)
            {
                try
                {
                    Array.set(convertedValues, iterValues, valueFactory.invoke(null, new Object[]{ctx.values[iterValues]}));
                }
                catch (IllegalAccessException e)
                {
                    throw new FileSystemException(e);
                }
                catch (InvocationTargetException e)
                {
                    throw new FileSystemException(e);
                }
            }

            invokeSetter(valueParameter, ctx, configSetter, convertedValues);
            return true;
        }

        return false;
    }

    /**
     * invokes the method with the converted values
     */
    private void invokeSetter(Class valueParameter, final Context ctx, final Method configSetter, final Object values)
        throws FileSystemException
    {
        Object[] args;
        if (valueParameter.isArray())
        {
            args = new Object[]
            {
                ctx.fso,
                values
            };
        }
        else
        {
            args = new Object[]
            {
                ctx.fso,
                Array.get(values, 0)
            };
        }
        try
        {
            configSetter.invoke(ctx.fileSystemConfigBuilder, args);
        }
        catch (IllegalAccessException e)
        {
            throw new FileSystemException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new FileSystemException(e);
        }
    }

    /**
     * fills all available set*() methods for the context-scheme into the context.
     */
    private boolean fillConfigSetters(final Context ctx)
        throws FileSystemException
    {
        Map schemeMethods = getSchemeMethods(ctx.scheme);
        List configSetters = (List) schemeMethods.get(ctx.name.toLowerCase());
        if (configSetters == null)
        {
            return false;
        }

        ctx.configSetters = configSetters;
        return true;
    }

    /**
     * get (cached) list of set*() methods for the given scheme
     */
    private Map getSchemeMethods(final String scheme) throws FileSystemException
    {
        Map schemeMethods = (Map) beanMethods.get(scheme);
        if (schemeMethods == null)
        {
            schemeMethods = createSchemeMethods(scheme);
            beanMethods.put(scheme, schemeMethods);
        }

        return schemeMethods;
    }

    /**
     * create the list of all set*() methods for the given scheme
     */
    private Map createSchemeMethods(String scheme) throws FileSystemException
    {
        final FileSystemConfigBuilder fscb = getManager().getFileSystemConfigBuilder(scheme);
        if (fscb == null)
        {
            throw new FileSystemException("vfs.provider/no-config-builder.error", scheme);
        }

        Map schemeMethods = new TreeMap();

        Method methods[] = fscb.getClass().getMethods();
        for (int iterMethods = 0; iterMethods < methods.length; iterMethods++)
        {
            Method method = methods[iterMethods];
            if (!Modifier.isPublic(method.getModifiers()))
            {
                continue;
            }

            String methodName = method.getName();
            if (!methodName.startsWith("set"))
            {
                // not a setter
                continue;
            }

            String key = methodName.substring(3).toLowerCase();

            List configSetter = (List) schemeMethods.get(key);
            if (configSetter == null)
            {
                configSetter = new ArrayList(2);
                schemeMethods.put(key, configSetter);
            }
            configSetter.add(method);
        }

        return schemeMethods;
    }
}
