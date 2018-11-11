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
package org.apache.commons.vfs2.test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileContentInfo;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.junit.Assert;

/**
 * Read-only test cases for file providers.
 * <p>
 * TODO - Test getLastModified(), getAttribute().
 */
public class ProviderReadTests extends AbstractProviderTestCase {

    /**
     * Returns the read folder named "dir1".
     *
     * @return the read folder named "dir1".
     * @throws FileSystemException
     */
    protected FileObject getReadFolderDir1() throws FileSystemException {
        return getReadFolder().resolveFile("dir1");
    }

    /**
     * Returns the capabilities required by the tests of this test case.
     */
    @Override
    protected Capability[] getRequiredCaps() {
        return new Capability[] { Capability.GET_TYPE, Capability.LIST_CHILDREN, Capability.READ_CONTENT };
    }

    /**
     * Walks the base folder structure, asserting it contains exactly the expected files and folders.
     */
    public void testStructure() throws Exception {
        final FileInfo baseInfo = buildExpectedStructure();
        assertSameStructure(getReadFolder(), baseInfo);
    }

    /**
     * Walks a folder structure, asserting it contains exactly the expected files and folders.
     */
    protected void assertSameStructure(final FileObject folder, final FileInfo expected) throws Exception {
        // Setup the structure
        final List<FileInfo> queueExpected = new ArrayList<>();
        queueExpected.add(expected);

        final List<FileObject> queueActual = new ArrayList<>();
        queueActual.add(folder);

        while (queueActual.size() > 0) {
            final FileObject file = queueActual.remove(0);
            final FileInfo info = queueExpected.remove(0);

            // Check the type is correct
            assertSame(info.type, file.getType());

            if (info.type == FileType.FILE) {
                continue;
            }

            // Check children
            final FileObject[] children = file.getChildren();

            // Make sure all children were found
            assertNotNull(children);
            int length = children.length;
            if (info.children.size() != children.length) {
                for (final FileObject element : children) {
                    if (element.getName().getBaseName().startsWith(".")) {
                        --length;
                        continue;
                    }
                    System.out.println(element.getName());
                }
            }

            assertEquals("count children of \"" + file.getName() + "\"", info.children.size(), length);

            // Recursively check each child
            for (final FileObject child : children) {
                final String childName = child.getName().getBaseName();
                if (childName.startsWith(".")) {
                    continue;
                }
                final FileInfo childInfo = info.children.get(childName);

                // Make sure the child is expected
                assertNotNull(childInfo);

                // Add to the queue of files to check
                queueExpected.add(childInfo);
                queueActual.add(child);
            }
        }
    }

    /**
     * Tests type determination.
     */
    public void testType() throws Exception {
        // Test a file
        FileObject file = resolveFile1Txt();
        assertSame(FileType.FILE, file.getType());
        assertTrue(file.isFile());

        // Test a folder
        file = getReadFolderDir1();
        assertSame(FileType.FOLDER, file.getType());
        assertTrue(file.isFolder());

        // Test an unknown file
        file = getReadFolder().resolveFile("unknown-child");
        assertSame(FileType.IMAGINARY, file.getType());
    }

    /**
     * Tests the contents of root of file system can be listed.
     */
    public void testRoot() throws FileSystemException {
        if (!this.getProviderConfig().isFileSystemRootAccessible()) {
            return;
        }
        final FileSystem fs = getFileSystem();
        final String uri = fs.getRootURI();
        final FileObject file = getManager().resolveFile(uri, fs.getFileSystemOptions());
        file.getChildren();
    }

    /**
     * Tests that FileObjects can be sorted.
     */
    public void testSort() throws FileSystemException {
        final FileInfo fileInfo = buildExpectedStructure();
        final VerifyingFileSelector selector = new VerifyingFileSelector(fileInfo);

        // Find the files
        final FileObject[] actualFiles = getReadFolder().findFiles(selector);
        Arrays.sort(actualFiles);
        FileObject prevActualFile = actualFiles[0];
        for (final FileObject actualFile : actualFiles) {
            assertTrue(prevActualFile.toString().compareTo(actualFile.toString()) <= 0);
            prevActualFile = actualFile;
        }

        // Compare actual and expected list of files
        final List<FileObject> expectedFiles = selector.finish();
        Collections.sort(expectedFiles);
        assertEquals(expectedFiles.size(), actualFiles.length);
        final int count = expectedFiles.size();
        for (int i = 0; i < count; i++) {
            final FileObject expected = expectedFiles.get(i);
            final FileObject actual = actualFiles[i];
            assertEquals(expected, actual);
        }
    }

    /**
     * Tests that folders have no content.
     */
    public void testFolderContent() throws Exception {
        if (getFileSystem().hasCapability(Capability.DIRECTORY_READ_CONTENT)) {
            // test wont fail
            return;
        }

        // Try getting the content of a folder
        final FileObject folder = getReadFolderDir1();
        try {
            folder.getContent().getInputStream();
            fail();
        } catch (final FileSystemException e) {
            assertSameMessage("vfs.provider/read-not-file.error", folder, e);
        }
    }

    /**
     * Tests that test read folder is not hidden.
     */
    public void testFolderIsHidden() throws Exception {
        final FileObject folder = getReadFolderDir1();
        Assert.assertFalse(folder.isHidden());
    }

    /**
     * Tests that test read folder is readable.
     */
    public void testFolderIsReadable() throws Exception {
        final FileObject folder = getReadFolderDir1();
        Assert.assertTrue(folder.isReadable());
    }

    public void testGetContent() throws Exception {
        final FileObject file = resolveFile1Txt();
        assertTrue(file.exists());
        final FileContent content = file.getContent();
        assertNotNull(content);
    }

    public void testGetContentInfo() throws Exception {
        final FileObject file = resolveFile1Txt();
        assertTrue(file.exists());
        final FileContent content = file.getContent();
        assertNotNull(content);
        final FileContentInfo contentInfo = content.getContentInfo();
        assertNotNull(contentInfo);
    }

    private FileObject resolveFile1Txt() throws FileSystemException {
        return getReadFolder().resolveFile("file1.txt");
    }

    /**
     * Tests can read multiple time end of stream of empty file
     */
    public void testReadEmptyMultipleEOF() throws Exception {
        final FileObject file = getReadFolder().resolveFile("empty.txt");
        assertTrue(file.exists());

        // Start reading from the file
        final InputStream instr = file.getContent().getInputStream();
        try {
            assertEquals("read() from empty file should return EOF", -1, instr.read());

            for (int i = 0; i < 5; i++) {
                assertEquals("multiple read() at EOF should return EOF", -1, instr.read());
            }
        } finally {
            instr.close();
        }
    }

    /**
     * Tests can read multiple time end of stream
     */
    public void testReadFileEOFMultiple() throws Exception {
        final FileObject file = getReadFolder().resolveFile("file1.txt");
        assertTrue(file.exists());
        assertEquals("Expecting 20 bytes test-data file1.txt", 20, file.getContent().getSize());

        // Start reading from the file
        final InputStream instr = file.getContent().getInputStream();
        try {
            final byte[] buf = new byte[25];
            assertEquals(20, instr.read(buf));

            for (int i = 0; i < 5; i++) {
                assertEquals("multiple read(byte[]) at EOF should return EOF", -1, instr.read(buf));
            }
        } finally {
            instr.close();
        }
    }

    /**
     * Tests can perform operations on a folder while reading from a different files.
     */
    public void testConcurrentReadFolder() throws Exception {
        final FileObject file = resolveFile1Txt();
        assertTrue(file.exists());
        final FileObject folder = getReadFolderDir1();
        assertTrue(folder.exists());

        // Start reading from the file
        final InputStream instr = file.getContent().getInputStream();
        try {
            // Do some operations
            folder.exists();
            folder.getType();
            folder.getChildren();
        } finally {
            instr.close();
        }
    }

    /**
     * Tests that findFiles() works.
     */
    public void testFindFiles() throws Exception {
        final FileInfo fileInfo = buildExpectedStructure();
        final VerifyingFileSelector selector = new VerifyingFileSelector(fileInfo);

        // Find the files
        final FileObject[] actualFiles = getReadFolder().findFiles(selector);

        // Compare actual and expected list of files
        final List<FileObject> expectedFiles = selector.finish();
        assertEquals(expectedFiles.size(), actualFiles.length);
        final int count = expectedFiles.size();
        for (int i = 0; i < count; i++) {
            final FileObject expected = expectedFiles.get(i);
            final FileObject actual = actualFiles[i];
            assertEquals(expected, actual);
        }
    }

    /**
     * Tests that we can traverse a folder that has JAR name.
     */
    public void testDotJarFolderName() throws Exception {
        final FileObject folder = getReadFolderDir1().resolveFile("subdir4.jar");
        Assert.assertTrue(folder.exists());
        final FileObject file = folder.resolveFile("file1.txt");
        Assert.assertTrue(file.exists());
    }

    /**
     * Tests that a folder can't be layered.
     */
    public void testDotJarFolderNameLayer() throws Exception {
        final FileObject folder = getReadFolderDir1().resolveFile("subdir4.jar");
        Assert.assertTrue("subdir4.jar/ must exist as folder, check test setup.", folder.isFolder());
        Assert.assertFalse("subdir4.jar/ must not be layerable", getManager().canCreateFileSystem(folder));
        try {
            final FileObject ignored = getManager().createFileSystem(folder);
            fail("Should not be able to create a layered filesystem on a directory. " + ignored);
        } catch (final FileSystemException e) {
            assertSame("Creation of layered filesystem should fail" + e, "vfs.impl/no-provider-for-file.error",
                    e.getCode());
        }
    }
}
