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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link FileObject}s with iterators its implementation the {@link Iterable} interface to allow a FileObject to
 * be the target of the Java 5 "foreach" statement.
 */
public class FileIteratorTest {

    private static FileObject baseFolder;

    private final static int FILE_COUNT = 14;

    /**
     * Creates a RAM FS.
     *
     * @throws Exception
     */
    @BeforeAll
    public static void setUpClass() throws Exception {
        baseFolder = VFS.getManager().resolveFile("ram://" + FileIteratorTest.class.getName());
        baseFolder.deleteAll();
        baseFolder.resolveFile("a.htm").createFile();
        baseFolder.resolveFile("a.html").createFile();
        baseFolder.resolveFile("a.xhtml").createFile();
        baseFolder.resolveFile("b.htm").createFile();
        baseFolder.resolveFile("b.html").createFile();
        baseFolder.resolveFile("b.xhtml").createFile();
        baseFolder.resolveFile("c.htm").createFile();
        baseFolder.resolveFile("c.html").createFile();
        baseFolder.resolveFile("c.xhtml").createFile();
        baseFolder.resolveFile("subdir1").createFolder();
        baseFolder.resolveFile("subdir1/subfile1.txt").createFile();
        baseFolder.resolveFile("subdir2").createFolder();
        baseFolder.resolveFile("subdir2/subfile1.txt").createFile();
    }

    /**
     * Deletes RAM FS files.
     *
     * @throws Exception
     */
    @AfterAll
    public static void tearDownClass() throws Exception {
        if (baseFolder != null) {
            baseFolder.deleteAll();
        }
    }

    @Test
    public void testIterator() throws FileSystemException {
        final FileObject[] findFiles = baseFolder.findFiles(Selectors.SELECT_ALL);
        assertEquals(FILE_COUNT, findFiles.length);
        final FileObject[] listFiles = baseFolder.getChildren();
        assertTrue(FILE_COUNT > listFiles.length);
        int i = 0;
        for (final FileObject actualFile : baseFolder) {
            final FileObject expectedFile = findFiles[i];
            assertEquals(expectedFile, actualFile);
            i++;
        }
        i = 0;
        for (final FileObject element : baseFolder) {
            final FileObject expectedFile = findFiles[i];
            assertEquals(expectedFile, element);
            i++;
        }
    }

}
