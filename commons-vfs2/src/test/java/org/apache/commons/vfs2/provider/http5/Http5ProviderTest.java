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
package org.apache.commons.vfs2.provider.http5;

import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectory;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
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
import org.apache.commons.vfs2.cache.SoftRefFilesCache;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.util.NHttpFileServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * JUnit 5 test cases for the HTTP5 provider.
 * <p>
 * This class replaces {@link Http5ProviderTestCase} with a pure JUnit 5 implementation.
 * </p>
 */
public class Http5ProviderTest extends ProviderTestSuiteJunit5 {

    private static final Duration ONE_MINUTE = Duration.ofMinutes(1);
    private static NHttpFileServer server;
    private static final String TEST_URI = "test.http.uri";
    private static String connectionUri;

    public Http5ProviderTest() throws Exception {
        super(new Http5ProviderTestConfig(), "", false);
    }

    private static String getSystemTestUriOverride() {
        return System.getProperty(TEST_URI);
    }

    @Override
    protected void setUp() throws Exception {
        if (getSystemTestUriOverride() == null) {
            server = NHttpFileServer.start(0, new File(getTestDirectory()), 5000);
            connectionUri = AbstractProviderTestConfig.getLocalHostUriString("http5", server.getPort());
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
        // HTTP5-specific tests are now part of this test suite
    }

    // ==================== HTTP5-Specific Tests ====================

    private void checkReadTestsFolder(final FileObject file) throws FileSystemException {
        Assertions.assertNotNull(file.getChildren());
        Assertions.assertTrue(file.getChildren().length > 0);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testHttpTimeoutConfig() {
        final FileSystemOptions opts = new FileSystemOptions();
        final Http5FileSystemConfigBuilder builder = Http5FileSystemConfigBuilder.getInstance();

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
        Http5FileSystemConfigBuilder.getInstance().setFollowRedirect(opts, followRedirect);
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
        final String ipv6Url = "http5://[fe80::1c42:dae:8370:aea6%en1]";

        @SuppressWarnings("rawtypes")
        final Http5FileObject fileObject = (Http5FileObject)
                VFS.getManager().resolveFile(ipv6Url, new FileSystemOptions());

        assertEquals("http5://[fe80::1c42:dae:8370:aea6%en1]/", fileObject.getFileSystem().getRootURI());
        assertEquals("http5://[fe80::1c42:dae:8370:aea6%en1]/", fileObject.getName().getURI());
    }

    @Test
    public void testReadFileOperations() throws Exception {
        try (DefaultFileSystemManager manager = new DefaultFileSystemManager();
                Http5FileProvider provider = new Http5FileProvider();
                SoftRefFilesCache filesCache = new SoftRefFilesCache();) {
            manager.setFilesCache(filesCache);
            manager.addProvider("http5", provider);
            manager.init();
            try (FileObject fo = manager.resolveFile(connectionUri + "/read-tests/file1.txt")) {
                Assertions.assertNotNull(fo.getContent().getInputStream());
            }
        }
    }

    /**
     * Configuration for HTTP5 provider tests.
     */
    private static class Http5ProviderTestConfig extends AbstractProviderTestConfig {

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
            if (!manager.hasProvider("http5")) {
                manager.addProvider("http5", new Http5FileProvider());
            }
        }

        private FileSystemOptions getFileSystemOptions() {
            final FileSystemOptions opts = new FileSystemOptions();
            final Http5FileSystemConfigBuilder builder = Http5FileSystemConfigBuilder.getInstance();
            builder.setMaxTotalConnections(opts, 200);
            builder.setMaxConnectionsPerHost(opts, 200);
            return opts;
        }
    }
}

