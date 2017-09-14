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

package org.apache.commons.vfs2.provider.zip.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.util.Os;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests https://issues.apache.org/jira/browse/VFS-291
 */
public class FileLockTestCase {

    private FileSystemManager manager;
    private File newZipFile;

    private String zipFileUri;

    private void assertDelete() {
        // We do not use newZipFile in the Assert message to avoid touching it before calling delete().
        Assert.assertTrue("Could not delete file", newZipFile.delete());
    }

    private void resolveAndOpenCloseContent() throws FileSystemException {
        try (final FileObject zipFileObject = manager.resolveFile(zipFileUri)) {
            zipFileObject.getContent().close();
        }
    }

    private void resolveAndOpenCloseInputStream() throws IOException, FileSystemException {
        try (final FileObject zipFileObject = manager.resolveFile(zipFileUri)) {
            zipFileObject.getContent().getInputStream().close();
        }
    }

    private void resolveAndOpenReadCloseInputStream() throws IOException, FileSystemException {
        try (final FileObject zipFileObject = manager.resolveFile(zipFileUri)) {
            try (InputStream inputStream = zipFileObject.getContent().getInputStream()) {
                readAndAssert(inputStream);
            }
        }
    }

    private void readAndAssert(InputStream inputStream) throws IOException {
        String string = IOUtils.toString(inputStream, "UTF-8");
        Assert.assertNotNull(string);
        Assert.assertEquals("This is a test file.", string);
    }

    @Before
    public void setup() throws IOException {
        final File zipFile = new File("src/test/resources/test-data/test.zip");
        newZipFile = File.createTempFile(getClass().getSimpleName(), ".zip");
        newZipFile.deleteOnExit();
        FileUtils.copyFile(zipFile, newZipFile);
        zipFileUri = "zip:file:" + newZipFile.getAbsolutePath() + "!/read-tests/file1.txt";
        manager = VFS.getManager();
    }

    @Test
    public void testCannotDeleteWhileStreaming() throws Exception {
        try (final FileObject zipFileObject = manager.resolveFile(zipFileUri)) {
            try (InputStream inputStream = zipFileObject.getContent().getInputStream()) {
                if (Os.isFamily(Os.OS_FAMILY_WINDOWS)) {
                    // We do not use newZipFile in the Assert message to avoid touching it before calling delete().
                    Assert.assertFalse("Could not delete file", newZipFile.delete());
                }
            }
        }
        assertDelete();
    }

    @Test
    public void testCannotDeleteWhileStreaming2() throws Exception {
        Assume.assumeTrue(Os.isFamily(Os.OS_FAMILY_WINDOWS));
        try (final FileObject zipFileObject = manager.resolveFile(zipFileUri)) {
            try (InputStream inputStream = zipFileObject.getContent().getInputStream()) {
                // We do not use newZipFile in the Assert message to avoid touching it before calling delete().
                Assert.assertFalse("Could not delete file", newZipFile.delete());
            }
        }
    }

    @Test
    public void testResolveAndOpenCloseContent() throws Exception {
        resolveAndOpenCloseContent();
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
    public void testResolveOpenCloseNestedInputStreams() throws Exception {
        try (final FileObject zipFileObject = manager.resolveFile(zipFileUri)) {
            try (final FileObject zipFileObject2 = manager.resolveFile(zipFileUri)) {
                zipFileObject2.getContent().getInputStream().close();
            }
            zipFileObject.getContent().getInputStream().close();
        }
        assertDelete();
    }

    @Test
    public void testReadClosedFileObject() throws Exception {
        final FileObject zipFileObjectRef;
        try (final FileObject zipFileObject = manager.resolveFile(zipFileUri)) {
            zipFileObjectRef = zipFileObject;
            try (final InputStream inputStream = zipFileObject.getContent().getInputStream()) {
                readAndAssert(inputStream);
            }
        }
        try (final InputStream inputStream = zipFileObjectRef.getContent().getInputStream()) {
            readAndAssert(inputStream);
        } finally {
            zipFileObjectRef.close();
        }
        assertDelete();
    }

}
