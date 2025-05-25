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
package org.apache.commons.vfs2.provider.ftp;

import junit.framework.Test;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.IPv6LocalConnectionTests;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.mockito.Mockito;

public class FtpProviderIPv6TestCase extends FtpProviderTestCase {

    private static class MockedClientFtpFileProvider extends FtpFileProvider {
        @Override
        protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions) {
            final GenericFileName rootName = (GenericFileName) name;
            return new FtpFileSystem(rootName, Mockito.mock(FtpClient.class), fileSystemOptions);
        }
    }

    public static Test suite() throws Exception {
        return getSystemTestUriOverride() == null ?
                suite(new FtpProviderIPv6TestCase(), FtpProviderIPv6TestCase.class, IPv6LocalConnectionTests.class) :
                suite(new FtpProviderIPv6TestCase(), FtpProviderIPv6TestCase.class);
    }

    @Override
    public void prepare(final DefaultFileSystemManager manager) throws Exception {
        manager.addProvider("ftp", new MockedClientFtpFileProvider());
    }

    @org.junit.Test
    public void testResolveIPv6Url() throws Exception {
        final String ipv6Url = "ftp://[fe80::1c42:dae:8370:aea6%en1]/file.txt";
        final FtpFileObject fileObject = (FtpFileObject) getManager().resolveFile(ipv6Url, new FileSystemOptions());
        assertEquals("ftp://[fe80::1c42:dae:8370:aea6%en1]/", fileObject.getFileSystem().getRootURI());
        assertEquals("file.txt", fileObject.getRelPath());
    }
}
