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

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.Selectors;

/**
 * File system test that check that a file system can be renamed.
 */
public class ProviderRenameTests extends AbstractProviderTestCase {
    /**
     * Returns the capabilities required by the tests of this test case.
     */
    @Override
    protected Capability[] getRequiredCaps() {
        return new Capability[] { Capability.CREATE, Capability.DELETE, Capability.GET_TYPE, Capability.LIST_CHILDREN,
                Capability.READ_CONTENT, Capability.WRITE_CONTENT, Capability.RENAME };
    }

    /**
     * Sets up a scratch folder for the test to use.
     */
    protected FileObject createScratchFolder() throws Exception {
        final FileObject scratchFolder = getWriteFolder();

        // Make sure the test folder is empty
        scratchFolder.delete(Selectors.EXCLUDE_SELF);
        scratchFolder.createFolder();

        return scratchFolder;
    }

    private void moveFile(final FileObject scratchFolder, final FileObject file, final String content)
            throws FileSystemException, Exception {
        final FileObject fileMove = scratchFolder.resolveFile("file1move.txt");
        assertTrue(!fileMove.exists());

        file.moveTo(fileMove);

        assertTrue(!file.exists());
        assertTrue(fileMove.exists());

        assertSameContent(content, fileMove);

        // Delete the file.
        assertTrue(fileMove.exists());
        assertTrue(fileMove.delete());
    }

    private String createTestFile(final FileObject file)
            throws FileSystemException, IOException, UnsupportedEncodingException, Exception {
        // Create the source file
        final String content = "Here is some sample content for the file.  Blah Blah Blah.";

        final OutputStream os = file.getContent().getOutputStream();
        try {
            os.write(content.getBytes("utf-8"));
        } finally {
            os.close();
        }
        assertSameContent(content, file);
        return content;
    }

    /**
     * Tests create-delete-create-a-file sequence on the same file system.
     */
    public void testRenameFile() throws Exception {
        final FileObject scratchFolder = createScratchFolder();

        // Create direct child of the test folder
        final FileObject file = scratchFolder.resolveFile("file1.txt");
        assertTrue(!file.exists());

        final String content = createTestFile(file);

        // Make sure we can move the new file to another file on the same file system
        moveFile(scratchFolder, file, content);
    }

    /**
     * Tests moving a file to empty folder.
     * <P>
     * This fails with VFS-558, but only with a CacheStrategy.ON_CALL.
     */
    public void testRenameFileIntoEmptyFolder() throws Exception {
        final FileObject scratchFolder = createScratchFolder();

        // Create direct child of the test folder
        final FileObject file = scratchFolder.resolveFile("file1.txt");
        assertTrue(!file.exists());

        final String content = createTestFile(file);

        final FileObject destFolder = scratchFolder.resolveFile("empty-target-folder");
        destFolder.createFolder();
        assertTrue("new destination must be folder", destFolder.getType().hasChildren());
        assertTrue("new destination must be emty", destFolder.getChildren().length == 0);

        moveFile(destFolder, file, content);
    }

    /**
     * Moves a file from a child folder to a parent folder to test what happens when the original folder is now empty.
     *
     * See [VFS-298] FTP: Exception is thrown when renaming a file.
     */
    public void testRenameFileAndLeaveFolderEmpty() throws Exception {
        final FileObject scratchFolder = createScratchFolder();
        final FileObject folder = scratchFolder.resolveFile("folder");
        folder.createFolder();
        assertTrue(folder.exists());
        final FileObject file = folder.resolveFile("file1.txt");
        assertTrue(!file.exists());

        final String content = createTestFile(file);

        // Make sure we can move the new file to another file on the same file system
        moveFile(scratchFolder, file, content);
        assertTrue(folder.getChildren().length == 0);
    }
}
