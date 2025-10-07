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

import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectoryFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.ProviderTestConfig;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DFSConfigKeys;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assume;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

/**
 * This test class uses the Hadoop MiniDFSCluster class to create an embedded Hadoop cluster.
 * <P>
 * This will only work on systems that Hadoop supports.
 */
@DisabledOnOs(value = OS.WINDOWS)
public class HdfsFileProviderTestCase extends AbstractProviderTestConfig {

    @DisabledOnOs(value = OS.WINDOWS)
    public static class HdfsProviderTestSuite extends ProviderTestSuiteJunit5 {

        // Turn off the MiniDFSCluster logging
        static {
            System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        }

        public HdfsProviderTestSuite(final ProviderTestConfig providerConfig, final String prefix, final boolean addEmptyDir)
                throws Exception {
            super(providerConfig, prefix, addEmptyDir);
        }

        @SuppressWarnings("deprecation")
        private void copyTestResources(final File directory, final Path parent) throws Exception {
            final File[] listFiles = Objects.requireNonNull(Objects.requireNonNull(directory, "directory").listFiles(),
                    () -> directory.toString() + " no data, directory exists: " + directory.exists() + ", current: " + PathUtils.current().toAbsolutePath().normalize());
            Assertions.assertNotNull(directory, directory::toString);
            for (final File file : listFiles) {
                if (file.isFile()) {
                    final Path src = new Path(file.getAbsolutePath());
                    final Path dst = new Path(parent, file.getName());
                    hdfs.copyFromLocalFile(src, dst);
                } else if (file.isDirectory()) {
                    final Path dir = new Path(parent, file.getName());
                    if (hdfs.mkdirs(dir)) {
                        copyTestResources(file, dir);
                    } else {
                        fail("Unable to make directory: " + dir);
                    }
                }
            }

        }

        @SuppressWarnings("deprecation")
        @Override
        protected void setUp() throws Exception {
            Assume.assumeFalse(SystemUtils.IS_OS_WINDOWS);
            System.setProperty("test.basedir", "../commons-vfs2/target/test-classes/test-data");
            Logger.getRootLogger().setLevel(Level.OFF);

            // Put the MiniDFSCluster directory in the target directory
            final File data = new File("target/test/hdfstestcasedata").getAbsoluteFile();
            data.mkdirs();
            System.setProperty("test.build.data", data.toString());
            FileUtils.cleanDirectory(data);

            // Setup HDFS
            conf = new Configuration();
            conf.set(FileSystem.FS_DEFAULT_NAME_KEY, HDFS_URI);
            conf.set("hadoop.security.token.service.use_ip", "true");
            conf.setLong(DFSConfigKeys.DFS_BLOCK_SIZE_KEY, 1024 * 1024); // 1M block size

            HdfsFileProviderTest.setUmask(conf);

            try {
                cluster = new MiniDFSCluster(PORT, conf, 1, true, true, true, null, null, null, null);
                cluster.waitActive();
            } catch (final IOException e) {
                Assertions.fail("Error setting up mini cluster", e);
            }
            hdfs = cluster.getFileSystem();

            // Copy the test directory into HDFS
            final Path base = new Path("/test-data");
            assertTrue("Unable to create base directory", hdfs.mkdirs(base));
            copyTestResources(getTestDirectoryFile(), base);

            super.setUp();
        }

        @Override
        protected void tearDown() throws Exception {
            super.tearDown();
            if (null != hdfs) {
                hdfs.close();
            }
        }
    }

    private static final int PORT = 8720;
    private static final String HDFS_URI = "hdfs://localhost:" + PORT;
    private static FileSystem hdfs;
    private static Configuration conf;

    private static MiniDFSCluster cluster;



    /**
     * Returns the base folder for read tests.
     */
    @Override
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
        final String uri = HDFS_URI + "/test-data";
        return manager.resolveFile(uri);
    }

    /**
     * Prepares the file system manager.
     */
    @Override
    public void prepare(final DefaultFileSystemManager manager) throws Exception {
        manager.addProvider("hdfs", new HdfsFileProvider());
    }

}
