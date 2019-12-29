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
package org.apache.commons;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.util.Messages;

import junit.framework.TestCase;

/**
 * A base class for VFS tests. Provides utility methods for locating test resources.
 */
public abstract class AbstractVfsTestCase extends TestCase {
    private static File baseDir;

    /** URL pattern */
    private static final Pattern URL_PATTERN = Pattern.compile("[a-z]+://.*");

    /** Password pattern */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(":(?:[^/]+)@");

    /**
     * Locates a test resource, and asserts that the resource exists
     *
     * @param name path of the resource, relative to this test's base directory.
     */
    public static File getTestResource(final String name) {
        return getTestResource(name, true);
    }

    /**
     * Locates a test resource.
     *
     * @param name path of the resource, relative to this test's base directory.
     */
    public static File getTestResource(final String name, final boolean mustExist) {
        File file = new File(getTestDirectoryFile(), name);
        file = getCanonicalFile(file);
        if (mustExist) {
            assertTrue("Test file \"" + file + "\" does not exist.", file.exists());
        } else {
            assertFalse("Test file \"" + file + "\" should not exist.", file.exists());
        }

        return file;
    }

    /**
     * Locates the base directory for this test.
     */
    public static File getTestDirectoryFile() {
        if (baseDir == null) {
            final String baseDirProp = getTestDirectory();
            // the directory maybe expressed as URI in certain environments
            if (baseDirProp.startsWith("file://")) {
                try {
                    baseDir = getCanonicalFile(new File(new URI(baseDirProp)));
                } catch (final URISyntaxException e) {
                    baseDir = getCanonicalFile(new File(baseDirProp));
                }
            } else {
                baseDir = getCanonicalFile(new File(baseDirProp));
            }
        }
        return baseDir;
    }

    /**
     * Returns the test directory as a String.
     * <p>
     * {@link #getTestDirectoryFile()} should be preferred.
     *
     * @return the test directory as a String
     */
    public static String getTestDirectory() {
        return System.getProperty("test.basedir", "target/test-classes/test-data");
    }

    public static String getResourceTestDirectory() {
        return System.getProperty("test.basedir.res", "test-data");
    }

    /**
     * Locates a test directory, creating it if it does not exist.
     *
     * @param name path of the directory, relative to this test's base directory.
     */
    public static File getTestDirectory(final String name) {
        File file = new File(getTestDirectoryFile(), name);
        file = getCanonicalFile(file);
        assertTrue("Test directory \"" + file + "\" does not exist or is not a directory.",
                file.isDirectory() || file.mkdirs());
        return file;
    }

    /**
     * Makes a file canonical
     */
    public static File getCanonicalFile(final File file) {
        try {
            return file.getCanonicalFile();
        } catch (final IOException e) {
            return file.getAbsoluteFile();
        }
    }

    /**
     * Asserts that an exception contains the expected message.
     */
    private static void assertSameMessage(final String code, final Object[] params, final Throwable throwable) {
        Object[] parmArray = params;
        if (throwable instanceof FileSystemException) {
            final FileSystemException fse = (FileSystemException) throwable;

            // Compare message code and params
            assertEquals(code, fse.getCode());
            assertEquals(params.length, fse.getInfo().length);
            parmArray = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                String value = String.valueOf(params[i]);
                // mask passwords (VFS-169)
                final Matcher urlMatcher = URL_PATTERN.matcher(value);
                if (urlMatcher.find()) {
                    final Matcher pwdMatcher = PASSWORD_PATTERN.matcher(value);
                    value = pwdMatcher.replaceFirst(":***@");
                }
                assertEquals(value, fse.getInfo()[i]);
                parmArray[i] = value;
            }
        }

        // Compare formatted message
        final String message = Messages.getString(code, parmArray);
        assertEquals(message, throwable.getMessage());
    }

    /**
     * Asserts that an exception contains the expected message.
     */
    public static void assertSameMessage(final String code, final Object param, final Throwable throwable) {
        assertSameMessage(code, new Object[] { param }, throwable);
    }

    /**
     * Compares 2 objects for equality, nulls are equal. Used by the test classes' equals() methods.
     */
    public static boolean equals(final Object o1, final Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        return o1.equals(o2);
    }
}
