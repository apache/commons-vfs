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
package org.apache.commons.vfs2.provider.ram;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.UriParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Custom tests for RamProvider.
 */
public class CustomRamProviderTest {

    private static final byte[] NON_EMPTY_FILE_CONTENT = { 1, 2, 3 };

    /** List of URL special characters encoded for AbstractFileObject#getChild */
    final char[] ENC = { /*'#',*/ '!', '?'};

    private final List<Closeable> closeables = new ArrayList<>();

    FileSystemOptions defaultRamFso = new FileSystemOptions();

    DefaultFileSystemManager manager;

    FileSystemOptions smallSizedFso = new FileSystemOptions();

    FileSystemOptions zeroSizedFso = new FileSystemOptions();

    /**
     * Closes the given {@link Closeable} during the tearDown phase.
     */
    private <C extends Closeable> C closeOnTearDown(final C closeable) {
        closeables.add(closeable);
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

    /** Create directory structure for {@link #testSpecialName()} and {@link #testSchemePrefix()} */
    private FileObject prepareSpecialFile(final String dirName, final String testFileName) throws FileSystemException
    {
        // set up a folder containing a file name with special characters:
        final FileObject dir = manager.resolveFile("ram:" + dirName);
        dir.createFolder();
        // construct the absolute name to make sure the relative name is not miss-interpreted
        // ("./" + UriParser.encode(testFileName, ENC) would also work)
        final String filePath = dir.getName().getPath() + "/" + UriParser.encode(testFileName, ENC);

        final FileObject specialFile = dir.resolveFile(filePath);
        specialFile.createFile();

        return dir;
    }

    @BeforeEach
    public void setUp() throws Exception {
        manager = new DefaultFileSystemManager();
        manager.addProvider("ram", new RamFileProvider());
        manager.init();

        // File Systems Options
        RamFileSystemConfigBuilder.getInstance().setMaxSize(zeroSizedFso, 0L);
        RamFileSystemConfigBuilder.getInstance().setMaxSize(smallSizedFso, 10L);
    }

    @AfterEach
    public void tearDown() {
        for (final Closeable closeable : closeables) {
            try {
                closeable.close();
            } catch (final Exception e) {
                // ignore
            }
        }
        manager.close();
    }

    @Test
    public void testFSOptions() throws Exception {
        // Default FS
        final FileObject fo1 = manager.resolveFile("ram:/");
        final FileObject fo2 = manager.resolveFile("ram:/");
        assertSame("Both files should exist in the same fs instance.", fo1.getFileSystem(), fo2.getFileSystem());

        FileSystemOptions fsOptions = fo1.getFileSystem().getFileSystemOptions();
        long maxFilesystemSize = RamFileSystemConfigBuilder.getInstance().getLongMaxSize(fsOptions);
        assertEquals(Long.MAX_VALUE, maxFilesystemSize, "Filesystem option maxSize must be unlimited");

        // Small FS
        final FileObject fo3 = manager.resolveFile("ram:/fo3", smallSizedFso);
        final FileObject fo4 = manager.resolveFile("ram:/", smallSizedFso);
        assertSame("Both files should exist in the same FileSystem instance.", fo3.getFileSystem(), fo4.getFileSystem());
        assertNotSame("Both files should exist in different FileSystem instance.", fo1.getFileSystem(), fo3.getFileSystem());

        fsOptions = fo3.getFileSystem().getFileSystemOptions();
        maxFilesystemSize = RamFileSystemConfigBuilder.getInstance().getLongMaxSize(fsOptions);
        assertEquals(10, maxFilesystemSize, "Filesystem option maxSize must be set");
    }

    /**
     * Tests VFS-625.
     * @throws FileSystemException
     */
    @Test
    public void testMoveFile() throws FileSystemException {
        final FileObject fileSource = manager.resolveFile("ram://virtual/source");
        fileSource.createFile();
        final FileObject fileDest = manager.resolveFile("ram://virtual/dest");
        assertTrue(fileSource.canRenameTo(fileDest));
        fileSource.moveTo(fileDest);
    }

    @Test
    public void testReadEmptyFileByteByByte() throws FileSystemException, IOException {
        final InputStream input = createEmptyFile();
        assertEquals(-1, input.read(),"Empty file didn't return EOF -1");
    }

    @Test
    public void testReadEmptyFileIntoBuffer() throws FileSystemException, IOException {
        final InputStream input = createEmptyFile();

        final byte[] buffer = new byte[100];
        assertEquals(-1, input.read(buffer), "Empty file didn't return when filling buffer");
        assertArrayEquals(new byte[100], buffer, "Buffer was written too");
    }

    @Test
    public void testReadEmptyFileIntoBufferWithOffsetAndLength() throws FileSystemException, IOException {
        final InputStream input = createEmptyFile();
        final byte[] buffer = new byte[100];
        assertEquals(-1, input.read(buffer, 10, 90), "Empty file didn't return when filling buffer");
        assertArrayEquals(new byte[100], buffer, "Buffer was written too");
    }

    @Test
    public void testReadNonEmptyFileByteByByte() throws FileSystemException, IOException {
        final InputStream input = createNonEmptyFile();

        assertEquals(1, input.read(), "Read 1st byte failed");
        assertEquals(2, input.read(), "Read 2st byte failed");
        assertEquals(3, input.read(), "Read 3st byte failed");
        assertEquals(-1, input.read(), "File should be empty");
    }

    @Test
    public void testReadNonEmptyFileIntoBuffer() throws FileSystemException, IOException {
        final InputStream input = createNonEmptyFile();

        final byte[] buffer = new byte[100];
        assertEquals(NON_EMPTY_FILE_CONTENT.length, input.read(buffer), "Filling buffer failed when file is not empty");

        final byte[] expectedBuffer = Arrays.copyOf(NON_EMPTY_FILE_CONTENT, 100);
        assertArrayEquals(expectedBuffer, buffer, "Buffer not filled");

        Arrays.fill(buffer, (byte) 0);
        Arrays.fill(expectedBuffer, (byte) 0);

        assertEquals(-1, input.read(buffer), "File should be empty after filling buffer");
        assertArrayEquals(expectedBuffer, buffer, "Buffer was written when empty");
    }

    @Test
    public void testReadNonEmptyFileIntoBufferWithOffsetAndLength() throws FileSystemException, IOException {
        final InputStream input = createNonEmptyFile();

        final byte[] buffer = new byte[100];
        final int offset = 10;
        assertEquals(NON_EMPTY_FILE_CONTENT.length,
                input.read(buffer, offset, 100 - offset), "Filling buffer failed when file is not empty");

        final byte[] expectedBuffer = new byte[100];
        System.arraycopy(NON_EMPTY_FILE_CONTENT, 0, expectedBuffer, offset, NON_EMPTY_FILE_CONTENT.length);
        assertArrayEquals(expectedBuffer, buffer, "Buffer not filled");

        Arrays.fill(buffer, (byte) 0);
        Arrays.fill(expectedBuffer, (byte) 0);
        assertEquals(-1, input.read(buffer, 10, 90), "File should be empty after filling buffer");
        assertArrayEquals(expectedBuffer, buffer, "Buffer was written when empty");
    }

    /**
     * Checks root folder exists.
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

    /**
     * Test if listing files with known scheme prefix works.
     * <p>
     * This test is not RamProvider specific but it uses it as a simple test-bed.
     * Verifies VFS-741.
     * </p>
     */
    @Test
    public void testSchemePrefix() throws FileSystemException
    {
        // use a :-prefix with a known scheme (unknown scheme works since VFS-398)
        final String KNOWN_SCHEME = manager.getSchemes()[0]; // typically "ram"

        // we test with this file name
        final String testDir = "/prefixtest/";
        final String testFileName = KNOWN_SCHEME + ":test:txt";
        final String expectedName = testDir + testFileName;

        final FileObject dir = prepareSpecialFile(testDir, testFileName);

        // verify we can list dir

        // if not it throws:
        // Caused by: org.apache.commons.vfs2.FileSystemException: Invalid descendent file name "ram:data:test.txt".
        //   at org.apache.commons.vfs2.impl.DefaultFileSystemManager.resolveName
        //   at org.apache.commons.vfs2.provider.AbstractFileObject.getChildren
        //   at org.apache.commons.vfs2.provider.AbstractFileObject.traverse
        //   at org.apache.commons.vfs2.provider.AbstractFileObject.findFiles

        // test methods to get the child:
        final FileObject[] findFilesResult = dir.findFiles(new AllFileSelector()); // includes dir
        final FileObject[] getChildrenResult = dir.getChildren();
        final FileObject getChildResult = dir.getChild(testFileName);

        // validate findFiles returns expected result
        assertEquals(2, findFilesResult.length, () -> "Unexpected result findFiles: " + Arrays.toString(findFilesResult));
        String resultName = findFilesResult[0].getName().getPathDecoded();
        assertEquals(expectedName, resultName, "findFiles Child name does not match");
        assertEquals(FileType.FILE, findFilesResult[0].getType(), "Did findFiles but child was no file");

        // validate getChildren returns expected result
        assertEquals(1, getChildrenResult.length, () -> "Unexpected result getChildren: " + Arrays.toString(getChildrenResult));
        resultName = getChildrenResult[0].getName().getPathDecoded();
        assertEquals(expectedName, resultName, "getChildren Child name does not match");
        assertEquals(FileType.FILE, getChildrenResult[0].getType(), "Did getChildren but child was no file");

        // validate getChild returns expected child
        assertNotNull(getChildResult, "Did not find direct child");
        resultName = getChildResult.getName().getPathDecoded();
        assertEquals(expectedName, resultName, "getChild name does not match");
        assertEquals(FileType.FILE, getChildResult.getType(), "getChild was no file");
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

    /**
     * Test some special file name symbols.
     * <p>
     * Use the RamProvider since it has no character limitations like
     * the (Windows) LocalFileProvider.
     */
    @Test
    public void testSpecialName() throws FileSystemException
    {
        // we test with this file name
        // does not work with '!'
        final String testDir = "/specialtest/";
        final String testFileName = "test:+-_ \"()<>%#.txt";
        final String expectedName = testDir + testFileName;

        final FileObject dir = prepareSpecialFile(testDir, testFileName);

        // DO: verify you can list it:
        final FileObject[] findFilesResult = dir.findFiles(new AllFileSelector()); // includes dir
        final FileObject[] getChildrenResult = dir.getChildren();
        final FileObject getChildResult = dir.getChild(UriParser.encode(testFileName, ENC));

        // validate findFiles returns expected result
        assertEquals(2, findFilesResult.length, () -> "Unexpected result findFiles: " + Arrays.toString(findFilesResult));
        String resultName = findFilesResult[0].getName().getPathDecoded();
        assertEquals(expectedName, resultName, "findFiles Child name does not match");
        assertEquals(FileType.FILE, findFilesResult[0].getType(), "Did findFiles but child was no file");

        // validate getChildren returns expected result
        assertEquals(1, getChildrenResult.length, () -> "Unexpected result getChildren: " + Arrays.toString(getChildrenResult));
        resultName = getChildrenResult[0].getName().getPathDecoded();
        assertEquals(expectedName, resultName, "getChildren Child name does not match");
        assertEquals(FileType.FILE, getChildrenResult[0].getType(), "Did getChildren but child was no file");

        // validate getChild returns expected child
        assertNotNull(getChildResult, "Did not find direct child");
        resultName = getChildResult.getName().getPathDecoded();
        assertEquals(expectedName, resultName, "getChild name does not match");
        assertEquals(FileType.FILE, getChildResult.getType(), "getChild was no file");
    }

}
