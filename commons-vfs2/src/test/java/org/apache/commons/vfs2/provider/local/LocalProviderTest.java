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
package org.apache.commons.vfs2.provider.local;

import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectoryFile;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.PermissionsTests;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;

/**
 * JUnit 5 tests for the local file system.
 * <p>
 * This class replaces {@link LocalProviderTestCase} with a pure JUnit 5 implementation.
 * </p>
 */
@DisplayName("Local File System Provider Tests")
@Tag("provider")
@Tag("local")
@Tag("filesystem")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LocalProviderTest extends ProviderTestSuiteJunit5 {

    public LocalProviderTest() throws Exception {
        super(new LocalProviderTestConfig(), "", false);
    }

    @Override
    protected void addBaseTests() throws Exception {
        super.addBaseTests();
        
        addTests(FileNameTests.class);
        // addTests(TempFileTests.class);
        // VFS-325
        addTests(UrlTests.class);
        addTests(PermissionsTests.class);

        if (SystemUtils.IS_OS_WINDOWS) {
            addTests(WindowsFileNameTests.class);
        }
    }

    /**
     * Configuration for local file system tests.
     */
    private static class LocalProviderTestConfig extends AbstractProviderTestConfig {
        @Override
        public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
            return manager.toFileObject(getTestDirectoryFile());
        }
    }
}

