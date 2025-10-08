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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.jupiter.api.Test;

/**
 * JUnit 5 tests for SFTP provider permission exception handling.
 * <p>
 * This class replaces {@code SftpPermissionExceptionTestCase} with a pure JUnit 5 implementation.
 * </p>
 * <p>
 * Tests that permission exceptions are properly handled when attempting to write to read-only files.
 * </p>
 */
public class SftpPermissionExceptionTest extends ProviderTestSuiteJunit5 {

    public SftpPermissionExceptionTest() throws Exception {
        super(new SftpPermissionExceptionTestConfig(), "", false);
    }

    @Override
    protected void addBaseTests() throws Exception {
        // Only add base tests if we have a real SFTP server configured
        // Otherwise, only the @Test methods in this class will run
        if (SftpProviderTestUtil.getSystemTestUriOverride() != null) {
            super.addBaseTests();
        }
    }

    /**
     * Tests that getting an output stream on a read-only file throws an exception.
     * <p>
     * This test creates a read-only file and verifies that attempting to copy to it
     * throws a FileSystemException.
     * </p>
     * <p>
     * This test requires a real SFTP server configured via system property.
     * </p>
     */
    @Test
    public void testGetOutputStreamException() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeTrue(SftpProviderTestUtil.getSystemTestUriOverride() != null,
            "Test requires SFTP server configured via system property");
        final FileObject scratchFolder = getWriteFolder();

        // Create a read-only file
        final FileObject readOnlyFile = scratchFolder.resolveFile("read-only-file.txt");
        readOnlyFile.createFile();
        readOnlyFile.setWritable(false, false);

        // Try to copy to the read-only file - should throw exception
        final FileObject sourceFile = scratchFolder.resolveFile("file1.txt");
        assertThrows(FileSystemException.class, () -> {
            readOnlyFile.copyFrom(sourceFile, null);
        });

        // Clean up
        readOnlyFile.setWritable(true, false);
        readOnlyFile.delete();
    }

    /**
     * Configuration for SFTP permission exception tests.
     */
    private static class SftpPermissionExceptionTestConfig extends AbstractProviderTestConfig {

        @Override
        public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
            final String uri = SftpProviderTestUtil.getSystemTestUriOverride();
            if (uri == null) {
                return null;
            }
            final FileSystemOptions fileSystemOptions = new FileSystemOptions();
            final SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();
            builder.setStrictHostKeyChecking(fileSystemOptions, "no");
            builder.setUserInfo(fileSystemOptions, new TrustEveryoneUserInfo());
            return manager.resolveFile(uri, fileSystemOptions);
        }

        public void prepare(final DefaultFileSystemManager manager) throws Exception {
            manager.addProvider("sftp", new SftpFileProvider());
        }
    }
}

