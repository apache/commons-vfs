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
public class HdfsFileSystemConfigBuilder extends FileSystemConfigBuilder
{
    private static final HdfsFileSystemConfigBuilder BUILDER = new HdfsFileSystemConfigBuilder();

    private HdfsFileSystemConfigBuilder() {
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

    private static final String CONFIG_NAME = "config.name";

    public String getConfigName(final FileSystemOptions opts) {
        return this.getString(opts, CONFIG_NAME);
    }

    /**
     * Sets the name of an alternate configuration file to be loaded after the defaults.
     */
    public void setConfigName(final FileSystemOptions opts, final String name) {
        this.setParam(opts, CONFIG_NAME, name);
    }


}
