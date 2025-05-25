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

import static org.apache.commons.vfs2.VfsTestUtils.getTestResource;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

/**
 * Test cases for the VFS factory.
 */
public class FileSystemManagerFactoryTest {

    private void check(final FileSystemManager manager, FileObject file) throws FileSystemException {
        assertNotNull(file);
        assertTrue(file.exists());
        assertSame(FileType.FILE, file.getType());
        assertTrue(file.isFile());

        // Expand it
        file = manager.createFileSystem(file);
        assertNotNull(file);
        assertTrue(file.exists());
        assertSame(FileType.FOLDER, file.getType());
        assertTrue(file.isFolder());
    }

    /**
     * Sanity test.
     */
    @Test
    public void testDefaultInstance() throws Exception {
        // Locate the default manager
        final FileSystemManager manager = VFS.getManager();

        // Lookup a test jar file
        final File jarFile = getTestResource("test.jar");
        // File
        final FileObject file = manager.toFileObject(jarFile);
        check(manager, file);
        // Path
        final FileObject path = manager.toFileObject(jarFile.toPath());
        check(manager, file);
        // URI
        final FileObject file2 = manager.resolveFile(jarFile.toURI());
        check(manager, file2);
        // URL
        final FileObject file3 = manager.resolveFile(jarFile.toURI().toURL());
        check(manager, file3);
    }

}
