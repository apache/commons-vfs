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
package org.apache.commons.vfs2.filter;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileFilter;
import org.apache.commons.vfs2.FileFilterSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for {@link HiddenFileFilter}.
 */
// CHECKSTYLE:OFF Test code
public class HiddenFileFilterTest extends BaseFilterTest {

    private static File testDir;

    private static File visibleFile;

    private static FileSelectInfo visibleFileInfo;

    private static File hiddenFile;

    private static FileSelectInfo hiddenFileInfo;

    private static File notExistingFile;

    private static FileSelectInfo notExistingFileInfo;

    private static File zipFile;

    private static FileObject zipFileObj;

    @BeforeClass
    public static void beforeClass() throws IOException {
        testDir = getTestDir(HiddenFileFilterTest.class.getName());
        testDir.mkdir();

        visibleFile = new File(testDir, "visible.txt");
        FileUtils.touch(visibleFile);
        visibleFileInfo = createFileSelectInfo(visibleFile);

        hiddenFile = new File(testDir, "hidden.txt");
        // TODO xxx In Java 6 there is no way to hide a file
        // hiddenFile.setVisible(false);
        hiddenFileInfo = createFileSelectInfo(hiddenFile);

        notExistingFile = new File(testDir, "not-existing-file.txt");
        notExistingFileInfo = createFileSelectInfo(notExistingFile);

        // Zip the test directory
        zipFile = new File(getTempDir(), HiddenFileFilterTest.class.getName() + ".zip");
        zipDir(testDir, "", zipFile);
        zipFileObj = getZipFileObject(zipFile);

    }

    @AfterClass
    public static void afterClass() throws IOException {

        visibleFile = null;
        visibleFileInfo = null;
        hiddenFile = null;
        hiddenFileInfo = null;
        notExistingFile = null;
        notExistingFileInfo = null;

        zipFileObj.close();
        FileUtils.deleteQuietly(zipFile);
        zipFile = null;

        FileUtils.deleteDirectory(testDir);
        testDir = null;
    }

    @Test
    public void testAcceptHidden() throws FileSystemException {

        final FileFilter testee = HiddenFileFilter.HIDDEN;

        Assert.assertFalse(testee.accept(visibleFileInfo));
        // TODO xxx In Java 6 there is no way to hide a file
        // assertThat(testee.accept(hiddenFileInfo));
        Assert.assertFalse(testee.accept(notExistingFileInfo));

    }

    @Test
    public void testAcceptVisible() throws FileSystemException {

        final FileFilter testee = HiddenFileFilter.VISIBLE;

        Assert.assertTrue(testee.accept(visibleFileInfo));
        // TODO xxx In Java 6 there is no way to hide a file
        // assertThat(testee.accept(hiddenFileInfo));
        Assert.assertTrue(testee.accept(notExistingFileInfo));

    }

    @Test
    public void testZipFile() throws FileSystemException {

        // Same test with ZIP file
        FileObject[] files;

        // TODO xxx In Java 6 there is no way to hide a file
        // files = zipFileObj.findFiles(new
        // FileFilterSelector(HiddenFileFilter.HIDDEN));
        // assertContains(files, hiddenFile.getName());
        // assertThat(files).hasSize(1);

        files = zipFileObj.findFiles(new FileFilterSelector(HiddenFileFilter.VISIBLE));
        assertContains(files, visibleFile.getName());
        Assert.assertEquals(1, files.length);

    }

}
// CHECKSTYLE:ON
