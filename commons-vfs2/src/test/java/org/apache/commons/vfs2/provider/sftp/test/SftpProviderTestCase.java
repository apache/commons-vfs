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
package org.apache.commons.vfs2.provider.sftp.test;

import org.apache.commons.vfs2.test.PermissionsTests;

import junit.framework.Test;

public class SftpProviderTestCase extends AbstractSftpProviderTestCase {

    @Override
    protected boolean isExecChannelClosed() {
        return false;
    }

    /**
     * Creates the test suite for the sftp file system.
     */
    public static Test suite() throws Exception {
        final SftpProviderTestSuite suite = new SftpProviderTestSuite(new SftpProviderTestCase());
        // VFS-405: set/get permissions
        suite.addTests(PermissionsTests.class);
        suite.addTests(SftpMultiThreadWriteTests.class);
        return suite;
    }
}
