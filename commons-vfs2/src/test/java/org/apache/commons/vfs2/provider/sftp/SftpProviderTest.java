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
package org.apache.commons.vfs2.provider.sftp;

import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.PermissionsTests;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;

/**
 * JUnit 5 tests for the SFTP file system provider.
 * <p>
 * This class replaces {@code SftpProviderTestCase} with a pure JUnit 5 implementation.
 * </p>
 */
public class SftpProviderTest extends ProviderTestSuiteJunit5 {

    public SftpProviderTest() throws Exception {
        super(new SftpProviderTestConfig(), "", false);
    }

    @Override
    protected void addBaseTests() throws Exception {
        // Only add base tests if we have a real SFTP server configured
        if (SftpProviderTestUtil.getSystemTestUriOverride() != null) {
            super.addBaseTests();
            // VFS-405: set/get permissions
            addTests(PermissionsTests.class);
            addTests(SftpMultiThreadWriteTests.class);
        }
    }

    /**
     * Configuration for SFTP provider tests.
     */
    private static class SftpProviderTestConfig extends AbstractProviderTestConfig {

        @Override
        public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
            final String uri = SftpProviderTestUtil.getSystemTestUriOverride();
            return uri != null ? manager.resolveFile(uri) : null;
        }

        @Override
        public void prepare(final DefaultFileSystemManager manager) throws Exception {
            manager.addProvider("sftp", new SftpFileProvider());
        }
    }
}

