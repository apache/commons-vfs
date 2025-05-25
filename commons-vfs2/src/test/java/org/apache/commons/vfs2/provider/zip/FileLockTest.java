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
package org.apache.commons.vfs2.provider.zip;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests https://issues.apache.org/jira/browse/VFS-291.
 */
public class FileLockTest {

    private FileSystemManager manager;
    private Path newZipFile;

    private String zipFileUri;

    private void assertDelete() throws IOException {
        // We do not use newZipFile in the Assert message to avoid touching it before calling delete().
        Files.delete(newZipFile);
    }

    private void readAndAssert(final InputStream inputStream) throws IOException {
        final String string = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        assertNotNull(string);
        assertEquals("This is a test file.", string);
    }

    private void resolveAndOpenCloseContent() throws FileSystemException {
        try (FileObject zipFileObject = manager.resolveFile(zipFileUri)) {
            zipFileObject.getContent().close();
        }
    }

    private void resolveAndOpenCloseInputStream() throws IOException, FileSystemException {
        try (FileObject zipFileObject = manager.resolveFile(zipFileUri)) {
            zipFileObject.getContent().getInputStream().close();
        }
    }

    private void resolveAndOpenReadCloseInputStream() throws IOException, FileSystemException {
        try (FileObject zipFileObject = manager.resolveFile(zipFileUri)) {
            try (InputStream inputStream = zipFileObject.getContent().getInputStream()) {
                readAndAssert(inputStream);
            }
        }
    }

    @BeforeEach
    public void setup() throws IOException {
        final Path zipFile = Paths.get("src/test/resources/test-data/test.zip");
        newZipFile = Files.createTempFile(getClass().getSimpleName(), ".zip");
        newZipFile.toFile().deleteOnExit();
        Files.copy(zipFile, newZipFile, StandardCopyOption.REPLACE_EXISTING);
        zipFileUri = "zip:file:" + newZipFile.toAbsolutePath() + "!/read-tests/file1.txt";
        manager = VFS.getManager();
    }

    @Test
    public void testCannotDeleteWhileStreaming() throws Exception {
        try (FileObject zipFileObject = manager.resolveFile(zipFileUri)) {
            try (InputStream inputStream = zipFileObject.getContent().getInputStream()) {
                if (SystemUtils.IS_OS_WINDOWS) {
                    // We do not use newZipFile in the Assert message to avoid touching it before calling delete().
                    assertFalse(newZipFile.toFile().delete(), "Could not delete file");
                }
            }
        }
        assertDelete();
    }

    @Test
    public void testCannotDeleteWhileStreaming2() throws Exception {
        assumeTrue(SystemUtils.IS_OS_WINDOWS);
        try (FileObject zipFileObject = manager.resolveFile(zipFileUri)) {
            try (InputStream inputStream = zipFileObject.getContent().getInputStream()) {
                // We do not use newZipFile in the Assert message to avoid touching it before calling delete().
                assertFalse(newZipFile.toFile().delete(), "Could not delete file");
            }
        }
    }

    @Test
    public void testReadClosedFileObject() throws Exception {
        final FileObject zipFileObjectRef;
        try (FileObject zipFileObject = manager.resolveFile(zipFileUri)) {
            zipFileObjectRef = zipFileObject;
            try (InputStream inputStream = zipFileObject.getContent().getInputStream()) {
                readAndAssert(inputStream);
            }
        }
        try (InputStream inputStream = zipFileObjectRef.getContent().getInputStream()) {
            readAndAssert(inputStream);
        } finally {
            zipFileObjectRef.close();
        }
        assertDelete();
    }

    @Test
    public void testResolveAndOpenCloseContent() throws Exception {
        resolveAndOpenCloseContent();
        assertDelete();
    }

    @Test
    public void testResolveAndOpenCloseContent3() throws Exception {
        resolveAndOpenCloseContent();
        resolveAndOpenCloseContent();
        resolveAndOpenCloseContent();

        assertDelete();
    }

    /**
     * This test checks whether we can modify an underlying Zip file after we have performed IO operations on files
     * within it, but although we no longer have any FileObjects explicitly open.
     *
     * @throws Exception
     */
    @Test
    public void testResolveAndOpenCloseInputStream() throws Exception {
        resolveAndOpenCloseInputStream();
        assertDelete();
    }

    @Test
    public void testResolveAndOpenCloseInputStream3() throws Exception {
        resolveAndOpenCloseInputStream();
        resolveAndOpenCloseInputStream();
        resolveAndOpenCloseInputStream();

        assertDelete();
    }

    @Test
    public void testResolveAndOpenReadCloseInputStream() throws Exception {
        resolveAndOpenReadCloseInputStream();
        assertDelete();
    }

    @Test
    public void testResolveAndOpenReadCloseInputStream3() throws Exception {
        resolveAndOpenReadCloseInputStream();
        resolveAndOpenReadCloseInputStream();
        resolveAndOpenReadCloseInputStream();
        assertDelete();
    }

    @Test
    public void testResolveOpenCloseNestedInputStreams() throws Exception {
        try (FileObject zipFileObject = manager.resolveFile(zipFileUri)) {
            try (FileObject zipFileObject2 = manager.resolveFile(zipFileUri)) {
                zipFileObject2.getContent().getInputStream().close();
            }
            zipFileObject.getContent().getInputStream().close();
        }
        assertDelete();
    }

}
