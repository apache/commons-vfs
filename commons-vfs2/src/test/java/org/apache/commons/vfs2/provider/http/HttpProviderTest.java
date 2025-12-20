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
package org.apache.commons.vfs2.provider.http;

import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectory;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.time.Duration;

import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileNotFolderException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.util.NHttpFileServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * JUnit 5 test cases for the HTTP provider.
 * <p>
 * This class replaces {@link HttpProviderTestCase} with a pure JUnit 5 implementation.
 * </p>
 */
/** HTTP Provider Tests */
@Tag("provider")
@Tag("http")
@Tag("network")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HttpProviderTest extends ProviderTestSuiteJunit5 {

    private static final Duration ONE_MINUTE = Duration.ofMinutes(1);

    private static NHttpFileServer server;

    private static final String TEST_URI = "test.http.uri";

    /**
     * Use %40 for @ in URLs
     */
    private static String connectionUri;

    private static String getSystemTestUriOverride() {
        return System.getProperty(TEST_URI);
    }

    public HttpProviderTest() throws Exception {
        super(new HttpProviderTestConfig(), "", false);
    }

    @Override
    protected void setUp() throws Exception {
        if (getSystemTestUriOverride() == null) {
            server = NHttpFileServer.start(0, new File(getTestDirectory()), 5000);
            connectionUri = AbstractProviderTestConfig.getLocalHostUriString("http", server.getPort());
        }
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        if (server != null) {
            server.close();
            server = null;
        }
        super.tearDown();
    }

    @Override
    protected void addBaseTests() throws Exception {
        // Add standard provider tests
        super.addBaseTests();

        // HTTP-specific tests are now part of this test suite

        // HttpAsyncServer returns 400 on link local requests from Httpclient
        // (e.g. Apache Web Server does the same https://bz.apache.org/bugzilla/show_bug.cgi?id=35122,
        // but not every HTTP server does).
        // Until this is addressed, local connection test won't work end-to-end

        // if (getSystemTestUriOverride() == null) {
        //    addTests(IPv6LocalConnectionTests.class);
        // }
    }

    // ==================== HTTP-Specific Tests ====================

    private void checkReadTestsFolder(final FileObject file) throws FileSystemException {
        Assertions.assertNotNull(file.getChildren());
        Assertions.assertTrue(file.getChildren().length > 0);
    }

    /** Ensure VFS-453 options are present. */
    @Test
    public void testHttpTimeoutConfig() {
        final FileSystemOptions options = new FileSystemOptions();
        final HttpFileSystemConfigBuilder builder = HttpFileSystemConfigBuilder.getInstance();

        // ensure defaults are 0
        assertEquals(0, builder.getConnectionTimeout(options));
        assertEquals(0, builder.getConnectionTimeoutDuration(options).toMillis());
        assertEquals(0, builder.getSoTimeout(options));
        assertEquals("Jakarta-Commons-VFS", builder.getUserAgent(options));

        // Set with deprecated milliseconds APIs.
        builder.setConnectionTimeout(options, 60000);
        builder.setSoTimeout(options, 60000);
        builder.setUserAgent(options, "foo/bar");

        // ensure changes are visible
        assertEquals(60000, builder.getConnectionTimeout(options));
        assertEquals(ONE_MINUTE, builder.getConnectionTimeoutDuration(options));
        assertEquals(60000, builder.getSoTimeout(options));
        assertEquals("foo/bar", builder.getUserAgent(options));

        // Set with Duration APIs.
        builder.setConnectionTimeout(options, ONE_MINUTE);
        builder.setSoTimeout(options, ONE_MINUTE);

        // ensure changes are visible
        assertEquals(60000, builder.getConnectionTimeout(options));
        assertEquals(ONE_MINUTE, builder.getConnectionTimeoutDuration(options));
        assertEquals(60000, builder.getSoTimeout(options));
        assertEquals(ONE_MINUTE, builder.getSoTimeoutDuration(options));
        assertEquals("foo/bar", builder.getUserAgent(options));

        // TODO: should also check the created HTTPClient
    }

    private void testResolveFolderSlash(final String uri, final boolean followRedirect) throws FileSystemException {
        VFS.getManager().getFilesCache().close();
        final FileSystemOptions opts = new FileSystemOptions();
        HttpFileSystemConfigBuilder.getInstance().setFollowRedirect(opts, followRedirect);
        try (FileObject file = VFS.getManager().resolveFile(uri, opts)) {
            checkReadTestsFolder(file);
        } catch (final FileNotFolderException e) {
            // Expected: VFS HTTP does not support listing children yet.
        }
    }

    @Test
    public void testResolveFolderSlashNoRedirectOff() throws FileSystemException {
        testResolveFolderSlash(connectionUri + "/read-tests", false);
    }

    @Test
    public void testResolveFolderSlashNoRedirectOn() throws FileSystemException {
        testResolveFolderSlash(connectionUri + "/read-tests", true);
    }

    @Test
    public void testResolveFolderSlashYesRedirectOff() throws FileSystemException {
        testResolveFolderSlash(connectionUri + "/read-tests/", false);
    }

    @Test
    public void testResolveFolderSlashYesRedirectOn() throws FileSystemException {
        testResolveFolderSlash(connectionUri + "/read-tests/", true);
    }

    @Test
    public void testResolveIPv6Url() throws FileSystemException {
        final String ipv6Url = "http://[fe80::1c42:dae:8370:aea6%en1]/file.txt";

        @SuppressWarnings("rawtypes")
        final FileObject fileObject = VFS.getManager().resolveFile(ipv6Url, new FileSystemOptions());

        assertEquals("http://[fe80::1c42:dae:8370:aea6%en1]/", fileObject.getFileSystem().getRootURI());
        assertEquals("http://[fe80::1c42:dae:8370:aea6%en1]/file.txt", fileObject.getName().getURI());
    }

    /**
     * Configuration for HTTP provider tests.
     */
    private static class HttpProviderTestConfig extends AbstractProviderTestConfig {

        /**
         * Returns the base folder for tests.
         */
        @Override
        public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
            String uri = getSystemTestUriOverride();
            if (uri == null) {
                uri = connectionUri;
            }
            return manager.resolveFile(uri);
        }

        /**
         * Prepares the file system manager.
         */
        @Override
        public void prepare(final DefaultFileSystemManager manager) throws Exception {
            manager.addProvider("http", new HttpFileProvider());
        }
    }
}

