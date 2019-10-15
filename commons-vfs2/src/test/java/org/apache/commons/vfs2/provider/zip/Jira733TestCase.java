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

package org.apache.commons.vfs2.provider.zip;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.cache.OnCallRefreshFileObject;
import org.apache.commons.vfs2.function.VfsConsumer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class Jira733TestCase {

    @After
    @Before
    public void reset() throws FileSystemException {
        VFS.reset();
    }

    @Test
    public void testZipParentLayer() throws Exception {
        final File file = new File("src/test/resources/test-data/test.zip");
        final String nestedPath = "zip:" + file.getAbsolutePath() + "!/read-tests/file1.txt";
        try (final FileObject fileObject = VFS.getManager().resolveFile(nestedPath);
                final FileObject wrappedFileObject = new OnCallRefreshFileObject(fileObject)) {
            // VFS.getManager().getFilesCache().close();
            Assert.assertNotNull("getParentLayer() 1", wrappedFileObject.getFileSystem().getParentLayer());
            wrappedFileObject.exists();
            wrappedFileObject.getContent();
            Assert.assertNotNull("getParentLayer() 2", wrappedFileObject.getFileSystem().getParentLayer());
        }
    }

    private void testZipParentLayer(final VfsConsumer<FileObject> consumer) throws Exception {
        final File file = new File("src/test/resources/test-data/test.zip");
        Assert.assertTrue(file.exists());
        final String nestedPath = "zip:" + file.getAbsolutePath() + "!/read-tests/file1.txt";
        try (final FileObject fileObject = VFS.getManager().resolveFile(nestedPath);
                final FileObject wrappedFileObject = new OnCallRefreshFileObject(fileObject)) {
            Assert.assertTrue(fileObject instanceof ZipFileObject);
            @SuppressWarnings({ "unused", "resource" })
            ZipFileObject zipFileObject = (ZipFileObject) fileObject;
            Assert.assertNotNull("getParentLayer() 1", wrappedFileObject.getFileSystem().getParentLayer());
            consumer.accept(wrappedFileObject);
            Assert.assertNotNull("getParentLayer() 2", wrappedFileObject.getFileSystem().getParentLayer());
        }
    }

    @Test
    public void testZipParentLayer_exists() throws Exception {
        testZipParentLayer(fileObject -> fileObject.exists());
    }

    @Test
    public void testZipParentLayer_exists_getContents() throws Exception {
        testZipParentLayer(fileObject -> {
            fileObject.exists();
            fileObject.getContent();
        });
    }

    @Test
    public void testZipParentLayer_getChildren() throws Exception {
        testZipParentLayer(fileObject -> fileObject.getParent().getChildren());
    }

    @Test
    public void testZipParentLayer_getContents() throws Exception {
        testZipParentLayer(fileObject -> fileObject.getContent());
    }

    @Test
    public void testZipParentLayer_getType() throws Exception {
        testZipParentLayer(fileObject -> fileObject.getType());
    }

    @Test
    public void testZipParentLayer_isAttached() throws Exception {
        testZipParentLayer(fileObject -> fileObject.isAttached());
    }

    @Test
    public void testZipParentLayer_isContentOpen() throws Exception {
        testZipParentLayer(fileObject -> fileObject.isContentOpen());
    }

    @Test
    public void testZipParentLayer_isExecutable() throws Exception {
        testZipParentLayer(fileObject -> fileObject.isExecutable());
    }

    @Test
    public void testZipParentLayer_isFile() throws Exception {
        testZipParentLayer(fileObject -> fileObject.isFile());
    }

    @Test
    public void testZipParentLayer_isFolder() throws Exception {
        testZipParentLayer(fileObject -> fileObject.isFolder());
    }

    @Test
    public void testZipParentLayer_isHidden() throws Exception {
        testZipParentLayer(fileObject -> fileObject.isHidden());
    }

    @Test
    public void testZipParentLayer_isReadable() throws Exception {
        testZipParentLayer(fileObject -> fileObject.isReadable());
    }

    @Test
    public void testZipParentLayer_isWriteable() throws Exception {
        testZipParentLayer(fileObject -> fileObject.isWriteable());
    }
}
