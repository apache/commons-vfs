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
package org.apache.commons.vfs2.provider.test;

import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectoryFile;

import java.io.File;

import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;

/**
 * JUnit 5 test cases for the virtual file system provider.
 * <p>
 * This class replaces {@link VirtualProviderTestCase} with a pure JUnit 5 implementation.
 * </p>
 */
public class VirtualProviderTest extends ProviderTestSuiteJunit5 {

    public VirtualProviderTest() throws Exception {
        super(new VirtualProviderTestConfig(), "", false);
    }

    @Override
    protected void addBaseTests() throws Exception {
        super.addBaseTests();
        addTests(JunctionTests.class);
    }

    /**
     * Configuration for virtual provider tests.
     */
    private static class VirtualProviderTestConfig extends AbstractProviderTestConfig {

        /**
         * Returns the base folder for tests.
         */
        @Override
        public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
            final File baseDir = getTestDirectoryFile();
            final FileObject baseFile = manager.toFileObject(baseDir);
            return manager.createVirtualFileSystem(baseFile);
        }
    }
}

