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

package org.apache.commons.vfs2.provider.zookeeper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;

public class ZkFileProvider extends AbstractOriginatingFileProvider {
  static final Collection<Capability> CAPABILITIES =
      Collections.unmodifiableCollection(Arrays.asList(Capability.GET_TYPE,
          Capability.READ_CONTENT, Capability.URI, Capability.WRITE_CONTENT,
          Capability.DIRECTORY_READ_CONTENT, Capability.LIST_CHILDREN));

  public ZkFileProvider() {
    super();
  }

  @Override
  protected FileSystem doCreateFileSystem(FileName rootName,
      FileSystemOptions fileSystemOptions) throws FileSystemException {
    ZkFileSystemConfigBuilder configBuilder =
        ZkFileSystemConfigBuilder.getInstance();

    // did the caller provide a curator framework?
    CuratorFramework framework =
        configBuilder.getCuratorFramework(fileSystemOptions);
    if (framework == null) {
      String connectionString =
          configBuilder.getZkConnectionSring(fileSystemOptions);
      RetryPolicy retryPolicy = configBuilder.getRetryPolicy(fileSystemOptions);
      String namespace = configBuilder.getZkNamespace(fileSystemOptions);
      int connectionTimeout =
          configBuilder.getConnectionTimeout(fileSystemOptions);
      int sessionTimeout = configBuilder.getSessionTimeout(fileSystemOptions);

      Builder builder = CuratorFrameworkFactory.builder();

      builder = builder.connectString(connectionString).retryPolicy(retryPolicy)
          .connectionTimeoutMs(connectionTimeout)
          .sessionTimeoutMs(sessionTimeout);

      if (namespace != null) {
        builder = builder.namespace(namespace);
      }

      if (configBuilder.getZkAuthScheme(fileSystemOptions) != null) {
        byte[] bytes = configBuilder.getZkAuthBytes(fileSystemOptions);
        if (ArrayUtils.isEmpty(bytes)) {
          throw new FileSystemException("vfs.provider.zk/authBytes",
              "missing auth bytes");
        }
        builder = builder.authorization(
            configBuilder.getZkAuthScheme(fileSystemOptions), bytes);
      }

      framework = builder.build();
      framework.start();

      configBuilder.setOwnsClient(fileSystemOptions, true);
    } else {
      configBuilder.setOwnsClient(fileSystemOptions, false);
    }

    return new ZkFileSystem(rootName, framework, fileSystemOptions);
  }

  @Override
  public Collection<Capability> getCapabilities() {
    return CAPABILITIES;
  }
}
