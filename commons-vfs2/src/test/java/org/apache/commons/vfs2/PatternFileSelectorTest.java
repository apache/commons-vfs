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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests FileExtensionSelector.
 */
public class PatternFileSelectorTest {

    private static FileObject baseFolder;

    /**
     * 9 files and 1 directory = 10
     */
    private static final int ENTRY_COUNT = 10;

    private static final int EXTENSION_COUNT = 3;

    private static final int FILES_PER_EXTENSION_COUNT = 3;

    static FileObject getBaseFolder() {
        return baseFolder;
    }

    /**
     * Creates a RAM FS.
     *
     * @throws Exception
     */
    @BeforeAll
    public static void setUpClass() throws Exception {
        baseFolder = VFS.getManager().resolveFile("ram://" + PatternFileSelectorTest.class.getName());
        baseFolder.deleteAll();
        baseFolder.createFolder();
        baseFolder.resolveFile("aa.htm").createFile();
        baseFolder.resolveFile("aa.html").createFile();
        baseFolder.resolveFile("aa.xhtml").createFile();
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
     * Tests a one extension selector.
     *
     * @throws Exception
     */
    @Test
    public void testFileExtensions() throws Exception {
        final FileObject[] foArray = baseFolder.findFiles(Selectors.SELECT_FILES);
        assertTrue(foArray.length > 0);
        final String regExPrefix = ".*\\.";
        // gather file extensions.
        final Set<String> extensionSet = new HashSet<>();
        for (final FileObject fo : foArray) {
            extensionSet.add(regExPrefix + fo.getName().getExtension());
        }
        final String message = String.format("Extensions: %s; files: %s", extensionSet.toString(),
                Arrays.asList(foArray).toString());
        assertEquals(EXTENSION_COUNT, extensionSet.size(), message);
        // check each extension
        for (final String extension : extensionSet) {
            final FileSelector selector = new PatternFileSelector(extension);
            final FileObject[] list = baseFolder.findFiles(selector);
            assertEquals(FILES_PER_EXTENSION_COUNT, list.length);
        }
        // check each file against itself
        for (final FileObject fo : foArray) {
            final FileSelector selector = new PatternFileSelector(regExPrefix + fo.getName().getExtension());
            final FileObject[] list = baseFolder.findFiles(selector);
            assertEquals(FILES_PER_EXTENSION_COUNT, list.length);
        }
    }

    /**
     * Tests matching all.
     *
     * @throws Exception
     */
    @Test
    public void testMatchAll() throws Exception {
        final FileObject[] list = baseFolder.findFiles(new PatternFileSelector(".*"));
        assertEquals(ENTRY_COUNT, list.length);
    }

    /**
     * Tests matching partial file names.
     *
     * @throws Exception
     */
    @Test
    public void testMatchPartial() throws Exception {
        final FileObject[] list = baseFolder.findFiles(new PatternFileSelector(".*a.htm"));
        assertEquals(1, list.length);
        assertEquals("aa.htm", list[0].getName().getBaseName());
    }

    /**
     * Tests matching partial file names with delimiter.
     *
     * @throws Exception
     */
    @Test
    public void testMatchPartialDelimited() throws Exception {
        final FileObject[] list = baseFolder.findFiles(new PatternFileSelector("^.*\\/b.htm$"));
        assertEquals(1, list.length);
        assertEquals("b.htm", list[0].getName().getBaseName());
    }

    /**
     * Tests a null selector.
     */
    @Test
    public void testNullString() {
        // Yep, this will blow up.
        assertThrows(NullPointerException.class, () -> new PatternFileSelector((String) null));
    }

}
