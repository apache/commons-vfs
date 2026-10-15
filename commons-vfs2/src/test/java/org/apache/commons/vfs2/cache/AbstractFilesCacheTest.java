/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.cache;

import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectoryFile;

import java.util.function.Supplier;

import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.AbstractProviderTestSuite;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FilesCache;
import org.junit.jupiter.api.Tag;

/**
 * Runs the tests of a {@link FilesCache} implementation on the local file system.
 */
@Tag("provider")
@Tag("local")
public abstract class AbstractFilesCacheTest extends AbstractProviderTestSuite {

    /**
     * Configures the local file system with the {@link FilesCache} under test.
     */
    private static final class FilesCacheTestConfig extends AbstractProviderTestConfig {

        private final Supplier<FilesCache> filesCacheFactory;

        FilesCacheTestConfig(final Supplier<FilesCache> filesCacheFactory) {
            this.filesCacheFactory = filesCacheFactory;
        }

        @Override
        protected FilesCache createFilesCache() {
            return filesCacheFactory.get();
        }

        @Override
        public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
            return manager.toFileObject(getTestDirectoryFile());
        }
    }

    private final Class<? extends AbstractFilesCacheTestsBase> testsClass;

    /**
     * Constructs a new instance.
     *
     * @param filesCacheFactory Creates the {@link FilesCache} under test.
     * @param testsClass The tests to run.
     */
    protected AbstractFilesCacheTest(final Supplier<FilesCache> filesCacheFactory, final Class<? extends AbstractFilesCacheTestsBase> testsClass) {
        super(new FilesCacheTestConfig(filesCacheFactory), "");
        this.testsClass = testsClass;
    }

    @Override
    protected void addBaseTests() throws Exception {
        addTests(testsClass);
    }
}
