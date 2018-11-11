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

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * {@code DefaultFileContentTest} tests for bug-VFS-614. This bug involves the stream implementation closing the stream
 * after reading to the end of the buffer, which broke marking.
 */
public class DefaultFileContentTest {
    private static final String expected = "testing";

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
                        Assert.assertEquals(expected, new String(data).trim());
                        stream.reset();
                    }
                }
            }
        }
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
                        Assert.assertEquals(readCount, 7);
                        Assert.assertEquals(expected, new String(data).trim());
                        readCount = stream.read(data, 8, 10);
                        Assert.assertEquals(readCount, -1);
                        stream.reset();
                    }
                }
            }
        }
    }
}
