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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.FilesCache;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.cache.NullFilesCache;
import org.apache.commons.vfs2.provider.GenericURLFileName;
import org.apache.commons.vfs2.provider.bzip2.Bzip2FileObject;
import org.apache.commons.vfs2.provider.gzip.GzipFileObject;
import org.apache.commons.vfs2.provider.jar.JarFileObject;
import org.apache.commons.vfs2.provider.ram.RamFileProvider;
import org.apache.commons.vfs2.provider.zip.ZipFileObject;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests {@link DefaultFileSystemManager}.
 */
public class DefaultFileSystemManagerTest {

    @Test
    public void testAddAndRemoveProvider() throws FileSystemException {
        try (DefaultFileSystemManager fileSystemManager = new DefaultFileSystemManager()) {
            fileSystemManager.setFilesCache(new NullFilesCache());
            fileSystemManager.setCacheStrategy(CacheStrategy.MANUAL);

            final RamFileProvider provider = Mockito.spy(new RamFileProvider());
            fileSystemManager.addProvider("ram1", provider);
            fileSystemManager.addProvider("ram2", provider);
            assertNotNull(fileSystemManager.resolveFile("ram1:///"));
            assertNotNull(fileSystemManager.resolveFile("ram2:///"));

            fileSystemManager.removeProvider("ram1");
            Mockito.verify(provider, Mockito.never()).close();
            assertThrows(FileSystemException.class, () -> fileSystemManager.resolveFile("ram1:///"));
            assertNotNull(fileSystemManager.resolveFile("ram2:///"));

            fileSystemManager.removeProvider("ram2");
            Mockito.verify(provider).close();
            assertThrows(FileSystemException.class, () -> fileSystemManager.resolveFile("ram2:///"));
        }
    }

    /**
     * Tests {@link DefaultFileSystemManager#close()}.
     *
     * @throws FileSystemException
     */
    @Test
    public void testClose() throws FileSystemException {
        try (FileSystemManager fileSystemManager = new DefaultFileSystemManager()) {
            VFS.setManager(fileSystemManager);
            VFS.setManager(null);
        }
        assertNotNull(VFS.getManager());
        final Path path = Paths.get("DoesNotExist.not");
        assertFalse(VFS.getManager().resolveFile(path.toUri()).exists());
        assertFalse(VFS.getManager().toFileObject(path.toFile()).exists());
        assertFalse(VFS.getManager().toFileObject(path).exists());
    }

    @Test
    public void testCreateBz2FileSystem() throws FileSystemException {
        testCreateFileSystem("src/test/resources/test-data/bla.txt.bz2", Bzip2FileObject.class);
    }

    private void testCreateFileSystem(final String fileStr, final Class<?> clazz) throws FileSystemException {
        final FileSystemManager manager = VFS.getManager();
        final Path path = Paths.get(fileStr);
        try (FileObject localFileObject = manager.toFileObject(path);
                FileObject fileObject = manager.createFileSystem(localFileObject)) {
            assertEquals(clazz, fileObject.getClass());
        }
        try (FileObject localFileObject = manager.toFileObject(path.toFile());
                FileObject fileObject = manager.createFileSystem(localFileObject)) {
            assertEquals(clazz, fileObject.getClass());
        }
        try (FileObject localFileObject = manager.resolveFile(new File(fileStr).getAbsolutePath());
                FileObject fileObject = manager.createFileSystem(localFileObject)) {
            assertEquals(clazz, fileObject.getClass());
        }
    }

    @Test
    @Disabled
    public void testCreateGzipFileSystem() throws FileSystemException {
        testCreateFileSystem("src/test/resources/test-data/好.txt.gz", GzipFileObject.class);
    }

    @Test
    public void testCreateJarFileSystem() throws FileSystemException {
        testCreateFileSystem("src/test/resources/test-data/nested.jar", JarFileObject.class);
    }

    @Test
    public void testCreateZipFileSystem() throws FileSystemException {
        testCreateFileSystem("src/test/resources/test-data/nested.zip", ZipFileObject.class);
    }

    @Test
    public void testFileCacheEmptyAfterManagerClose() throws FileSystemException {
        final FileSystemManager manager = VFS.getManager();
        assertNotNull(manager);
        try (FileObject fileObject = manager.resolveFile(Paths.get("src/test/resources/test-data/read-tests/file1.txt").toUri())) {
            assertTrue(fileObject.exists());
            final FilesCache filesCache = manager.getFilesCache();
            final FileName name = fileObject.getName();
            // Make sure we have file object in the cache.
            assertNotNull(filesCache.getFile(fileObject.getFileSystem(), name));
            manager.close();
            // Cache MUST now be empty.
            assertNull(filesCache.getFile(fileObject.getFileSystem(), name));
        } finally {
            // Makes sure we reset the singleton or other tests will fail.
            VFS.close();
        }
    }

    @Test
    public void testFileCacheEmptyAfterVFSClose() throws FileSystemException {
        final FileSystemManager manager = VFS.getManager();
        assertNotNull(manager);
        try (FileObject fileObject = manager.resolveFile(Paths.get("src/test/resources/test-data/read-tests/file1.txt").toUri())) {
            assertTrue(fileObject.exists());
            final FilesCache filesCache = manager.getFilesCache();
            final FileName name = fileObject.getName();
            // Make sure we have file object in the cache.
            assertNotNull(filesCache.getFile(fileObject.getFileSystem(), name));
            VFS.close();
            // Cache MUST now be empty.
            assertNull(filesCache.getFile(fileObject.getFileSystem(), name));
        }
    }

    /**
     * Even if the file name is absolute, the base file must be given. This is an inconsistency in the API, but it is documented as such.
     *
     * @see "VFS-519"
     */
    @Test
    public void testResolveFileAbsoluteThrows() {
        final String absolute = new File("/").getAbsoluteFile().toURI().toString();
        assertThrows(NullPointerException.class, () -> VFS.getManager().resolveFile((File) null, absolute));
    }

    /**
     * If the base name is {@code null}, the file system manager should fail throwing a FileSystemException.
     *
     * @see VFS-189
     */
    @Test
    public void testResolveFileNameNull() {
        assertThrows(FileSystemException.class, () -> VFS.getManager().resolveName((FileName) null, "../"));
    }

    /**
     * If the path ends with one of '/' or '.' or '..' or anyPath/..' or 'anyPath/.' , the resulting FileName should be of FileType.FOLDER, else of
     * FileType.FILE.
     */
    @Test
    public void testResolveFileNameType() {
        try (DefaultFileSystemManager fileSystemManager = new DefaultFileSystemManager()) {
            // @formatter:off
            final FileName baseNameFolder = new GenericURLFileName(
                    "sftp",
                    "localhost",
                    22,
                    22,
                    "user",
                    "password",
                    "basePath",
                     FileType.FOLDER,
                    "query=test");
            // @formatter:on

            assertEquals(FileType.FOLDER, fileSystemManager.resolveName(baseNameFolder, "").getType());
            assertEquals(FileType.FOLDER, fileSystemManager.resolveName(baseNameFolder, "/").getType());
            assertEquals(FileType.FOLDER, fileSystemManager.resolveName(baseNameFolder, ".").getType());
            assertEquals(FileType.FOLDER, fileSystemManager.resolveName(baseNameFolder, "..").getType());
            assertEquals(FileType.FOLDER, fileSystemManager.resolveName(baseNameFolder, "./").getType());
            assertEquals(FileType.FOLDER, fileSystemManager.resolveName(baseNameFolder, "../").getType());
            assertEquals(FileType.FOLDER, fileSystemManager.resolveName(baseNameFolder, "./Sub Folder/").getType());
            assertEquals(FileType.FOLDER, fileSystemManager.resolveName(baseNameFolder, "../Descendant Folder/").getType());
            assertEquals(FileType.FOLDER, fileSystemManager.resolveName(baseNameFolder, "./Sub Folder/.").getType());
            assertEquals(FileType.FOLDER, fileSystemManager.resolveName(baseNameFolder, "../Descendant Folder/..").getType());
            assertEquals(FileType.FOLDER, fileSystemManager.resolveName(baseNameFolder, "./Sub Folder/./").getType());
            assertEquals(FileType.FOLDER, fileSystemManager.resolveName(baseNameFolder, "../Descendant Folder/../").getType());

            assertEquals(FileType.FILE, fileSystemManager.resolveName(baseNameFolder, "File.txt").getType());
            assertEquals(FileType.FILE, fileSystemManager.resolveName(baseNameFolder, "/File.txt").getType());
            assertEquals(FileType.FILE, fileSystemManager.resolveName(baseNameFolder, "./File.txt").getType());
            assertEquals(FileType.FILE, fileSystemManager.resolveName(baseNameFolder, "../File.txt").getType());
            assertEquals(FileType.FILE, fileSystemManager.resolveName(baseNameFolder, "./Sub Folder/File.txt").getType());
            assertEquals(FileType.FILE, fileSystemManager.resolveName(baseNameFolder, "../Descendant Folder/File.txt").getType());
            assertEquals(FileType.FILE, fileSystemManager.resolveName(baseNameFolder, "./Sub Folder/./File.txt").getType());
            assertEquals(FileType.FILE, fileSystemManager.resolveName(baseNameFolder, "../Descendant Folder/../File.txt").getType());
            assertEquals(FileType.FILE, fileSystemManager.resolveName(baseNameFolder, "../Descendant Folder/../File.").getType());
            assertEquals(FileType.FILE, fileSystemManager.resolveName(baseNameFolder, "../Descendant Folder/../File..").getType());

        } catch (final FileSystemException e) {
            fail(e);
        }
    }

    @Test
    public void testResolveFileObjectNullAbsolute() throws FileSystemException {
        final String absolute = new File("/").getAbsoluteFile().toURI().toString();
        VFS.getManager().resolveFile((FileObject) null, absolute);
    }

    @Test
    public void testResolveFileObjectRelativeThrows() {
        assertThrows(FileSystemException.class, () -> VFS.getManager().resolveFile((FileObject) null, "relativePath"));
    }

    @Test
    public void testResolveFileRelativeThrows() {
        assertThrows(NullPointerException.class, () -> VFS.getManager().resolveFile((File) null, "relativePath"));
    }

}
