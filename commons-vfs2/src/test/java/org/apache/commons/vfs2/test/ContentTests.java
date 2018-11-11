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

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;

/**
 * Test cases for reading file content.
 */
public class ContentTests extends AbstractProviderTestCase {
    /**
     * Asserts that every expected file exists, and has the expected content.
     */
    public void testAllContent() throws Exception {
        final FileInfo expectedFileInfo = buildExpectedStructure();
        final FileObject actualFolder = getReadFolder();

        assertSameContent(expectedFileInfo, actualFolder);
    }

    /**
     * Asserts every file in a folder exists and has the expected content.
     */
    private void assertSameContent(final FileInfo expected, final FileObject folder) throws Exception {
        for (final FileInfo fileInfo : expected.children.values()) {
            final FileObject child = folder.resolveFile(fileInfo.baseName, NameScope.CHILD);

            assertTrue(child.getName().toString(), child.exists());
            if (fileInfo.type == FileType.FILE) {
                assertSameContent(fileInfo.content, child);
            } else {
                assertSameContent(fileInfo, child);
            }
        }
    }

    /**
     * Tests existence determination.
     */
    public void testExists() throws Exception {
        // Test a file
        FileObject file = getReadFolder().resolveFile("file1.txt");
        assertTrue("file exists", file.exists());
        assertTrue("file exists", file.getType() != FileType.IMAGINARY);

        // Test a folder
        file = getReadFolder().resolveFile("dir1");
        assertTrue("folder exists", file.exists());
        assertTrue("folder exists", file.getType() != FileType.IMAGINARY);

        // Test an unknown file
        file = getReadFolder().resolveFile("unknown-child");
        assertTrue("unknown file does not exist", !file.exists());
        assertTrue("unknown file does not exist", file.getType() == FileType.IMAGINARY);

        // Test an unknown file in an unknown folder
        file = getReadFolder().resolveFile("unknown-folder/unknown-child");
        assertTrue("unknown file does not exist", !file.exists());
        assertTrue("unknown file does not exist", file.getType() == FileType.IMAGINARY);
    }

    /**
     * Tests attributes
     */
    public void testAttributes() throws FileSystemException {
        this.getReadFolder().getContent().getAttributes();
    }

    /**
     * Tests root of file system exists.
     */
    public void testRootURI() throws FileSystemException {
        if (!this.getProviderConfig().isFileSystemRootAccessible()) {
            return;
        }
        final FileSystem fileSystem = getFileSystem();
        final String uri = fileSystem.getRootURI();
        testRoot(getManager().resolveFile(uri, fileSystem.getFileSystemOptions()));
    }

    /**
     * Tests root of file system exists.
     */
    public void testRootAPI() throws FileSystemException {
        if (!this.getProviderConfig().isFileSystemRootAccessible()) {
            return;
        }
        testRoot(getFileSystem().getRoot());
    }

    private void testRoot(final FileObject root) throws FileSystemException {
        assertTrue(root.exists());
        assertTrue(root.getType() != FileType.IMAGINARY);
    }

    /**
     * Tests parent identity
     */
    public void testParent() throws FileSystemException {
        // Test when both exist
        FileObject folder = getReadFolder().resolveFile("dir1");
        FileObject child = folder.resolveFile("file3.txt");
        assertTrue("folder exists", folder.exists());
        assertTrue("child exists", child.exists());
        assertSame(folder, child.getParent());

        // Test when file does not exist
        child = folder.resolveFile("unknown-file");
        assertTrue("folder exists", folder.exists());
        assertTrue("child does not exist", !child.exists());
        assertSame(folder, child.getParent());

        // Test when neither exists
        folder = getReadFolder().resolveFile("unknown-folder");
        child = folder.resolveFile("unknown-file");
        assertTrue("folder does not exist", !folder.exists());
        assertTrue("child does not exist", !child.exists());
        assertSame(folder, child.getParent());

        // Test the parent of the root of the file system
        // TODO - refactor out test cases for layered vs originating fs
        final FileSystem fileSystem = getFileSystem();
        final FileObject root = fileSystem.getRoot();
        if (fileSystem.getParentLayer() == null) {
            // No parent layer, so parent should be null
            assertNull("root has null parent", root.getParent());
        } else {
            // Parent should be parent of parent layer.
            assertSame(fileSystem.getParentLayer().getParent(), root.getParent());
        }
    }

    /**
     * Tests that children cannot be listed for non-folders.
     */
    public void testChildren() throws FileSystemException {
        // Check for file
        FileObject file = getReadFolder().resolveFile("file1.txt");
        assertSame(FileType.FILE, file.getType());
        assertTrue(file.isFile());
        try {
            file.getChildren();
            fail();
        } catch (final FileSystemException e) {
            assertSameMessage("vfs.provider/list-children-not-folder.error", file, e);
        }

        // Should be able to get child by name
        file = file.resolveFile("some-child");
        assertNotNull(file);

        // Check for unknown file
        file = getReadFolder().resolveFile("unknown-file");
        assertTrue(!file.exists());
        try {
            file.getChildren();
            fail();
        } catch (final FileSystemException e) {
            assertSameMessage("vfs.provider/list-children-not-folder.error", file, e);
        }

        // Should be able to get child by name
        final FileObject child = file.resolveFile("some-child");
        assertNotNull(child);
    }

    /**
     * Tests content.
     */
    public void testContent() throws Exception {
        // Test non-empty file
        FileObject file = getReadFolder().resolveFile("file1.txt");
        assertSameContent(FILE1_CONTENT, file);

        // Test empty file
        file = getReadFolder().resolveFile("empty.txt");
        assertSameContent("", file);
    }

    /**
     * Tests that unknown files have no content.
     */
    public void testUnknownContent() throws Exception {

        // Try getting the content of an unknown file
        final FileObject unknownFile = getReadFolder().resolveFile("unknown-file");
        final FileContent content = unknownFile.getContent();
        try {
            content.getInputStream();
            fail();
        } catch (final FileSystemException e) {
            assertSameMessage("vfs.provider/read-not-file.error", unknownFile, e);
        }
        try {
            content.getSize();
            fail();
        } catch (final FileSystemException e) {
            assertSameMessage("vfs.provider/get-size-not-file.error", unknownFile, e);
        }
    }

    /**
     * Tests concurrent reads on a file.
     */
    public void testReadSingleSequencial() throws Exception {
        final FileObject file = getReadFolder().resolveFile("file1.txt");
        assertTrue(file.exists());

        file.getContent().getInputStream().close();
        file.getContent().getInputStream().close();
    }

    /**
     * Tests concurrent reads on a file.
     */
    public void testReadSingleConcurrent() throws Exception {
        final FileObject file = getReadFolder().resolveFile("file1.txt");
        assertTrue(file.exists());

        // Start reading from the file
        final InputStream instr = file.getContent().getInputStream();
        try {
            // Start reading again
            file.getContent().getInputStream().close();
        } finally {
            instr.close();
        }
    }

    /**
     * Tests concurrent reads on different files works.
     */
    public void testReadMultipleConcurrent() throws Exception {
        final FileObject file = getReadFolder().resolveFile("file1.txt");
        assertTrue(file.exists());
        final FileObject emptyFile = getReadFolder().resolveFile("empty.txt");
        assertTrue(emptyFile.exists());

        // Start reading from the file
        final InputStream instr = file.getContent().getInputStream();
        try {
            // Try to read from other file
            assertSameContent("", emptyFile);
        } finally {
            instr.close();
        }
    }

    /**
     * Tests that content and file objects are usable after being closed.
     */
    public void testReuse() throws Exception {
        // Get the test file
        final FileObject file = getReadFolder().resolveFile("file1.txt");
        assertEquals(FileType.FILE, file.getType());
        assertTrue(file.isFile());

        // Get the file content
        assertSameContent(FILE1_CONTENT, file);

        // Read the content again
        assertSameContent(FILE1_CONTENT, file);

        // Close the content + file
        file.getContent().close();
        file.close();

        // Read the content again
        assertSameContent(FILE1_CONTENT, file);
    }

    /**
     * Tests that input streams are cleaned up on file close.
     */
    public void testInputStreamMultipleCleanup() throws Exception {
        // Get the test file
        final FileObject file = getReadFolder().resolveFile("file1.txt");
        assertEquals(FileType.FILE, file.getType());
        assertTrue(file.isFile());

        // Open some input streams
        final InputStream instr1 = file.getContent().getInputStream();
        assertTrue(instr1.read() == FILE1_CONTENT.charAt(0));
        final InputStream instr2 = file.getContent().getInputStream();
        assertTrue(instr2.read() == FILE1_CONTENT.charAt(0));

        // Close the file
        file.close();

        // Check
        assertTrue(instr1.read() == -1);
        assertTrue(instr2.read() == -1);
    }

    /**
     * Tests that input streams are cleaned up on file close.
     */
    public void testInputStreamSingleCleanup() throws Exception {
        // Get the test file
        final FileObject file = getReadFolder().resolveFile("file1.txt");
        assertEquals(FileType.FILE, file.getType());
        assertTrue(file.isFile());

        // Open some input streams
        final InputStream instr1 = file.getContent().getInputStream();
        assertTrue(instr1.read() == FILE1_CONTENT.charAt(0));

        // Close the file
        file.close();

        // Check
        assertTrue(instr1.read() == -1);
    }
}
