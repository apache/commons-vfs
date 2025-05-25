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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ZipFileObjectTest {

    private static final String NESTED_FILE_1 = "/read-xml-tests/file1.xml";
    private static final String NESTED_FILE_2 = "/read-xml-tests/file2.xml";

    private void assertDelete(final Path fileObject) throws IOException {
        Files.delete(fileObject);
    }

    private Path createTempFile() throws IOException {
        final Path zipFile = Paths.get("src/test/resources/test-data/read-xml-tests.zip");
        final Path newZipFile = Files.createTempFile(getClass().getSimpleName(), ".zip");
        newZipFile.toFile().deleteOnExit();
        Files.copy(zipFile, newZipFile, StandardCopyOption.REPLACE_EXISTING);
        return newZipFile;
    }

    private void getInputStreamAndAssert(final FileObject fileObject, final String expectedId)
            throws FileSystemException, IOException {
        readAndAssert(fileObject, fileObject.getContent().getInputStream(), expectedId);
    }

    private void readAndAssert(final FileObject fileObject, final InputStream inputStream, final String expectedId)
            throws IOException {
        final String streamData = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        final String fileObjectString = fileObject.toString();
        assertNotNull(fileObjectString, streamData);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<Root" + expectedId + ">foo" + expectedId + "</Root" + expectedId + ">\r\n", streamData,
                fileObjectString);
    }

    private void resolveReadAssert(final FileObject zipFileObject, final String path)
            throws IOException, FileSystemException {
        try (FileObject zipFileObject2 = zipFileObject.resolveFile(path)) {
            try (InputStream inputStream = zipFileObject2.getContent().getInputStream()) {
                readAndAssert(zipFileObject2, inputStream, "2");
            }
        }
    }

    /**
     * Tests that when we read a file inside a file Zip and leave it open, we can still delete the Zip after we clean up
     * the Zip file.
     *
     * @throws IOException
     */
    @Test
    @Disabled("Shows that leaving a stream open and not closing any resource leaves the container file locked")
    public void testLeaveNestedFileOpen() throws IOException {
        final Path newZipFile = createTempFile();
        final FileSystemManager manager = VFS.getManager();
        try (FileObject zipFileObject = manager.resolveFile("zip:file:" + newZipFile.toAbsolutePath())) {
            @SuppressWarnings({ "resource" })
            final FileObject zipFileObject1 = zipFileObject.resolveFile(NESTED_FILE_1);
            getInputStreamAndAssert(zipFileObject1, "1");
        }
        assertDelete(newZipFile);
    }

    /**
     * Tests that we can read more than one file within a Zip file, especially after closing each FileObject.
     *
     * @throws IOException
     */
    @Test
    public void testReadingFilesInZipFile() throws IOException {
        final Path newZipFile = createTempFile();
        final FileSystemManager manager = VFS.getManager();
        try (FileObject zipFileObject = manager.resolveFile("zip:file:" + newZipFile.toAbsolutePath())) {
            try (FileObject zipFileObject1 = zipFileObject.resolveFile(NESTED_FILE_1)) {
                try (InputStream inputStream = zipFileObject1.getContent().getInputStream()) {
                    readAndAssert(zipFileObject1, inputStream, "1");
                }
            }
            resolveReadAssert(zipFileObject, NESTED_FILE_2);
        }
        assertDelete(newZipFile);
    }

    /**
     * Tests that we can get a stream from one file in a ZIP file, then close another file from the same zip, then
     * process the initial input stream.
     *
     * @throws IOException
     */
    @Test
    public void testReadingOneAfterClosingAnotherFile() throws IOException {
        final Path newZipFile = createTempFile();
        final FileSystemManager manager = VFS.getManager();
        final FileObject zipFileObject1;
        final InputStream inputStream1;
        try (FileObject zipFileObject = manager.resolveFile("zip:file:" + newZipFile.toAbsolutePath())) {
            // leave resources open
            zipFileObject1 = zipFileObject.resolveFile(NESTED_FILE_1);
            inputStream1 = zipFileObject1.getContent().getInputStream();
        }
        // The zip file is "closed", but we read from the stream now.
        readAndAssert(zipFileObject1, inputStream1, "1");
        // clean up
        zipFileObject1.close();
        assertDelete(newZipFile);
    }

    /**
     * Tests that we can get a stream from one file in a ZIP file, then close another file from the same zip, then
     * process the initial input stream. If our internal reference counting is correct, the test passes.
     *
     * @throws IOException
     */
    @Test
    public void testReadingOneAfterClosingAnotherStream() throws IOException {
        final Path newZipFile = createTempFile();
        final FileSystemManager manager = VFS.getManager();
        final FileObject zipFileObject1;
        final InputStream inputStream1;
        try (FileObject zipFileObject = manager.resolveFile("zip:file:" + newZipFile.toAbsolutePath())) {
            // leave resources open (note that internal counters are updated)
            zipFileObject1 = zipFileObject.resolveFile(NESTED_FILE_1);
            inputStream1 = zipFileObject1.getContent().getInputStream();
            resolveReadAssert(zipFileObject, NESTED_FILE_2);
        }
        // The Zip file is "closed", but we read from the stream now, which currently fails.
        // Why aren't internal counters preventing the stream from closing?
        readAndAssert(zipFileObject1, inputStream1, "1");
        // clean up
        zipFileObject1.close();
        assertDelete(newZipFile);
    }

    /**
     * Test read file with special name in a ZIP file.
     */
    @Test
    public void testReadSpecialNameFileInZipFile() throws FileSystemException {

        final File testFile = new File("src/test/resources/test-data/special_fileName.zip");
        final String[] fileNames = {"file.txt", "file^.txt", "file~.txt", "file?.txt", "file@.txt", "file$.txt",
                                    "file*.txt", "file&.txt", "file#.txt", "file%.txt", "file!.txt"};
        final FileSystemManager manager = VFS.getManager();
        final String baseUrl = "zip:file:"+testFile.getAbsolutePath();

        // test
        try (FileObject fileObject = manager.resolveFile(baseUrl)) {
            // test getChildren() number equal
            assertEquals(fileObject.getChildren().length, fileNames.length);

            // test getChild(String)
            for (final String fileName : fileNames) {
                assertNotNull(fileObject.getChild(fileName), () -> "can't read file " + fileName);
            }
        }
    }

    /**
     * Tests that we can resolve a file in a Zip file, then close the container zip, which should still let us delete
     * the Zip file.
     *
     * @throws IOException
     */
    @Test
    public void testResolveNestedFileWithoutCleanup() throws IOException {
        final Path newZipFile = createTempFile();
        final FileSystemManager manager = VFS.getManager();
        try (FileObject zipFileObject = manager.resolveFile("zip:file:" + newZipFile.toAbsolutePath())) {
            @SuppressWarnings({ "unused", "resource" })
            // We resolve a nested file and do nothing else.
            final FileObject zipFileObject1 = zipFileObject.resolveFile(NESTED_FILE_1);
        }
        assertDelete(newZipFile);
    }

}
