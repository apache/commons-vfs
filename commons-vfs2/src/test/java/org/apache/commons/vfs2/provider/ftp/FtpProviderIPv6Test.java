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
package org.apache.commons.vfs2.provider.ftp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.IPv6LocalConnectionTests;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.junit.jupiter.api.Test;

/**
 * JUnit 5 tests for FTP provider with IPv6 support.
 * <p>
 * This class replaces {@code FtpProviderIPv6TestCase} with a pure JUnit 5 implementation.
 * This test uses mocked providers and doesn't require actual file system access.
 * </p>
 */
public class FtpProviderIPv6Test extends ProviderTestSuiteJunit5 {

    public FtpProviderIPv6Test() throws Exception {
        super(new FtpProviderIPv6TestConfig(), "", false);
    }

    @Override
    protected void addBaseTests() throws Exception {
        // Only add base tests if we have a real FTP server configured
        if (getSystemTestUriOverride() != null) {
            addTests(IPv6LocalConnectionTests.class);
        }
        // Otherwise, only the @Test methods in this class will run (testResolveIPv6Url)
    }

    /**
     * Tests resolving an IPv6 URL.
     */
    @Test
    public void testResolveIPv6Url() throws FileSystemException {
        final String ipv6Url = "ftp://[fe80::1c42:dae:8370:aea6%en1]/file.txt";
        final FtpFileObject fileObject = (FtpFileObject) getManager().resolveFile(ipv6Url, new FileSystemOptions());
        assertEquals("ftp://[fe80::1c42:dae:8370:aea6%en1]/", fileObject.getFileSystem().getRootURI());
        assertEquals("file.txt", fileObject.getRelPath());
    }

    /**
     * Gets the system test URI override.
     */
    private static String getSystemTestUriOverride() {
        return System.getProperty("test.ftp.uri");
    }

    /**
     * Configuration for FTP IPv6 tests.
     */
    private static class FtpProviderIPv6TestConfig extends AbstractProviderTestConfig {

        @Override
        public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
            // Use a mocked FTP provider for IPv6 tests
            return null;
        }

        public void prepare(final DefaultFileSystemManager manager) throws Exception {
            manager.addProvider("ftp", new MockedClientFtpFileProvider());
        }
    }

    /**
     * Mocked FTP file provider for testing IPv6 without actual network connection.
     */
    private static class MockedClientFtpFileProvider extends FtpFileProvider {
        @Override
        protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions) {
            final GenericFileName rootName = (GenericFileName) name;
            return new FtpFileSystem(rootName, mock(FtpClient.class), fileSystemOptions);
        }
    }
}

