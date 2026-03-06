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

package org.apache.commons.vfs2.provider.http4;

import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectory;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.util.NHttpFileServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests HTTP Basic Authentication https://issues.apache.org/jira/browse/VFS-861.
 */
public class Http4BasicAuthTest {

    private static final String TEST_USERNAME = "USER";

    private static final String TEST_PASSWORD = "PWD";

    private static NHttpFileServer server;

    private static String baseUri;

    @BeforeAll
    static void startServer() throws Exception {
        server = NHttpFileServer.startWithBasicAuth(0, new File(getTestDirectory()), 5000, TEST_USERNAME, TEST_PASSWORD);
        baseUri = AbstractProviderTestConfig.getLocalHostUriString("http4", server.getPort());
    }

    @AfterAll
    static void stopServer() throws InterruptedException {
        if (server != null) {
            server.shutdown(5, TimeUnit.SECONDS);
        }
    }

    private FileSystemOptions authOptions() throws FileSystemException {
        final FileSystemOptions opts = new FileSystemOptions();
        final StaticUserAuthenticator auth = new StaticUserAuthenticator(null, TEST_USERNAME, TEST_PASSWORD);
        DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(opts, auth);
        return opts;
    }

    @Test
    void testResolveFile() throws FileSystemException {
        final FileSystemOptions opts = authOptions();
        try (FileObject file = VFS.getManager().resolveFile(baseUri + "/read-tests/file1.txt", opts)) {
            assertNotNull(file);
            assertTrue(file.exists(), "file1.txt should exist");
            assertEquals(FileType.FILE, file.getType());
            assertNotNull(file.getContent(), "Content should be readable when credentials are correct");
        }
    }

    @Test
    void testResolveFolder() throws FileSystemException {
        final FileSystemOptions opts = authOptions();
        try (FileObject folder = VFS.getManager().resolveFile(baseUri + "/read-tests", opts)) {
            assertNotNull(folder);
            assertTrue(folder.exists(), "read-tests folder should exist");
        }
    }

    @Test
    void testResolveFolderWithTrailingSlash() throws FileSystemException {
        final FileSystemOptions opts = authOptions();
        try (FileObject folder = VFS.getManager().resolveFile(baseUri + "/read-tests/", opts)) {
            assertNotNull(folder);
            assertTrue(folder.exists(), "read-tests/ folder should exist");
        }
    }

    @Test
    void testUnauthenticatedRequestReturns401() {
        assertThrows(FileSystemException.class, () -> VFS.getManager().resolveFile(baseUri + "/read-tests/file1.txt", new FileSystemOptions()).exists(),
                "Expected FileSystemException when accessing a resource without credentials");
    }
}
