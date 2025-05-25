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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link CanExecuteFileFilter}.
 */
// CHECKSTYLE:OFF Test code
@Disabled("Disabled pre junit v5")
public class CanExecuteFileFilterTest extends BaseFilterTest {

    private static final String EXECUTABLE = "executable.txt";

    private static final String NOT_EXECUTABLE = "notexecutable.txt";

    private static File testDir;

    private static File executableFile;

    private static FileSelectInfo executableFileInfo;

    private static File notExecutableFile;

    private static FileSelectInfo notExecutableFileInfo;

    private static File notExistingFile;

    private static FileSelectInfo notExistingFileInfo;

    private static File zipFile;

    private static FileObject zipFileObj;

    @AfterAll
    public static void afterClass() throws IOException {
        executableFileInfo = null;
        delete(executableFile);

        notExecutableFileInfo = null;
        delete(notExecutableFile);
        notExecutableFile = null;

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

        testDir = getTestDir(CanExecuteFileFilterTest.class.getName());

        executableFile = new File(testDir, EXECUTABLE);
        executableFileInfo = createFileSelectInfo(executableFile);
        FileUtils.touch(executableFile);
        executableFile.setExecutable(true);

        notExecutableFile = new File(testDir, NOT_EXECUTABLE);
        notExecutableFileInfo = createFileSelectInfo(notExecutableFile);
        FileUtils.touch(notExecutableFile);
        notExecutableFile.setExecutable(false);

        notExistingFile = new File(testDir, "not-existing-file.txt");
        notExistingFileInfo = createFileSelectInfo(notExistingFile);

        zipFile = new File(getTempDir(), CanExecuteFileFilterTest.class.getName() + ".zip");
        zipDir(testDir, "", zipFile);
        zipFileObj = getZipFileObject(zipFile);
    }

    @Test
    public void testAcceptCanExecute() throws FileSystemException {
        assertTrue(CanExecuteFileFilter.CAN_EXECUTE.accept(executableFileInfo));
        assertTrue(CanExecuteFileFilter.CAN_EXECUTE.accept(notExecutableFileInfo));
        assertFalse(CanExecuteFileFilter.CAN_EXECUTE.accept(notExistingFileInfo));
    }

    @Test
    public void testAcceptCannotExecute() throws FileSystemException {
        assertFalse(CanExecuteFileFilter.CANNOT_EXECUTE.accept(executableFileInfo));
        assertFalse(CanExecuteFileFilter.CANNOT_EXECUTE.accept(notExecutableFileInfo));
        assertTrue(CanExecuteFileFilter.CANNOT_EXECUTE.accept(notExistingFileInfo));
    }

}
// CHECKSTYLE:ON
