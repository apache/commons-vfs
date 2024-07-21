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
package org.apache.commons.vfs2.provider.smb.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.ProviderTestConfig;
import org.apache.commons.vfs2.ProviderTestSuite;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.smb.SmbFileProvider;
import org.junit.jupiter.api.Assertions;

/**
 * Tests for the SMB file system.
 */
public class SmbProviderTestCase extends AbstractProviderTestConfig implements ProviderTestConfig {

    private static final String TEST_URI = "test.smb.uri";

    public static Test suite() throws Exception {
        if (System.getProperty(TEST_URI) != null) {
            return new ProviderTestSuite(new SmbProviderTestCase());
        }

        // Cannot run IPv6LocalConnectionTests for smb, because there is no end-to-end test
        // infrastructure implemented yet

        return new TestSuite(SmbProviderTestCase.class);
    }

    /**
     * Returns the base folder for tests.
     */
    @Override
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
        final String uri = System.getProperty(TEST_URI);
        return manager.resolveFile(uri);
    }

    /**
     * Prepares the file system manager.
     */
    @Override
    public void prepare(final DefaultFileSystemManager manager) throws Exception {
        manager.addProvider("smb", new SmbFileProvider());
    }

    @org.junit.jupiter.api.Test
    public void testResolveIPv6Url() throws Exception {
        final String ipv6Url = "smb://user:pass@[fe80::1c42:dae:8370:aea6%en1]/share";

        final FileObject fileObject = VFS.getManager().resolveFile(ipv6Url, new FileSystemOptions());

        Assertions.assertEquals(
                "smb://user:pass@[fe80::1c42:dae:8370:aea6%en1]/share/", fileObject.getFileSystem().getRootURI());

        Assertions.assertEquals("smb://user:pass@[fe80::1c42:dae:8370:aea6%en1]/share/", fileObject.getName().getURI());
    }
}
