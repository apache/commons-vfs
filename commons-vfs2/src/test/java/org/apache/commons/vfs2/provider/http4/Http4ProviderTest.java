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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * JUnit 5 test cases for the HTTP4 provider.
 * <p>
 * This class replaces {@link Http4ProviderTestCase} with a pure JUnit 5 implementation.
 * </p>
 */
public class Http4ProviderTest extends ProviderTestSuiteJunit5 {

    private static final Duration ONE_MINUTE = Duration.ofMinutes(1);
    private static NHttpFileServer server;
    private static final String TEST_URI = "test.http.uri";
    private static String connectionUri;

    public Http4ProviderTest() throws Exception {
        super(new Http4ProviderTestConfig(), "", false);
    }

    private static String getSystemTestUriOverride() {
        return System.getProperty(TEST_URI);
    }

    @Override
    protected void setUp() throws Exception {
        if (getSystemTestUriOverride() == null) {
            server = NHttpFileServer.start(0, new File(getTestDirectory()), 5000);
            connectionUri = AbstractProviderTestConfig.getLocalHostUriString("http4", server.getPort());
        }
        super.setUp();
    }

    @AfterAll
    public static void tearDownClass() throws InterruptedException {
        if (server != null) {
            server.shutdown(5000, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void addBaseTests() throws Exception {
        super.addBaseTests();
        // HTTP4-specific tests are now part of this test suite
    }

    // ==================== HTTP4-Specific Tests ====================

    private void checkReadTestsFolder(final FileObject file) throws FileSystemException {
        Assertions.assertNotNull(file.getChildren());
        Assertions.assertTrue(file.getChildren().length > 0);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testHttpTimeoutConfig() {
        final FileSystemOptions opts = new FileSystemOptions();
        final Http4FileSystemConfigBuilder builder = Http4FileSystemConfigBuilder.getInstance();

        // ensure defaults are 0
        assertEquals(0, builder.getConnectionTimeout(opts));
        assertEquals(0, builder.getConnectionTimeoutDuration(opts).toMillis());
        assertEquals(0, builder.getSoTimeout(opts));
        assertEquals("Jakarta-Commons-VFS", builder.getUserAgent(opts));

        // Set with deprecated milliseconds APIs.
        builder.setConnectionTimeout(opts, 60000);
        builder.setSoTimeout(opts, 60000);
        builder.setUserAgent(opts, "foo/bar");

        // ensure changes are visible
        assertEquals(60000, builder.getConnectionTimeout(opts));
        assertEquals(ONE_MINUTE, builder.getConnectionTimeoutDuration(opts));
        assertEquals(60000, builder.getSoTimeout(opts));
        assertEquals("foo/bar", builder.getUserAgent(opts));

        // Set with Duration APIs.
        builder.setConnectionTimeout(opts, ONE_MINUTE);
        builder.setSoTimeout(opts, ONE_MINUTE);

        // ensure changes are visible
        assertEquals(60000, builder.getConnectionTimeout(opts));
        assertEquals(ONE_MINUTE, builder.getConnectionTimeoutDuration(opts));
        assertEquals(60000, builder.getSoTimeout(opts));
        assertEquals(ONE_MINUTE, builder.getSoTimeoutDuration(opts));
        assertEquals("foo/bar", builder.getUserAgent(opts));
    }

    private void testResolveFolderSlash(final String uri, final boolean followRedirect) throws FileSystemException {
        VFS.getManager().getFilesCache().close();
        final FileSystemOptions opts = new FileSystemOptions();
        Http4FileSystemConfigBuilder.getInstance().setFollowRedirect(opts, followRedirect);
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
        final String ipv6Url = "http4://[fe80::1c42:dae:8370:aea6%en1]";

        @SuppressWarnings("rawtypes")
        final Http4FileObject fileObject = (Http4FileObject)
                VFS.getManager().resolveFile(ipv6Url, new FileSystemOptions());

        assertEquals("http4://[fe80::1c42:dae:8370:aea6%en1]/", fileObject.getFileSystem().getRootURI());
        assertEquals("http4://[fe80::1c42:dae:8370:aea6%en1]/", fileObject.getName().getURI());
    }

    /**
     * Configuration for HTTP4 provider tests.
     */
    private static class Http4ProviderTestConfig extends AbstractProviderTestConfig {

        @Override
        public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
            String uri = getSystemTestUriOverride();
            if (uri == null) {
                uri = connectionUri;
            }
            return manager.resolveFile(uri, getFileSystemOptions());
        }

        @Override
        public void prepare(final DefaultFileSystemManager manager) throws Exception {
            if (!manager.hasProvider("http4")) {
                manager.addProvider("http4", new Http4FileProvider());
            }
        }

        private FileSystemOptions getFileSystemOptions() {
            final FileSystemOptions opts = new FileSystemOptions();
            final Http4FileSystemConfigBuilder builder = Http4FileSystemConfigBuilder.getInstance();
            builder.setMaxTotalConnections(opts, 200);
            builder.setMaxConnectionsPerHost(opts, 200);
            return opts;
        }
    }
}

