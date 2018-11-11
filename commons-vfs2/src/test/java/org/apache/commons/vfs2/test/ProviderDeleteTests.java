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

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.FileTypeSelector;
import org.apache.commons.vfs2.Selectors;

/**
 * File system test that do some delete operations.
 */
public class ProviderDeleteTests extends AbstractProviderTestCase {
    private class FileNameSelector implements FileSelector {
        final String basename;

        private FileNameSelector(final String basename) {
            this.basename = basename;
        }

        @Override
        public boolean includeFile(final FileSelectInfo fileInfo) throws Exception {
            return this.basename.equals(fileInfo.getFile().getName().getBaseName());
        }

        @Override
        public boolean traverseDescendents(final FileSelectInfo fileInfo) throws Exception {
            return true;
        }
    }

    /**
     * Returns the capabilities required by the tests of this test case.
     */
    @Override
    protected Capability[] getRequiredCaps() {
        return new Capability[] { Capability.CREATE, Capability.DELETE, Capability.GET_TYPE,
                Capability.LIST_CHILDREN, };
    }

    /**
     * Sets up a scratch folder for the test to use.
     */
    protected FileObject createScratchFolder() throws Exception {
        final FileObject scratchFolder = getWriteFolder();

        // Make sure the test folder is empty
        scratchFolder.delete(Selectors.EXCLUDE_SELF);
        scratchFolder.createFolder();

        final FileObject dir1 = scratchFolder.resolveFile("dir1");
        dir1.createFolder();
        final FileObject dir1file1 = dir1.resolveFile("a.txt");
        dir1file1.createFile();
        final FileObject dir2 = scratchFolder.resolveFile("dir2");
        dir2.createFolder();
        final FileObject dir2file1 = dir2.resolveFile("b.txt");
        dir2file1.createFile();

        return scratchFolder;
    }

    /**
     * deletes the complete structure
     */
    public void testDeleteFiles() throws Exception {
        final FileObject scratchFolder = createScratchFolder();

        assertEquals(scratchFolder.delete(Selectors.EXCLUDE_SELF), 4);
    }

    /**
     * deletes a single file
     */
    public void testDeleteFile() throws Exception {
        final FileObject scratchFolder = createScratchFolder();

        final FileObject file = scratchFolder.resolveFile("dir1/a.txt");

        assertTrue(file.delete());
    }

    /**
     * Deletes a non existent file
     */
    public void testDeleteNonExistantFile() throws Exception {
        final FileObject scratchFolder = createScratchFolder();

        final FileObject file = scratchFolder.resolveFile("dir1/aa.txt");

        assertFalse(file.delete());
    }

    /**
     * deletes files
     */
    public void testDeleteAllFiles() throws Exception {
        final FileObject scratchFolder = createScratchFolder();

        assertEquals(scratchFolder.delete(new FileTypeSelector(FileType.FILE)), 2);
    }

    /**
     * deletes a.txt
     */
    public void testDeleteOneFiles() throws Exception {
        final FileObject scratchFolder = createScratchFolder();

        assertEquals(scratchFolder.delete(new FileNameSelector("a.txt")), 1);
    }
}
