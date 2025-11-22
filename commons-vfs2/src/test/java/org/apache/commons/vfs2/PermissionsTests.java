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
package org.apache.commons.vfs2;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.apache.commons.io.file.PathUtils;
import org.apache.commons.io.file.StandardDeleteOption;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.vfs2.provider.local.LocalFileSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Additional file permission tests.
 *
 * Used by Local and SFTP File System.
 */
public class PermissionsTests extends AbstractProviderTestCase {

    public static final String FILE_NAME = "permission.txt";

    private FileObject createTestFile() throws Exception {
        // Get the scratch folder
        final FileObject scratchFolder = getWriteFolder();
        assertNotNull(scratchFolder);
        // Make sure the test folder is empty
        scratchFolder.delete(Selectors.EXCLUDE_SELF);
        scratchFolder.createFolder();
        // Create direct child of the test folder
        final FileObject file = scratchFolder.resolveFile(FILE_NAME);
        assertFalse(file.exists());
        // Create the source file
        final String content = "Here is some sample content for the file.  Blah Blah Blah.";
        try (OutputStream os = file.getContent().getOutputStream()) {
            os.write(content.getBytes(StandardCharsets.UTF_8));
        }
        return file;
    }

    /**
     * Returns the capabilities required by the tests of this test case.
     */
    @Override
    protected Capability[] getRequiredCapabilities() {
        return new Capability[] { Capability.CREATE, Capability.DELETE, Capability.READ_CONTENT,
                Capability.WRITE_CONTENT, };
    }

    /**
     * Returns true if the file system is a LocalFileSystem on Windows
     */
    private boolean isWindows() {
        return SystemUtils.IS_OS_WINDOWS && getFileSystem() instanceof LocalFileSystem;
    }

    /**
     * Clean up the permission-modified file to not affect other tests.
     */
    @AfterEach
    public void tearDown() throws Exception {
        final FileObject scratchFolder = getWriteFolder();
        final FileObject file = scratchFolder.resolveFile(FILE_NAME);
        try {
            file.setReadable(true, false);
            file.getParent().setReadable(true, false);
        } catch (final FileSystemException e) {
            e.printStackTrace();
        }
        try {
            file.setWritable(true, false);
            file.getParent().setWritable(true, false);
        } catch (final FileSystemException e) {
            e.printStackTrace();
        }
        if (!file.delete()) {
            final Path path = file.getPath();
            if ((getFileSystem() instanceof LocalFileSystem)
                    && (PathUtils.deleteFile(path, StandardDeleteOption.OVERRIDE_READ_ONLY).getFileCounter().get() == 0)) {
                File f = path.toFile();
                if (!f.delete()) {
                    f.deleteOnExit();
                }
                if (f.exists()) {
                    System.err.println("Test tear down can't delete " + path);
                }
            }
        }
    }

    /**
     * Tests for the execution permission.
     */
    @Test
    public void testExecutable() throws Exception {
        final FileObject file = createTestFile();
        // On Windows, all files are executable
        if (isWindows()) {
            assertTrue(file.isExecutable(), "File expected to be executable: " + file);
        } else {
            // Set the executable flag for owner
            assertTrue(file.setExecutable(true, true), "Setting executable permission failed: " + file);
            assertTrue(file.isExecutable(), "File expected to be executable: " + file);
            // Set the executable flag for all
            assertTrue(file.setExecutable(true, false), "Setting executable permission failed: " + file);
            assertTrue(file.isExecutable(), "File expected to be executable: " + file);
            // Clear the executable flag
            assertTrue(file.setExecutable(false, true), "Setting executable permission failed: " + file);
            assertFalse(file.isExecutable(), "File expected to be not executable: " + file);
        }
    }

    /**
     * Tests for the readable permission.
     */
    @Test
    public void testReadable() throws Exception {
        final FileObject file = createTestFile();
        if (isWindows()) {
            // On Windows, all owned files are readable
            assertTrue(file.isReadable(), "File expected to be readable: " + file);
        } else {
            // Set the readable permission for owner
            assertTrue(file.setReadable(true, true), "Setting read permission failed: " + file);
            assertTrue(file.isReadable(), "File expected to be readable: " + file);
            // Set the readable permission for all
            assertTrue(file.setReadable(true, false), "Setting read permission failed: " + file);
            assertTrue(file.isReadable(), "File expected to be readable: " + file);
            // Clear the readable permission
            assertTrue(file.setReadable(false, true), "Setting read permission failed: " + file);
            assertFalse(file.isReadable(), "File expected to be not readable: " + file);
        }
    }

    /**
     * Tests for the writable permission.
     */
    @Test
    public void testWriteable() throws Exception {
        final FileObject file = createTestFile();
        // Set the write permission for owner
        assertTrue(file.setWritable(true, true), "Setting write permission failed: " + file);
        assertTrue(file.isWriteable(), "File expected to be writable: " + file);
        // Set the write permission for all
        assertTrue(file.setWritable(true, false), "Setting write permission failed: " + file);
        assertTrue(file.isWriteable(), "File expected to be writable: " + file);
        // Clear the write permission
        assertTrue(file.setWritable(false, true), "Setting write permission failed: " + file);
        assertFalse(file.isWriteable(), "File expected to be not writable: " + file);
    }

}
