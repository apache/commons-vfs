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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests FileObject sorting.
 */
public class FileObjectSortTest {

    /**
     * The size of arrays to sort.
     */
    private static final int SIZE = 100;

    /** Consider @Immutable. */
    private static FileSystem vfsFileSystem;

    /** Consider @Immutable */
    private static FileObject[] sortedArray;

    /** Consider @Immutable */
    private static FileObject[] unSortedArray;

    private static FileObject resolveFile(final FileSystem fs, final int i) throws FileSystemException {
        return fs.resolveFile(String.format("%010d", i));
    }

    @BeforeAll
    public static void setUpClass() throws FileSystemException {
        vfsFileSystem = VFS.getManager().createVirtualFileSystem("vfs://").getFileSystem();
        sortedArray = new FileObject[SIZE];
        for (int i = 0; i < SIZE; i++) {
            sortedArray[i] = FileObjectSortTest.resolveFile(vfsFileSystem, i);
        }
        unSortedArray = new FileObject[SIZE];
        for (int i = 0; i < SIZE; i++) {
            unSortedArray[i] = FileObjectSortTest.resolveFile(vfsFileSystem, SIZE - i - 1);
        }
    }

    /**
     * Tests that sorting ignores case.
     *
     * @throws FileSystemException
     */
    @Test
    public void testSortArrayIgnoreCase() throws FileSystemException {
        final FileObject file1 = vfsFileSystem.resolveFile("A1");
        final FileObject file2 = vfsFileSystem.resolveFile("a2");
        final FileObject file3 = vfsFileSystem.resolveFile("A3");
        final FileObject[] actualArray = { file3, file1, file2, file1, file2 };
        final FileObject[] expectedArray = { file1, file1, file2, file2, file3 };
        Arrays.sort(actualArray);
        assertArrayEquals(expectedArray, actualArray);
    }

    /**
     * Tests sorting an array.
     */
    @Test
    public void testSortArrayMoveAll() {
        final FileObject[] actualArray = unSortedArray.clone();
        assertFalse(Arrays.equals(unSortedArray, sortedArray));
        Arrays.sort(actualArray);
        assertArrayEquals(sortedArray, actualArray);
    }

    /**
     * Tests that sorting an array already in oder does not mess it up.
     */
    @Test
    public void testSortArrayMoveNone() {
        final FileObject[] actualArray = sortedArray.clone();
        Arrays.sort(actualArray);
        assertArrayEquals(sortedArray, actualArray);
    }

    /**
     * Tests sorting a list.
     */
    @Test
    public void testSortListMoveAll() {
        final List<FileObject> actualList = Arrays.asList(unSortedArray);
        final List<FileObject> expectedSortedList = Arrays.asList(sortedArray);
        assertNotEquals(actualList, expectedSortedList);
        actualList.sort(null);
        assertEquals(actualList, expectedSortedList);
    }

    /**
     * Tests that sorting a list already in oder does not mess it up.
     */
    @Test
    public void testSortListMoveNone() {
        final List<FileObject> actualList = Arrays.asList(sortedArray);
        final List<FileObject> expectedSortedList = Arrays.asList(sortedArray);
        actualList.sort(null);
        assertEquals(actualList, expectedSortedList);
    }

}
