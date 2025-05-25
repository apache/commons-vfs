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
import org.apache.commons.vfs2.FileFilterSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link CanWriteFileFilter}.
 */
// CHECKSTYLE:OFF Test code
public class CanWriteFileFilterTest extends BaseFilterTest {

    private static final String WRITABLE = "writable.txt";

    private static final String READONLY = "readonly.txt";

    private static File testDir;

    private static File writableFile;

    private static FileSelectInfo writableFileInfo;

    private static File readOnlyFile;

    private static FileSelectInfo readOnlyFileInfo;

    private static File notExistingFile;

    private static FileSelectInfo notExistingFileInfo;

    private static File zipFile;

    private static FileObject zipFileObj;

    @AfterAll
    public static void afterClass() throws IOException {

        writableFileInfo = null;
        delete(writableFile);
        writableFile = null;

        readOnlyFileInfo = null;
        delete(readOnlyFile);
        readOnlyFile = null;

        notExistingFileInfo = null;
        notExistingFile = null;

        IOUtils.close(zipFileObj);
        FileUtils.deleteQuietly(zipFile);
        zipFile = null;

        FileUtils.deleteDirectory(testDir);
        testDir = null;
    }

    @BeforeAll
    public static void beforeClass() throws IOException {

        testDir = getTestDir(CanWriteFileFilterTest.class.getName());

        writableFile = new File(testDir, WRITABLE);
        writableFileInfo = createFileSelectInfo(writableFile);
        FileUtils.touch(writableFile);

        readOnlyFile = new File(testDir, READONLY);
        readOnlyFileInfo = createFileSelectInfo(readOnlyFile);
        FileUtils.touch(readOnlyFile);
        readOnlyFile.setReadable(true);
        readOnlyFile.setWritable(false);

        notExistingFile = new File(testDir, "not-existing-file.txt");
        notExistingFileInfo = createFileSelectInfo(notExistingFile);

        zipFile = new File(getTempDir(), CanWriteFileFilterTest.class.getName() + ".zip");
        zipDir(testDir, "", zipFile);
        zipFileObj = getZipFileObject(zipFile);
    }

    @Test
    public void testAcceptCannotWrite() throws FileSystemException {

        assertFalse(CanWriteFileFilter.CANNOT_WRITE.accept(writableFileInfo));
        assertTrue(CanWriteFileFilter.CANNOT_WRITE.accept(readOnlyFileInfo));
        assertFalse(CanWriteFileFilter.CANNOT_WRITE.accept(notExistingFileInfo));
    }

    @Test
    public void testAcceptCanWrite() throws FileSystemException {

        assertTrue(CanWriteFileFilter.CAN_WRITE.accept(writableFileInfo));
        assertFalse(CanWriteFileFilter.CAN_WRITE.accept(readOnlyFileInfo));
        assertTrue(CanWriteFileFilter.CAN_WRITE.accept(notExistingFileInfo));
    }

    @Test
    public void testAcceptZipFile() throws FileSystemException {

        FileObject[] files;

        // CAN_WRITE Filter
        files = zipFileObj.findFiles(new FileFilterSelector(CanWriteFileFilter.CAN_WRITE));
        assertTrue(files == null || files.length == 0);

        // CANNOT_WRITE Filter
        files = zipFileObj.findFiles(new FileFilterSelector(CanWriteFileFilter.CANNOT_WRITE));
        assertContains(files, READONLY, WRITABLE);
        assertEquals(2, files.length);
    }

}
// CHECKSTYLE:ON
