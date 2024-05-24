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

import java.util.Collection;
import java.util.List;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs2.provider.http.HttpFileNameParser;

/**
 * FileProvider for HDFS files.
 *
 * @since 2.1
 */
public class HdfsFileProvider extends AbstractOriginatingFileProvider {

    private static final Collection<Capability> CAPABILITIES = List.of(
        Capability.GET_TYPE,
        Capability.READ_CONTENT,
        Capability.CREATE,
        Capability.DELETE,
        Capability.RENAME,
        Capability.WRITE_CONTENT,
        Capability.URI,
        Capability.GET_LAST_MODIFIED,
        Capability.SET_LAST_MODIFIED_FILE,
        Capability.ATTRIBUTES,
        Capability.RANDOM_ACCESS_READ,
        Capability.DIRECTORY_READ_CONTENT,
        Capability.LIST_CHILDREN
    );

    /**
     * Constructs a new HdfsFileProvider.
     */
    public HdfsFileProvider() {
        setFileNameParser(HttpFileNameParser.getInstance());
    }

    /**
     * Creates a new HdfsFileSystem instance.
     *
     * @param rootName Name of the root file.
     * @param fileSystemOptions Configuration options for this instance.
     * @return a new HdfsFileSystem instance
     * @throws FileSystemException if an error occurs.
     */
    @Override
    protected FileSystem doCreateFileSystem(FileName rootName, FileSystemOptions fileSystemOptions)
            throws FileSystemException {
        return new HdfsFileSystem(rootName, fileSystemOptions);
    }

    /**
     * Gets the capabilities of HdfsFileSystem.
     *
     * @return The capabilities (unmodifiable).
     */
    @Override
    public Collection<Capability> getCapabilities() {
        return CAPABILITIES;
    }

    /**
     * Gets the configuration builder for HdfsFileSystems.
     *
     * @return The configuration builder.
     */
    @Override
    public FileSystemConfigBuilder getConfigBuilder() {
        return HdfsFileSystemConfigBuilder.getInstance();
    }
}
