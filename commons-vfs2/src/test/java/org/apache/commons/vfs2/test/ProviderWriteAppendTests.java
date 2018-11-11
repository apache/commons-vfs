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

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.Selectors;

/**
 * File system test that check that a file system can be modified.
 */
public class ProviderWriteAppendTests extends AbstractProviderTestCase {
    /**
     * Returns the capabilities required by the tests of this test case.
     */
    @Override
    protected Capability[] getRequiredCaps() {
        return new Capability[] { Capability.CREATE, Capability.DELETE, Capability.GET_TYPE, Capability.LIST_CHILDREN,
                Capability.READ_CONTENT, Capability.WRITE_CONTENT, Capability.APPEND_CONTENT };
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

    /**
     * Tests create-delete-create-a-file sequence on the same file system.
     */
    public void testAppendContent() throws Exception {
        final FileObject scratchFolder = createScratchFolder();

        // Create direct child of the test folder
        final FileObject file = scratchFolder.resolveFile("file1.txt");
        assertTrue(!file.exists());

        // Create the source file
        final String content = "Here is some sample content for the file.  Blah Blah Blah.";
        final String contentAppend = content + content;

        final OutputStream os = file.getContent().getOutputStream();
        try {
            os.write(content.getBytes("utf-8"));
        } finally {
            os.close();
        }
        assertSameContent(content, file);

        // Append to the new file
        final OutputStream os2 = file.getContent().getOutputStream(true);
        try {
            os2.write(content.getBytes("utf-8"));
        } finally {
            os2.close();
        }
        assertSameContent(contentAppend, file);

        // Make sure we can copy the new file to another file on the same filesystem
        final FileObject fileCopy = scratchFolder.resolveFile("file1copy.txt");
        assertTrue(!fileCopy.exists());
        fileCopy.copyFrom(file, Selectors.SELECT_SELF);

        assertSameContent(contentAppend, fileCopy);

        // Delete the file.
        assertTrue(fileCopy.exists());
        assertTrue(fileCopy.delete());
    }
}
