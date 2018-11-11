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

import java.util.Arrays;
import java.util.Collection;
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
public class FileExtensionSelectorTest {
    private static FileObject BaseFolder;

    private static final int FileCount = 9;

    private static final int ExtensionCount = 3;

    private static final int FilesPerExtensionCount = 3;

    /**
     * Creates a RAM FS.
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseFolder = VFS.getManager().resolveFile("ram://" + FileExtensionSelectorTest.class.getName());
        BaseFolder.deleteAll();
        BaseFolder.createFolder();
        BaseFolder.resolveFile("a.htm").createFile();
        BaseFolder.resolveFile("a.html").createFile();
        BaseFolder.resolveFile("a.xhtml").createFile();
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
     * Tests an empty selector.
     *
     * @throws Exception
     */
    @Test
    public void testEmpty() throws Exception {
        final FileSelector selector = new FileExtensionSelector();
        final FileObject[] foList = BaseFolder.findFiles(selector);
        Assert.assertEquals(0, foList.length);
    }

    /**
     * Tests many extensions at once.
     *
     * @throws Exception
     */
    @Test
    public void testManyExtensions() throws Exception {
        final FileObject[] foArray = BaseFolder.findFiles(Selectors.SELECT_FILES);
        Assert.assertTrue(foArray.length > 0);
        // gather file extensions.
        final Set<String> extensionSet = new HashSet<>();
        for (final FileObject fo : foArray) {
            extensionSet.add(fo.getName().getExtension());
        }
        final String message = String.format("Extensions: %s; files: %s", extensionSet.toString(),
                Arrays.asList(foArray).toString());
        Assert.assertTrue(message, extensionSet.size() > 0);
        Assert.assertEquals(message, ExtensionCount, extensionSet.size());
        // check all unique extensions
        final FileSelector selector = new FileExtensionSelector(extensionSet);
        final FileObject[] list = BaseFolder.findFiles(selector);
        Assert.assertEquals(FileCount, list.length);
    }

    /**
     * Tests a null selector.
     *
     * @throws Exception
     */
    @Test
    public void testNullCollection() throws Exception {
        final FileSelector selector0 = new FileExtensionSelector((Collection<String>) null);
        final FileObject[] foList = BaseFolder.findFiles(selector0);
        Assert.assertEquals(0, foList.length);
    }

    /**
     * Tests a null selector.
     *
     * @throws Exception
     */
    @Test
    public void testNullString() throws Exception {
        final FileSelector selector0 = new FileExtensionSelector((String) null);
        final FileObject[] foList = BaseFolder.findFiles(selector0);
        Assert.assertEquals(0, foList.length);
    }

    /**
     * Tests a one extension selector.
     *
     * @throws Exception
     */
    @Test
    public void testOneExtension() throws Exception {
        final FileObject[] foArray = BaseFolder.findFiles(Selectors.SELECT_FILES);
        Assert.assertTrue(foArray.length > 0);
        // gather file extensions.
        final Set<String> extensionSet = new HashSet<>();
        for (final FileObject fo : foArray) {
            extensionSet.add(fo.getName().getExtension());
        }
        final String message = String.format("Extensions: %s; files: %s", extensionSet.toString(),
                Arrays.asList(foArray).toString());
        Assert.assertEquals(message, ExtensionCount, extensionSet.size());
        // check each extension
        for (final String extension : extensionSet) {
            final FileSelector selector = new FileExtensionSelector(extension);
            final FileObject[] list = BaseFolder.findFiles(selector);
            Assert.assertEquals(FilesPerExtensionCount, list.length);
        }
        // check each file against itself
        for (final FileObject fo : foArray) {
            final FileSelector selector = new FileExtensionSelector(fo.getName().getExtension());
            final FileObject[] list = BaseFolder.findFiles(selector);
            Assert.assertEquals(FilesPerExtensionCount, list.length);
        }
    }

}
