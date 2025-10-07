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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;

/**
 * JUnit 5 test cases for the HTTP provider.
 * <p>
 * This class replaces {@link HttpProviderTestCase} with a pure JUnit 5 implementation.
 * </p>
 */
@DisplayName("HTTP Provider Tests")
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

