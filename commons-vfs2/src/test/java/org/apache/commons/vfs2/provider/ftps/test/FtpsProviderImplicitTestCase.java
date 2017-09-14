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
package org.apache.commons.vfs2.provider.ftps.test;

import junit.framework.Test;

import org.apache.commons.vfs2.provider.ftps.FtpsFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.ftps.FtpsMode;

/**
 * Tests for FTPS file systems with implicit FTPS connection.
 *
 * TODO: Fails for concurrent access. Note, that the implicit mode is not standardized and the protocol may differ
 * between the FTPS servers.
 *
 * @see <a href="http://en.wikipedia.org/wiki/FTPS#Implicit">Wikipedia: FTPS/Implicit</a>
 */
public class FtpsProviderImplicitTestCase extends AbstractFtpsProviderTestCase {
    @Override
    protected boolean isImplicit() {
        return true;
    }

    @Override
    protected void setupOptions(final FtpsFileSystemConfigBuilder builder) {
        super.setupOptions(builder);
        builder.setFtpsMode(fileSystemOptions, FtpsMode.IMPLICIT);
    }

    /**
     * Creates the test suite for the ftps file system.
     */
    public static Test suite() throws Exception {
        return new FtpProviderTestSuite(new FtpsProviderImplicitTestCase());
    }
}
