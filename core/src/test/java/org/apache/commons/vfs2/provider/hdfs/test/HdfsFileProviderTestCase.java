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

import junit.framework.Test;

import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.hdfs.HdfsFileProvider;
import org.apache.commons.vfs2.test.AbstractProviderTestConfig;
import org.apache.commons.vfs2.test.ProviderTestConfig;
import org.apache.commons.vfs2.test.ProviderTestSuite;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DFSConfigKeys;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * This test class uses the Hadoop MiniDFSCluster class to create an embedded Hadoop cluster. This will only work on
 * systems that Hadoop supports. This test does not run on Windows because Hadoop does not run on Windows.
 */
public class HdfsFileProviderTestCase extends AbstractProviderTestConfig implements ProviderTestConfig
{
    public static class HdfsProviderTestSuite extends ProviderTestSuite
    {

        // Turn off the MiniDFSCluster logging
        static
        {
            System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        }

        public HdfsProviderTestSuite(final ProviderTestConfig providerConfig, final boolean addEmptyDir) throws Exception
        {
            super(providerConfig, addEmptyDir);
        }

        @SuppressWarnings("deprecation")
        private void copyTestResources(final File directory, final Path parent) throws Exception
        {
            for (final File file : directory.listFiles())
            {
                if (file.isFile())
                {
                    final Path src = new Path(file.getAbsolutePath());
                    final Path dst = new Path(parent, file.getName());
                    hdfs.copyFromLocalFile(src, dst);
                }
                else if (file.isDirectory())
                {
                    final Path dir = new Path(parent, file.getName());
                    if (hdfs.mkdirs(dir))
                    {
                        copyTestResources(file, dir);
                    }
                    else
                    {
                        fail("Unable to make directory: " + dir);
                    }
                }
            }

        }

        @SuppressWarnings("deprecation")
        @Override
        protected void setUp() throws Exception
        {
            Logger.getRootLogger().setLevel(Level.OFF);

            // Put the MiniDFSCluster directory in the target directory
            System.setProperty("test.build.data", "target/build/test2/data");

            // Setup HDFS
            conf = new Configuration();
            conf.set(FileSystem.FS_DEFAULT_NAME_KEY, HDFS_URI);
            conf.set("hadoop.security.token.service.use_ip", "true");

            // MiniDFSCluster will check the permissions on the data directories, but does not do a good job of setting them
            // properly. We need to get the users umask and set the appropriate Hadoop property so that the data directories
            // will be created with the correct permissions.
            try
            {
                final Process p = Runtime.getRuntime().exec("/bin/sh -c umask");
                final BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
                final String line = bri.readLine();
                p.waitFor();
                // System.out.println("umask response: " + line);
                final Short umask = Short.parseShort(line.trim(), 8);
                // Need to set permission to 777 xor umask
                // leading zero makes java interpret as base 8
                final int newPermission = 0777 ^ umask;
                conf.set("dfs.datanode.data.dir.perm", String.format("%03o", newPermission));
            }
            catch (final Exception e)
            {
                throw new RuntimeException("Error getting umask from O/S", e);
            }

            conf.setLong(DFSConfigKeys.DFS_BLOCK_SIZE_KEY, 1024 * 100); // 100K blocksize

            try
            {
                cluster = new MiniDFSCluster(PORT, conf, 1, true, true, true, null, null, null, null);
                cluster.waitActive();
            }
            catch (final IOException e)
            {
                throw new RuntimeException("Error setting up mini cluster", e);
            }
            hdfs = cluster.getFileSystem();

            // Copy the test directory into HDFS
            final Path base = new Path("/test-data");
            assertTrue("Unable to create base directory", hdfs.mkdirs(base));
            final File testDir = AbstractVfsTestCase.getTestDirectory();
            copyTestResources(testDir, base);

            super.setUp();
        }

        @Override
        protected void tearDown() throws Exception
        {
            super.tearDown();
            if (null != hdfs)
            {
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
     * Creates the test suite for the zip file system.
     */
    public static Test suite() throws Exception
    {
        return new HdfsProviderTestSuite(new HdfsFileProviderTestCase(), false);
    }

    /**
     * Returns the base folder for read tests.
     */
    @Override
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception
    {
        final String uri = HDFS_URI + "/test-data";
        return manager.resolveFile(uri);
    }

    /**
     * Prepares the file system manager.
     */
    @Override
    public void prepare(final DefaultFileSystemManager manager) throws Exception
    {
        manager.addProvider("hdfs", new HdfsFileProvider());
    }

}
