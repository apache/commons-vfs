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
package org.apache.commons.vfs2.cache;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FilesCache;
import org.apache.commons.vfs2.test.AbstractProviderTestCase;
import org.apache.commons.vfs2.test.CacheTestSuite;

/**
 * Base class for different FilesCache tests.
 * <p>
 * Make sure {@link CacheTestSuite} is configured with correct FilesCache.
 */
public abstract class AbstractFilesCacheTestsBase extends AbstractProviderTestCase {
    /**
     * Will test if the cache is cleared and if it is still useable afterwards. It will actually ensure the test is
     * hitting the cache.
     */
    public void testClearFiles() throws Exception {
        final FilesCache cache = getManager().getFilesCache();

        final FileObject fo1 = getWriteFolder().resolveFile("dir1");

        // clean the cache for this file system
        cache.clear(fo1.getFileSystem());
        // make sure a empty cache clean does not fail
        cache.clear(fo1.getFileSystem());

        final FileObject fo2 = getWriteFolder().resolveFile("dir1");

        assertFalse("Objects after cache clear should be different", fo1 == fo2);
    }

    /**
     * Basic Cache operations, work for all caches (besides {@link NullFilesCache#testBasicCacheOps() NullFilesCache}).
     */
    public void testBasicCacheOps() throws Exception {
        final FilesCache cache = getManager().getFilesCache();
        final FileObject fo = getWriteFolder().resolveFile("dir1");
        final FileName fn = fo.getName();
        final FileSystem fs = fo.getFileSystem();

        cache.clear(fs);
        assertNull(cache.getFile(fs, fn));

        cache.putFile(fo);
        assertSame(fo, cache.getFile(fs, fn));

        assertFalse(cache.putFileIfAbsent(fo));
        cache.clear(fs);
        assertNull(cache.getFile(fs, fn));
        assertTrue(cache.putFileIfAbsent(fo));

        cache.removeFile(fs, fn);
        assertNull(cache.getFile(fs, fn));
        assertTrue(cache.putFileIfAbsent(fo));
    }

    /** Helper method, may be used in cache specific tests. */
    protected int getFileHashCode() throws FileSystemException {
        final FileObject fo = getWriteFolder().resolveFile("file2");
        if (!fo.exists()) {
            fo.createFile();
        }

        return fo.hashCode();
    }
}
