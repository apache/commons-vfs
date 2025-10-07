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
package org.apache.commons.vfs2.provider.webdav.test;

import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.junit.jupiter.api.TestInstance;

/**
 * JUnit 5 test suite for WebDAV provider.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WebdavProviderTest extends ProviderTestSuiteJunit5 {

    public WebdavProviderTest() throws Exception {
        super(new WebdavProviderTestCase(), "", true);
    }

    @Override
    protected void addBaseTests() throws Exception {
        super.addBaseTests();
        addTests(WebdavProviderTestCase.class);
        // WebDAV underlying implementation doesn't support link-local IPv6 url (but WebDAV4 does)
        // if (getSystemTestUriOverride() == null) {
        //    addTests(IPv6LocalConnectionTests.class);
        // }
    }

    @Override
    protected void setUp() throws Exception {
        if (WebdavProviderTestCase.getSystemTestUriOverride() == null) {
            WebdavProviderTestCase.setUpClass();
        }
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        WebdavProviderTestCase.tearDownClass();
        super.tearDown();
    }
}

