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
package org.apache.commons.vfs2.provider.test;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FilesCache;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.test.ProviderTestConfig;

/**
 * A provider config that wraps another provider, to run the tests via junctions.
 */
public class JunctionProviderConfig implements ProviderTestConfig {
    private final ProviderTestConfig config;

    public JunctionProviderConfig(final ProviderTestConfig config) {
        this.config = config;
    }

    /**
     * Returns a DefaultFileSystemManager instance (or subclass instance).
     */
    @Override
    public DefaultFileSystemManager getDefaultFileSystemManager() {
        return config.getDefaultFileSystemManager();
    }

    @Override
    public FilesCache getFilesCache() {
        return config.getFilesCache();
    }

    /**
     * Prepares the file system manager.
     */
    @Override
    public void prepare(final DefaultFileSystemManager manager) throws Exception {
        config.prepare(manager);
    }

    /**
     * Returns the base folder for tests.
     */
    @Override
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
        final FileObject baseFolder = config.getBaseTestFolder(manager);

        // Create an empty file system, then link in the base folder
        final FileSystem newFs = manager.createVirtualFileSystem("vfs:").getFileSystem();
        final String junctionPoint = "/some/dir";
        newFs.addJunction(junctionPoint, baseFolder);

        return newFs.resolveFile(junctionPoint);
    }

    @Override
    public boolean isFileSystemRootAccessible() {
        return true;
    }
}
