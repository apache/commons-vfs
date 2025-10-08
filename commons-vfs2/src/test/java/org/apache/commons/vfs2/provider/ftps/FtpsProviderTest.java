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

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.apache.ftpserver.ftplet.FtpException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.opentest4j.TestAbortedException;

/**
 * Tests for FTPS file systems with both explicit and implicit FTPS connections (JUnit 5).
 * Uses @Nested classes to organize tests by connection mode while sharing server lifecycle.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FtpsProviderTest {

    /**
     * Tests for FTPS with explicit SSL/TLS mode.
     * In explicit mode, the client connects to the standard FTP port and then
     * explicitly requests security via the AUTH command.
     */
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ExplicitMode extends ProviderTestSuiteJunit5 {

        public ExplicitMode() throws Exception {
            super(new FtpsProviderExplicitTestCase(), "", false);
        }

        @BeforeAll
        void setUpServer() throws Exception {
            if (FtpsProviderExplicitTestCase.getSystemTestUriOverride() == null) {
                try {
                    FtpsProviderExplicitTestCase.setUpClass(false); // explicit mode
                } catch (final FtpException e) {
                    // Server failed to start - abort all tests in this class
                    throw new TestAbortedException("FTP server failed to start: " + e.getMessage(), e);
                }
            }
        }

        @Override
        protected void setUp() throws Exception {
            try {
                super.setUp();
            } catch (final FileSystemException e) {
                // Could not connect to FTP server - abort test
                throw new TestAbortedException("Could not connect to FTP server: " + e.getMessage(), e);
            }
        }

        @AfterAll
        void tearDownServer() throws Exception {
            try {
                tearDown();
            } finally {
                FtpsProviderExplicitTestCase.tearDownClass();
            }
        }
    }

    /**
     * Tests for FTPS with implicit SSL/TLS mode.
     * In implicit mode, the SSL/TLS connection is established immediately
     * upon connection, without an explicit AUTH command.
     * 
     * Note: Implicit mode is not standardized and the protocol may differ
     * between FTPS servers. This mode is deprecated in favor of explicit mode.
     */
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ImplicitMode extends ProviderTestSuiteJunit5 {

        public ImplicitMode() throws Exception {
            super(new FtpsProviderImplicitTestCase(), "", false);
        }

        @BeforeAll
        void setUpServer() throws Exception {
            if (FtpsProviderImplicitTestCase.getSystemTestUriOverride() == null) {
                try {
                    FtpsProviderImplicitTestCase.setUpClass(true); // implicit mode
                } catch (final FtpException e) {
                    // Server failed to start - abort all tests in this class
                    throw new TestAbortedException("FTP server failed to start: " + e.getMessage(), e);
                }
            }
        }

        @Override
        protected void setUp() throws Exception {
            try {
                super.setUp();
            } catch (final FileSystemException e) {
                // Could not connect to FTP server - abort test
                throw new TestAbortedException("Could not connect to FTP server: " + e.getMessage(), e);
            }
        }

        @AfterAll
        void tearDownServer() throws Exception {
            try {
                tearDown();
            } finally {
                FtpsProviderImplicitTestCase.tearDownClass();
            }
        }
    }
}

