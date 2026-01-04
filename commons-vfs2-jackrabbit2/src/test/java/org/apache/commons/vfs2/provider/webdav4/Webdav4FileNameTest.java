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
package org.apache.commons.vfs2.provider.webdav4;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.GenericURLFileName;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link Webdav4FileName}.
 */
public class Webdav4FileNameTest {

    /**
     * If the resolved path ends without a '/'
     */
    @Test
    public void testWebdavUrlWithoutTrailingSlash() throws FileSystemException {
        @SuppressWarnings("resource")
        final FileSystemOptions fsoptsWithToutrailingSlashDefault = new FileSystemOptions();
        final FileSystemOptions fsoptsWithoutTrailingSlash = new FileSystemOptions();
        Webdav4FileSystemConfigBuilder.getInstance().setAppendTrailingSlash(fsoptsWithoutTrailingSlash, false);
        final FileSystemManager fileSystemManager = VFS.getManager();

        final String urlBase = "webdav4://localhost:80";
        final String urlWithFile1 = "webdav4://localhost:80/File.txt";
        final String urlWithFile2 = "webdav4://localhost:80/Path/File.txt";
        final String urlWithFileWithoutExtension1 = "webdav4://localhost:80/File";
        final String urlWithFileWithoutExtension2 = "webdav4://localhost:80/Path/File";
        final String urlWithSubpath = "webdav4://localhost:80/Path/Sub Path/";
        final String urlWithRelativePart1 = "webdav4://localhost:80/Path/.";
        final String urlWithRelativePart2 = "webdav4://localhost:80/Path/./";
        final String urlWithRelativePart3 = "webdav4://localhost:80/Path/../Decendant Path/";
        final String urlWithRelativePart4 = "webdav4://localhost:80/Path/Sub Path/..";
        final String urlWithRelativePart5 = "webdav4://localhost:80/Path/Sub Path/../";
        final String urlWithQuery1 = "webdav4://localhost:80/Path/Sub Path/?";
        final String urlWithQuery2 = "webdav4://localhost:80/Path/Sub Path/?foo=bar";
        final String urlWithQuery3 = "webdav4://localhost:80/Path/Sub Path/?foo=1&bar=2";
        final String urlWithQuery4 = "webdav4://localhost:80/Path/Sub Path/?foo=1&bar=2";
        final String urlWithQuery5 = "webdav4://localhost:80/Path/File?foo=1&bar=2";

        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlBase, fsoptsWithoutTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            //The ROOT is always with trailing slash
            assertEquals("http://localhost/", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlBase, fsoptsWithToutrailingSlashDefault)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            //The ROOT is always with trailing slash
            assertEquals("http://localhost/", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithFile1, fsoptsWithoutTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/File.txt", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithFile1, fsoptsWithToutrailingSlashDefault)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/File.txt", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithFile2, fsoptsWithoutTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/File.txt", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithFile2, fsoptsWithToutrailingSlashDefault)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/File.txt", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithFileWithoutExtension1, fsoptsWithoutTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/File", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithFileWithoutExtension1, fsoptsWithToutrailingSlashDefault)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/File", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithFileWithoutExtension2, fsoptsWithoutTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/File", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithFileWithoutExtension2, fsoptsWithToutrailingSlashDefault)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/File", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithSubpath, fsoptsWithoutTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/Sub%20Path", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithSubpath, fsoptsWithToutrailingSlashDefault)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/Sub%20Path", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithRelativePart1, fsoptsWithoutTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithRelativePart1, fsoptsWithToutrailingSlashDefault)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithRelativePart2, fsoptsWithoutTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithRelativePart2, fsoptsWithToutrailingSlashDefault)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithRelativePart3, fsoptsWithoutTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Decendant%20Path", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithRelativePart3, fsoptsWithToutrailingSlashDefault)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Decendant%20Path", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithRelativePart4, fsoptsWithoutTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithRelativePart4, fsoptsWithToutrailingSlashDefault)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithRelativePart5, fsoptsWithoutTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithRelativePart5, fsoptsWithToutrailingSlashDefault)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithQuery1, fsoptsWithoutTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/Sub%20Path", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithQuery1, fsoptsWithToutrailingSlashDefault)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/Sub%20Path", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithQuery2, fsoptsWithoutTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/Sub%20Path?foo=bar", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithQuery2, fsoptsWithToutrailingSlashDefault)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/Sub%20Path?foo=bar", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithQuery3, fsoptsWithoutTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/Sub%20Path?foo=1&bar=2", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithQuery3, fsoptsWithToutrailingSlashDefault)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/Sub%20Path?foo=1&bar=2", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithQuery4, fsoptsWithoutTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/Sub%20Path?foo=1&bar=2", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithQuery4, fsoptsWithToutrailingSlashDefault)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/Sub%20Path?foo=1&bar=2", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithQuery5, fsoptsWithoutTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/File?foo=1&bar=2", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithQuery5, fsoptsWithToutrailingSlashDefault)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/File?foo=1&bar=2", file.toUrlString(fileName));
        }
    }

    /**
     * If the resolved path ends with a '/'
     */
    @Test
    public void testWebdavUrlWithTrailingSlash() throws FileSystemException {
        @SuppressWarnings("resource")
        final
        FileSystemOptions fsoptsWithTrailingSlash = new FileSystemOptions();
        Webdav4FileSystemConfigBuilder.getInstance().setAppendTrailingSlash(fsoptsWithTrailingSlash, true);

        final FileSystemManager fileSystemManager = VFS.getManager();

        final String urlBase = "webdav4://localhost:80";
        final String urlWithFile1 = "webdav4://localhost:80/File.txt";
        final String urlWithFile2 = "webdav4://localhost:80/Path/File.txt";
        final String urlWithFileWithoutExtension1 = "webdav4://localhost:80/File";
        final String urlWithFileWithoutExtension2 = "webdav4://localhost:80/Path/File";
        final String urlWithSubpath = "webdav4://localhost:80/Path/Sub Path/";
        final String urlWithRelativePart1 = "webdav4://localhost:80/Path/.";
        final String urlWithRelativePart2 = "webdav4://localhost:80/Path/./";
        final String urlWithRelativePart3 = "webdav4://localhost:80/Path/../Decendant Path/";
        final String urlWithRelativePart4 = "webdav4://localhost:80/Path/Sub Path/..";
        final String urlWithRelativePart5 = "webdav4://localhost:80/Path/Sub Path/../";
        final String urlWithQuery1 = "webdav4://localhost:80/Path/Sub Path/?";
        final String urlWithQuery2 = "webdav4://localhost:80/Path/Sub Path/?foo=bar";
        final String urlWithQuery3 = "webdav4://localhost:80/Path/Sub Path/?foo=1&bar=2";
        final String urlWithQuery4 = "webdav4://localhost:80/Path/Sub Path/?foo=1&bar=2";
        final String urlWithQuery5 = "webdav4://localhost:80/Path/File?foo=1&bar=2";

        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlBase, fsoptsWithTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithFile1, fsoptsWithTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/File.txt", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithFile2, fsoptsWithTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/File.txt", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithFileWithoutExtension1, fsoptsWithTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/File", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithFileWithoutExtension2, fsoptsWithTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/File", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithSubpath, fsoptsWithTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/Sub%20Path/", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithRelativePart1, fsoptsWithTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithRelativePart2, fsoptsWithTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithRelativePart3, fsoptsWithTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Decendant%20Path/", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithRelativePart4, fsoptsWithTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithRelativePart5, fsoptsWithTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithQuery1, fsoptsWithTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/Sub%20Path/", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithQuery2, fsoptsWithTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/Sub%20Path/?foo=bar", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithQuery3, fsoptsWithTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/Sub%20Path/?foo=1&bar=2", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithQuery4, fsoptsWithTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/Sub%20Path/?foo=1&bar=2", file.toUrlString(fileName));
        }
        try (final Webdav4FileObject file = (Webdav4FileObject) fileSystemManager.resolveFile(urlWithQuery5, fsoptsWithTrailingSlash)) {
            final GenericURLFileName fileName = (GenericURLFileName) file.getName();
            assertEquals("http://localhost/Path/File?foo=1&bar=2", file.toUrlString(fileName));
        }
    }
}
