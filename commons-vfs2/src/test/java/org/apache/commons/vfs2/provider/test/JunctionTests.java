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
package org.apache.commons.vfs2.provider.test;

import java.io.File;

import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.test.AbstractProviderTestCase;

/**
 * Additional junction test cases.
 */
public class JunctionTests extends AbstractProviderTestCase {
    private FileObject getBaseDir() throws FileSystemException {
        final File file = AbstractVfsTestCase.getTestDirectoryFile();
        assertTrue(file.exists());
        return getManager().toFileObject(file);
    }

    /**
     * Checks nested junctions are not supported.
     */
    public void testNestedJunction() throws Exception {
        final FileSystem fs = getManager().createVirtualFileSystem("vfs:").getFileSystem();
        final FileObject baseDir = getBaseDir();
        fs.addJunction("/a", baseDir);

        // Nested
        try {
            fs.addJunction("/a/b", baseDir);
            fail();
        } catch (final Exception e) {
            assertSameMessage("vfs.impl/nested-junction.error", "vfs:/a/b", e);
        }

        // At same point
        try {
            fs.addJunction("/a", baseDir);
            fail();
        } catch (final Exception e) {
            assertSameMessage("vfs.impl/nested-junction.error", "vfs:/a", e);
        }
    }

    /**
     * Checks ancestors are created when a junction is created.
     */
    public void testAncestors() throws Exception {
        final FileSystem fs = getManager().createVirtualFileSystem("vfs://").getFileSystem();
        final FileObject baseDir = getBaseDir();

        // Make sure the file at the junction point and its ancestors do not exist
        FileObject file = fs.resolveFile("/a/b");
        assertFalse(file.exists());
        file = file.getParent();
        assertFalse(file.exists());
        file = file.getParent();
        assertFalse(file.exists());

        // Add the junction
        fs.addJunction("/a/b", baseDir);

        // Make sure the file at the junction point and its ancestors exist
        file = fs.resolveFile("/a/b");
        assertTrue("Does not exist", file.exists());
        file = file.getParent();
        assertTrue("Does not exist", file.exists());
        file = file.getParent();
        assertTrue("Does not exist", file.exists());
    }

    // Check that file @ junction point exists only when backing file exists
    // Add 2 junctions with common parent
    // Compare real and virtual files
    // Events
    // Remove junctions

}
