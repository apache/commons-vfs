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
package org.apache.commons.vfs2.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link FileObjectUtils}.
 */
public class FileObjectUtilsTest {

    private void assertProperties(final Properties p) {
        assertNotNull(p);
        assertEquals("1", p.getProperty("one"));
        assertEquals("2", p.getProperty("two"));
    }

    @Test
    public void testExistsNotNull() throws FileSystemException {
        assertTrue(FileObjectUtils.exists(VFS.getManager().toFileObject(SystemUtils.getJavaIoTmpDir())));
    }

    @Test
    public void testgetContentAsStringCharset() throws IOException {
        assertEquals("This is a test file.",
            FileObjectUtils.getContentAsString(
                VFS.getManager().toFileObject(new File("src/test/resources/test-data/read-tests/file1.txt")),
                StandardCharsets.UTF_8));
    }

    @Test
    public void testgetContentAsStringCharsetNull() throws IOException {
        assertEquals("This is a test file.",
            FileObjectUtils.getContentAsString(
                VFS.getManager().toFileObject(new File("src/test/resources/test-data/read-tests/file1.txt")),
                (Charset) null));
    }

    @Test
    public void testgetContentAsStringString() throws IOException {
        assertEquals("This is a test file.", FileObjectUtils.getContentAsString(
                VFS.getManager().toFileObject(new File("src/test/resources/test-data/read-tests/file1.txt")), StandardCharsets.UTF_8.name()));
    }

    @Test
    public void testgetContentAsStringStringNull() throws IOException {
        assertEquals("This is a test file.",
            FileObjectUtils.getContentAsString(
                VFS.getManager().toFileObject(new File("src/test/resources/test-data/read-tests/file1.txt")),
                (String) null));
    }

    @Test
    public void testNotExistsNotNull() throws FileSystemException {
        assertFalse(
            FileObjectUtils.exists(VFS.getManager().toFileObject(new File("This file can't possibly exist, right?"))));
    }

    @Test
    public void testNotExistsNull() throws FileSystemException {
        assertFalse(FileObjectUtils.exists(null));
    }

    @Test
    public void testReadProperties() throws FileSystemException, IOException {
        assertProperties(FileObjectUtils
            .readProperties(VFS.getManager().toFileObject(new File("src/test/resources/test.properties"))));
    }

    @Test
    public void testReadPropertiesInto() throws IOException {
        final Properties p = new Properties();
        p.setProperty("extraKey", "extraValue");
        assertProperties(FileObjectUtils
            .readProperties(VFS.getManager().toFileObject(new File("src/test/resources/test.properties")), p));
        assertEquals("extraValue", p.getProperty("extraKey"));
    }

}
