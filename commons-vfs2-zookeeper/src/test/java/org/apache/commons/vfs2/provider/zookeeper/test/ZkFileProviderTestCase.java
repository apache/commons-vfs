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

package org.apache.commons.vfs2.provider.zookeeper.test;

import java.io.File;
import java.io.FileInputStream;

import junit.framework.Test;
import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.zookeeper.ZkFileProvider;
import org.apache.commons.vfs2.provider.zookeeper.ZkFileSystemConfigBuilder;
import org.apache.commons.vfs2.test.AbstractProviderTestConfig;
import org.apache.commons.vfs2.test.FileOrFolderContentTests;
import org.apache.commons.vfs2.test.LastModifiedTests;
import org.apache.commons.vfs2.test.NamingTests;
import org.apache.commons.vfs2.test.ProviderCacheStrategyTests;
import org.apache.commons.vfs2.test.ProviderDeleteTests;
import org.apache.commons.vfs2.test.ProviderRandomReadTests;
import org.apache.commons.vfs2.test.ProviderRandomReadWriteTests;
import org.apache.commons.vfs2.test.ProviderRandomSetLengthTests;
import org.apache.commons.vfs2.test.ProviderReadFileOrFolderTests;
import org.apache.commons.vfs2.test.ProviderRenameTests;
import org.apache.commons.vfs2.test.ProviderTestConfig;
import org.apache.commons.vfs2.test.ProviderTestSuite;
import org.apache.commons.vfs2.test.ProviderWriteAppendTests;
import org.apache.commons.vfs2.test.ProviderWriteTests;
import org.apache.commons.vfs2.test.UriTests;
import org.apache.commons.vfs2.test.UrlStructureTests;
import org.apache.commons.vfs2.test.UrlTests;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.ZKPaths;
import org.apache.hadoop.fs.Path;

public class ZkFileProviderTestCase extends AbstractProviderTestConfig {

  public static class ZkFileProviderTestSuite extends ProviderTestSuite {

    protected static TestingServer zkServer;
    protected static String zkConnectionString;
    protected static FileSystemOptions opts = new FileSystemOptions();

    public ZkFileProviderTestSuite(final ProviderTestConfig providerConfig,
        final boolean addEmptyDir) throws Exception {
      super(providerConfig, addEmptyDir);
    }

    private void copyTestResources(final CuratorFramework client,
        final File directory, final Path parent) throws Exception {
      for (final File file : directory.listFiles()) {
        if (file.isFile()) {
          final String ext = FilenameUtils.getExtension(file.getName());
          if (ext.equals("gz") || ext.equals("zip") || ext.equals("tar")
              || ext.equals("jar") || ext.equals("tbz2") || ext.equals("tgz")) {
            continue;
          }
          client.create().creatingParentsIfNeeded().forPath(
              ZKPaths.makePath(parent.toString(), file.getName()),
              IOUtils.toByteArray(new FileInputStream(file)));
        } else if (file.isDirectory()) {
          final Path dir = new Path(parent, file.getName());
          client.create().creatingParentsIfNeeded()
              .forPath(ZKPaths.makePath(parent.toString(), file.getName()));
          copyTestResources(client, file, dir);
        }
      }
    }

    @Override
    protected void setUp() throws Exception {
      final File data = new File("target/test/zktestdata").getAbsoluteFile();
      data.mkdirs();

      System.setProperty("test.build.data", data.toString());
      FileUtils.cleanDirectory(data);
      zkServer = new TestingServer();
      zkServer.start();
      zkConnectionString = zkServer.getConnectString();
      ZkFileSystemConfigBuilder.getInstance().setZkConnectionString(opts,
          zkConnectionString);
      final Path base = new Path("/test-data");
      final File testDir = AbstractVfsTestCase.getTestDirectoryFile();
      try (CuratorFramework client = CuratorFrameworkFactory.newClient(
          zkConnectionString, new ExponentialBackoffRetry(1000, 3))) {
        client.start();
        copyTestResources(client, testDir, base);
      }
      super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
      // tear down super FIRST
      // this will close the curator clients
      // if you stop the server first we'll be flooded
      // with exception logs
      super.tearDown();
      // because super.tearDown() does gc and sleeps, the curator
      // stuff will all be gone, so we can just shut the server
      // down
      if (zkServer != null) {
        zkServer.close();
        zkServer = null;
      }
    }

    @Override
    protected void addBaseTests() throws Exception {
      addTests(ProviderCacheStrategyTests.class);
      addTests(UriTests.class);
      addTests(NamingTests.class);
      addTests(FileOrFolderContentTests.class);
      addTests(ProviderReadFileOrFolderTests.class);
      addTests(ProviderWriteTests.class);
      addTests(ProviderWriteAppendTests.class);
      addTests(ProviderRandomReadTests.class);
      addTests(ProviderRandomReadWriteTests.class);
      addTests(ProviderRandomSetLengthTests.class);
      addTests(ProviderRenameTests.class);
      addTests(ProviderDeleteTests.class);
      addTests(LastModifiedTests.class);
      addTests(UrlTests.class);
      addTests(UrlStructureTests.class);
    }
  }

  public static Test suite() throws Exception {
    return new ZkFileProviderTestSuite(new ZkFileProviderTestCase(), false);
  }

  /**
   * Returns the base folder for read tests.
   */
  @Override
  public FileObject getBaseTestFolder(final FileSystemManager manager)
      throws Exception {
    final String uri = "zk://test-data";
    return manager.resolveFile(uri, ZkFileProviderTestSuite.opts);
  }

  @Override
  public void prepare(final DefaultFileSystemManager manager) throws Exception {
    manager.addProvider("zk", new ZkFileProvider());
  }

}
