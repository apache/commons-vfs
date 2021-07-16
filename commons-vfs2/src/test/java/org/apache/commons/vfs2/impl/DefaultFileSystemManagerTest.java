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

import java.io.File;
import java.nio.file.Paths;

import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FilesCache;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.cache.NullFilesCache;
import org.apache.commons.vfs2.provider.bzip2.Bzip2FileObject;
import org.apache.commons.vfs2.provider.gzip.GzipFileObject;
import org.apache.commons.vfs2.provider.jar.JarFileObject;
import org.apache.commons.vfs2.provider.ram.RamFileProvider;
import org.apache.commons.vfs2.provider.zip.ZipFileObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests {@link DefaultFileSystemManager}.
 *
 * @since 2.5.0
 */
public class DefaultFileSystemManagerTest {

    /**
     * Tests {@link DefaultFileSystemManager#close()}.
     *
     * @throws FileSystemException
     */
    @Test
    public void test_close() throws FileSystemException {
        try (FileSystemManager fileSystemManager = new DefaultFileSystemManager()) {
            VFS.setManager(fileSystemManager);
            VFS.setManager(null);
        }
        Assert.assertNotNull(VFS.getManager());
        Assert.assertFalse(VFS.getManager().resolveFile(Paths.get("DoesNotExist.not").toUri()).exists());
    }

    @Test
    public void testAddAndRemoveProvider() throws FileSystemException {
        try (DefaultFileSystemManager fileSystemManager = new DefaultFileSystemManager()) {
            fileSystemManager.setFilesCache(new NullFilesCache());
            fileSystemManager.setCacheStrategy(CacheStrategy.MANUAL);

            final RamFileProvider provider = Mockito.spy(new RamFileProvider());
            fileSystemManager.addProvider("ram1", provider);
            fileSystemManager.addProvider("ram2", provider);
            Assert.assertNotNull(fileSystemManager.resolveFile("ram1:///"));
            Assert.assertNotNull(fileSystemManager.resolveFile("ram2:///"));

            fileSystemManager.removeProvider("ram1");
            Mockito.verify(provider, Mockito.never()).close();
            Assert.assertThrows(FileSystemException.class, () -> fileSystemManager.resolveFile("ram1:///"));
            Assert.assertNotNull(fileSystemManager.resolveFile("ram2:///"));

            fileSystemManager.removeProvider("ram2");
            Mockito.verify(provider).close();
            Assert.assertThrows(FileSystemException.class, () -> fileSystemManager.resolveFile("ram2:///"));
        }
    }

    @Test
    public void testCreateBz2FileSystem() throws FileSystemException {
        testCreateFileSystem("src/test/resources/test-data/bla.txt.bz2", Bzip2FileObject.class);
    }

    private void testCreateFileSystem(final String path, Class<?> clazz) throws FileSystemException {
        FileSystemManager manager = VFS.getManager();
        try (FileObject localFileObject = manager.resolveFile(new File(path).getAbsolutePath());
                FileObject fileObject = manager.createFileSystem(localFileObject)) {
            Assert.assertEquals(clazz, fileObject.getClass());
        }
    }

    @Test
    @Ignore
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
        Assert.assertNotNull(manager);
        try (final FileObject fileObject = manager
                .resolveFile(Paths.get("src/test/resources/test-data/read-tests/file1.txt").toUri())) {
            Assert.assertTrue(fileObject.exists());
            final FilesCache filesCache = manager.getFilesCache();
            final FileName name = fileObject.getName();
            // Make sure we have file object in the cache.
            Assert.assertNotNull(filesCache.getFile(fileObject.getFileSystem(), name));
            manager.close();
            // Cache MUST now be empty.
            Assert.assertNull(filesCache.getFile(fileObject.getFileSystem(), name));
        } finally {
            // Makes sure we reset the singleton or other tests will fail.
            VFS.close();
        }
    }

    @Test
    public void testFileCacheEmptyAfterVFSClose() throws FileSystemException {
        final FileSystemManager manager = VFS.getManager();
        Assert.assertNotNull(manager);
        try (final FileObject fileObject = manager
                .resolveFile(Paths.get("src/test/resources/test-data/read-tests/file1.txt").toUri())) {
            Assert.assertTrue(fileObject.exists());
            final FilesCache filesCache = manager.getFilesCache();
            final FileName name = fileObject.getName();
            // Make sure we have file object in the cache.
            Assert.assertNotNull(filesCache.getFile(fileObject.getFileSystem(), name));
            VFS.close();
            // Cache MUST now be empty.
            Assert.assertNull(filesCache.getFile(fileObject.getFileSystem(), name));
        }
    }

    /**
     * Even if the file name is absolute, the base file must be given. This is an inconsistency in the API, but it is
     * documented as such.
     *
     * @see "VFS-519"
     */
    @Test(expected = NullPointerException.class)
    public void testResolveFileAbsoluteThrows() throws FileSystemException {
        final String absolute = new File("/").getAbsoluteFile().toURI().toString();
        VFS.getManager().resolveFile((File) null, absolute);
    }

    /**
     * If the base name is {@code null}, the file system manager should fail throwing a FileSystemException.
     *
     * @see VFS-189
     */
    @Test(expected = FileSystemException.class)
    public void testResolveFileNameNull() throws FileSystemException {
        VFS.getManager().resolveName((FileName) null, "../");
    }

    @Test
    public void testResolveFileObjectNullAbsolute() throws FileSystemException {
        final String absolute = new File("/").getAbsoluteFile().toURI().toString();
        VFS.getManager().resolveFile((FileObject) null, absolute);
    }

    @Test(expected = FileSystemException.class)
    public void testResolveFileObjectRelativeThrows() throws FileSystemException {
        VFS.getManager().resolveFile((FileObject) null, "relativePath");
    }

    @Test(expected = NullPointerException.class)
    public void testResolveFileRelativeThrows() throws FileSystemException {
        VFS.getManager().resolveFile((File) null, "relativePath");
    }
}
