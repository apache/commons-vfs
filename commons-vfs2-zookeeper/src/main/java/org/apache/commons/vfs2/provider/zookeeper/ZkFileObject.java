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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;

public class ZkFileObject extends AbstractFileObject<ZkFileSystem> {

  private CuratorFramework framework;
  private String path;
  private Stat stat;

  protected ZkFileObject(final AbstractFileName name,
      final ZkFileSystem fileSystem, final CuratorFramework framework,
      final String path) {
    super(name, fileSystem);
    this.framework = Objects.requireNonNull(framework);
    this.path = Objects.requireNonNull(path);
  }

  @Override
  protected long doGetContentSize() throws Exception {
    return this.stat.getDataLength();
  }

  @Override
  protected InputStream doGetInputStream() throws Exception {
    if (this.stat == null) {
      throw new FileSystemException("vfs.provider/read-not-file.error",
          getName());
    }

    final byte[] data = (this.stat.getDataLength() == 0) ? new byte[0]
        : framework.getData().forPath(this.path);
    return new ByteArrayInputStream(data);
  }
  
  @Override
  protected FileType doGetType() throws Exception {
    return (this.stat == null) ? FileType.IMAGINARY : FileType.FILE_OR_FOLDER;
  }

  /**
   * Checks if this file is a folder by using its file type. ZooKeeper nodes can
   * always be considered as folders or files.
   *
   * @return true if this file is a regular file.
   * @throws FileSystemException if an error occurs.
   * @see #getType()
   * @see FileType#FOLDER
   */
  @Override
  public boolean isFolder() throws FileSystemException {
    try {
      return doGetType() == FileType.FILE_OR_FOLDER;
    } catch (Exception e) {
      throw new FileSystemException(e);
    }
  }

  @Override
  protected String[] doListChildren() throws Exception {
    if (this.stat == null || this.stat.getNumChildren() == 0) {
      return new String[0];
    }

    List<String> children = framework.getChildren().forPath(this.path);
    return UriParser.encode(children.toArray(new String[0]));
  }

  @Override
  protected void doAttach() throws Exception {
    try {
      this.stat = this.framework.checkExists().forPath(this.path);
    } catch (final Exception e) {
      this.stat = null;
    }
  }

  /**
   * Determines if the file exists.
   *
   * @see org.apache.commons.vfs2.provider.AbstractFileObject#exists()
   * @return boolean true if file exists, false if not
   */
  /*@Override
  public boolean exists() throws FileSystemException {
    try {
      doAttach();
      return this.stat != null;
    } catch (final Exception e) {
      throw new FileSystemException("Unable to check existance: {}", getName(),
          e);
    }
  }
*/
}
