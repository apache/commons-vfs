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
package org.apache.commons.vfs2.provider.tar;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FilesCache;
import org.apache.commons.vfs2.cache.WeakRefFilesCache;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.junit.Assert;
import org.junit.Test;

public class TarFileSystemTestCase {

    @Test
    public void testTarFileUseDefaultFilesCache() throws FileSystemException {
        testUseWeakRefFilesCache("tar", "src/test/resources/test-data/test.tar", null);
    }

    @Test
    @SuppressWarnings("resource") // Managed by the file system, no need to close.
    public void testTarFileUseWeakRefFilesCache() throws FileSystemException {
        testUseWeakRefFilesCache("tar", "src/test/resources/test-data/test.tar", new WeakRefFilesCache());
    }

    @Test
    public void testTbz2FileUseDefautlFilesCache() throws FileSystemException {
        testUseWeakRefFilesCache("tbz2", "src/test/resources/test-data/test.tbz2", null);
    }

    @Test
    @SuppressWarnings("resource") // Managed by the file system, no need to close.
    public void testTbz2FileUseWeakRefFilesCache() throws FileSystemException {
        testUseWeakRefFilesCache("tbz2", "src/test/resources/test-data/test.tbz2", new WeakRefFilesCache());
    }

    @Test
    public void testTgzFileUseDefaultFilesCache() throws FileSystemException {
        testUseWeakRefFilesCache("tgz", "src/test/resources/test-data/test.tgz", null);
    }

    @Test
    @SuppressWarnings("resource") // Managed by the file system, no need to close.
    public void testTgzFileUseWeakRefFilesCache() throws FileSystemException {
        testUseWeakRefFilesCache("tgz", "src/test/resources/test-data/test.tgz", new WeakRefFilesCache());
    }

    /**
     * Sets a file system's file cache to use WeakReference, and test resolve file after GC.
     *
     * @param filesCache TODO
     */
    private void testUseWeakRefFilesCache(final String scheme, final String filePath, final FilesCache filesCache)
        throws FileSystemException {

        final String fileUri = scheme + ":file:" + new File(filePath).getAbsolutePath();
        FileObject fileObject = null;

        try (final StandardFileSystemManager manager = new StandardFileSystemManager()) {
            if (filesCache != null) {
                manager.setFilesCache(filesCache);
            }
            manager.init();

            int cnt = 0;
            while (cnt < 100_000) {
                cnt++;

                // resolve file, assert fileObject exist. clear fileObject to null and wait GC.
                try {
                    fileObject = manager.resolveFile(fileUri);
                    Assert.assertTrue(fileObject.exists());
                } finally {
                    if (fileObject != null) {
                        fileObject.close();
                    }
                    fileObject = null;
                }

                // every 200 times suggest one gc
                if (cnt % 200 == 0) {
                    System.gc();
                }
            }
        }
    }
}
