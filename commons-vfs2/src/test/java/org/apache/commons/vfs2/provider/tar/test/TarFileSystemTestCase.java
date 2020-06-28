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
package org.apache.commons.vfs2.provider.tar.test;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.cache.WeakRefFilesCache;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class TarFileSystemTestCase {

    @Test
    public void testTarFileUseWeakRefFilesCache() throws FileSystemException {
        testUseWeakRefFilesCache("tar", "src/test/resources/test-data/test.tar");
    }

    @Test
    public void testTgzFileUseWeakRefFilesCache() throws FileSystemException {
        testUseWeakRefFilesCache("tgz", "src/test/resources/test-data/test.tgz");
    }

    @Test
    public void testTbz2FileUseWeakRefFilesCache() throws FileSystemException {
        testUseWeakRefFilesCache("tbz2", "src/test/resources/test-data/test.tbz2");
    }

    /**
     * set file system's file cache use WeakReference, and test resolve file after gc
     **/
    private void testUseWeakRefFilesCache(String scheme, String filePath) throws FileSystemException {

        String fileUri = scheme + ":file:" + new File(filePath).getAbsolutePath();
        FileObject fileObject = null;

        try (StandardFileSystemManager manager = new StandardFileSystemManager()) {
            // set file system's file cache use WeakReference, and init file system
            manager.setFilesCache(new WeakRefFilesCache());
            manager.init();

            int cnt = 0;
            while (cnt < 100000) {
                cnt++;

                // resolve file, assert fileObject exist. clear fileObject to null and wait gc
                fileObject = manager.resolveFile(fileUri);
                Assert.assertTrue(fileObject.exists());
                fileObject = null;

                // every 200 times suggest one gc
                if (cnt % 200 == 0) {
                    System.gc();
                }
            }
        }
    }
}
