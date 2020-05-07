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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.curator.framework.CuratorFramework;

public class ZkFileSystem extends AbstractFileSystem {

  private CuratorFramework framework;
  private boolean ownsClient = false;

  protected ZkFileSystem(final FileName rootName,
      final CuratorFramework framework,
      final FileSystemOptions fileSystemOptions) {
    super(rootName, null, fileSystemOptions);
    this.framework = Objects.requireNonNull(framework);
    this.ownsClient = ZkFileSystemConfigBuilder.getInstance()
        .getOwnsClient(fileSystemOptions);
  }

  @Override
  protected void doCloseCommunicationLink() {
    if (framework != null && ownsClient) {
      framework.close();
      framework = null;
    }
  }

  @Override
  protected FileObject createFile(AbstractFileName name) throws Exception {
    return null;
  }

  /**
   * Resolve FileName into FileObject.
   *
   * @param name The name of a file on the HdfsFileSystem.
   * @return resolved FileObject.
   * @throws FileSystemException if an error occurred.
   */
  @Override
  public FileObject resolveFile(final FileName name)
      throws FileSystemException {
    final boolean useCache =
        null != getContext().getFileSystemManager().getFilesCache();

    FileObject fileObject = null;
    if (useCache) {
      fileObject = getFileFromCache(name);
    }

    if (fileObject == null) {
      String path = null;
      try {
        path = URLDecoder.decode(name.getPath(), StandardCharsets.UTF_8.name());
      } catch (final UnsupportedEncodingException e) {
        path = name.getPath();
      }
      fileObject =
          new ZkFileObject((AbstractFileName) name, this, framework, path);
    }
    if (useCache) {
      this.putFileToCache(fileObject);
    }
    return fileObject;
  }

  @Override
  protected void addCapabilities(Collection<Capability> caps) {
    caps.addAll(ZkFileProvider.CAPABILITIES);
  }
}
