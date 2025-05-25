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
package org.apache.commons.vfs2.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileFilterSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link AgeFileFilter}.
 */
// CHECKSTYLE:OFF Test code
public class AgeFileFilterTest extends BaseFilterTest {

    private static final long DAY_MILLIS = 24 * 60 * 60 * 1000;

    private static final long NOW_MILLIS = System.currentTimeMillis();

    private static final long TWO_DAYS_AGO_MILLIS = NOW_MILLIS - 2 * DAY_MILLIS;

    private static final long TWO_DAYS_LATER_MILLIS = NOW_MILLIS + 2 * DAY_MILLIS;

    private static File testDir;

    private static File oldFile;

    private static FileSelectInfo oldFileInfo;

    private static File newFile;

    private static FileSelectInfo newFileInfo;

    private static File currentFile;

    private static FileSelectInfo currentFileInfo;

    private static File zipFile;

    private static FileObject zipFileObj;

    @AfterAll
    public static void afterClass() throws IOException {
        newFileInfo = null;
        newFile = null;

        currentFileInfo = null;
        currentFile = null;

        oldFileInfo = null;
        oldFile = null;

        IOUtils.close(zipFileObj);
        FileUtils.deleteQuietly(zipFile);
        zipFile = null;

        FileUtils.deleteDirectory(testDir);
        testDir = null;
    }

    @BeforeAll
    public static void beforeClass() throws IOException {
        testDir = getTestDir(AgeFileFilterTest.class.getName());

        // Set the file's time stamp two days back
        oldFile = new File(testDir, "old.txt");
        FileUtils.touch(oldFile);
        oldFile.setLastModified(TWO_DAYS_AGO_MILLIS);
        oldFileInfo = createFileSelectInfo(oldFile);

        // Reference file
        currentFile = new File(testDir, "current.txt");
        FileUtils.touch(currentFile);
        currentFile.setLastModified(NOW_MILLIS);
        currentFileInfo = createFileSelectInfo(currentFile);

        // Set the file's time stamp two days into the future
        newFile = new File(testDir, "new.txt");
        FileUtils.touch(newFile);
        newFile.setLastModified(TWO_DAYS_LATER_MILLIS);
        newFileInfo = createFileSelectInfo(newFile);

        // Zip the test directory
        zipFile = new File(getTempDir(), AgeFileFilterTest.class.getName() + ".zip");
        zipDir(testDir, "", zipFile);
        zipFileObj = getZipFileObject(zipFile);
    }

    @Test
    public void testAgeFileFilterDate() throws FileSystemException {

        final AgeFileFilter testee = new AgeFileFilter(new Date());
        assertTrue(testee.accept(oldFileInfo));
        assertTrue(testee.accept(currentFileInfo));
        assertFalse(testee.accept(newFileInfo));
    }

    @Test
    public void testAgeFileFilterDateBoolean() throws FileSystemException {

        AgeFileFilter testee;

        testee = new AgeFileFilter(new Date(), true);
        assertTrue(testee.accept(oldFileInfo));
        assertTrue(testee.accept(currentFileInfo));
        assertFalse(testee.accept(newFileInfo));

        testee = new AgeFileFilter(new Date(), false);
        assertFalse(testee.accept(oldFileInfo));
        assertFalse(testee.accept(currentFileInfo));
        assertTrue(testee.accept(newFileInfo));
    }

    @Test
    public void testAgeFileFilterFile() throws FileSystemException {

        final AgeFileFilter testee = new AgeFileFilter(currentFileInfo.getFile());
        assertTrue(testee.accept(oldFileInfo));
        assertTrue(testee.accept(currentFileInfo));
        assertFalse(testee.accept(newFileInfo));
    }

    @Test
    public void testAgeFileFilterFileBoolean() throws FileSystemException {

        AgeFileFilter testee;

        testee = new AgeFileFilter(currentFileInfo.getFile(), true);
        assertTrue(testee.accept(oldFileInfo));
        assertTrue(testee.accept(currentFileInfo));
        assertFalse(testee.accept(newFileInfo));

        testee = new AgeFileFilter(currentFileInfo.getFile(), false);
        assertFalse(testee.accept(oldFileInfo));
        assertFalse(testee.accept(currentFileInfo));
        assertTrue(testee.accept(newFileInfo));
    }

    @Test
    public void testAgeFileFilterLong() throws FileSystemException {

        final AgeFileFilter testee = new AgeFileFilter(NOW_MILLIS);
        assertTrue(testee.accept(oldFileInfo));
        assertTrue(testee.accept(currentFileInfo));
        assertFalse(testee.accept(newFileInfo));
    }

    @Test
    public void testAgeFileFilterLongBoolean() throws FileSystemException {

        AgeFileFilter testee;

        testee = new AgeFileFilter(NOW_MILLIS, true);
        assertTrue(testee.accept(oldFileInfo));
        assertTrue(testee.accept(currentFileInfo));
        assertFalse(testee.accept(newFileInfo));

        testee = new AgeFileFilter(NOW_MILLIS, false);
        assertFalse(testee.accept(oldFileInfo));
        assertFalse(testee.accept(currentFileInfo));
        assertTrue(testee.accept(newFileInfo));

        // Same test with ZIP file
        FileObject[] files;

        files = zipFileObj.findFiles(new FileFilterSelector(new AgeFileFilter(NOW_MILLIS, true)));
        assertContains(files, oldFile.getName(), currentFile.getName());
        assertEquals(2, files.length);

        files = zipFileObj.findFiles(new FileFilterSelector(new AgeFileFilter(NOW_MILLIS, false)));
        assertContains(files, newFile.getName());
        assertEquals(1, files.length);
    }

}
// CHECKSTYLE:ON
