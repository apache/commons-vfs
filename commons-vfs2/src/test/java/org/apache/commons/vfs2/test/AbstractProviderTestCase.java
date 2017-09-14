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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLConnection;
import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;

/**
 * File system test cases, which verifies the structure and naming functionality.
 * <p>
 * Works from a base folder, and assumes a particular structure under that base folder.
 */
public abstract class AbstractProviderTestCase extends AbstractVfsTestCase {
    private FileObject baseFolder;
    private FileObject readFolder;
    private FileObject writeFolder;
    private DefaultFileSystemManager manager;
    private ProviderTestConfig providerConfig;
    private Method method;
    private boolean addEmptyDir;

    // Expected contents of "file1.txt"
    public static final String FILE1_CONTENT = "This is a test file.";

    // Expected contents of test files
    public static final String TEST_FILE_CONTENT = "A test file.";

    /**
     * Sets the test method.
     */
    public void setMethod(final Method method) {
        this.method = method;
    }

    /**
     * Configures this test.
     */
    public void setConfig(final DefaultFileSystemManager manager, final ProviderTestConfig providerConfig,
            final FileObject baseFolder, final FileObject readFolder, final FileObject writeFolder) {
        this.manager = manager;
        this.providerConfig = providerConfig;
        this.baseFolder = baseFolder;
        this.readFolder = readFolder;
        this.writeFolder = writeFolder;
    }

    /**
     * Returns the file system manager used by this test.
     */
    protected DefaultFileSystemManager getManager() {
        return manager;
    }

    /**
     * creates a new uninitialized file system manager
     *
     * @throws Exception
     */
    protected DefaultFileSystemManager createManager() throws Exception {
        final DefaultFileSystemManager fs = getProviderConfig().getDefaultFileSystemManager();
        fs.setFilesCache(getProviderConfig().getFilesCache());
        getProviderConfig().prepare(fs);
        if (!fs.hasProvider("file")) {
            fs.addProvider("file", new DefaultLocalFileProvider());
        }
        return fs;
    }

    /**
     * some provider config do some post-initialization in getBaseTestFolder. This is a hack to allow access to this
     * code for {@code createManager}
     */
    public FileObject getBaseTestFolder(final FileSystemManager fs) throws Exception {
        return providerConfig.getBaseTestFolder(fs);
    }

    protected FileSystem getFileSystem() {
        return getReadFolder().getFileSystem();
    }

    /**
     * Returns the base test folder. This is the parent of both the read test and write test folders.
     */
    public FileObject getBaseFolder() {
        return baseFolder;
    }

    /**
     * get the provider configuration
     */
    public ProviderTestConfig getProviderConfig() {
        return providerConfig;
    }

    /**
     * Returns the read test folder.
     */
    protected FileObject getReadFolder() {
        return readFolder;
    }

    /**
     * Returns the write test folder.
     */
    protected FileObject getWriteFolder() {
        return writeFolder;
    }

    /**
     * Sets the write test folder.
     *
     * @param folder
     */
    protected void setWriteFolder(final FileObject folder) {
        writeFolder = folder;
    }

    /**
     * Returns the capabilities required by the tests of this test case. The tests are not run if the provider being
     * tested does not support all the required capabilities. Return null or an empty array to always run the tests.
     * <p>
     * This implementation returns null.
     */
    protected Capability[] getRequiredCaps() {
        return null;
    }

    /**
     * Runs the test. This implementation short-circuits the test if the provider being tested does not have the
     * capabilities required by this test.
     * <p>
     * TODO - Handle negative caps as well - ie, only run a test if the provider does not have certain caps.<br>
     * TODO - Figure out how to remove the test from the TestResult if the test is skipped.
     */
    @Override
    protected void runTest() throws Throwable {
        // Check the capabilities
        final Capability[] caps = getRequiredCaps();
        if (caps != null) {
            for (final Capability cap2 : caps) {
                final Capability cap = cap2;
                final FileSystem fs = readFolder.getFileSystem();
                if (!fs.hasCapability(cap)) {
                    // String name = fs.getClass().getName();
                    // int index = name.lastIndexOf('.');
                    // String fsName = (index > 0) ? name.substring(index + 1) : name;
                    // System.out.println("skipping " + getName() + " because " +
                    // fsName + " does not have capability " + cap);
                    return;
                }
            }
        }

        // Provider has all the capabilities - execute the test
        if (method != null) {
            try {
                method.invoke(this, (Object[]) null);
            } catch (final InvocationTargetException e) {
                throw e.getTargetException();
            }
        } else {
            super.runTest();
        }

        if (((AbstractFileSystem) readFolder.getFileSystem()).isOpen()) {
            String name = "unknown";
            if (method != null) {
                name = method.getName();
            }

            throw new IllegalStateException(getClass().getName() + ": filesystem has open streams after: " + name);
        }
    }

    /**
     * Asserts that the content of a file is the same as expected. Checks the length reported by getContentLength() is
     * correct, then reads the content as a byte stream and compares the result with the expected content. Assumes files
     * are encoded using UTF-8.
     */
    protected void assertSameURLContent(final String expected, final URLConnection connection) throws Exception {
        // Get file content as a binary stream
        final byte[] expectedBin = expected.getBytes("utf-8");

        // Check lengths
        assertEquals("same content length", expectedBin.length, connection.getContentLength());

        // Read content into byte array
        final InputStream instr = connection.getInputStream();
        final ByteArrayOutputStream outstr;
        try {
            outstr = new ByteArrayOutputStream();
            final byte[] buffer = new byte[256];
            int nread = 0;
            while (nread >= 0) {
                outstr.write(buffer, 0, nread);
                nread = instr.read(buffer);
            }
        } finally {
            instr.close();
        }

        // Compare
        assertTrue("same binary content", Arrays.equals(expectedBin, outstr.toByteArray()));
    }

    /**
     * Asserts that the content of a file is the same as expected. Checks the length reported by getSize() is correct,
     * then reads the content as a byte stream and compares the result with the expected content. Assumes files are
     * encoded using UTF-8.
     */
    protected void assertSameContent(final String expected, final FileObject file) throws Exception {
        // Check the file exists, and is a file
        assertTrue(file.exists());
        assertSame(FileType.FILE, file.getType());
        assertTrue(file.isFile());

        // Get file content as a binary stream
        final byte[] expectedBin = expected.getBytes("utf-8");

        // Check lengths
        final FileContent content = file.getContent();
        assertEquals("same content length", expectedBin.length, content.getSize());

        // Read content into byte array
        final InputStream instr = content.getInputStream();
        final ByteArrayOutputStream outstr;
        try {
            outstr = new ByteArrayOutputStream(expectedBin.length);
            final byte[] buffer = new byte[256];
            int nread = 0;
            while (nread >= 0) {
                outstr.write(buffer, 0, nread);
                nread = instr.read(buffer);
            }
        } finally {
            instr.close();
        }

        // Compare
        assertTrue("same binary content", Arrays.equals(expectedBin, outstr.toByteArray()));
    }

    /**
     * Builds the expected structure of the read tests folder.
     *
     * @throws FileSystemException (possibly)
     */
    protected FileInfo buildExpectedStructure() throws FileSystemException {
        // Build the expected structure
        final FileInfo base = new FileInfo(getReadFolder().getName().getBaseName(), FileType.FOLDER);
        base.addFile("file1.txt", FILE1_CONTENT);
        // file%.txt - test out encoding
        base.addFile("file%25.txt", FILE1_CONTENT);

        // file?test.txt - test out encoding (test.txt is not the queryString)
        // as we do not know if the current file provider we need to
        // ask it to normalize the name
        // todo: move this into the FileInfo class to do it generally?
        /*
         * webdav-bug?: didnt manage to get the "?" correctly through webdavlib FileSystemManager fsm =
         * getReadFolder().getFileSystem().getFileSystemManager(); FileName fn =
         * fsm.resolveName(getReadFolder().getName(), "file%3ftest.txt"); String baseName = fn.getBaseName();
         * base.addFile(baseName, FILE1_CONTENT);
         */
        base.addFile("file space.txt", FILE1_CONTENT);

        base.addFile("empty.txt", "");
        if (addEmptyDir) {
            base.addFolder("emptydir");
        }

        final FileInfo dir = base.addFolder("dir1");
        dir.addFile("file1.txt", TEST_FILE_CONTENT);
        dir.addFile("file2.txt", TEST_FILE_CONTENT);
        dir.addFile("file3.txt", TEST_FILE_CONTENT);

        final FileInfo subdir1 = dir.addFolder("subdir1");
        subdir1.addFile("file1.txt", TEST_FILE_CONTENT);
        subdir1.addFile("file2.txt", TEST_FILE_CONTENT);
        subdir1.addFile("file3.txt", TEST_FILE_CONTENT);

        final FileInfo subdir2 = dir.addFolder("subdir2");
        subdir2.addFile("file1.txt", TEST_FILE_CONTENT);
        subdir2.addFile("file2.txt", TEST_FILE_CONTENT);
        subdir2.addFile("file3.txt", TEST_FILE_CONTENT);

        final FileInfo subdir3 = dir.addFolder("subdir3");
        subdir3.addFile("file1.txt", TEST_FILE_CONTENT);
        subdir3.addFile("file2.txt", TEST_FILE_CONTENT);
        subdir3.addFile("file3.txt", TEST_FILE_CONTENT);

        final FileInfo subdir4 = dir.addFolder("subdir4.jar");
        subdir4.addFile("file1.txt", TEST_FILE_CONTENT);
        subdir4.addFile("file2.txt", TEST_FILE_CONTENT);
        subdir4.addFile("file3.txt", TEST_FILE_CONTENT);

        return base;
    }

    protected void addEmptyDir(final boolean addEmptyDir) {
        this.addEmptyDir = addEmptyDir;
    }

    protected static Test notConfigured(final Class<?> testClass) {
        return warning(testClass + " is not configured for tests, skipping");
    }

    private static Test warning(final String message) {
        return new TestCase("warning") {
            @Override
            protected void runTest() {
                System.out.println(message);
            }
        };
    }
}
