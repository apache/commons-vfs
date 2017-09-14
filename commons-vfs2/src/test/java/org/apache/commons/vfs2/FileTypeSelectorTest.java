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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests FileTypeSelector.
 *
 * @since 2.1
 */
public class FileTypeSelectorTest {
    private static FileObject BaseFolder;

    /**
     * Creates a RAM FS.
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseFolder = VFS.getManager().resolveFile("ram://" + FileTypeSelectorTest.class.getName());
        BaseFolder.resolveFile("root1.html").createFile();
        BaseFolder.resolveFile("root2.html").createFile();
        BaseFolder.resolveFile("f1/a.html").createFile();
        BaseFolder.resolveFile("f2/b.html").createFile();
        BaseFolder.resolveFile("f3/c.html").createFile();
        BaseFolder.resolveFile("f4/").createFolder();
        BaseFolder.resolveFile("f5/").createFolder();
        BaseFolder.resolveFile("f6/f7").createFolder();
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

    @Test
    public void testFileOrFolders() throws Exception {
        final FileSelector selector = new FileTypeSelector(FileType.FILE_OR_FOLDER);
        final FileObject[] foList = BaseFolder.findFiles(selector);
        // Why 0?
        Assert.assertEquals(0, foList.length);
    }

    @Test
    public void testFiles() throws Exception {
        final FileSelector selector = new FileTypeSelector(FileType.FILE);
        final FileObject[] foList = BaseFolder.findFiles(selector);
        Assert.assertEquals(5, foList.length);
    }

    @Test
    public void testFolders() throws Exception {
        final FileSelector selector = new FileTypeSelector(FileType.FOLDER);
        final FileObject[] foList = BaseFolder.findFiles(selector);
        Assert.assertEquals(8, foList.length);
    }
}
