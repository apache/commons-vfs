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
package org.apache.commons.vfs2.provider.https.test;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.http.HttpFileSystemConfigBuilder;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests VFS-427 NPE on HttpFileObject.getContent().getContentInfo()
 *
 * @since 2.1
 */
public class GetContentInfoFunctionalTest {

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
            final HttpFileSystemConfigBuilder builder = HttpFileSystemConfigBuilder.getInstance();
            builder.setProxyHost(opts, httpsProxyHost);
            if (httpsProxyPort >= 0) {
                builder.setProxyPort(opts, httpsProxyPort);
            }
        } else {
            opts = null;
        }

        final FileSystemManager fsManager = VFS.getManager();
        final FileObject fo = fsManager.resolveFile("http://www.apache.org/licenses/LICENSE-2.0.txt", opts);
        final FileContent content = fo.getContent();
        Assert.assertNotNull(content);
        // Used to NPE before fix:
        content.getContentInfo();
    }
}
