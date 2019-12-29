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
package org.apache.commons.vfs2.provider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.Test;

/**
 * {@code DefaultFileContentTest} tests for bug-VFS-614. This bug involves the stream implementation closing the stream
 * after reading to the end of the buffer, which broke marking.
 */
public class DefaultFileContentTest {
    private static final String expected = "testing";

    /**
     * Test VFS-724 should be done on a website which render a page with no content size. Note the getSize() is
     * currently the value sent back by the server then zero usually means no content length attached.
     */
    @Test
    public void testGetZeroContents() throws IOException {
        final FileSystemManager fsManager = VFS.getManager();
        try (final FileObject fo = fsManager.resolveFile(new File("."), "src/test/resources/test-data/size-0-file.bin");
                final FileContent content = fo.getContent()) {
            Assert.assertEquals(0, content.getSize());
            Assert.assertTrue(content.isEmpty());
            Assert.assertEquals(StringUtils.EMPTY, content.getString(StandardCharsets.UTF_8));
            Assert.assertEquals(StringUtils.EMPTY, content.getString(StandardCharsets.UTF_8.name()));
            Assert.assertArrayEquals(ArrayUtils.EMPTY_BYTE_ARRAY, content.getByteArray());
        }
    }

    private void testInputStreamBufferSize(final int bufferSize) throws Exception {
        final File temp = File.createTempFile("temp-file-name", ".tmp");
        final FileSystemManager fileSystemManager = VFS.getManager();

        try (FileObject file = fileSystemManager.resolveFile(temp.getAbsolutePath())) {
            file.getContent().getInputStream(bufferSize);
        }
    }

    @Test
    public void testInputStreamBufferSize0() throws Exception {
        testInputStreamBufferSize(0);
    }

    @Test
    public void testInputStreamBufferSize1() throws Exception {
        testInputStreamBufferSize(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInputStreamBufferSizeNegative() throws Exception {
        testInputStreamBufferSize(-2);
    }

    @Test
    public void testMarkingWhenReadingEOS() throws Exception {
        final File temp = File.createTempFile("temp-file-name", ".tmp");
        final FileSystemManager fileSystemManager = VFS.getManager();

        try (FileObject file = fileSystemManager.resolveFile(temp.getAbsolutePath())) {
            try (OutputStream outputStream = file.getContent().getOutputStream()) {
                outputStream.write(expected.getBytes());
                outputStream.flush();
            }
            try (InputStream stream = file.getContent().getInputStream()) {
                int readCount = 0;
                if (stream.markSupported()) {
                    for (int i = 0; i < 10; i++) {
                        stream.mark(0);
                        final byte[] data = new byte[100];
                        readCount = stream.read(data, 0, 7);
                        stream.read();
                        Assert.assertEquals(7, readCount);
                        Assert.assertEquals(expected, new String(data).trim());
                        readCount = stream.read(data, 8, 10);
                        Assert.assertEquals(-1, readCount);
                        stream.reset();
                    }
                }
            }
        }
    }

    @Test
    public void testMarkingWorks() throws Exception {
        final File temp = File.createTempFile("temp-file-name", ".tmp");
        final FileSystemManager fileSystemManager = VFS.getManager();

        try (FileObject file = fileSystemManager.resolveFile(temp.getAbsolutePath())) {
            try (OutputStream outputStream = file.getContent().getOutputStream()) {
                outputStream.write(expected.getBytes());
                outputStream.flush();
            }
            try (InputStream stream = file.getContent().getInputStream()) {
                if (stream.markSupported()) {
                    for (int i = 0; i < 10; i++) {
                        stream.mark(0);
                        final byte[] data = new byte[100];
                        stream.read(data, 0, 7);
                        stream.read();
                        Assert.assertEquals(expected, new String(data).trim());
                        stream.reset();
                    }
                }
            }
        }
    }

    private void testOutputStreamBufferSize(final int bufferSize) throws Exception {
        final File temp = File.createTempFile("temp-file-name", ".tmp");
        final FileSystemManager fileSystemManager = VFS.getManager();

        try (FileObject file = fileSystemManager.resolveFile(temp.getAbsolutePath())) {
            file.getContent().getOutputStream(bufferSize).close();
        }
    }

    @Test
    public void testOutputStreamBufferSize0() throws Exception {
        testOutputStreamBufferSize(0);
    }

    @Test
    public void testOutputStreamBufferSize1() throws Exception {
        testOutputStreamBufferSize(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOutputStreamBufferSizeNegative() throws Exception {
        testOutputStreamBufferSize(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOutputStreamBufferSizeNegativeWithAppendFlag() throws Exception {
        final File temp = File.createTempFile("temp-file-name", ".tmp");
        final FileSystemManager fileSystemManager = VFS.getManager();

        try (FileObject file = fileSystemManager.resolveFile(temp.getAbsolutePath())) {
            file.getContent().getOutputStream(true, -1);
        }
    }

}
