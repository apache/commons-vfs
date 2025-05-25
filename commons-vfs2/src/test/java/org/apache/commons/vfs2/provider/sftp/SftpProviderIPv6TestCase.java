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

import java.io.IOException;

import junit.framework.Test;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.IPv6LocalConnectionTests;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.mockito.Mockito;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SftpProviderIPv6TestCase extends AbstractSftpProviderTestCase {

    private static class MockedClientSftpFileProvider extends SftpFileProvider {
        @Override
        protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions) {
            final GenericFileName rootName = (GenericFileName) name;

            final Session sessionMock = Mockito.mock(Session.class);
            final ChannelExec channelExecMock = Mockito.mock(ChannelExec.class);

            Mockito.when(sessionMock.isConnected()).thenReturn(true);

            try {
                Mockito.when(sessionMock.openChannel(Mockito.anyString())).thenReturn(channelExecMock);
            } catch (final JSchException e) {
                throw new AssertionError("Should never happen", e);
            }

            Mockito.when(channelExecMock.isClosed()).thenReturn(true);

            try {
                Mockito.when(channelExecMock.getInputStream()).thenReturn(new NullInputStream());
            } catch (final IOException e) {
                throw new AssertionError("Should never happen", e);
            }

            return new SftpFileSystem(rootName, sessionMock, fileSystemOptions);
        }
    }

    public static Test suite() throws Exception {
        return new SftpProviderTestSuite(new SftpProviderIPv6TestCase()) {
            @Override
            protected void addBaseTests() throws Exception {
                addTests(SftpProviderIPv6TestCase.class);

                if (getSystemTestUriOverride() == null) {
                    addTests(IPv6LocalConnectionTests.class);
                }
            }
        };
    }

    @Override
    protected boolean isExecChannelClosed() {
        return false;
    }

    @org.junit.Test
    public void testResolveIPv6Url() throws Exception {
        try {
            // We only want to use mocked client for this test, not for the test class initialization
            getManager().removeProvider("sftp");
            getManager().addProvider("sftp", new MockedClientSftpFileProvider());

            final String ipv6Url = "sftp://user:pass@[fe80::1c42:dae:8370:aea6%en1]/file.txt";

            final FileObject fileObject = getManager().resolveFile(ipv6Url, new FileSystemOptions());

            assertEquals("sftp://user:pass@[fe80::1c42:dae:8370:aea6%en1]/", fileObject.getFileSystem().getRootURI());
            assertEquals("sftp://user:pass@[fe80::1c42:dae:8370:aea6%en1]/file.txt", fileObject.getName().getURI());
        } finally {
            getManager().removeProvider("sftp");
            getManager().addProvider("sftp", new SftpFileProvider());
        }
    }
}
