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
import org.apache.commons.vfs2.FileFilterSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for {@link SizeFileFilter}.
 */
// CHECKSTYLE:OFF Test code
public class SizeFileFilterTest extends BaseFilterTest {

    private static File testDir;

    private static File minFile;

    private static FileSelectInfo minFileInfo;

    private static File optFile;

    private static FileSelectInfo optFileInfo;

    private static File maxFile;

    private static FileSelectInfo maxFileInfo;

    private static File zipFile;

    private static FileObject zipFileObj;

    @BeforeClass
    public static void beforeClass() throws IOException {
        testDir = getTestDir(SizeFileFilterTest.class.getName());

        // 2 characters
        minFile = new File(testDir, "min.txt");
        FileUtils.write(minFile, "12");
        minFileInfo = createFileSelectInfo(minFile);

        // 4 characters
        optFile = new File(testDir, "opt.txt");
        FileUtils.write(optFile, "1234");
        optFileInfo = createFileSelectInfo(optFile);

        // 6 characters
        maxFile = new File(testDir, "max.txt");
        FileUtils.write(maxFile, "123456");
        maxFileInfo = createFileSelectInfo(maxFile);

        // Zip the test directory
        zipFile = new File(getTempDir(), SizeFileFilterTest.class.getName() + ".zip");
        zipDir(testDir, "", zipFile);
        zipFileObj = getZipFileObject(zipFile);

    }

    @AfterClass
    public static void afterClass() throws IOException {

        minFileInfo = null;
        minFile = null;

        optFileInfo = null;
        optFile = null;

        maxFileInfo = null;
        maxFile = null;

        zipFileObj.close();
        FileUtils.deleteQuietly(zipFile);
        zipFile = null;

        FileUtils.deleteDirectory(testDir);
        testDir = null;
    }

    @Test
    public void testSizeFileFilterLong() throws FileSystemException {

        final SizeFileFilter testee = new SizeFileFilter(4);
        Assert.assertFalse(testee.accept(minFileInfo));
        Assert.assertTrue(testee.accept(optFileInfo));
        Assert.assertTrue(testee.accept(maxFileInfo));

    }

    @Test
    public void testSizeFileFilterLongBoolean() throws FileSystemException {

        SizeFileFilter testee;

        testee = new SizeFileFilter(4, true);
        Assert.assertFalse(testee.accept(minFileInfo));
        Assert.assertTrue(testee.accept(optFileInfo));
        Assert.assertTrue(testee.accept(maxFileInfo));

        testee = new SizeFileFilter(4, false);
        Assert.assertTrue(testee.accept(minFileInfo));
        Assert.assertFalse(testee.accept(optFileInfo));
        Assert.assertFalse(testee.accept(maxFileInfo));

    }

    @Test
    public void testSizeRangeFileFilter() throws FileSystemException {

        SizeRangeFileFilter testee;

        testee = new SizeRangeFileFilter(2, 6);
        Assert.assertTrue(testee.accept(minFileInfo));
        Assert.assertTrue(testee.accept(optFileInfo));
        Assert.assertTrue(testee.accept(maxFileInfo));

        testee = new SizeRangeFileFilter(3, 6);
        Assert.assertFalse(testee.accept(minFileInfo));
        Assert.assertTrue(testee.accept(optFileInfo));
        Assert.assertTrue(testee.accept(maxFileInfo));

        testee = new SizeRangeFileFilter(2, 5);
        Assert.assertTrue(testee.accept(minFileInfo));
        Assert.assertTrue(testee.accept(optFileInfo));
        Assert.assertFalse(testee.accept(maxFileInfo));

        testee = new SizeRangeFileFilter(3, 5);
        Assert.assertFalse(testee.accept(minFileInfo));
        Assert.assertTrue(testee.accept(optFileInfo));
        Assert.assertFalse(testee.accept(maxFileInfo));

        testee = new SizeRangeFileFilter(4, 4);
        Assert.assertFalse(testee.accept(minFileInfo));
        Assert.assertTrue(testee.accept(optFileInfo));
        Assert.assertFalse(testee.accept(maxFileInfo));

    }

    @Test
    public void testSizeFileFilterZipDir() throws FileSystemException {

        // Same test with ZIP file
        FileObject[] files;

        files = zipFileObj.findFiles(new FileFilterSelector(new SizeFileFilter(4, true)));
        assertContains(files, optFile.getName(), maxFile.getName());
        Assert.assertEquals(2, files.length);

        files = zipFileObj.findFiles(new FileFilterSelector(new SizeFileFilter(4, false)));
        assertContains(files, minFile.getName());
        Assert.assertEquals(1, files.length);

        files = zipFileObj.findFiles(new FileFilterSelector(new SizeRangeFileFilter(2, 6)));
        assertContains(files, minFile.getName(), optFile.getName(), maxFile.getName());
        Assert.assertEquals(3, files.length);

        files = zipFileObj.findFiles(new FileFilterSelector(new SizeRangeFileFilter(3, 6)));
        assertContains(files, optFile.getName(), maxFile.getName());
        Assert.assertEquals(2, files.length);

        files = zipFileObj.findFiles(new FileFilterSelector(new SizeRangeFileFilter(2, 5)));
        assertContains(files, minFile.getName(), optFile.getName());
        Assert.assertEquals(2, files.length);

        files = zipFileObj.findFiles(new FileFilterSelector(new SizeRangeFileFilter(3, 5)));
        assertContains(files, optFile.getName());
        Assert.assertEquals(1, files.length);

        files = zipFileObj.findFiles(new FileFilterSelector(new SizeRangeFileFilter(4, 4)));
        assertContains(files, optFile.getName());
        Assert.assertEquals(1, files.length);

    }

}
// CHECKSTYLE:ON
