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
package org.apache.commons.vfs2.provider.webdav4.test;

import org.apache.commons.vfs2.IPv6LocalConnectionTests;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.webdav4.Webdav4FileProvider;
import org.junit.jupiter.api.TestInstance;

/**
 * JUnit 5 test suite for WebDAV4 provider.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Webdav4ProviderTest extends Webdav4ProviderTestSuite {

    public Webdav4ProviderTest() throws Exception {
        super(new Webdav4ProviderTestCase(), "", false);
    }

    @Override
    protected void addBaseTests() throws Exception {
        super.addBaseTests();
        addTests(Webdav4ProviderTestCase.class);

        if (Webdav4ProviderTestCase.getSystemTestUriOverride() == null) {
            addTests(IPv6LocalConnectionTests.class);
        }
    }

    @Override
    protected void setUp() throws Exception {
        if (Webdav4ProviderTestCase.getSystemTestUriOverride() == null) {
            Webdav4ProviderTestCase.setUpClass();
        }
        try {
            // Since webdav4 is not registered in the standard file system configuration yet,
            // it must be registered manually here. Otherwise, HostFileNameParser#extractToPath() fails.
            final DefaultFileSystemManager manager = (DefaultFileSystemManager) VFS.getManager();
            if (!manager.hasProvider("webdav4")) {
                manager.addProvider("webdav4", new Webdav4FileProvider());
            }
            super.setUp();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        Webdav4ProviderTestCase.tearDownClass();
        super.tearDown();
    }
}

