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
import org.apache.commons.vfs2.cache.WeakRefFilesCache;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.junit.Assert;
import org.junit.Test;

public class ZipFileSystemTestCase {

    /**
     * Sets a file system's file cache to use WeakReference, and test resolve file after GC.
     */
    @Test
    public void testZipFileUseWeakRefFilesCache() throws FileSystemException {

        final File file = new File("src/test/resources/test-data/test.zip");
        final String fileUri = "zip:file:" + file.getAbsolutePath();
        FileObject fileObject = null;

        try (StandardFileSystemManager manager = new StandardFileSystemManager()) {
            // set file system's file cache use WeakReference, and init file system
            @SuppressWarnings("resource") // Managed by the file system, no need to close.
            final WeakRefFilesCache filesCache = new WeakRefFilesCache();
            manager.setFilesCache(filesCache);
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