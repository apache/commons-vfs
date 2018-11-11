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
package org.apache.commons.vfs2.provider.ram.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.ram.RamFileProvider;
import org.apache.commons.vfs2.provider.ram.RamFileSystemConfigBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Custom tests
 *
 * @version $Id$
 */
public class CustomRamProviderTest {
    private static final byte[] NON_EMPTY_FILE_CONTENT = new byte[] { 1, 2, 3 };

    private final List<Closeable> closeables = new ArrayList<>();

    FileSystemOptions defaultRamFso = new FileSystemOptions();

    DefaultFileSystemManager manager;

    FileSystemOptions smallSizedFso = new FileSystemOptions();

    FileSystemOptions zeroSizedFso = new FileSystemOptions();

    /**
     * Closes the given {@link Closeable} during the tearDown phase.
     */
    private <C extends Closeable> C closeOnTearDown(final C closeable) {
        this.closeables.add(closeable);
        return closeable;
    }

    private InputStream createEmptyFile() throws FileSystemException, IOException {
        final FileObject root = manager.resolveFile("ram://file");
        root.createFile();
        return this.closeOnTearDown(root.getContent().getInputStream());
    }

    private InputStream createNonEmptyFile() throws FileSystemException, IOException {
        final FileObject root = manager.resolveFile("ram://file");
        root.createFile();

        final FileContent content = root.getContent();
        final OutputStream output = this.closeOnTearDown(content.getOutputStream());
        output.write(1);
        output.write(2);
        output.write(3);
        output.flush();
        output.close();

        return this.closeOnTearDown(content.getInputStream());
    }

    @Before
    public void setUp() throws Exception {
        manager = new DefaultFileSystemManager();
        manager.addProvider("ram", new RamFileProvider());
        manager.init();

        // File Systems Options
        RamFileSystemConfigBuilder.getInstance().setMaxSize(zeroSizedFso, 0L);
        RamFileSystemConfigBuilder.getInstance().setMaxSize(smallSizedFso, 10L);
    }

    @After
    public void tearDown() throws Exception {
        for (final Closeable closeable : this.closeables) {
            try {
                closeable.close();
            } catch (final Exception e) {
                // ignore
            }
        }
        manager.close();
    }

    @Test
    public void testReadEmptyFileByteByByte() throws FileSystemException, IOException {
        final InputStream input = this.createEmptyFile();
        assertEquals("Empty file didnt return EOF -1", -1, input.read());
    }

    @Test
    public void testReadEmptyFileIntoBuffer() throws FileSystemException, IOException {
        final InputStream input = this.createEmptyFile();

        final byte[] buffer = new byte[100];
        assertEquals("Empty file didnt return when filling buffer", -1, input.read(buffer));
        assertArrayEquals("Buffer was written too", new byte[100], buffer);
    }

    @Test
    public void testReadEmptyFileIntoBufferWithOffsetAndLength() throws FileSystemException, IOException {
        final InputStream input = this.createEmptyFile();
        final byte[] buffer = new byte[100];
        assertEquals("Empty file didnt return when filling buffer", -1, input.read(buffer, 10, 90));
        assertArrayEquals("Buffer was written too", new byte[100], buffer);
    }

    @Test
    public void testReadNonEmptyFileByteByByte() throws FileSystemException, IOException {
        final InputStream input = this.createNonEmptyFile();

        assertEquals("Read 1st byte failed", 1, input.read());
        assertEquals("Rread 2st byte failed", 2, input.read());
        assertEquals("Read 3st byte failed", 3, input.read());
        assertEquals("File should be empty", -1, input.read());
    }

    @Test
    public void testReadNonEmptyFileIntoBuffer() throws FileSystemException, IOException {
        final InputStream input = this.createNonEmptyFile();

        final byte[] buffer = new byte[100];
        assertEquals("Filling buffer failed when file is not empty", NON_EMPTY_FILE_CONTENT.length, input.read(buffer));

        final byte[] expectedBuffer = new byte[100];
        System.arraycopy(NON_EMPTY_FILE_CONTENT, 0, expectedBuffer, 0, NON_EMPTY_FILE_CONTENT.length);
        assertArrayEquals("Buffer not filled", expectedBuffer, buffer);

        Arrays.fill(buffer, (byte) 0);
        Arrays.fill(expectedBuffer, (byte) 0);

        assertEquals("File should be empty after filling buffer", -1, input.read(buffer));
        assertArrayEquals("Buffer was written when empty", expectedBuffer, buffer);
    }

    @Test
    public void testReadNonEmptyFileIntoBufferWithOffsetAndLength() throws FileSystemException, IOException {
        final InputStream input = this.createNonEmptyFile();

        final byte[] buffer = new byte[100];
        final int offset = 10;
        assertEquals("Filling buffer failed when file is not empty", NON_EMPTY_FILE_CONTENT.length,
                input.read(buffer, offset, 100 - offset));

        final byte[] expectedBuffer = new byte[100];
        System.arraycopy(NON_EMPTY_FILE_CONTENT, 0, expectedBuffer, offset, NON_EMPTY_FILE_CONTENT.length);
        assertArrayEquals("Buffer not filled", expectedBuffer, buffer);

        Arrays.fill(buffer, (byte) 0);
        Arrays.fill(expectedBuffer, (byte) 0);
        assertEquals("File should be empty after filling buffer", -1, input.read(buffer, 10, 90));
        assertArrayEquals("Buffer was written when empty", expectedBuffer, buffer);
    }

    /**
     *
     * Checks root folder exists
     *
     * @throws FileSystemException
     */
    @Test
    public void testRootFolderExists() throws FileSystemException {
        final FileObject root = manager.resolveFile("ram:///", defaultRamFso);
        assertTrue(root.getType().hasChildren());

        try {
            root.delete();
            fail();
        } catch (final FileSystemException e) {
            // Expected
        }

    }

    @Test
    public void testFSOptions() throws Exception {
        // Default FS
        final FileObject fo1 = manager.resolveFile("ram:/");
        final FileObject fo2 = manager.resolveFile("ram:/");
        assertTrue("Both files should exist in the same fs instance.", fo1.getFileSystem() == fo2.getFileSystem());

        FileSystemOptions fsOptions = fo1.getFileSystem().getFileSystemOptions();
        long maxFilesystemSize = RamFileSystemConfigBuilder.getInstance().getLongMaxSize(fsOptions);
        assertEquals("Filesystem option maxSize must be unlimited", Long.MAX_VALUE, maxFilesystemSize);

        // Small FS
        final FileObject fo3 = manager.resolveFile("ram:/fo3", smallSizedFso);
        final FileObject fo4 = manager.resolveFile("ram:/", smallSizedFso);
        assertTrue("Both files should exist in the same FileSystem instance.",
                fo3.getFileSystem() == fo4.getFileSystem());
        assertTrue("Both files should exist in different FileSystem instance.",
                fo1.getFileSystem() != fo3.getFileSystem());

        fsOptions = fo3.getFileSystem().getFileSystemOptions();
        maxFilesystemSize = RamFileSystemConfigBuilder.getInstance().getLongMaxSize(fsOptions);
        assertEquals("Filesystem option maxSize must be set", 10, maxFilesystemSize);
    }

    @Test
    public void testSmallFS() throws Exception {
        // Small FS
        final FileObject fo3 = manager.resolveFile("ram:/fo3", smallSizedFso);
        fo3.createFile();
        try {
            final OutputStream os = fo3.getContent().getOutputStream();
            os.write(new byte[10]);
            os.close();
        } catch (final FileSystemException e) {
            fail("Test should be able to save such a small file");
        }

        try {
            final OutputStream os = fo3.getContent().getOutputStream();
            os.write(new byte[11]);
            os.close();
            fail("It shouldn't save such a big file");
        } catch (final FileSystemException e) {
            // Expected
        }

    }
}
