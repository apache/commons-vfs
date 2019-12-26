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

import java.io.OutputStream;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.provider.local.LocalFileSystem;
import org.junit.Assert;

/**
 * Additional file permission tests.
 *
 * Used by Local and SFTP File System.
 *
 * @since 2.1
 */
public class PermissionsTests extends AbstractProviderTestCase {
    public static final String FILENAME = "permission.txt";

    /**
     * Returns the capabilities required by the tests of this test case.
     */
    @Override
    protected Capability[] getRequiredCaps() {
        return new Capability[] { Capability.CREATE, Capability.DELETE, Capability.READ_CONTENT,
                Capability.WRITE_CONTENT, };
    }

    /**
     * Tests for the execution permission.
     */
    public void testExecutable() throws Exception {
        final FileObject file = createTestFile();

        // On Windows, all files are executable
        if (isWindows()) {
            Assert.assertTrue("File expected to be executable: " + file, file.isExecutable());

        } else {
            // Set the executable flag for owner
            Assert.assertTrue("Setting executable permission failed: " + file, file.setExecutable(true, true));
            Assert.assertTrue("File expected to be executable: " + file, file.isExecutable());

            // Set the executable flag for all
            Assert.assertTrue("Setting executable permission failed: " + file, file.setExecutable(true, false));
            Assert.assertTrue("File expected to be executable: " + file, file.isExecutable());

            // Clear the executable flag
            Assert.assertTrue("Setting executable permission failed: " + file, file.setExecutable(false, true));
            Assert.assertFalse("File expected to be not executable: " + file, file.isExecutable());
        }
    }

    /**
     * Tests for the writeable permission
     */
    public void testWriteable() throws Exception {
        final FileObject file = createTestFile();

        // Set the write permission for owner
        Assert.assertTrue("Setting write permission failed: " + file, file.setWritable(true, true));
        Assert.assertTrue("File expected to be writable: " + file, file.isWriteable());

        // Set the write permission for all
        Assert.assertTrue("Setting write permission failed: " + file, file.setWritable(true, false));
        Assert.assertTrue("File expected to be writable: " + file, file.isWriteable());

        // Clear the write permission
        Assert.assertTrue("Setting write permission failed: " + file, file.setWritable(false, true));
        Assert.assertFalse("File expected to be not writable: " + file, file.isWriteable());
    }

    /**
     * Tests for the readable permission
     */
    public void testReadable() throws Exception {
        final FileObject file = createTestFile();

        if (isWindows()) {
            // On Windows, all owned files are readable
            Assert.assertTrue("File expected to be readable: " + file, file.isReadable());
        } else {
            // Set the readable permission for owner
            Assert.assertTrue("Setting read permission failed: " + file, file.setReadable(true, true));
            Assert.assertTrue("File expected to be readable: " + file, file.isReadable());

            // Set the readable permission for all
            Assert.assertTrue("Setting read permission failed: " + file, file.setReadable(true, false));
            Assert.assertTrue("File expected to be readable: " + file, file.isReadable());

            // Clear the readable permission
            Assert.assertTrue("Setting read permission failed: " + file, file.setReadable(false, true));
            Assert.assertFalse("File expected to be not readable: " + file, file.isReadable());
        }
    }

    /**
     * Clean up the permission-modified file to not affect other tests.
     */
    @Override
    protected void tearDown() throws Exception {
        final FileObject scratchFolder = getWriteFolder();
        final FileObject file = scratchFolder.resolveFile(FILENAME);
        file.setWritable(true, true);
        file.delete();

        super.tearDown();
    }

    private FileObject createTestFile() throws Exception {
        // Get the scratch folder
        final FileObject scratchFolder = getWriteFolder();
        assertNotNull(scratchFolder);

        // Make sure the test folder is empty
        scratchFolder.delete(Selectors.EXCLUDE_SELF);
        scratchFolder.createFolder();

        // Create direct child of the test folder
        final FileObject file = scratchFolder.resolveFile(FILENAME);
        assertFalse(file.exists());

        // Create the source file
        final String content = "Here is some sample content for the file.  Blah Blah Blah.";

        final OutputStream os = file.getContent().getOutputStream();
        try {
            os.write(content.getBytes("utf-8"));
        } finally {
            os.close();
        }
        return file;
    }

    /**
     * Returns true if the file system is a LocalFileSystem on Windows
     */
    private boolean isWindows() {
        return SystemUtils.IS_OS_WINDOWS && this.getFileSystem() instanceof LocalFileSystem;
    }
}
