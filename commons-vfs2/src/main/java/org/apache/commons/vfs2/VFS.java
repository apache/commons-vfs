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

/**
 * The main entry point for the VFS. Used to create {@link FileSystemManager} instances.
 */
public final class VFS {

    /** The FileSystemManager */
    private static FileSystemManager instance;

    /** The URI style */
    private static Boolean uriStyle;

    /**
     * Closes the default {@link FileSystemManager} instance.
     * <p>
     * Warning, if you close the default instance, a new one will be created by {@link #getManager()}.
     * </p>
     *
     * @since 2.8.0
     */
    public static synchronized void close() {
        if (instance != null) {
            instance.close();
        }
    }

    /**
     * Creates a file system manager instance.
     *
     * @param managerClassName The specific manager implementation class name.
     * @return The FileSystemManager.
     * @throws FileSystemException if an error occurs creating the manager.
     */
    private static FileSystemManager createFileSystemManager(final String managerClassName) throws FileSystemException {
        try {
            // Create instance
            final Class<FileSystemManager> clazz = (Class<FileSystemManager>) Class.forName(managerClassName);
            final FileSystemManager manager = clazz.newInstance();

            try {
                // Initialize
                clazz.getMethod("init", (Class[]) null).invoke(manager, (Object[]) null);
            } catch (final NoSuchMethodException e) {
                /* Ignore; don't initialize. */
                e.printStackTrace();
            }

            return manager;
        } catch (final InvocationTargetException e) {
            throw new FileSystemException("vfs/create-manager.error", managerClassName, e.getTargetException());
        } catch (final Exception e) {
            throw new FileSystemException("vfs/create-manager.error", managerClassName, e);
        }
    }

    /**
     * Returns the default {@link FileSystemManager} instance.
     * <p>
     * Warning, if you close this instance you may affect all current and future users of this manager singleton.
     * </p>
     *
     * @return The FileSystemManager.
     * @throws FileSystemException if an error occurs creating the manager.
     */
    public static synchronized FileSystemManager getManager() throws FileSystemException {
        if (instance == null) {
            instance = reset();
        }
        return instance;
    }

    public static boolean isUriStyle() {
        if (uriStyle == null) {
            uriStyle = Boolean.FALSE;
        }
        return uriStyle.booleanValue();
    }

    /**
     * Resets the FileSystemManager to the default.
     *
     * @return the new FileSystemManager.
     * @throws FileSystemException if an error occurs creating the manager.
     * @since 2.5.0
     */
    public static synchronized FileSystemManager reset() throws FileSystemException {
        close();
        return instance = createFileSystemManager("org.apache.commons.vfs2.impl.StandardFileSystemManager");
    }

    /**
     * Sets the file system manager
     *
     * @param manager the file system manager
     * @since 2.2
     */
    public static synchronized void setManager(final FileSystemManager manager) {
        VFS.instance = manager;
    }

    public static void setUriStyle(final boolean uriStyle) {
        if (VFS.uriStyle != null && VFS.uriStyle.booleanValue() != uriStyle) {
            throw new IllegalStateException("VFS.uriStyle was already set differently.");
        }
        VFS.uriStyle = Boolean.valueOf(uriStyle);
    }

    private VFS() {
        // no public instantiation.
    }
}
