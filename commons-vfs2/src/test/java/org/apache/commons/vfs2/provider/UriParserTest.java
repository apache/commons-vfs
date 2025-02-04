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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.junit.jupiter.api.Test;

public class UriParserTest {

    private static final String[] schemes = {"ftp", "file"};

    private void checkNormalizedPath(final String path, final String normalized) throws FileSystemException {
        final StringBuilder pathBuilder = new StringBuilder(path);
        UriParser.fixSeparators(pathBuilder);
        UriParser.normalisePath(pathBuilder);
        assertEquals(normalized, pathBuilder.toString());
    }

    @Test
    public void testColonInFileName() {
        assertNull(UriParser.extractScheme("some/path/some:file"));
    }

    @Test
    public void testColonInFileNameAndNotSupportedScheme() {
        assertNull(UriParser.extractScheme(schemes, "some:file"));
    }

    @Test
    public void testColonInFileNameWithPath() {
        assertNull(UriParser.extractScheme(schemes, "some/path/some:file"));
    }

    @Test
    public void testColonNotFollowedBySlash() {
        assertEquals("file", UriParser.extractScheme(schemes, "file:user/subdir/some/path/some:file"));
    }

    @Test
    public void testIPv6CheckUriEncoding() throws FileSystemException {
        UriParser.checkUriEncoding("http://[fe80::14b5:1204:5410:64ca%en1]:8080");
    }

    @Test
    public void testNormalScheme() {
        assertEquals("ftp", UriParser.extractScheme(schemes, "ftp://user:pass@host/some/path/some:file"));
    }

    @Test
    public void testNormalSchemeWithBuffer() {
        final StringBuilder buffer = new StringBuilder();
        UriParser.extractScheme(schemes, "ftp://user:pass@host/some/path/some:file", buffer);
        assertEquals("//user:pass@host/some/path/some:file", buffer.toString());
    }

    @Test
    public void testOneSlashScheme() {
        assertEquals("file", UriParser.extractScheme(schemes, "file:/user:pass@host/some/path/some:file"));
    }
    @Test
    public void testOneSlashSchemeWithBuffer() {
        final StringBuilder buffer = new StringBuilder();
        UriParser.extractScheme(schemes, "file:/user:pass@host/some/path/some:file", buffer);
        assertEquals("/user:pass@host/some/path/some:file", buffer.toString());
    }

    @Test
    public void testPathOfNormalizedPath() throws FileSystemException {
        checkNormalizedPath("./Sub Folder/", "Sub Folder");
        checkNormalizedPath("./Sub Folder/../", "");
        checkNormalizedPath("./Sub Folder%2f..%2f", "");
        checkNormalizedPath("File.txt", "File.txt");
        checkNormalizedPath("./Sub Folder/./File.txt", "Sub Folder/File.txt");
        checkNormalizedPath("./Sub Folder%2F.%2FFile.txt", "Sub Folder/File.txt");
    }

    @Test
    public void testTypeOfNormalizedPath() {
        try {
            assertEquals(FileType.FOLDER, UriParser.normalisePath(new StringBuilder("")));
            assertEquals(FileType.FOLDER, UriParser.normalisePath(new StringBuilder("/")));
            assertEquals(FileType.FOLDER, UriParser.normalisePath(new StringBuilder(".")));
            assertEquals(FileType.FOLDER, UriParser.normalisePath(new StringBuilder("./")));
            assertEquals(FileType.FOLDER, UriParser.normalisePath(new StringBuilder("./Sub Folder/")));
            assertEquals(FileType.FOLDER, UriParser.normalisePath(new StringBuilder("./Sub Folder/.")));
            assertEquals(FileType.FOLDER, UriParser.normalisePath(new StringBuilder("./Sub Folder/./")));

            assertEquals(FileType.FILE, UriParser.normalisePath(new StringBuilder("File.txt")));
            assertEquals(FileType.FILE, UriParser.normalisePath(new StringBuilder("/File.txt")));
            assertEquals(FileType.FILE, UriParser.normalisePath(new StringBuilder("./File.txt")));
            assertEquals(FileType.FILE, UriParser.normalisePath(new StringBuilder("./Sub Folder/File.txt")));
            assertEquals(FileType.FILE, UriParser.normalisePath(new StringBuilder("./Sub Folder/./File.txt")));
            assertEquals(FileType.FILE, UriParser.normalisePath(new StringBuilder("./Sub Folder/./File.")));
            assertEquals(FileType.FILE, UriParser.normalisePath(new StringBuilder("./Sub Folder/./File..")));

        } catch (final FileSystemException e) {
            fail(e);
        }
    }
}
