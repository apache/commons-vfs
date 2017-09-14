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
package org.apache.commons.vfs2.test;

import org.apache.commons.vfs2.FilesCache;
import org.apache.commons.vfs2.cache.SoftRefFilesCache;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;

/**
 * A partial {@link org.apache.commons.vfs2.test.ProviderTestConfig} implementation.
 */
public abstract class AbstractProviderTestConfig extends AbstractProviderTestCase implements ProviderTestConfig {
    private FilesCache cache = null;

    /**
     * Returns a DefaultFileSystemManager instance (or subclass instance).
     */
    @Override
    public DefaultFileSystemManager getDefaultFileSystemManager() {
        return new DefaultFileSystemManager();
    }

    /**
     * Prepares the file system manager. This implementation does nothing.
     */
    @Override
    public void prepare(final DefaultFileSystemManager manager) throws Exception {
    }

    @Override
    public FilesCache getFilesCache() {
        if (cache == null) {
            // cache = new DefaultFilesCache();
            cache = new SoftRefFilesCache();
        }

        return cache;
    }

    @Override
    public boolean isFileSystemRootAccessible() {
        return true;
    }

}
