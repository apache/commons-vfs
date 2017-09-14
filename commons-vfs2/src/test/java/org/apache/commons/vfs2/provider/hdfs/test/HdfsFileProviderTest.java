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
package org.apache.commons.vfs2.provider.hdfs.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.hdfs.HdfsFileAttributes;
import org.apache.commons.vfs2.provider.hdfs.HdfsFileProvider;
import org.apache.commons.vfs2.util.Os;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DFSConfigKeys;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This test class uses the Hadoop MiniDFSCluster class to create an embedded Hadoop cluster.
 * <P>
 * This will only work on systems that Hadoop supports.
 */
@SuppressWarnings("resource")
public class HdfsFileProviderTest {

    // Turn off the MiniDFSCluster logging
    static {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
    }

    private static final int PORT = 8620;
    private static final String HDFS_URI = "hdfs://localhost:" + PORT;
    private static final String TEST_DIR1 = HDFS_URI + "/test-dir";
    private static final Path DIR1_PATH = new Path("/test-dir");
    private static final String TEST_FILE1 = TEST_DIR1 + "/accumulo-test-1.jar";
    private static final Path FILE1_PATH = new Path(DIR1_PATH, "accumulo-test-1.jar");

    private static DefaultFileSystemManager manager;
    private static FileSystem hdfs;

    protected static Configuration conf;
    protected static MiniDFSCluster cluster;

    @BeforeClass
    public static void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.ERROR);

        // Put the MiniDFSCluster directory in the target directory
        final File data = new File("target/test/hdfstestdata").getAbsoluteFile();
        data.mkdirs();
        System.setProperty("test.build.data", data.toString());
        FileUtils.cleanDirectory(data);

        // Setup HDFS
        conf = new Configuration();
        conf.set(FileSystem.FS_DEFAULT_NAME_KEY, HDFS_URI);
        conf.set("hadoop.security.token.service.use_ip", "true");
        conf.setLong(DFSConfigKeys.DFS_BLOCK_SIZE_KEY, 1024 * 1024); // 1M blocksize

        setUmask(conf);

        cluster = new MiniDFSCluster(PORT, conf, 1, true, true, true, null, null, null, null);
        cluster.waitActive();

        // Set up the VFS
        manager = new DefaultFileSystemManager();
        manager.addProvider("hdfs", new HdfsFileProvider());
        manager.init();
        hdfs = cluster.getFileSystem();
    }

    /**
     * Add {@code dfs.datanode.data.dir.perm} setting if OS needs it.
     * <P>
     * MiniDFSCluster will check the permissions on the data directories, but does not do a good job of setting them
     * properly. We need to get the users umask and set the appropriate Hadoop property so that the data directories
     * will be created with the correct permissions.
     * <P>
     * Will do nothing on Windows.
     */
    public static void setUmask(final Configuration conf2) {
        if (Os.isFamily(Os.OS_FAMILY_WINDOWS)) {
            return;
        }

        try {
            final Process p = Runtime.getRuntime().exec("/bin/sh -c umask");
            final BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
            final String line = bri.readLine();
            p.waitFor();
            final Short umask = Short.parseShort(line.trim(), 8);
            // Need to set permission to 777 xor umask
            // leading zero makes java interpret as base 8
            final int newPermission = 0777 ^ umask;
            conf2.set("dfs.datanode.data.dir.perm", String.format("%03o", newPermission));
        } catch (final Exception e) {
            throw new RuntimeException("Error getting umask from O/S", e);
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (null != hdfs) {
            hdfs.close();
        }
        manager.close();
    }

    @After
    public void after() throws Exception {
        if (null != hdfs) {
            hdfs.delete(DIR1_PATH, true);
        }
    }

    private FileObject createTestFile(final FileSystem hdfs) throws IOException {
        // Create the directory
        hdfs.mkdirs(DIR1_PATH);
        final FileObject dir = manager.resolveFile(TEST_DIR1);
        Assert.assertNotNull(dir);
        Assert.assertTrue(dir.exists());
        Assert.assertTrue(dir.getType().equals(FileType.FOLDER));

        // Create the file in the directory
        hdfs.create(FILE1_PATH).close();
        final FileObject f = manager.resolveFile(TEST_FILE1);
        Assert.assertNotNull(f);
        Assert.assertTrue(f.exists());
        Assert.assertTrue(f.getType().equals(FileType.FILE));
        return f;
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCanRenameTo() throws Exception {
        final FileObject fo = createTestFile(hdfs);
        Assert.assertNotNull(fo);
        fo.canRenameTo(fo);
    }

    @Test
    public void testDoListChildren() throws Exception {
        final FileObject fo = manager.resolveFile(TEST_DIR1);
        Assert.assertNotNull(fo);
        Assert.assertFalse(fo.exists());

        // Create the test file
        final FileObject file = createTestFile(hdfs);
        Assert.assertTrue(fo.exists());
        final FileObject dir = file.getParent();

        final FileObject[] children = dir.getChildren();
        Assert.assertTrue(children.length == 1);
        Assert.assertTrue(children[0].getName().equals(file.getName()));

    }

    @Test
    public void testEquals() throws Exception {
        // Create test file (and check parent was created)
        final FileObject dir = manager.resolveFile(TEST_DIR1);
        Assert.assertNotNull(dir);
        Assert.assertFalse(dir.exists());
        final FileObject file1 = createTestFile(hdfs);
        Assert.assertTrue(file1.exists());
        Assert.assertTrue(dir.exists());

        // Get a handle to the same file and ensure it is equal
        final FileObject file2 = manager.resolveFile(TEST_FILE1);
        Assert.assertEquals(file1, file2);

        // Ensure different files on same filesystem are not equal
        Assert.assertNotEquals(dir, file1);
        Assert.assertNotEquals(dir, file2);
    }

    @Test
    public void testGetAttributes() throws Exception {
        final FileObject fo = manager.resolveFile(TEST_DIR1);
        Assert.assertNotNull(fo);
        Assert.assertFalse(fo.exists());

        // Create the test file
        final FileObject file = createTestFile(hdfs);
        Assert.assertTrue(fo.exists());
        final Map<String, Object> attributes = file.getContent().getAttributes();
        Assert.assertTrue(attributes.containsKey(HdfsFileAttributes.BLOCK_SIZE.toString()));
        Assert.assertTrue(attributes.containsKey(HdfsFileAttributes.GROUP.toString()));
        Assert.assertTrue(attributes.containsKey(HdfsFileAttributes.LAST_ACCESS_TIME.toString()));
        Assert.assertTrue(attributes.containsKey(HdfsFileAttributes.LENGTH.toString()));
        Assert.assertTrue(attributes.containsKey(HdfsFileAttributes.MODIFICATION_TIME.toString()));
        Assert.assertTrue(attributes.containsKey(HdfsFileAttributes.OWNER.toString()));
        Assert.assertTrue(attributes.containsKey(HdfsFileAttributes.PERMISSIONS.toString()));
    }

    @Test
    public void testGetContentSize() throws Exception {
        final FileObject fo = manager.resolveFile(TEST_DIR1);
        Assert.assertNotNull(fo);
        Assert.assertFalse(fo.exists());

        // Create the test file
        final FileObject file = createTestFile(hdfs);
        Assert.assertTrue(fo.exists());
        Assert.assertEquals(0, file.getContent().getSize());
    }

    @Test
    public void testGetInputStream() throws Exception {
        final FileObject fo = manager.resolveFile(TEST_DIR1);
        Assert.assertNotNull(fo);
        Assert.assertFalse(fo.exists());

        // Create the test file
        final FileObject file = createTestFile(hdfs);
        Assert.assertTrue(fo.exists());
        file.getContent().getInputStream().close();
    }

    @Test
    public void testInit() throws Exception {
        final FileObject fo = manager.resolveFile(TEST_FILE1);
        Assert.assertNotNull(fo);
        Assert.assertFalse(fo.exists());
    }

    @Test
    public void testIsHidden() throws Exception {
        final FileObject fo = manager.resolveFile(TEST_DIR1);
        Assert.assertNotNull(fo);
        Assert.assertFalse(fo.exists());

        // Create the test file
        final FileObject file = createTestFile(hdfs);
        Assert.assertTrue(fo.exists());
        Assert.assertFalse(file.isHidden());
    }

    @Test
    public void testIsReadable() throws Exception {
        final FileObject fo = manager.resolveFile(TEST_DIR1);
        Assert.assertNotNull(fo);
        Assert.assertFalse(fo.exists());

        // Create the test file
        final FileObject file = createTestFile(hdfs);
        Assert.assertTrue(fo.exists());
        Assert.assertTrue(file.isReadable());
    }

    @Test
    public void testIsWritable() throws Exception {
        final FileObject fo = manager.resolveFile(TEST_DIR1);
        Assert.assertNotNull(fo);
        Assert.assertFalse(fo.exists());

        // Create the test file
        final FileObject file = createTestFile(hdfs);
        Assert.assertTrue(fo.exists());
        Assert.assertFalse(file.isWriteable());
    }

    @Test
    public void testLastModificationTime() throws Exception {
        final FileObject fo = manager.resolveFile(TEST_DIR1);
        Assert.assertNotNull(fo);
        Assert.assertFalse(fo.exists());

        // Create the test file
        final FileObject file = createTestFile(hdfs);
        Assert.assertTrue(fo.exists());
        Assert.assertFalse(-1 == file.getContent().getLastModifiedTime());
    }

    @Test(expected = FileSystemException.class)
    public void testRandomAccessContent() throws Exception {
        final FileObject fo = manager.resolveFile(TEST_DIR1);
        Assert.assertNotNull(fo);
        Assert.assertFalse(fo.exists());

        // Create the test file
        final FileObject file = createTestFile(hdfs);
        Assert.assertTrue(fo.exists());
        file.getContent().getRandomAccessContent(RandomAccessMode.READWRITE).close();
    }

    @Test
    public void testRandomAccessContent2() throws Exception {
        final FileObject fo = manager.resolveFile(TEST_DIR1);
        Assert.assertNotNull(fo);
        Assert.assertFalse(fo.exists());

        // Create the test file
        final FileObject file = createTestFile(hdfs);
        Assert.assertTrue(fo.exists());
        file.getContent().getRandomAccessContent(RandomAccessMode.READ).close();
    }

}
