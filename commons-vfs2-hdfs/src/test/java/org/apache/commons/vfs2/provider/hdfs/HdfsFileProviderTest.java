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
package org.apache.commons.vfs2.provider.hdfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DFSConfigKeys;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.condition.OS;

/**
 * This test class uses the Hadoop MiniDFSCluster class to create an embedded Hadoop cluster.
 * <p>
 * This will only work on systems that Hadoop supports.
 * </p>
 */
@SuppressWarnings("resource")
@DisabledForJreRange(min = JRE.JAVA_23)
@DisabledOnOs(OS.WINDOWS)
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

    /**
     * Add {@code dfs.datanode.data.dir.perm} setting if OS needs it.
     * <p>
     * MiniDFSCluster will check the permissions on the data directories, but does not do a good job of setting them
     * properly. We need to get the users umask and set the appropriate Hadoop property so that the data directories
     * will be created with the correct permissions.
     * </p>
     * <p>
     * Will do nothing on Windows.
     * </p>
     */
    public static void setUmask(final Configuration config) {
        try {
            final Process p = Runtime.getRuntime().exec("/bin/sh -c umask");
            final BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
            final String line = bri.readLine();
            p.waitFor();
            final Short umask = Short.parseShort(line.trim(), 8);
            // Need to set permission to 777 xor umask
            // leading zero makes java interpret as base 8
            final int newPermission = 0777 ^ umask;
            config.set("dfs.datanode.data.dir.perm", String.format("%03o", newPermission));
        } catch (final Exception e) {
            throw new IllegalStateException("Error getting umask from O/S", e);
        }
    }

    @BeforeAll
    public static void setUp() throws Exception {
        System.setProperty("test.basedir", "../commons-vfs2/target/test-classes/test-data");
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
        conf.setLong(DFSConfigKeys.DFS_BLOCK_SIZE_KEY, 1024 * 1024); // 1M block size

        setUmask(conf);

        cluster = new MiniDFSCluster.Builder(conf).nameNodePort(PORT).numDataNodes(1).build();
        cluster.waitActive();

        // Set up the VFS
        manager = new DefaultFileSystemManager();
        manager.addProvider("hdfs", new HdfsFileProvider());
        manager.init();
        hdfs = cluster.getFileSystem();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        if (null != hdfs) {
            hdfs.close();
        }
        if (manager != null) {
            manager.close();
        }
    }

    @AfterEach
    public void after() throws Exception {
        if (null != hdfs) {
            hdfs.delete(DIR1_PATH, true);
        }
    }

    private FileObject createTestFile(final FileSystem hdfs) throws IOException {
        // Create the directory
        hdfs.mkdirs(DIR1_PATH);
        final FileObject dir = manager.resolveFile(TEST_DIR1);
        assertNotNull(dir);
        assertTrue(dir.exists());
        assertEquals(dir.getType(), FileType.FOLDER);

        // Create the file in the directory
        hdfs.create(FILE1_PATH).close();
        final FileObject f = manager.resolveFile(TEST_FILE1);
        assertNotNull(f);
        assertTrue(f.exists());
        assertEquals(f.getType(), FileType.FILE);
        return f;
    }

    @Test
    public void testCanRenameTo() throws Exception {
        final FileObject fo = createTestFile(hdfs);
        assertNotNull(fo);
        fo.canRenameTo(fo);
    }

    @Test
    public void testDoListChildren() throws Exception {
        final FileObject fo = manager.resolveFile(TEST_DIR1);
        assertNotNull(fo);
        assertFalse(fo.exists());

        // Create the test file
        final FileObject file = createTestFile(hdfs);
        assertTrue(fo.exists());
        final FileObject dir = file.getParent();

        final FileObject[] children = dir.getChildren();
        assertEquals(1, children.length);
        assertEquals(children[0].getName(), file.getName());
    }

    @Test
    public void testEquals() throws Exception {
        // Create test file (and check parent was created)
        final FileObject dir = manager.resolveFile(TEST_DIR1);
        assertNotNull(dir);
        assertFalse(dir.exists());
        final FileObject file1 = createTestFile(hdfs);
        assertTrue(file1.exists());
        assertTrue(dir.exists());

        // Get a handle to the same file and ensure it is equal
        final FileObject file2 = manager.resolveFile(TEST_FILE1);
        assertEquals(file1, file2);

        // Ensure different files on same filesystem are not equal
        assertNotEquals(dir, file1);
        assertNotEquals(dir, file2);
    }

    @Test
    public void testGetAttributes() throws Exception {
        final FileObject fo = manager.resolveFile(TEST_DIR1);
        assertNotNull(fo);
        assertFalse(fo.exists());

        // Create the test file
        final FileObject file = createTestFile(hdfs);
        assertTrue(fo.exists());
        final Map<String, Object> attributes = file.getContent().getAttributes();
        assertTrue(attributes.containsKey(HdfsFileAttributes.BLOCK_SIZE.toString()));
        assertTrue(attributes.containsKey(HdfsFileAttributes.GROUP.toString()));
        assertTrue(attributes.containsKey(HdfsFileAttributes.LAST_ACCESS_TIME.toString()));
        assertTrue(attributes.containsKey(HdfsFileAttributes.LENGTH.toString()));
        assertTrue(attributes.containsKey(HdfsFileAttributes.MODIFICATION_TIME.toString()));
        assertTrue(attributes.containsKey(HdfsFileAttributes.OWNER.toString()));
        assertTrue(attributes.containsKey(HdfsFileAttributes.PERMISSIONS.toString()));
    }

    @Test
    public void testGetContentSize() throws Exception {
        final FileObject fo = manager.resolveFile(TEST_DIR1);
        assertNotNull(fo);
        assertFalse(fo.exists());

        // Create the test file
        final FileObject file = createTestFile(hdfs);
        assertTrue(fo.exists());
        assertEquals(0, file.getContent().getSize());
        assertTrue(file.getContent().isEmpty());
    }

    @Test
    public void testGetInputStream() throws Exception {
        final FileObject fo = manager.resolveFile(TEST_DIR1);
        assertNotNull(fo);
        assertFalse(fo.exists());

        // Create the test file
        final FileObject file = createTestFile(hdfs);
        assertTrue(fo.exists());
        file.getContent().getInputStream().close();
    }

    @Test
    public void testInit() throws Exception {
        final FileObject fo = manager.resolveFile(TEST_FILE1);
        assertNotNull(fo);
        assertFalse(fo.exists());
    }

    @Test
    public void testIsHidden() throws Exception {
        final FileObject fo = manager.resolveFile(TEST_DIR1);
        assertNotNull(fo);
        assertFalse(fo.exists());

        // Create the test file
        final FileObject file = createTestFile(hdfs);
        assertTrue(fo.exists());
        assertFalse(file.isHidden());
    }

    @Test
    public void testIsReadable() throws Exception {
        final FileObject fo = manager.resolveFile(TEST_DIR1);
        assertNotNull(fo);
        assertFalse(fo.exists());

        // Create the test file
        final FileObject file = createTestFile(hdfs);
        assertTrue(fo.exists());
        assertTrue(file.isReadable());
    }

    @Test
    public void testIsWritable() throws Exception {
        final FileObject fo = manager.resolveFile(TEST_DIR1);
        assertNotNull(fo);
        assertFalse(fo.exists());

        // Create the test file
        final FileObject file = createTestFile(hdfs);
        assertTrue(fo.exists());
        assertTrue(file.isWriteable());
    }

    @Test
    public void testLastModificationTime() throws Exception {
        final FileObject fo = manager.resolveFile(TEST_DIR1);
        assertNotNull(fo);
        assertFalse(fo.exists());

        // Create the test file
        final FileObject file = createTestFile(hdfs);
        assertTrue(fo.exists());
        assertNotEquals(-1, file.getContent().getLastModifiedTime());
    }

    @Test
    public void testRandomAccessContent() throws Exception {
        final FileObject fo = manager.resolveFile(TEST_DIR1);
        assertNotNull(fo);
        assertFalse(fo.exists());

        // Create the test file
        final FileObject file = createTestFile(hdfs);
        assertTrue(fo.exists());
        assertThrows(FileSystemException.class, () -> file.getContent().getRandomAccessContent(RandomAccessMode.READWRITE).close());
    }

    @Test
    public void testRandomAccessContent2() throws Exception {
        final FileObject fo = manager.resolveFile(TEST_DIR1);
        assertNotNull(fo);
        assertFalse(fo.exists());

        // Create the test file
        final FileObject file = createTestFile(hdfs);
        assertTrue(fo.exists());
        file.getContent().getRandomAccessContent(RandomAccessMode.READ).close();
    }

}
