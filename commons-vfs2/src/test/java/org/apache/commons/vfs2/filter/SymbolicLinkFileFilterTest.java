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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileFilter;
import org.apache.commons.vfs2.FileFilterSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link SymbolicLinkFileFilter}.
 * <p>
 * On Windows, in order for this test to pass, you MUST run the VM with admin rights.
 * </p>
 * <p>
 * To enable this test set the system property "SymbolicLinkFileFilterTest.Enable" to "true".
 *
 * <p>
 * To run only this test with Maven:
 * </p>
 *
 * <pre>
 * mvn test -Dtest=SymbolicLinkFileFilterTest -pl commons-vfs2 -DSymbolicLinkFileFilterTest.Enable=true
 * </pre>
 */
// CHECKSTYLE:OFF Test code
public class SymbolicLinkFileFilterTest extends BaseFilterTest {

    private static File testDir;

    private static File targetFile;

    private static FileSelectInfo targetFileInfo;

    private static File linkFile;

    private static FileSelectInfo linkFileInfo;

    private static File notExistingFile;

    private static FileSelectInfo notExistingFileInfo;

    private static File zipFile;

    private static FileObject zipFileObject;

    @AfterAll
    public static void afterClass() throws IOException {
        targetFile = null;
        targetFileInfo = null;
        linkFile = null;
        linkFileInfo = null;
        notExistingFile = null;
        notExistingFileInfo = null;
        if (zipFileObject != null) {
            zipFileObject.close();
        }
        if (zipFile != null) {
            FileUtils.deleteQuietly(zipFile);
            zipFile = null;
        }
        if (testDir != null) {
            FileUtils.deleteDirectory(testDir);
            testDir = null;
        }
    }

    @BeforeAll
    public static void beforeClass() throws IOException {
        assumeTrue(Boolean.getBoolean(SymbolicLinkFileFilterTest.class.getSimpleName() + ".Enable"));

        testDir = getTestDir(SymbolicLinkFileFilterTest.class.getName());
        testDir.mkdir();

        linkFile = new File(testDir, "visible.txt");
        linkFileInfo = createFileSelectInfo(linkFile);

        targetFile = new File(testDir, "symbolic.txt");
        Files.deleteIfExists(targetFile.toPath());
        FileUtils.touch(targetFile);
        Files.createSymbolicLink(linkFile.toPath(), targetFile.toPath());
        targetFileInfo = createFileSelectInfo(targetFile);

        notExistingFile = new File(testDir, "not-existing-file.txt");
        notExistingFileInfo = createFileSelectInfo(notExistingFile);

        // Zip the test directory
        zipFile = new File(getTempDir(), SymbolicLinkFileFilterTest.class.getName() + ".zip");
        zipDir(testDir, "", zipFile);
        zipFileObject = getZipFileObject(zipFile);
    }

    @Test
    public void testAcceptActual() throws FileSystemException {
        final FileFilter testee = SymbolicLinkFileFilter.ACTUAL;
        assertTrue(targetFileInfo.getBaseFolder().exists());
        assertTrue(targetFileInfo.getFile().exists());
        assertTrue(testee.accept(targetFileInfo), targetFileInfo.toString());
        assertTrue(testee.accept(notExistingFileInfo), notExistingFileInfo.toString());
    }

    @Test
    public void testAcceptSymbolic() throws FileSystemException {
        final FileFilter testee = SymbolicLinkFileFilter.SYMBOLIC;
        assertTrue(testee.accept(linkFileInfo), linkFileInfo.toString());
        assertFalse(testee.accept(notExistingFileInfo), notExistingFileInfo.toString());
    }

    @Test
    public void testZipFile() throws FileSystemException {
        final FileObject[] files = zipFileObject.findFiles(new FileFilterSelector(SymbolicLinkFileFilter.SYMBOLIC));
        assertEquals(0, files.length);
    }

}
// CHECKSTYLE:ON
