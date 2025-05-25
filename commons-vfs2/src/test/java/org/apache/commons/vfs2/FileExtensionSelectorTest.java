/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests FileExtensionSelector.
 */
public class FileExtensionSelectorTest {

    private static FileObject baseFolder;

    private static final int FILE_COUNT = 9;

    private static final int EXTENSION_COUNT = 3;

    private static final int FILES_PER_EXTENSION_COUNT = 3;

    /**
     * Creates a RAM FS.
     *
     * @throws Exception
     */
    @BeforeAll
    public static void setUpClass() throws Exception {
        baseFolder = VFS.getManager().resolveFile("ram://" + FileExtensionSelectorTest.class.getName());
        baseFolder.deleteAll();
        baseFolder.createFolder();
        baseFolder.resolveFile("a.htm").createFile();
        baseFolder.resolveFile("a.html").createFile();
        baseFolder.resolveFile("a.xhtml").createFile();
        baseFolder.resolveFile("b.htm").createFile();
        baseFolder.resolveFile("b.html").createFile();
        baseFolder.resolveFile("b.xhtml").createFile();
        baseFolder.resolveFile("c.htm").createFile();
        baseFolder.resolveFile("c.html").createFile();
        baseFolder.resolveFile("c.xhtml").createFile();
    }

    /**
     * Deletes RAM FS files.
     *
     * @throws Exception
     */
    @AfterAll
    public static void tearDownClass() throws Exception {
        if (baseFolder != null) {
            baseFolder.deleteAll();
        }
    }

    /**
     * Tests an empty selector.
     *
     * @throws Exception
     */
    @Test
    public void testEmpty() throws Exception {
        final FileSelector selector = new FileExtensionSelector();
        final FileObject[] foList = baseFolder.findFiles(selector);
        assertEquals(0, foList.length);
    }

    /**
     * Tests many extensions at once.
     *
     * @throws Exception
     */
    @Test
    public void testManyExtensions() throws Exception {
        final FileObject[] foArray = baseFolder.findFiles(Selectors.SELECT_FILES);
        assertTrue(foArray.length > 0);
        // gather file extensions.
        final Set<String> extensionSet = new HashSet<>();
        for (final FileObject fo : foArray) {
            extensionSet.add(fo.getName().getExtension());
        }
        final String message = String.format("Extensions: %s; files: %s", extensionSet.toString(),
                Arrays.asList(foArray).toString());
        assertFalse(extensionSet.isEmpty(), message);
        assertEquals(EXTENSION_COUNT, extensionSet.size(), message);
        // check all unique extensions
        final FileSelector selector = new FileExtensionSelector(extensionSet);
        final FileObject[] list = baseFolder.findFiles(selector);
        assertEquals(FILE_COUNT, list.length);
    }

    /**
     * Tests a null selector.
     *
     * @throws Exception
     */
    @Test
    public void testNullCollection() throws Exception {
        final FileSelector selector0 = new FileExtensionSelector((Collection<String>) null);
        final FileObject[] foList = baseFolder.findFiles(selector0);
        assertEquals(0, foList.length);
    }

    /**
     * Tests a null selector.
     *
     * @throws Exception
     */
    @Test
    public void testNullString() throws Exception {
        final FileSelector selector0 = new FileExtensionSelector((String) null);
        final FileObject[] foList = baseFolder.findFiles(selector0);
        assertEquals(0, foList.length);
    }

    /**
     * Tests a one extension selector.
     *
     * @throws Exception
     */
    @Test
    public void testOneExtension() throws Exception {
        final FileObject[] foArray = baseFolder.findFiles(Selectors.SELECT_FILES);
        assertTrue(foArray.length > 0);
        // gather file extensions.
        final Set<String> extensionSet = new HashSet<>();
        for (final FileObject fo : foArray) {
            extensionSet.add(fo.getName().getExtension());
        }
        final String message = String.format("Extensions: %s; files: %s", extensionSet.toString(),
                Arrays.asList(foArray).toString());
        assertEquals(EXTENSION_COUNT, extensionSet.size(), message);
        // check each extension
        for (final String extension : extensionSet) {
            final FileSelector selector = new FileExtensionSelector(extension);
            final FileObject[] list = baseFolder.findFiles(selector);
            assertEquals(FILES_PER_EXTENSION_COUNT, list.length);
        }
        // check each file against itself
        for (final FileObject fo : foArray) {
            final FileSelector selector = new FileExtensionSelector(fo.getName().getExtension());
            final FileObject[] list = baseFolder.findFiles(selector);
            assertEquals(FILES_PER_EXTENSION_COUNT, list.length);
        }
    }

}
