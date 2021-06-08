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
package org.apache.commons.vfs2.provider.http4;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.junit.Assert;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * Tests VFS-427 NPE on Http4FileObject.getContent().getContentInfo().
 */
public class Http4GetContentInfoTest extends TestCase {

    FileSystemOptions getOptionsWithProxy() throws MalformedURLException {
        // get proxy host and port from env var "https_proxy"
        String proxyHost = null;
        int proxyPort = -1;
        final String proxyUrl = System.getenv("https_proxy");
        if (proxyUrl != null) {
            final URL url = new URL(proxyUrl);
            proxyHost = url.getHost();
            proxyPort = url.getPort();
        }

        // return null if proxy host or port invalid
        if (proxyHost == null || proxyPort == -1) {
            return null;
        }

        // return options with proxy
        final Http4FileSystemConfigBuilder builder = Http4FileSystemConfigBuilder.getInstance();
        final FileSystemOptions opts = new FileSystemOptions();
        builder.setProxyHost(opts, proxyHost);
        builder.setProxyPort(opts, proxyPort);
        builder.setProxyScheme(opts, "http");
        return opts;
    }

    private FileSystemOptions getOptionsWithProxyAuthentication() throws MalformedURLException {
        // get proxy host and port from env var "https_proxy"
        String proxyHost = null;
        int proxyPort = -1;
        String[] user = null;
        final String proxyUrl = System.getenv("https_proxy");
        if (proxyUrl != null) {
            final URL url = new URL(proxyUrl);
            proxyHost = url.getHost();
            proxyPort = url.getPort();
            final String userInfo = url.getUserInfo();
            if (userInfo != null) {
                user = userInfo.split(":");

            }
        }

        // return null if proxy host or port invalid
        if (proxyHost == null || proxyPort == -1) {
            return null;
        }

        // return options with proxy
        final Http4FileSystemConfigBuilder builder = Http4FileSystemConfigBuilder.getInstance();
        final FileSystemOptions opts = new FileSystemOptions();
        builder.setProxyHost(opts, proxyHost);
        builder.setProxyPort(opts, proxyPort);
        if (user != null) {
            builder.setProxyAuthenticator(opts, new StaticUserAuthenticator(null, user[0], user[1]));
        }
        return opts;
    }

    /**
     * Tests VFS-427 NPE on Http4FileObject.getContent().getContentInfo().
     *
     * @throws FileSystemException thrown when the getContentInfo API fails.
     */
    @Test
    public void testGetContentInfo() throws FileSystemException, MalformedURLException {
        @SuppressWarnings("resource") // getManager() returns a global.
        final FileSystemManager fsManager = VFS.getManager();
        final String uri = "http4://www.apache.org/licenses/LICENSE-2.0.txt";
        try (final FileObject fo = fsManager.resolveFile(uri, getOptionsWithProxy());
            final FileContent content = fo.getContent()) {
            Assert.assertNotNull(content);
            // Used to NPE before fix:
            content.getContentInfo();
        }
    }

    /**
     * Tests VFS-782 pass correct proxy authentication credentials.
     *
     * @throws FileSystemException thrown when the authentication fails.
     */
    @Test
    public void testGetContentWithProxyAuthInfo() throws FileSystemException, MalformedURLException {
        @SuppressWarnings("resource") // getManager() returns a global.
        final FileSystemManager fsManager = VFS.getManager();
        final String uri = "http4://www.apache.org/licenses/LICENSE-2.0.txt";
        try (final FileObject fo = fsManager.resolveFile(uri, getOptionsWithProxyAuthentication());
            final FileContent content = fo.getContent()) {
            Assert.assertNotNull(content);
            content.getContentInfo();
        }
    }
}
