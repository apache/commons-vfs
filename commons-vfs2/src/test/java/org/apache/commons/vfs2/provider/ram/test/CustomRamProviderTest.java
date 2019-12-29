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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.apache.commons.vfs2.provider.ram.RamFileProvider;
import org.apache.commons.vfs2.provider.ram.RamFileSystemConfigBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Custom tests for RamProvider.
 */
public class CustomRamProviderTest {
    /** List of URL special characters encoded for AbstractFileObject#getChild */
    final char[] ENC = { /*'#',*/ '!', '?'};

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

    /** Create directory structure for {@link #testSpecialName()} and {@link #testSchemePrefix()} */
    private FileObject prepareSpecialFile(final String dirname, final String testFileName) throws FileSystemException
    {
        // set up a folder containing an filename with special characters:
        final FileObject dir = manager.resolveFile("ram:" + dirname);
        dir.createFolder();
        // construct the absolute name to make sure the relative name is not miss-interpreted
        // ("./" + UriParser.encode(testFileName, ENC) would also work)
        final String filePath = dir.getName().getPath() + "/" + UriParser.encode(testFileName, ENC);

        final FileObject specialFile = dir.resolveFile(filePath);
        specialFile.createFile();

        return dir;
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
        assertSame("Both files should exist in the same fs instance.", fo1.getFileSystem(), fo2.getFileSystem());

        FileSystemOptions fsOptions = fo1.getFileSystem().getFileSystemOptions();
        long maxFilesystemSize = RamFileSystemConfigBuilder.getInstance().getLongMaxSize(fsOptions);
        assertEquals("Filesystem option maxSize must be unlimited", Long.MAX_VALUE, maxFilesystemSize);

        // Small FS
        final FileObject fo3 = manager.resolveFile("ram:/fo3", smallSizedFso);
        final FileObject fo4 = manager.resolveFile("ram:/", smallSizedFso);
        assertSame("Both files should exist in the same FileSystem instance.", fo3.getFileSystem(), fo4.getFileSystem());
        assertNotSame("Both files should exist in different FileSystem instance.", fo1.getFileSystem(), fo3.getFileSystem());

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

    /**
     * Tests VFS-625.
     * @throws FileSystemException
     */
    @Test
    public void testMoveFile() throws FileSystemException {
        final FileObject fileSource = manager.resolveFile("ram://virtual/source");
        fileSource.createFile();
        final FileObject fileDest = manager.resolveFile("ram://virtual/dest");
        Assert.assertTrue(fileSource.canRenameTo(fileDest));
        fileSource.moveTo(fileDest);
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
        final String testDir = "/spacialtest/";
        final String testFileName = "test:+-_ \"()<>%#.txt";
        final String expectedName = testDir + testFileName;

        final FileObject dir = prepareSpecialFile(testDir, testFileName);


        // DO: verify you can list it:
        final FileObject[] findFilesResult = dir.findFiles(new AllFileSelector()); // includes dir
        final FileObject[] getChildrenResult = dir.getChildren();
        final FileObject getChildResult = dir.getChild(UriParser.encode(testFileName, ENC));


        // validate findFiles returns expected result
        assertEquals("Unexpected result findFiles: " + Arrays.toString(findFilesResult), 2, findFilesResult.length);
        String resultName = findFilesResult[0].getName().getPathDecoded();
        assertEquals("findFiles Child name does not match", expectedName, resultName);
        assertEquals("Did findFiles but child was no file", FileType.FILE, findFilesResult[0].getType());

        // validate getChildren returns expected result
        assertEquals("Unexpected result getChildren: " + Arrays.toString(getChildrenResult), 1, getChildrenResult.length);
        resultName = getChildrenResult[0].getName().getPathDecoded();
        assertEquals("getChildren Child name does not match", expectedName, resultName);
        assertEquals("Did getChildren but child was no file", FileType.FILE, getChildrenResult[0].getType());

        // validate getChild returns expected child
        assertNotNull("Did not find direct child", getChildResult);
        resultName = getChildResult.getName().getPathDecoded();
        assertEquals("getChild name does not match", expectedName, resultName);
        assertEquals("getChild was no file", FileType.FILE, getChildResult.getType());
    }


    /**
     * Test if listing files with known scheme prefix works.
     * <p>
     * This test is not RamProvider specific but it uses it as a simple test-bed.
     * Verifies VFS-741.
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
        assertEquals("Unexpected result findFiles: " + Arrays.toString(findFilesResult), 2, findFilesResult.length);
        String resultName = findFilesResult[0].getName().getPathDecoded();
        assertEquals("findFiles Child name does not match", expectedName, resultName);
        assertEquals("Did findFiles but child was no file", FileType.FILE, findFilesResult[0].getType());

        // validate getChildren returns expected result
        assertEquals("Unexpected result getChildren: " + Arrays.toString(getChildrenResult), 1, getChildrenResult.length);
        resultName = getChildrenResult[0].getName().getPathDecoded();
        assertEquals("getChildren Child name does not match", expectedName, resultName);
        assertEquals("Did getChildren but child was no file", FileType.FILE, getChildrenResult[0].getType());

        // validate getChild returns expected child
        assertNotNull("Did not find direct child", getChildResult);
        resultName = getChildResult.getName().getPathDecoded();
        assertEquals("getChild name does not match", expectedName, resultName);
        assertEquals("getChild was no file", FileType.FILE, getChildResult.getType());
    }
}
