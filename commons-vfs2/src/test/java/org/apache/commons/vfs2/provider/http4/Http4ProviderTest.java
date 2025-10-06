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

import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.util.NHttpFileServer;
import org.junit.jupiter.api.AfterAll;

/**
 * JUnit 5 test cases for the HTTP4 provider.
 * <p>
 * This class replaces {@link Http4ProviderTestCase} with a pure JUnit 5 implementation.
 * </p>
 */
public class Http4ProviderTest extends ProviderTestSuiteJunit5 {

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

