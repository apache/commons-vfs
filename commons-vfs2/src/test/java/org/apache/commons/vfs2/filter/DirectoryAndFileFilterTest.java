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
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for {@link DirectoryFileFilter} and {@link FileFileFilter}.
 */
// CHECKSTYLE:OFF Test code
public class DirectoryAndFileFilterTest extends BaseFilterTest {

    private static final String FILE = "myfile.txt";

    private static final String DIR = "mydir";

    private static File testDir;

    private static File file;

    private static FileSelectInfo fileInfo;

    private static File dir;

    private static FileSelectInfo dirInfo;

    private static File notExistingFile;

    private static FileSelectInfo notExistingFileInfo;

    private static File zipFile;

    private static FileObject zipFileObj;

    @BeforeClass
    public static void beforeClass() throws IOException {

        testDir = getTestDir(DirectoryAndFileFilterTest.class.getName());
        testDir.mkdir();

        dir = new File(testDir, DIR);
        dir.mkdir();
        dirInfo = createFileSelectInfo(dir);

        file = new File(dir, FILE);
        FileUtils.touch(file);
        fileInfo = createFileSelectInfo(file);

        notExistingFile = new File(testDir, "not-existing-file.txt");
        notExistingFileInfo = createFileSelectInfo(notExistingFile);

        zipFile = new File(getTempDir(), DirectoryAndFileFilterTest.class.getName() + ".zip");
        zipDir(testDir, "", zipFile);
        zipFileObj = getZipFileObject(zipFile);

    }

    @AfterClass
    public static void afterClass() throws IOException {

        file = null;
        fileInfo = null;

        dir = null;
        dirInfo = null;

        notExistingFileInfo = null;
        notExistingFile = null;

        zipFileObj.close();
        FileUtils.deleteQuietly(zipFile);
        zipFile = null;

        FileUtils.deleteDirectory(testDir);

        testDir = null;

    }

    @Test
    public void testDirectoryFileFilter() throws FileSystemException {

        final FileFilter testee = DirectoryFileFilter.DIRECTORY;

        Assert.assertTrue(testee.accept(dirInfo));
        Assert.assertFalse(testee.accept(fileInfo));
        Assert.assertFalse(testee.accept(notExistingFileInfo));

    }

    @Test
    public void testFileFileFilter() throws FileSystemException {

        final FileFilter testee = FileFileFilter.FILE;

        Assert.assertTrue(testee.accept(fileInfo));
        Assert.assertFalse(testee.accept(dirInfo));
        Assert.assertFalse(testee.accept(notExistingFileInfo));

    }

    @Test
    public void testAcceptZipFile() throws FileSystemException {

        FileObject[] files;

        // FILE Filter
        files = zipFileObj.findFiles(new FileSelector() {
            @Override
            public boolean traverseDescendents(final FileSelectInfo fileInfo) throws Exception {
                return true;
            }

            @Override
            public boolean includeFile(final FileSelectInfo fileInfo) throws Exception {
                return FileFileFilter.FILE.accept(fileInfo);
            }
        });
        assertContains(files, FILE);
        Assert.assertEquals(1, files.length);

        // DIRECTORY Filter
        files = zipFileObj.findFiles(new FileFilterSelector(DirectoryFileFilter.DIRECTORY));
        assertContains(files, DIR);
        Assert.assertEquals(1, files.length);

    }

}
// CHECKSTYLE:ON
