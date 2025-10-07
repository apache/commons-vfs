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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;






import org.apache.commons.vfs2.ProviderTestConfig;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.apache.ftpserver.ftplet.FtpException;
import org.junit.jupiter.api.TestInstance;
import org.opentest4j.TestAbortedException;

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
            try {
                FtpsProviderImplicitTestCase.setUpClass(true); // implicit mode
            } catch (final FtpException e) {
                // Server failed to start - abort test
                throw new TestAbortedException("FTP server failed to start: " + e.getMessage(), e);
            }
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
}

