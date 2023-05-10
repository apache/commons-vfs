/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.provider.http;

import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectory;

import java.io.File;
import java.time.Duration;

import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileNotFolderException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.ProviderTestSuite;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.util.NHttpFileServer;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Test cases for the HTTP provider.
 */
public class HttpProviderTestCase extends AbstractProviderTestConfig {

    private static final Duration ONE_MINUTE = Duration.ofMinutes(1);

    private static NHttpFileServer Server;

    private static int SocketPort;

    private static final String TEST_URI = "test.http.uri";

    /**
     * Use %40 for @ in URLs
     */
    private static String ConnectionUri;

    private static String getSystemTestUriOverride() {
        return System.getProperty(TEST_URI);
    }

    /**
     * Creates and starts an embedded Apache HTTP Server (HttpComponents).
     *
     * @throws Exception
     */
    private static void setUpClass() throws Exception {
        Server = NHttpFileServer.start(0, new File(getTestDirectory()), 5000);
        SocketPort = Server.getPort();
        ConnectionUri = "http://localhost:" + SocketPort;
    }

    /**
     * Creates a new test suite.
     *
     * @return a new test suite.
     * @throws Exception Thrown when the suite cannot be constructed.
     */
    public static junit.framework.Test suite() throws Exception {
        return new ProviderTestSuite(new HttpProviderTestCase()) {
            /**
             * Adds base tests - excludes the nested test cases.
             */
            @Override
            protected void addBaseTests() throws Exception {
                super.addBaseTests();
                addTests(HttpProviderTestCase.class);
            }

            @Override
            protected void setUp() throws Exception {
                if (getSystemTestUriOverride() == null) {
                    setUpClass();
                }
                super.setUp();
            }

            @Override
            protected void tearDown() throws Exception {
                tearDownClass();
                super.tearDown();
            }
        };
    }

    /**
     * Stops the embedded Apache HTTP Server.
     */
    private static void tearDownClass() {
        if (Server != null) {
            Server.close();
        }
    }

    private void checkReadTestsFolder(final FileObject file) throws FileSystemException {
        Assertions.assertNotNull(file.getChildren());
        Assertions.assertTrue(file.getChildren().length > 0);
    }

    /**
     * Returns the base folder for tests.
     */
    @Override
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
        String uri = getSystemTestUriOverride();
        if (uri == null) {
            uri = ConnectionUri;
        }
        return manager.resolveFile(uri);
    }

    // Test no longer passing 2016/04/28
    public void ignoreTestHttp405() throws FileSystemException {
        try (FileObject fileObject = VFS.getManager()
                .resolveFile("http://www.w3schools.com/webservices/tempconvert.asmx?action=WSDL")) {
            assert fileObject.getContent().getSize() > 0;
            assert !fileObject.getContent().isEmpty();

        }
    }

    /**
     * Prepares the file system manager.
     */
    @Override
    public void prepare(final DefaultFileSystemManager manager) throws Exception {
        manager.addProvider("http", new HttpFileProvider());
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
        testResolveFolderSlash(ConnectionUri + "/read-tests", false);
    }

    @Test
    public void testResolveFolderSlashNoRedirectOn() throws FileSystemException {
        testResolveFolderSlash(ConnectionUri + "/read-tests", true);
    }

    @Test
    public void testResolveFolderSlashYesRedirectOff() throws FileSystemException {
        testResolveFolderSlash(ConnectionUri + "/read-tests/", false);
    }

    @Test
    public void testResolveFolderSlashYesRedirectOn() throws FileSystemException {
        testResolveFolderSlash(ConnectionUri + "/read-tests/", true);
    }

}
