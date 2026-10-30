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

package org.apache.commons.vfs2.provider.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.junit.jupiter.api.Test;

/**
 * Tests random reads when the HTTP server ignores range requests.
 */
class HttpRandomAccessContentTest {

    private HttpRandomAccessContent<HttpFileSystem> createContent(final HttpConnection connection) throws Exception {
        return createContent(connection, null, null);
    }

    private HttpRandomAccessContent<HttpFileSystem> createContent(final HttpConnection connection, final IOException readFailure,
            final IOException closeFailure) throws Exception {
        final byte[] headers = "Content-Length: 32\r\n\r\n".getBytes(StandardCharsets.US_ASCII);
        final byte[] response = new byte[headers.length + 32];
        System.arraycopy(headers, 0, response, 0, headers.length);
        for (int i = 0; i < 32; i++) {
            response[headers.length + i] = (byte) i;
        }
        // Parsing the headers leaves seven body bytes in the connection buffer.
        final FilterInputStream source = new FilterInputStream(new ByteArrayInputStream(response)) {

            private int readCount;

            @Override
            public int read(final byte[] bytes, final int offset, final int length) throws IOException {
                final int currentRead = readCount++;
                if (currentRead == 1 && readFailure != null) {
                    throw readFailure;
                }
                if (currentRead == 2 && closeFailure != null) {
                    throw closeFailure;
                }
                return super.read(bytes, offset, length);
            }
        };
        final BufferedInputStream input = new BufferedInputStream(source, headers.length + 7);
        when(connection.isOpen()).thenReturn(true);
        when(connection.getHost()).thenReturn("localhost");
        when(connection.getPort()).thenReturn(80);
        when(connection.getProtocol()).thenReturn(Protocol.getProtocol("http"));
        when(connection.getParams()).thenReturn(new HttpConnectionParams());
        when(connection.getRequestOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(connection.readLine(anyString())).thenReturn("HTTP/1.1 200 OK");
        when(connection.getResponseInputStream()).thenReturn(input);
        final HttpClient client = mock(HttpClient.class);
        when(client.executeMethod(any(HttpMethod.class)))
                .thenAnswer(invocation -> ((HttpMethod) invocation.getArgument(0)).execute(new HttpState(), connection));
        final HttpFileSystem fileSystem = mock(HttpFileSystem.class);
        when(fileSystem.getClient()).thenReturn(client);
        @SuppressWarnings("unchecked")
        final HttpFileObject<HttpFileSystem> file = mock(HttpFileObject.class);
        when(file.getFileSystem()).thenReturn(fileSystem);
        return new HttpRandomAccessContent<>(file, RandomAccessMode.READ);
    }

    @Test
    void testIgnoredRangeBeyondEndClosesConnection() throws Exception {
        final HttpConnection connection = mock(HttpConnection.class);
        try (HttpRandomAccessContent<HttpFileSystem> content = createContent(connection)) {
            content.seek(40);
            assertThrows(FileSystemException.class, content::readByte);
            assertEquals(40, content.getFilePointer());
            verify(connection).releaseConnection();
        }
    }

    @Test
    void testIgnoredRangeCloseFailureIsSuppressed() throws Exception {
        final HttpConnection connection = mock(HttpConnection.class);
        final IOException readFailure = new IOException("Read failed");
        final IOException closeFailure = new IOException("Close failed");
        try (HttpRandomAccessContent<HttpFileSystem> content = createContent(connection, readFailure, closeFailure)) {
            content.seek(10);
            assertSame(readFailure, assertThrows(IOException.class, content::readByte));
            assertEquals(1, readFailure.getSuppressed().length);
            assertSame(closeFailure, readFailure.getSuppressed()[0]);
            verify(connection).releaseConnection();
        }
    }

    @Test
    void testIgnoredRangeReadFailureClosesConnection() throws Exception {
        final HttpConnection connection = mock(HttpConnection.class);
        final IOException failure = new IOException("Read failed");
        try (HttpRandomAccessContent<HttpFileSystem> content = createContent(connection, failure, null)) {
            content.seek(10);
            assertSame(failure, assertThrows(IOException.class, content::readByte));
            assertEquals(10, content.getFilePointer());
            verify(connection).releaseConnection();
        }
    }

    @Test
    void testIgnoredRangeSameReadAndCloseFailure() throws Exception {
        final HttpConnection connection = mock(HttpConnection.class);
        final IOException failure = new IOException("Read and close failed");
        try (HttpRandomAccessContent<HttpFileSystem> content = createContent(connection, failure, failure)) {
            content.seek(10);
            assertSame(failure, assertThrows(IOException.class, content::readByte));
            assertEquals(0, failure.getSuppressed().length);
            verify(connection).releaseConnection();
        }
    }

    @Test
    void testIgnoredRangeWithShortSkip() throws Exception {
        final HttpConnection connection = mock(HttpConnection.class);
        try (HttpRandomAccessContent<HttpFileSystem> content = createContent(connection)) {
            content.seek(10);
            assertEquals(10, content.readByte());
            assertEquals(11, content.getFilePointer());
            assertEquals(11, content.readByte());
            assertEquals(12, content.getFilePointer());
        }
        verify(connection).releaseConnection();
    }
}
