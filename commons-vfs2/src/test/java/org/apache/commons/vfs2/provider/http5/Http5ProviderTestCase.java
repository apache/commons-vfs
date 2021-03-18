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
package org.apache.commons.vfs2.provider.http5;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

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
import org.junit.Assert;

import junit.framework.Test;

/**
 * Test cases for the HTTP5 provider.
 *
 */
public class Http5ProviderTestCase extends AbstractProviderTestConfig {

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
        ConnectionUri = "http5://localhost:" + SocketPort;
    }

    /**
     * Creates a new test suite.
     *
     * @return a new test suite.
     * @throws Exception Thrown when the suite cannot be constructed.
     */
    public static Test suite() throws Exception {
        return new ProviderTestSuite(new Http5ProviderTestCase()) {
            /**
             * Adds base tests - excludes the nested test cases.
             */
            @Override
            protected void addBaseTests() throws Exception {
                super.addBaseTests();
                addTests(Http5ProviderTestCase.class);
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
     * @throws InterruptedException
     */
    private static void tearDownClass() throws InterruptedException {
        if (Server != null) {
            Server.shutdown(5000, TimeUnit.SECONDS);
        }
    }

    private void checkReadTestsFolder(final FileObject file) throws FileSystemException {
        Assert.assertNotNull(file.getChildren());
        Assert.assertTrue(file.getChildren().length > 0);
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
        final FileObject fileObject = VFS.getManager()
                .resolveFile("http5://www.w3schools.com/webservices/tempconvert.asmx?action=WSDL");
        assert !fileObject.getContent().isEmpty();
    }

    /**
     * Prepares the file system manager.
     */
    @Override
    public void prepare(final DefaultFileSystemManager manager) throws Exception {
        if (!manager.hasProvider("http5")) {
            manager.addProvider("http5", new Http5FileProvider());
        }
    }

    /** Ensure VFS-453 options are present. */
    @SuppressWarnings("deprecation")
    public void testHttpTimeoutConfig() {
        final FileSystemOptions opts = new FileSystemOptions();
        final Http5FileSystemConfigBuilder builder = Http5FileSystemConfigBuilder.getInstance();

        // ensure defaults are 0
        assertEquals(0, builder.getConnectionTimeout(opts));
        assertEquals(Duration.ZERO, builder.getConnectionTimeoutDuration(opts));
        assertEquals(0, builder.getSoTimeout(opts));
        assertEquals(Duration.ZERO, builder.getSoTimeoutDuration(opts));
        assertEquals("Jakarta-Commons-VFS", builder.getUserAgent(opts));

        // timeout as int
        builder.setConnectionTimeout(opts, 60000);
        builder.setSoTimeout(opts, 60000);
        builder.setUserAgent(opts, "foo/bar");

        // ensure changes are visible
        assertEquals(60000, builder.getConnectionTimeout(opts));
        assertEquals(60000, builder.getSoTimeout(opts));
        assertEquals("foo/bar", builder.getUserAgent(opts));

        // timeout as Duration
        builder.setConnectionTimeout(opts, Duration.ofMinutes(1));
        builder.setSoTimeout(opts, Duration.ofMinutes(1));
        builder.setUserAgent(opts, "foo/bar");

        // ensure changes are visible
        assertEquals(60000, builder.getConnectionTimeoutDuration(opts).toMillis());
        assertEquals(60000, builder.getSoTimeoutDuration(opts).toMillis());
        assertEquals("foo/bar", builder.getUserAgent(opts));
    }

    private void testResloveFolderSlash(final String uri, final boolean followRedirect) throws FileSystemException {
        VFS.getManager().getFilesCache().close();
        final FileSystemOptions opts = new FileSystemOptions();
        Http5FileSystemConfigBuilder.getInstance().setFollowRedirect(opts, followRedirect);
        final FileObject file = VFS.getManager().resolveFile(uri, opts);
        try {
            checkReadTestsFolder(file);
        } catch (final FileNotFolderException e) {
            // Expected: VFS HTTP does not support listing children yet.
        }
    }

    public void testResloveFolderSlashNoRedirectOff() throws FileSystemException {
        testResloveFolderSlash(ConnectionUri + "/read-tests", false);
    }

    public void testResloveFolderSlashNoRedirectOn() throws FileSystemException {
        testResloveFolderSlash(ConnectionUri + "/read-tests", true);
    }

    public void testResloveFolderSlashYesRedirectOff() throws FileSystemException {
        testResloveFolderSlash(ConnectionUri + "/read-tests/", false);
    }

    public void testResloveFolderSlashYesRedirectOn() throws FileSystemException {
        testResloveFolderSlash(ConnectionUri + "/read-tests/", true);
    }
}
