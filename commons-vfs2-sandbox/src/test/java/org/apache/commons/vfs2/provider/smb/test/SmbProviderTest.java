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
package org.apache.commons.vfs2.provider.smb.test;

import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.junit.jupiter.api.TestInstance;

/**
 * JUnit 5 test suite for SMB provider.
 * <p>
 * This test requires a test.smb.uri system property to be set.
 * </p>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SmbProviderTest extends ProviderTestSuiteJunit5 {

    private static final String TEST_URI = "test.smb.uri";

    public SmbProviderTest() throws Exception {
        super(new SmbProviderTestCase(), "", true);
    }

    @Override
    protected void setUp() throws Exception {
        // Only run if test URI is provided
        if (System.getProperty(TEST_URI) == null) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "SMB test URI not provided. Set -Dtest.smb.uri=<uri>");
        }
        super.setUp();
    }
}

