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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests FileExtensionSelector.
 *
 * @since 2.1
 */
public class PatternFileSelectorTest {
    private static FileObject BaseFolder;

    /**
     * 9 files and 1 directory = 10
     */
    private static final int EntryCount = 10;

    private static final int ExtensionCount = 3;

    private static final int FilesPerExtensionCount = 3;

    /**
     * Creates a RAM FS.
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseFolder = VFS.getManager().resolveFile("ram://" + PatternFileSelectorTest.class.getName());
        BaseFolder.deleteAll();
        BaseFolder.createFolder();
        BaseFolder.resolveFile("aa.htm").createFile();
        BaseFolder.resolveFile("aa.html").createFile();
        BaseFolder.resolveFile("aa.xhtml").createFile();
        BaseFolder.resolveFile("b.htm").createFile();
        BaseFolder.resolveFile("b.html").createFile();
        BaseFolder.resolveFile("b.xhtml").createFile();
        BaseFolder.resolveFile("c.htm").createFile();
        BaseFolder.resolveFile("c.html").createFile();
        BaseFolder.resolveFile("c.xhtml").createFile();
    }

    /**
     * Deletes RAM FS files.
     *
     * @throws Exception
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        if (BaseFolder != null) {
            BaseFolder.deleteAll();
        }
    }

    /**
     * Tests a null selector.
     *
     * @throws Exception
     */
    @Test(expected = NullPointerException.class)
    public void testNullString() throws Exception {
        // Yep, this will blow up.
        new PatternFileSelector((String) null);
    }

    /**
     * Tests matching all
     *
     * @throws Exception
     */
    @Test
    public void testMatchAll() throws Exception {
        final FileObject[] list = BaseFolder.findFiles(new PatternFileSelector(".*"));
        Assert.assertEquals(EntryCount, list.length);
    }

    /**
     * Tests a one extension selector.
     *
     * @throws Exception
     */
    @Test
    public void testFileExtensions() throws Exception {
        final FileObject[] foArray = BaseFolder.findFiles(Selectors.SELECT_FILES);
        Assert.assertTrue(foArray.length > 0);
        final String regExPrefix = ".*\\.";
        // gather file extensions.
        final Set<String> extensionSet = new HashSet<>();
        for (final FileObject fo : foArray) {
            extensionSet.add(regExPrefix + fo.getName().getExtension());
        }
        final String message = String.format("Extensions: %s; files: %s", extensionSet.toString(),
                Arrays.asList(foArray).toString());
        Assert.assertEquals(message, ExtensionCount, extensionSet.size());
        // check each extension
        for (final String extension : extensionSet) {
            final FileSelector selector = new PatternFileSelector(extension);
            final FileObject[] list = BaseFolder.findFiles(selector);
            Assert.assertEquals(FilesPerExtensionCount, list.length);
        }
        // check each file against itself
        for (final FileObject fo : foArray) {
            final FileSelector selector = new PatternFileSelector(regExPrefix + fo.getName().getExtension());
            final FileObject[] list = BaseFolder.findFiles(selector);
            Assert.assertEquals(FilesPerExtensionCount, list.length);
        }
    }

    /**
     * Tests matching partial file names
     *
     * @throws Exception
     */
    @Test
    public void testMatchPartial() throws Exception {
        final FileObject[] list = BaseFolder.findFiles(new PatternFileSelector(".*a.htm"));
        Assert.assertEquals(1, list.length);
        assertEquals(list[0].getName().getBaseName(), "aa.htm");
    }

    /**
     * Tests matching partial file names with delimiter
     *
     * @throws Exception
     */
    @Test
    public void testMatchPartialDelimited() throws Exception {
        final FileObject[] list = BaseFolder.findFiles(new PatternFileSelector("^.*\\/b.htm$"));
        Assert.assertEquals(1, list.length);
        assertEquals(list[0].getName().getBaseName(), "b.htm");
    }

    static FileObject getBaseFolder() {
        return BaseFolder;
    }

}
