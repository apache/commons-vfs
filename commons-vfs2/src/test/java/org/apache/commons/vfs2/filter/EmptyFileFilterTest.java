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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileFilter;
import org.apache.commons.vfs2.FileFilterSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link EmptyFileFilter}.
 */
// CHECKSTYLE:OFF Test code
public class EmptyFileFilterTest extends BaseFilterTest {

    private static File testDir;

    private static File notEmptyFile;

    private static FileSelectInfo notEmptyFileInfo;

    private static File emptyFile;

    private static FileSelectInfo emptyFileInfo;

    private static File notEmptyDir;

    private static FileSelectInfo notEmptyDirInfo;

    private static File emptyDir;

    private static FileSelectInfo emptyDirInfo;

    private static File notExistingFile;

    private static FileSelectInfo notExistingFileInfo;

    private static File zipFile;

    private static FileObject zipFileObj;

    @AfterAll
    public static void afterClass() throws IOException {

        notEmptyFile = null;
        notEmptyFileInfo = null;
        emptyFile = null;
        emptyFileInfo = null;
        notEmptyDir = null;
        notEmptyDirInfo = null;
        emptyDir = null;
        emptyDirInfo = null;
        notExistingFile = null;
        notExistingFileInfo = null;

        IOUtils.close(zipFileObj);
        FileUtils.deleteQuietly(zipFile);
        zipFile = null;

        FileUtils.deleteDirectory(testDir);
        testDir = null;
    }

    @BeforeAll
    public static void beforeClass() throws IOException {
        testDir = getTestDir(EmptyFileFilterTest.class.getName());
        testDir.mkdir();

        notEmptyFile = new File(testDir, "full.txt");
        FileUtils.write(notEmptyFile, "whatever");
        notEmptyFileInfo = createFileSelectInfo(notEmptyFile);

        emptyFile = new File(testDir, "empty.txt");
        FileUtils.touch(emptyFile);
        emptyFileInfo = createFileSelectInfo(emptyFile);

        notEmptyDir = new File(testDir, "full-dir");
        notEmptyDir.mkdir();
        notEmptyDirInfo = createFileSelectInfo(notEmptyDir);
        FileUtils.touch(new File(notEmptyDir, "foobar.txt"));

        emptyDir = new File(testDir, "empty-dir");
        emptyDir.mkdir();
        emptyDirInfo = createFileSelectInfo(emptyDir);

        notExistingFile = new File(testDir, "not-existing-file.txt");
        notExistingFileInfo = createFileSelectInfo(notExistingFile);

        // Zip the test directory
        zipFile = new File(getTempDir(), EmptyFileFilterTest.class.getName() + ".zip");
        zipDir(testDir, "", zipFile);
        zipFileObj = getZipFileObject(zipFile);
    }

    @Test
    public void testAcceptEmpty() throws FileSystemException {

        final FileFilter testee = EmptyFileFilter.EMPTY;

        assertFalse(testee.accept(notEmptyFileInfo));
        assertTrue(testee.accept(emptyFileInfo));
        assertFalse(testee.accept(notEmptyDirInfo));
        assertTrue(testee.accept(emptyDirInfo));
        assertTrue(testee.accept(notExistingFileInfo));
    }

    @Test
    public void testAcceptNotEmpty() throws FileSystemException {

        final FileFilter testee = EmptyFileFilter.NOT_EMPTY;

        assertTrue(testee.accept(notEmptyFileInfo));
        assertFalse(testee.accept(emptyFileInfo));
        assertTrue(testee.accept(notEmptyDirInfo));
        assertFalse(testee.accept(emptyDirInfo));
        assertFalse(testee.accept(notExistingFileInfo));
    }

    @Test
    public void testZipFile() throws FileSystemException {

        // Same test with ZIP file
        FileObject[] files;

        files = zipFileObj.findFiles(new FileFilterSelector(EmptyFileFilter.EMPTY));
        assertContains(files, emptyFile.getName());
        assertEquals(1, files.length);

        files = zipFileObj.findFiles(new FileFilterSelector(EmptyFileFilter.NOT_EMPTY));
        assertContains(files, notEmptyFile.getName(), notEmptyDir.getName());
        assertEquals(2, files.length);
    }

}
// CHECKSTYLE:ON
