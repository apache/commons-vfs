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
package org.apache.commons.vfs2.provider.url;

import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectory;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.util.NHttpFileServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.TestInstance;

/**
 * JUnit 5 test cases for HTTP with the default URL provider.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UrlProviderHttpTest extends ProviderTestSuiteJunit5 {

    private static NHttpFileServer server;
    private static String connectionUri;
    private static final String TEST_URI = "test.http.uri";

    public UrlProviderHttpTest() throws Exception {
        super(new UrlProviderHttpTestConfig(), "", false);
    }

    protected static String getSystemTestUriOverride() {
        return System.getProperty(TEST_URI);
    }

    @Override
    protected void setUp() throws Exception {
        if (getSystemTestUriOverride() == null) {
            setUpClass();
        }
        super.setUp();
    }

    @AfterAll
    protected void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            tearDownClass();
        }
    }

    /**
     * Starts the embedded Apache HTTP Server.
     */
    private static void setUpClass() throws Exception {
        server = NHttpFileServer.start(0, new File(getTestDirectory()), 5000);
        connectionUri = AbstractProviderTestConfig.getLocalHostUriString("http", server.getPort());
    }

    /**
     * Stops the embedded Apache HTTP Server.
     */
    private static void tearDownClass() throws InterruptedException {
        if (server != null) {
            server.shutdown(5000, TimeUnit.SECONDS);
            server = null;
        }
    }

    /**
     * Configuration for URL provider HTTP tests.
     */
    private static class UrlProviderHttpTestConfig extends AbstractProviderTestConfig {

        @Override
        public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
            String uri = getSystemTestUriOverride();
            if (uri == null) {
                uri = connectionUri;
            }
            return manager.resolveFile(uri);
        }

        @Override
        public void prepare(final DefaultFileSystemManager manager) throws Exception {
            manager.addProvider("http", new UrlFileProvider());
        }
    }
}

