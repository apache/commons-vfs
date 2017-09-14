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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;

/**
 * This class use reflection to set a configuration value using the fileSystemConfigBuilder associated the a scheme.<br>
 * <br>
 * Example:<br>
 *
 * <pre>
 * FileSystemOptions fso = new FileSystemOptions();
 * DelegatingFileSystemOptionsBuilder delegate = new DelegatingFileSystemOptionsBuilder(VFS.getManager());
 * delegate.setConfigString(fso, "sftp", "identities", "c:/tmp/test.ident");
 * delegate.setConfigString(fso, "http", "proxyPort", "8080");
 * delegate.setConfigClass(fso, "sftp", "userinfo", TrustEveryoneUserInfo.class);
 * </pre>
 */
public class DelegatingFileSystemOptionsBuilder {
    @SuppressWarnings("unchecked") // OK, it is a String
    private static final Class<String>[] STRING_PARAM = new Class[] { String.class };
    private static final Map<String, Class<?>> PRIMATIVE_TO_OBJECT = new TreeMap<>();
    private static final Log log = LogFactory.getLog(DelegatingFileSystemOptionsBuilder.class);

    private final FileSystemManager manager;
    private final Map<String, Map<String, List<Method>>> beanMethods = new TreeMap<>();

    static {
        PRIMATIVE_TO_OBJECT.put(Void.TYPE.getName(), Void.class);
        PRIMATIVE_TO_OBJECT.put(Boolean.TYPE.getName(), Boolean.class);
        PRIMATIVE_TO_OBJECT.put(Byte.TYPE.getName(), Byte.class);
        PRIMATIVE_TO_OBJECT.put(Character.TYPE.getName(), Character.class);
        PRIMATIVE_TO_OBJECT.put(Short.TYPE.getName(), Short.class);
        PRIMATIVE_TO_OBJECT.put(Integer.TYPE.getName(), Integer.class);
        PRIMATIVE_TO_OBJECT.put(Long.TYPE.getName(), Long.class);
        PRIMATIVE_TO_OBJECT.put(Double.TYPE.getName(), Double.class);
        PRIMATIVE_TO_OBJECT.put(Float.TYPE.getName(), Float.class);
    }

    /**
     * Context.
     */
    private static final class Context {
        private final FileSystemOptions fso;
        private final String scheme;
        private final String name;
        private final Object[] values;

        private List<Method> configSetters;
        private FileSystemConfigBuilder fileSystemConfigBuilder;

        private Context(final FileSystemOptions fso, final String scheme, final String name, final Object[] values) {
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
    public DelegatingFileSystemOptionsBuilder(final FileSystemManager manager) {
        this.manager = manager;
    }

    protected FileSystemManager getManager() {
        return manager;
    }

    /**
     * Set a single string value.
     *
     * @param fso FileSystemOptions
     * @param scheme scheme
     * @param name name
     * @param value value
     * @throws FileSystemException if an error occurs.
     */
    public void setConfigString(final FileSystemOptions fso, final String scheme, final String name, final String value)
            throws FileSystemException {
        setConfigStrings(fso, scheme, name, new String[] { value });
    }

    /**
     * Set an array of string value.
     *
     * @param fso FileSystemOptions
     * @param scheme scheme
     * @param name name
     * @param values values
     * @throws FileSystemException if an error occurs.
     */
    public void setConfigStrings(final FileSystemOptions fso, final String scheme, final String name,
            final String[] values) throws FileSystemException {
        final Context ctx = new Context(fso, scheme, name, values);

        setValues(ctx);
    }

    /**
     * Set a single class value.<br>
     * The class has to implement a no-args constructor, else the instantiation might fail.
     *
     * @param fso FileSystemOptions
     * @param scheme scheme
     * @param name name
     * @param className className
     * @throws FileSystemException if an error occurs.
     * @throws IllegalAccessException if a class canoot be accessed.
     * @throws InstantiationException if a class cannot be instantiated.
     */
    public void setConfigClass(final FileSystemOptions fso, final String scheme, final String name,
            final Class<?> className) throws FileSystemException, IllegalAccessException, InstantiationException {
        setConfigClasses(fso, scheme, name, new Class[] { className });
    }

    /**
     * Set an array of class values.<br>
     * The class has to implement a no-args constructor, else the instantiation might fail.
     *
     * @param fso FileSystemOptions
     * @param scheme scheme
     * @param name name
     * @param classNames classNames
     * @throws FileSystemException if an error occurs.
     * @throws IllegalAccessException if a class canoot be accessed.
     * @throws InstantiationException if a class cannot be instantiated.
     */
    public void setConfigClasses(final FileSystemOptions fso, final String scheme, final String name,
            final Class<?>[] classNames) throws FileSystemException, IllegalAccessException, InstantiationException {
        final Object[] values = new Object[classNames.length];
        for (int iterClassNames = 0; iterClassNames < values.length; iterClassNames++) {
            values[iterClassNames] = classNames[iterClassNames].newInstance();
        }

        final Context ctx = new Context(fso, scheme, name, values);

        setValues(ctx);
    }

    /**
     * sets the values using the informations of the given context.<br>
     */
    private void setValues(final Context ctx) throws FileSystemException {
        // find all setter methods suitable for the given "name"
        if (!fillConfigSetters(ctx)) {
            throw new FileSystemException("vfs.provider/config-key-invalid.error", ctx.scheme, ctx.name);
        }

        // get the fileSystemConfigBuilder
        ctx.fileSystemConfigBuilder = getManager().getFileSystemConfigBuilder(ctx.scheme);

        // try to find a setter which could accept the value
        final Iterator<Method> iterConfigSetters = ctx.configSetters.iterator();
        while (iterConfigSetters.hasNext()) {
            final Method configSetter = iterConfigSetters.next();
            if (convertValuesAndInvoke(configSetter, ctx)) {
                return;
            }
        }

        throw new FileSystemException("vfs.provider/config-value-invalid.error", ctx.scheme, ctx.name, ctx.values);
    }

    /**
     * tries to convert the value and pass it to the given method
     */
    private boolean convertValuesAndInvoke(final Method configSetter, final Context ctx) throws FileSystemException {
        final Class<?>[] parameters = configSetter.getParameterTypes();
        if (parameters.length < 2) {
            return false;
        }
        if (!parameters[0].isAssignableFrom(FileSystemOptions.class)) {
            return false;
        }

        final Class<?> valueParameter = parameters[1];
        Class<?> type;
        if (valueParameter.isArray()) {
            type = valueParameter.getComponentType();
        } else {
            if (ctx.values.length > 1) {
                return false;
            }

            type = valueParameter;
        }

        if (type.isPrimitive()) {
            final Class<?> objectType = PRIMATIVE_TO_OBJECT.get(type.getName());
            if (objectType == null) {
                log.warn(Messages.getString("vfs.provider/config-unexpected-primitive.error", type.getName()));
                return false;
            }
            type = objectType;
        }

        final Class<? extends Object> valueClass = ctx.values[0].getClass();
        if (type.isAssignableFrom(valueClass)) {
            // can set value directly
            invokeSetter(valueParameter, ctx, configSetter, ctx.values);
            return true;
        }
        if (valueClass != String.class) {
            log.warn(Messages.getString("vfs.provider/config-unexpected-value-class.error", valueClass.getName(),
                    ctx.scheme, ctx.name));
            return false;
        }

        final Object convertedValues = Array.newInstance(type, ctx.values.length);

        Constructor<?> valueConstructor;
        try {
            valueConstructor = type.getConstructor(STRING_PARAM);
        } catch (final NoSuchMethodException e) {
            valueConstructor = null;
        }
        if (valueConstructor != null) {
            // can convert using constructor
            for (int iterValues = 0; iterValues < ctx.values.length; iterValues++) {
                try {
                    Array.set(convertedValues, iterValues,
                            valueConstructor.newInstance(new Object[] { ctx.values[iterValues] }));
                } catch (final InstantiationException e) {
                    throw new FileSystemException(e);
                } catch (final IllegalAccessException e) {
                    throw new FileSystemException(e);
                } catch (final InvocationTargetException e) {
                    throw new FileSystemException(e);
                }
            }

            invokeSetter(valueParameter, ctx, configSetter, convertedValues);
            return true;
        }

        Method valueFactory;
        try {
            valueFactory = type.getMethod("valueOf", STRING_PARAM);
            if (!Modifier.isStatic(valueFactory.getModifiers())) {
                valueFactory = null;
            }
        } catch (final NoSuchMethodException e) {
            valueFactory = null;
        }

        if (valueFactory != null) {
            // can convert using factory method (valueOf)
            for (int iterValues = 0; iterValues < ctx.values.length; iterValues++) {
                try {
                    Array.set(convertedValues, iterValues,
                            valueFactory.invoke(null, new Object[] { ctx.values[iterValues] }));
                } catch (final IllegalAccessException e) {
                    throw new FileSystemException(e);
                } catch (final InvocationTargetException e) {
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
    private void invokeSetter(final Class<?> valueParameter, final Context ctx, final Method configSetter,
            final Object values) throws FileSystemException {
        Object[] args;
        if (valueParameter.isArray()) {
            args = new Object[] { ctx.fso, values };
        } else {
            args = new Object[] { ctx.fso, Array.get(values, 0) };
        }
        try {
            configSetter.invoke(ctx.fileSystemConfigBuilder, args);
        } catch (final IllegalAccessException e) {
            throw new FileSystemException(e);
        } catch (final InvocationTargetException e) {
            throw new FileSystemException(e);
        }
    }

    /**
     * fills all available set*() methods for the context-scheme into the context.
     */
    private boolean fillConfigSetters(final Context ctx) throws FileSystemException {
        final Map<String, List<Method>> schemeMethods = getSchemeMethods(ctx.scheme);
        final List<Method> configSetters = schemeMethods.get(ctx.name.toLowerCase());
        if (configSetters == null) {
            return false;
        }

        ctx.configSetters = configSetters;
        return true;
    }

    /**
     * get (cached) list of set*() methods for the given scheme
     */
    private Map<String, List<Method>> getSchemeMethods(final String scheme) throws FileSystemException {
        Map<String, List<Method>> schemeMethods = beanMethods.get(scheme);
        if (schemeMethods == null) {
            schemeMethods = createSchemeMethods(scheme);
            beanMethods.put(scheme, schemeMethods);
        }

        return schemeMethods;
    }

    /**
     * create the list of all set*() methods for the given scheme
     */
    private Map<String, List<Method>> createSchemeMethods(final String scheme) throws FileSystemException {
        final FileSystemConfigBuilder fscb = getManager().getFileSystemConfigBuilder(scheme);
        if (fscb == null) {
            throw new FileSystemException("vfs.provider/no-config-builder.error", scheme);
        }

        final Map<String, List<Method>> schemeMethods = new TreeMap<>();

        final Method[] methods = fscb.getClass().getMethods();
        for (final Method method : methods) {
            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }

            final String methodName = method.getName();
            if (!methodName.startsWith("set")) {
                // not a setter
                continue;
            }

            final String key = methodName.substring(3).toLowerCase();

            List<Method> configSetter = schemeMethods.get(key);
            if (configSetter == null) {
                configSetter = new ArrayList<>(2);
                schemeMethods.put(key, configSetter);
            }
            configSetter.add(method);
        }

        return schemeMethods;
    }
}
