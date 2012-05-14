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
package org.apache.commons.vfs2.provider.http.test;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.http.HttpFileProvider;
import org.apache.commons.vfs2.test.AbstractProviderTestConfig;
import org.apache.commons.vfs2.test.ProviderTestSuite;
import org.apache.commons.vfs2.util.FreeSocketPortUtil;
import org.apache.commons.vfs2.util.NHttpServer;

/**
 * Test cases for the HTTP provider.
 * 
 */
public class HttpProviderTestCase extends AbstractProviderTestConfig
{
    private static NHttpServer Server;

    private static int SocketPort;

    private static final String TEST_URI = "test.http.uri";

    /**
     * Use %40 for @ in URLs
     */
    private static String ConnectionUri;

    private static String getSystemTestUriOverride()
    {
        return System.getProperty(TEST_URI);
    }

    /**
     * Creates and starts an embedded Apache HTTP Server (HttpComponents).
     * 
     * @throws Exception
     */
    private static void setUpClass() throws Exception
    {
        Server = new NHttpServer();
        if (!Server.run(SocketPort, new File(getTestDirectory()), 5000))
        {
            throw new IllegalStateException("The embedded HTTP server has not completed startup, increase wait time");
        }
    }

    /**
     * Creates a new test suite.
     * 
     * @return a new test suite.
     * @throws Exception
     *             Thrown when the suite cannot be constructed.
     */
    public static Test suite() throws Exception
    {
        return new ProviderTestSuite(new HttpProviderTestCase())
        {
            @Override
            protected void setUp() throws Exception
            {
                if (getSystemTestUriOverride() == null)
                {
                    setUpClass();
                }
                super.setUp();
            }

            @Override
            protected void tearDown() throws Exception
            {
                tearDownClass();
                super.tearDown();
            }
        };
    }

    /**
     * Stops the embedded Apache HTTP Server.
     * 
     * @throws IOException
     */
    private static void tearDownClass() throws IOException
    {
        if (Server != null)
        {
            Server.stop();
        }
    }

    /**
     * Builds a new test case.
     * 
     * @throws IOException
     *             Thrown if a free local socket port cannot be found.
     */
    public HttpProviderTestCase() throws IOException
    {
        SocketPort = FreeSocketPortUtil.findFreeLocalPort();
        // Use %40 for @ in a URL
        ConnectionUri = "http://localhost:" + SocketPort;
    }

    /**
     * Returns the base folder for tests.
     */
    @Override
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception
    {
        String uri = getSystemTestUriOverride();
        if (uri == null)
        {
            uri = ConnectionUri;
        }
        return manager.resolveFile(uri);
    }

    /**
     * Prepares the file system manager.
     */
    @Override
    public void prepare(final DefaultFileSystemManager manager) throws Exception
    {
        manager.addProvider("http", new HttpFileProvider());
    }
}
