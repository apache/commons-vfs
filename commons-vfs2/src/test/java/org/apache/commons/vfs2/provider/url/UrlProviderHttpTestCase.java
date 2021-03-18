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
package org.apache.commons.vfs2.provider.url;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.ProviderTestSuite;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.util.NHttpFileServer;

import junit.framework.Test;

/**
 * Test cases for HTTP with the default provider.
 *
 */
public class UrlProviderHttpTestCase extends AbstractProviderTestConfig {
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
     * Creates and starts an embedded Apache HTTP Server ().
     *
     * @throws Exception
     */
    private static void setUpClass() throws Exception {
        Server = NHttpFileServer.start(0, new File(getTestDirectory()), 5000);
        SocketPort = Server.getPort();
        ConnectionUri = "http://localhost:" + SocketPort;
    }

    public static Test suite() throws Exception {
        return new ProviderTestSuite(new UrlProviderHttpTestCase()) {
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
     * Stops the embedded Apache HTTP Server ().
     * @throws InterruptedException
     */
    public static void tearDownClass() throws InterruptedException {
        if (Server != null) {
            Server.shutdown(5000, TimeUnit.SECONDS);
        }
    }

    public UrlProviderHttpTestCase() throws IOException {
        // empty
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

    /**
     * Prepares the file system manager.
     */
    @Override
    public void prepare(final DefaultFileSystemManager manager) throws Exception {
        manager.addProvider("http", new UrlFileProvider());
    }
}
