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
package org.apache.commons.vfs2.provider.test;

import static org.apache.commons.vfs2.VfsTestUtils.assertSameMessage;
import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectoryFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;

import org.apache.commons.vfs2.AbstractProviderTestCase;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.DelegateFileObject;
import org.apache.commons.vfs2.util.WeakRefFileListener;
import org.junit.jupiter.api.Test;

class DebugFileListener implements FileListener {

    private boolean changed;
    private boolean created;
    private boolean deleted;

    @Override
    public void fileChanged(final FileChangeEvent event) throws Exception {
        changed = true;
    }

    @Override
    public void fileCreated(final FileChangeEvent event) throws Exception {
        created = true;
    }

    @Override
    public void fileDeleted(final FileChangeEvent event) throws Exception {
        deleted = true;
    }

    @Override
    public String toString() {
        return "Listener " + changed + " " + created + " " + deleted;
    }
}

/**
 * Additional junction test cases.
 */
public class JunctionTests extends AbstractProviderTestCase {

    private FileObject getBaseDir() throws FileSystemException {
        final File file = getTestDirectoryFile();
        assertTrue(file.exists());
        return getManager().toFileObject(file);
    }

    /**
     * Checks ancestors are created when a junction is created.
     */
    @Test
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
        assertTrue(file.exists(), "Does not exist");
        file = file.getParent();
        assertTrue(file.exists(), "Does not exist");
        file = file.getParent();
        assertTrue(file.exists(), "Does not exist");
    }

    /**
     * Checks that change events from delegated files are fired.
     */
    public void testEvent() throws Exception {
        // we use the VirtualFileSystem to check change event propagation of DecoratedFileObject
        final FileSystem fs = getManager().createVirtualFileSystem("vfs://").getFileSystem();
        final FileObject baseDir = getBaseDir().resolveFile("junctiontest");

        // Add the junction
        fs.addJunction("/a", baseDir);

        // Make sure the file at the junction point and its ancestors exist
        final FileObject file = fs.resolveFile("/a/hardref.txt");
        assertSame(file.getClass(), DelegateFileObject.class, "VirtualFileSystem does not use DelegateFO anymore?");

        // Clean up any leftover files from previous test runs
        if (file.exists()) {
            file.delete();
        }

        // Do with a hard reference listener
        final FileListener listener1 = new DebugFileListener();
        file.getFileSystem().addListener(file, listener1);
        // Create and delete the file through the virtual file system to trigger events
        file.createFile();
        assertEquals("Listener false true false", listener1.toString(), "Strong Listener was not notified (create)");
        file.delete();
        assertEquals("Listener false true true", listener1.toString(), "Strong Listener was not notified (delete)");

        final FileObject file2 = fs.resolveFile("/a/weakref.txt");
        assertSame(file2.getClass(), DelegateFileObject.class, "VirtualFileSystem does not use DelegateFO anymore?");

        // Clean up any leftover files from previous test runs
        if (file2.exists()) {
            file2.delete();
        }

        // repeat with Weak reference listener
        final FileListener listener2 = new DebugFileListener();
        // since we hold the listener2 reference it should not get GC
        WeakRefFileListener.installListener(file2, listener2);

        // force the WeakRefFileListener reference to (not) clear
        System.gc();
        Thread.sleep(1000);
        System.gc();
        Thread.sleep(1000);
        System.gc();
        Thread.sleep(1000);
        System.gc();
        Thread.sleep(1000);

        // Create the file through the virtual file system to trigger events
        file2.createFile();
        try {
            assertEquals("Listener false true false", listener2.toString(), "Weak Listener was abandoned");
        } finally {
            assertTrue(file2.delete(), "Don't contaminate the fs for the next time the test runs");
        }
    }

    /**
     * Checks nested junctions are not supported.
     */
    @Test
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

    // Check that file @ junction point exists only when backing file exists
    // Add 2 junctions with common parent
    // Compare real and virtual files
    // Events
    // Remove junctions

}

