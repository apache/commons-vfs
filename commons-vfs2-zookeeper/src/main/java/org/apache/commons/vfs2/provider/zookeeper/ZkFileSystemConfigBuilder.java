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

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class ZkFileSystemConfigBuilder extends FileSystemConfigBuilder {
  protected static final String KEY_CONFIGURED_FRAMEWORK = "framework";
  protected static final String KEY_ZK_CONNECTION_STRING = "connectionString";
  protected static final String KEY_RETRY_TIMEOUT = "retryTimeout";
  protected static final String KEY_RETRY_RETRIES = "retries";
  protected static final String KEY_RETRY_POLICY = "retryPolicy";
  protected static final String KEY_CONNECTION_TIMEOUT = "connectionTimeout";
  protected static final String KEY_SESSION_TIMEOUT = "sessionTimeout";
  protected static final String KEY_NAMESPACE = "zkNamespace";
  protected static final String KEY_AUTH_SCHEME = "zkAuthScheme";
  protected static final String KEY_AUTH_BYTES = "zkAuthBytes";
  protected static final String KEY_OWNS_CLIENT = "ownsClientFlag";

  private static final int DEFAULT_RETRY_TIMOUT = 1000;
  private static final int DEFAULT_RETRY_RETRIES = 3;
  private static final int DEFAULT_CONNECTION_TIMEOUT = 1000;
  private static final int DEFAULT_SESSION_TIMEOUT = 5000;
  private static final String DEFAULT_ZK_CONNECTION_STRING = "localhost:2180";

  private static final ZkFileSystemConfigBuilder BUILDER =
      new ZkFileSystemConfigBuilder();

  private ZkFileSystemConfigBuilder() {
    super("zk.");

  }

  public static ZkFileSystemConfigBuilder getInstance() {
    return BUILDER;
  }

  public CuratorFramework getCuratorFramework(final FileSystemOptions opts) {
    return (CuratorFramework) getParam(opts, KEY_CONFIGURED_FRAMEWORK);
  }

  public void setCuratorFramework(final FileSystemOptions opts,
      final CuratorFramework curatorFramework) {
    setParam(opts, KEY_CONFIGURED_FRAMEWORK, curatorFramework);
  }

  public RetryPolicy getRetryPolicy(final FileSystemOptions opts) {
    if (!hasParam(opts, KEY_RETRY_POLICY)) {
      setParam(opts, KEY_RETRY_POLICY, new ExponentialBackoffRetry(
          getRetryTimeout(opts), getRetryRetries(opts)));
    }
    return (RetryPolicy) getParam(opts, KEY_RETRY_POLICY);
  }

  public void setRetryPolicy(final FileSystemOptions opts, RetryPolicy policy) {
    setParam(opts, KEY_RETRY_POLICY, policy);
  }

  public int getRetryTimeout(FileSystemOptions opts) {
    if (!hasParam(opts, KEY_RETRY_TIMEOUT)) {
      setParam(opts, KEY_RETRY_TIMEOUT, DEFAULT_RETRY_TIMOUT);
    }
    return getInteger(opts, KEY_RETRY_TIMEOUT);
  }

  public void setRetryTimeout(final FileSystemOptions opts, final int timeout) {
    setParam(opts, KEY_RETRY_TIMEOUT, Integer.valueOf(timeout));
  }

  public int getRetryRetries(final FileSystemOptions opts) {
    if (!hasParam(opts, KEY_RETRY_RETRIES)) {
      setParam(opts, KEY_RETRY_RETRIES, DEFAULT_RETRY_RETRIES);
    }
    return getInteger(opts, KEY_RETRY_TIMEOUT);
  }

  public void setRetryRetries(final FileSystemOptions opts,
      final int retryCount) {
    setParam(opts, KEY_RETRY_TIMEOUT, Integer.valueOf(retryCount));
  }

  public int getConnectionTimeout(final FileSystemOptions opts) {
    if (!hasParam(opts, KEY_CONNECTION_TIMEOUT)) {
      setParam(opts, KEY_CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT);
    }
    return getInteger(opts, KEY_CONNECTION_TIMEOUT);
  }

  public void setConnectionTimeout(final FileSystemOptions opts,
      final int timeout) {
    setParam(opts, KEY_CONNECTION_TIMEOUT, Integer.valueOf(timeout));
  }

  public int getSessionTimeout(final FileSystemOptions opts) {
    if (!hasParam(opts, KEY_SESSION_TIMEOUT)) {
      setParam(opts, KEY_SESSION_TIMEOUT, DEFAULT_SESSION_TIMEOUT);
    }
    return getInteger(opts, KEY_SESSION_TIMEOUT);
  }

  public void setSessionTimeout(final FileSystemOptions opts,
      final int timeout) {
    setParam(opts, KEY_SESSION_TIMEOUT, Integer.valueOf(timeout));
  }

  public String getZkConnectionSring(final FileSystemOptions opts) {
    if (!hasParam(opts, KEY_ZK_CONNECTION_STRING)) {
      setParam(opts, KEY_ZK_CONNECTION_STRING, DEFAULT_ZK_CONNECTION_STRING);
    }
    return getString(opts, KEY_ZK_CONNECTION_STRING);
  }

  public void setZkConnectionString(final FileSystemOptions opts,
      final String serverName) {
    setParam(opts, KEY_ZK_CONNECTION_STRING, serverName);
  }

  public String getZkNamespace(final FileSystemOptions opts) {
    return getString(opts, KEY_NAMESPACE);
  }

  public void setZkNamespace(final FileSystemOptions opts,
      final String namespace) {
    setParam(opts, KEY_NAMESPACE, namespace);
  }

  public String getZkAuthScheme(final FileSystemOptions opts) {
    return getString(opts, KEY_AUTH_SCHEME);
  }

  public void setZkAuthScheme(final FileSystemOptions opts,
      final String userName) {
    setParam(opts, KEY_AUTH_SCHEME, userName);
  }

  public byte[] getZkAuthBytes(final FileSystemOptions opts) {
    return (byte[]) getParam(opts, KEY_AUTH_BYTES);
  }

  public void setZkAuthBytes(final FileSystemOptions opts, final byte[] bytes) {
    setParam(opts, KEY_AUTH_BYTES, bytes);
  }

  public boolean getOwnsClient(final FileSystemOptions opts) {
    return hasParam(opts, KEY_OWNS_CLIENT);
  }

  public void setOwnsClient(final FileSystemOptions opts, final Boolean bool) {
    setParam(opts, KEY_OWNS_CLIENT, bool);
  }

  @Override
  protected Class<? extends FileSystem> getConfigClass() {
    return ZkFileSystem.class;
  }
}
