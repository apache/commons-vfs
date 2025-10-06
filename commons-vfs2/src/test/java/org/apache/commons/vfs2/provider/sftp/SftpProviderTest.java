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
package org.apache.commons.vfs2.provider.sftp;

import org.apache.commons.vfs2.PermissionsTests;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.apache.sshd.common.session.AbstractSession;
import org.junit.jupiter.api.AfterAll;

/**
 * JUnit 5 tests for the SFTP provider.
 * <p>
 * This class replaces {@link SftpProviderTestCase} with a pure JUnit 5 implementation.
 * </p>
 */
public class SftpProviderTest extends ProviderTestSuiteJunit5 {

    private final boolean isExecChannelClosed = false;

    public SftpProviderTest() throws Exception {
        super(new SftpProviderTestConfig(), "", false);
    }

    @Override
    protected void setUp() throws Exception {
        if (AbstractSftpProviderTestCase.getSystemTestUriOverride() == null) {
            AbstractSftpProviderTestCase.setUpClass(isExecChannelClosed, sessionFactory());
        }
        super.setUp();
    }

    @AfterAll
    public static void tearDownClass() throws InterruptedException {
        // Close all active sessions
        if (AbstractSftpProviderTestCase.server != null) {
            for (final AbstractSession session : AbstractSftpProviderTestCase.server.getActiveSessions()) {
                session.close(true);
            }
            AbstractSftpProviderTestCase.tearDownClass();
        }
    }

    @Override
    protected void addBaseTests() throws Exception {
        super.addBaseTests();
        addTests(PermissionsTests.class);
        addTests(SftpMultiThreadWriteTests.class);
    }

    protected org.apache.sshd.server.session.SessionFactory sessionFactory() {
        return null;
    }

    /**
     * Configuration for SFTP provider tests.
     */
    private static class SftpProviderTestConfig extends AbstractSftpProviderTestCase {

        @Override
        protected boolean isExecChannelClosed() {
            return false;
        }
    }
}

