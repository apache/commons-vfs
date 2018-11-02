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
package org.apache.commons.vfs2.provider.http4s.test;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.http4.Http4FileProvider;
import org.apache.commons.vfs2.provider.http4.Http4FileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.http4s.Http4sFileProvider;
import org.junit.Assert;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * Tests VFS-427 NPE on HttpFileObject.getContent().getContentInfo()
 */
public class Http4sGetContentInfoTest extends TestCase {

    // TODO: VFS-360 - Remove this manual registration of http4 once http4 becomes part of standard providers.
    @Override
    protected void setUp() throws Exception {
        final DefaultFileSystemManager manager = (DefaultFileSystemManager) VFS.getManager();
        if (!manager.hasProvider("http4")) {
            manager.addProvider("http4", new Http4FileProvider());
        }
        if (!manager.hasProvider("http4s")) {
            manager.addProvider("http4s", new Http4sFileProvider());
        }
    }

    /**
     * Tests VFS-427 NPE on HttpFileObject.getContent().getContentInfo().
     *
     * @throws FileSystemException thrown when the getContentInfo API fails.
     * @throws MalformedURLException thrown when the System environment contains an invalid URL for an HTTPS proxy.
     */
    @Test
    public void testGetContentInfo() throws FileSystemException, MalformedURLException {
        String httpsProxyHost = null;
        int httpsProxyPort = -1;
        final String httpsProxy = System.getenv("https_proxy");
        if (httpsProxy != null) {
            final URL url = new URL(httpsProxy);
            httpsProxyHost = url.getHost();
            httpsProxyPort = url.getPort();
        }
        final FileSystemOptions opts;
        if (httpsProxyHost != null) {
            opts = new FileSystemOptions();
            final Http4FileSystemConfigBuilder builder = Http4FileSystemConfigBuilder.getInstance();
            builder.setProxyHost(opts, httpsProxyHost);
            if (httpsProxyPort >= 0) {
                builder.setProxyPort(opts, httpsProxyPort);
            }
        } else {
            opts = null;
        }

        final FileSystemManager fsManager = VFS.getManager();
        final FileObject fo = fsManager.resolveFile("http4://www.apache.org/licenses/LICENSE-2.0.txt", opts);
        final FileContent content = fo.getContent();
        Assert.assertNotNull(content);
        // Used to NPE before fix:
        content.getContentInfo();
    }
}
