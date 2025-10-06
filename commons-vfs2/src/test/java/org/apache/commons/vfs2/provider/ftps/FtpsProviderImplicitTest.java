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
package org.apache.commons.vfs2.provider.ftps;

import org.apache.commons.vfs2.ProviderTestConfig;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.junit.jupiter.api.TestInstance;

/**
 * Tests for FTPS file systems with implicit FTPS connection (JUnit 5).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FtpsProviderImplicitTest extends ProviderTestSuiteJunit5 {

    public FtpsProviderImplicitTest() throws Exception {
        super(new FtpsProviderImplicitTestCase(), "", false);
    }

    @Override
    protected void setUp() throws Exception {
        if (FtpsProviderImplicitTestCase.getSystemTestUriOverride() == null) {
            FtpsProviderImplicitTestCase.setUpClass(true); // implicit mode
        }
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            FtpsProviderImplicitTestCase.tearDownClass();
        }
    }

    /**
     * Nested config class to reuse existing test configuration.
     */
    private static class FtpsProviderImplicitTestCase extends AbstractFtpsProviderTestCase {
        @Override
        protected boolean isImplicit() {
            return true;
        }

        @Override
        protected void setupOptions(final FtpsFileSystemConfigBuilder builder) {
            super.setupOptions(builder);
            builder.setDataChannelProtectionLevel(fileSystemOptions, FtpsDataChannelProtectionLevel.P);
            builder.setFtpsMode(fileSystemOptions, FtpsMode.IMPLICIT);
        }
    }
}

