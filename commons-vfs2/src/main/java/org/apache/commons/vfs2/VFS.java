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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The main entry point for the VFS. Used to create {@link FileSystemManager} instances.
 */
public final class VFS {
    /** The URI style */
    private static Boolean uriStyle;

    /** The FileSystemManager */
    private static FileSystemManager instance;

    private VFS() {
    }

    /**
     * Returns the default {@link FileSystemManager} instance.
     * <p>
     * Warning, if you close this instance you may affect all current and future users of this manager singleton.
     *
     * @return The FileSystemManager.
     * @throws FileSystemException if an error occurs creating the manager.
     */
    public static synchronized FileSystemManager getManager() throws FileSystemException {
        if (instance == null) {
            instance = createManager("org.apache.commons.vfs2.impl.StandardFileSystemManager");
        }
        return instance;
    }

    /**
     * Creates a file system manager instance.
     *
     * @param managerClassName The specific manager impelmentation class name.
     * @return The FileSystemManager.
     * @throws FileSystemException if an error occurs creating the manager.
     */
    private static FileSystemManager createManager(final String managerClassName) throws FileSystemException {
        try {
            // Create instance
            final Class<?> mgrClass = Class.forName(managerClassName);
            final FileSystemManager mgr = (FileSystemManager) mgrClass.newInstance();

            try {
                // Initialize
                final Method initMethod = mgrClass.getMethod("init", (Class[]) null);
                initMethod.invoke(mgr, (Object[]) null);
            } catch (final NoSuchMethodException ignored) {
                /* Ignore; don't initialize. */
            }

            return mgr;
        } catch (final InvocationTargetException e) {
            throw new FileSystemException("vfs/create-manager.error", managerClassName, e.getTargetException());
        } catch (final Exception e) {
            throw new FileSystemException("vfs/create-manager.error", managerClassName, e);
        }
    }

    public static boolean isUriStyle() {
        if (uriStyle == null) {
            uriStyle = Boolean.FALSE;
        }
        return uriStyle.booleanValue();
    }

    public static void setUriStyle(final boolean uriStyle) {
        if (VFS.uriStyle != null && VFS.uriStyle.booleanValue() != uriStyle) {
            throw new IllegalStateException("VFS.uriStyle was already set differently.");
        }
        VFS.uriStyle = Boolean.valueOf(uriStyle);
    }

    /**
     * Sets the file system manager
     *
     * @param manager the file system manager
     * @since 2.2
     */
    public static void setManager(final FileSystemManager manager) {
        VFS.instance = manager;
    }
}
