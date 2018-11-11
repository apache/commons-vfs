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
package org.apache.commons.vfs2.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * A proxy for URLs that are supported by the standard stream handler factory.
 */
class URLStreamHandlerProxy extends URLStreamHandler {
    @Override
    protected URLConnection openConnection(final URL url) throws IOException {
        final URL proxyURL = new URL(url.toExternalForm());
        return proxyURL.openConnection();
    }

    @Override
    protected void parseURL(final URL u, final String spec, final int start, final int limit) {
        try {
            final URL url = new URL(u, spec);
            setURL(u, url.getProtocol(), url.getHost(), url.getPort(), url.getAuthority(), url.getUserInfo(),
                    url.getFile(), url.getQuery(), url.getRef());
        } catch (final MalformedURLException mue) {
            // We retrow this as a simple runtime exception.
            // It is retrown in URL as a MalformedURLException anyway.
            throw new RuntimeException(mue.getMessage());
        }
    }
}
