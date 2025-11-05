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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

/**
 * Path test cases for providers.
 */
public class PathTests extends AbstractProviderTestCase {

    /**
     * Returns the capabilities required by the tests of this test case. The tests are not run if the provider being
     * tested does not support all the required capabilities. Return null or an empty array to always run the tests.
     */
    @Override
    protected Capability[] getRequiredCapabilities() {
        return new Capability[] {Capability.URI};
    }

    /**
     * Tests resolution of absolute URI.
     */
    @Test
    public void testAbsoluteURI() throws Exception {
        final FileObject readFolder = getReadFolder();

        // Try fetching base folder again by its Path
        final String pathStr = readFolder.getPath().toString();
        try (FileObject fileObject = getManager().resolveFile(pathStr,
            readFolder.getFileSystem().getFileSystemOptions())) {
            assertSame(readFolder, fileObject, "file object");
        }

        // Try fetching the filesystem root by its Path
        final Path rootPath = Paths.get(readFolder.getName().getRootURI());
        try (FileObject fileObject = getManager().resolveFile(rootPath.toString(),
            readFolder.getFileSystem().getFileSystemOptions())) {
            assertSame(readFolder.getFileSystem().getRoot(), fileObject);
            assertEquals(rootPath, Paths.get(fileObject.getName().getRootURI()));
            assertEquals(rootPath, fileObject.getName().getPath());
            assertEquals(FileName.ROOT_PATH, fileObject.getName().getPath());
        }
    }

    @Test
    public void testGetPath() throws Exception {
        try (FileObject fileObject = getReadFolder().resolveFile("some-dir/")) {
            final Path path = fileObject.getPath();

            // FileName#getURI() returns a String, not a URI.
            assertEquals(Paths.get(fileObject.getName().getURI()).toString(), path.toString());
            assertEquals(Paths.get(fileObject.getName().getURI()), path);

            assertEquals(fileObject.getPath().toString(), fileObject.getURI().toString());
        }
    }

    @Test
    public void testReservedCharacterSpace() throws FileSystemException {
        try (FileObject fileObject = getReadFolder().resolveFile("file with spaces.txt")) {
            final Path path = fileObject.getPath();
            final String string = path.toString();
            assertTrue(string.contains("file%20with%20spaces.txt"), string);
        }
        try (FileObject fileObject = getReadFolder().resolveFile("file%20with%20spaces.txt")) {
            final Path path = fileObject.getPath();
            final String string = path.toString();
            assertTrue(string.contains("file%20with%20spaces.txt"), string);
        }
    }

    /**
     * Tests content.
     */
    @Test
    public void testURIContentProvider() throws Exception {
        // Test non-empty file
        try (FileObject fileObject = getReadFolder().resolveFile("file1.txt")) {
            assertTrue(fileObject.exists());

            final Path path = fileObject.getPath();
            final String pathStr = path.toString();
            final FileSystemOptions options = getReadFolder().getFileSystem().getFileSystemOptions();

            try (FileObject f1 = getManager().resolveFile(pathStr, options);
                final FileObject f2 = getManager().resolveFile(pathStr, options)) {

                assertEquals(f1, f2, "Two files resolved by URI must be equals on " + pathStr);
                assertSame(f1.getFileSystem(), f2.getFileSystem(),
                    "Resolving two times should not produce new filesystem on " + pathStr);
            }
        }
    }

}