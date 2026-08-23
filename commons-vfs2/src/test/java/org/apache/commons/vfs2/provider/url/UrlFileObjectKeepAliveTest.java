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

package org.apache.commons.vfs2.provider.url;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.junit.jupiter.api.Test;

public class UrlFileObjectKeepAliveTest {

    private UrlFileObject createObject(final URL url) throws Exception {
        final FileName rootName = mock(FileName.class);
        when(rootName.getURI()).thenReturn("http://example.com/");
        // real FS -> useCount is initialized
        final UrlFileSystem fs = new UrlFileSystem(rootName, null);
        final AbstractFileName name = mock(AbstractFileName.class);
        final UrlFileObject obj = new UrlFileObject(fs, name);
        final Field urlField = UrlFileObject.class.getDeclaredField("url");
        urlField.setAccessible(true);
        urlField.set(obj, url);
        return obj;
    }

    @Test
    public void doGetContentSize_usesHead_andDoesNotOpenStream() throws Exception {
        final HttpURLConnection conn = mock(HttpURLConnection.class);
        when(conn.getContentLengthLong()).thenReturn(1024L);
        final UrlFileObject obj = createObject(urlWithHandler(conn));
        final Method m = UrlFileObject.class.getDeclaredMethod("doGetContentSize");
        m.setAccessible(true);
        final long size = (Long) m.invoke(obj);
        verify(conn).setRequestMethod("HEAD");
        verify(conn).connect();
        verify(conn, never()).getInputStream();
        assertEquals(1024L, size);
    }

    @Test
    public void doGetInputStream_setsConnectionCloseForHttp() throws Exception {
        final HttpURLConnection conn = mock(HttpURLConnection.class);
        when(conn.getInputStream()).thenReturn(mock(java.io.InputStream.class));
        final UrlFileObject obj = createObject(urlWithHandler(conn));
        final Method m = UrlFileObject.class.getDeclaredMethod("doGetInputStream", int.class);
        m.setAccessible(true);
        m.invoke(obj, 8192);
        verify(conn).setRequestProperty("Connection", "close");
        verify(conn).getInputStream();
    }

    @Test
    public void doGetLastModifiedTime_nonHttpDoesNotSetHead() throws Exception {
        final URLConnection conn = mock(URLConnection.class);
        when(conn.getLastModified()).thenReturn(999L);
        final UrlFileObject obj = createObject(urlWithHandler(conn));
        final Method m = UrlFileObject.class.getDeclaredMethod("doGetLastModifiedTime");
        m.setAccessible(true);
        final long lm = (Long) m.invoke(obj);
        verify(conn).connect();
        assertEquals(999L, lm);
    }

    @Test
    public void doGetLastModifiedTime_usesHead_andDoesNotOpenStream() throws Exception {
        final HttpURLConnection conn = mock(HttpURLConnection.class);
        when(conn.getLastModified()).thenReturn(123456789L);
        final UrlFileObject obj = createObject(urlWithHandler(conn));
        final Method m = UrlFileObject.class.getDeclaredMethod("doGetLastModifiedTime");
        m.setAccessible(true);
        final long lm = (Long) m.invoke(obj);
        verify(conn).setRequestMethod("HEAD");
        verify(conn).connect();
        verify(conn, never()).getInputStream();
        assertEquals(123456789L, lm);
    }

    @Test
    public void doGetType_usesHead_andDoesNotOpenStream() throws Exception {
        final HttpURLConnection conn = mock(HttpURLConnection.class);
        when(conn.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        final UrlFileObject obj = createObject(urlWithHandler(conn));
        final Method m = UrlFileObject.class.getDeclaredMethod("doGetType");
        m.setAccessible(true);
        final Object type = m.invoke(obj);
        verify(conn).setRequestMethod("HEAD");
        verify(conn).setRequestProperty("Connection", "close");
        verify(conn).connect();
        verify(conn, never()).getInputStream();
        assertNotNull(type);
    }

    private URL urlWithHandler(final URLConnection conn) {
        try {
            return new URL(null, "http://example.com/path", new URLStreamHandler() {

                @Override
                protected URLConnection openConnection(final URL u) throws IOException {
                    return conn;
                }
            });
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
