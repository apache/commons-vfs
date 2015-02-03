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
package org.apache.commons.vfs2.provider.hdfs;

import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;

/**
 * Configuration settings for the HdfsFileSystem.
 *
 * @since 2.1
 */
public final class HdfsFileSystemConfigBuilder extends FileSystemConfigBuilder
{
    private static final HdfsFileSystemConfigBuilder BUILDER = new HdfsFileSystemConfigBuilder();
    private static final String KEY_CONFIG_NAME = "configName";

    private HdfsFileSystemConfigBuilder()
    {
        super("hdfs.");
    }

    /**
     * @return HdfsFileSystemConfigBuilder instance
     */
    public static HdfsFileSystemConfigBuilder getInstance()
    {
        return BUILDER;
    }

    /**
     * @return HDFSFileSystem
     */
    @Override
    protected Class<? extends FileSystem> getConfigClass()
    {
        return HdfsFileSystem.class;
    }

    /**
     * Get name of alternate configuration file.
     *
     * @return resource name of alternate configuration file or null.
     * @param opts The FileSystemOptions.
     * @see #setConfigName(FileSystemOptions, String)
     */
    public String getConfigName(final FileSystemOptions opts)
    {
        return this.getString(opts, KEY_CONFIG_NAME);
    }

    /**
     * Sets the name of configuration file to be loaded after the defaults.
     * <p>
     * Specifies the name of a config file to override any specific HDFS settings.
     * The property will be passed on to {@code org.apache.hadoop.conf.Configuration#addResource(String)}
     * after the URL was set as the default name with: {@code Configuration#set(FileSystem.FS_DEFAULT_NAME_KEY, url)}.
     * <p>
     * One use for this is to set a different value for the {@code dfs.client.use.datanode.hostname}
     * property in order to access HDFS files stored in an AWS installation (from outside their
     * firewall). There are other possible uses too.
     *
     * @param opts The FileSystemOptions to modify.
     * @param name resource name of additional configuration file or null.
     */
    public void setConfigName(final FileSystemOptions opts, final String name)
    {
        this.setParam(opts, KEY_CONFIG_NAME, name);
    }

}
