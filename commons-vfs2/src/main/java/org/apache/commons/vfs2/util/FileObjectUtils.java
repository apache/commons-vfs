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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DecoratedFileObject;
import org.apache.commons.vfs2.provider.AbstractFileObject;

/**
 * Stuff to get some strange things from an FileObject.
 */
public final class FileObjectUtils {

    private FileObjectUtils() {
        // noop
    }

    /**
     * Null-safe call to {@link FileObject#exists()}.
     *
     * @param fileObject
     * @return false if {@code fileObject} is null, otherwise, see {@link FileObject#exists()}.
     * @throws FileSystemException On error determining if this file exists.
     * @since 2.4
     */
    public static boolean exists(final FileObject fileObject) throws FileSystemException {
        return fileObject != null && fileObject.exists();
    }

    /**
     * Gets access to the base object even if decorated.
     *
     * @param fileObject The FileObject.
     * @return The decorated FileObject or null.
     * @throws FileSystemException if an error occurs.
     */
    public static AbstractFileObject getAbstractFileObject(final FileObject fileObject) throws FileSystemException {
        Object searchObject = fileObject;
        while (searchObject instanceof DecoratedFileObject) {
            searchObject = ((DecoratedFileObject) searchObject).getDecoratedFileObject();
        }
        if (searchObject instanceof AbstractFileObject) {
            return (AbstractFileObject) searchObject;
        }
        if (searchObject == null) {
            return null;
        }

        throw new FileSystemException("vfs.util/find-abstract-file-object.error",
                fileObject == null ? "null" : fileObject.getClass().getName());
    }

    /**
     * Checks if the given FileObject is instance of given class argument.
     *
     * @param fileObject  The FileObject.
     * @param wantedClass The Class to check.
     * @return true if fileObject is an instance of the specified Class.
     * @throws FileSystemException if an error occurs.
     */
    public static boolean isInstanceOf(final FileObject fileObject, final Class<?> wantedClass)
            throws FileSystemException {
        Object searchObject = fileObject;
        while (searchObject instanceof DecoratedFileObject) {
            if (wantedClass.isInstance(searchObject)) {
                return true;
            }

            searchObject = ((DecoratedFileObject) searchObject).getDecoratedFileObject();
        }

        if (wantedClass.isInstance(searchObject)) {
            return true;
        }

        return false;
    }

    /**
     * Reads the given file into a new {@link Properties}.
     *
     * @param fileObject the file to read
     * @return a new {@link Properties}.
     * @throws IOException
     * @throws FileSystemException On error getting this file's content.
     * @throws IOException On error getting this file's content.
     * @since 2.4
     */
    public static Properties readProperties(final FileObject fileObject) throws FileSystemException, IOException {
        return readProperties(fileObject, new Properties());
    }

    /**
     * Reads the given file into a new given {@link Properties}.
     *
     * @param fileObject the file to read
     * @param properties the destination
     * @return a new {@link Properties}.
     * @throws FileSystemException On error getting this file's content.
     * @throws IOException On error getting this file's content.
     * @since 2.4
     */
    public static Properties readProperties(final FileObject fileObject, final Properties properties)
            throws FileSystemException, IOException {
        if (fileObject == null) {
            return properties;
        }
        try (InputStream inputStream = fileObject.getContent().getInputStream()) {
            properties.load(inputStream);
        }
        return properties;
    }
}
