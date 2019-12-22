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

import java.net.URI;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystem;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpStreamProxy;
import org.apache.commons.vfs2.provider.sftp.TrustEveryoneUserInfo;
import org.apache.commons.vfs2.test.PermissionsTests;
import org.apache.commons.vfs2.test.ProviderReadTests;

import com.jcraft.jsch.TestIdentityRepositoryFactory;

import junit.framework.Test;

public class SftpProviderStreamProxyModeTestCase extends AbstractSftpProviderTestCase {
    @Override
    protected boolean isExecChannelClosed() {
        return false;
    }

    // --- VFS-440: stream proxy test suite
    // We override the addBaseTests method so that only
    // one test is run (we just test that the input/output are correctly forwarded, and
    // hence if the reading test succeeds/fails the other will also succeed/fail)
    public static Test suite() throws Exception {
        final SftpProviderTestSuite suite = new SftpProviderTestSuite(new SftpProviderStreamProxyModeTestCase()) {
            @Override
            protected void addBaseTests() throws Exception {
                // Just tries to read
                addTests(ProviderReadTests.class);
                // VFS-405: set/get permissions
                addTests(PermissionsTests.class);
            }
        };
        return suite;
    }

    @Override
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
        String uri = getSystemTestUriOverride();
        if (uri == null) {
            uri = ConnectionUri;
        }

        final FileSystemOptions fileSystemOptions = new FileSystemOptions();
        final SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();
        builder.setStrictHostKeyChecking(fileSystemOptions, "no");
        builder.setUserInfo(fileSystemOptions, new TrustEveryoneUserInfo());
        builder.setIdentityRepositoryFactory(fileSystemOptions, new TestIdentityRepositoryFactory());

        final FileSystemOptions proxyOptions = (FileSystemOptions) fileSystemOptions.clone();

        final URI parsedURI = new URI(uri);
        final String userInfo = parsedURI.getUserInfo();
        final String[] userFields = userInfo == null ? null : userInfo.split(":", 2);

        builder.setProxyType(fileSystemOptions, SftpFileSystemConfigBuilder.PROXY_STREAM);
        if (userFields != null) {
            if (userFields.length > 0) {
                builder.setProxyUser(fileSystemOptions, userFields[0]);
            }
            if (userFields.length > 1) {
                builder.setProxyPassword(fileSystemOptions, userFields[1]);
            }
        }
        builder.setProxyHost(fileSystemOptions, parsedURI.getHost());
        builder.setProxyPort(fileSystemOptions, parsedURI.getPort());
        builder.setProxyCommand(fileSystemOptions, SftpStreamProxy.NETCAT_COMMAND);
        builder.setProxyOptions(fileSystemOptions, proxyOptions);
        builder.setProxyPassword(fileSystemOptions, parsedURI.getAuthority());

        // Set up the new URI
        if (userInfo == null) {
            uri = String.format("sftp://localhost:%d", parsedURI.getPort());
        } else {
            uri = String.format("sftp://%s@localhost:%d", userInfo, parsedURI.getPort());
        }


        final FileObject fileObject = manager.resolveFile(uri, fileSystemOptions);
        this.fileSystem = (SftpFileSystem) fileObject.getFileSystem();
        return fileObject;
    }
}
