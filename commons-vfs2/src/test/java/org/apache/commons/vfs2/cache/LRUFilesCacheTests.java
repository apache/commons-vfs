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

import org.apache.commons.vfs2.FileObject;

/**
 * Tests for {@link LRUFilesCache} used by {@link LRUFilesCacheTestCase}.
 */
public class LRUFilesCacheTests extends AbstractFilesCacheTestsBase {
    public void testFilesCache() throws Exception {
        final FileObject scratchFolder = getWriteFolder();

        // releaseable
        final FileObject dir1 = scratchFolder.resolveFile("dir1");

        // avoid cache removal
        final FileObject dir2 = scratchFolder.resolveFile("dir2");
        dir2.getContent();

        // releaseable
        @SuppressWarnings("unused")
        final FileObject dir3 = scratchFolder.resolveFile("dir3");

        // releaseable
        @SuppressWarnings("unused")
        final FileObject dir4 = scratchFolder.resolveFile("dir4");

        // releaseable
        @SuppressWarnings("unused")
        final FileObject dir5 = scratchFolder.resolveFile("dir5");

        // releaseable
        @SuppressWarnings("unused")
        final FileObject dir6 = scratchFolder.resolveFile("dir6");

        // releaseable
        @SuppressWarnings("unused")
        final FileObject dir7 = scratchFolder.resolveFile("dir7");

        // releaseable
        @SuppressWarnings("unused")
        final FileObject dir8 = scratchFolder.resolveFile("dir8");

        // check if the cache still holds the right instance
        final FileObject dir2_2 = scratchFolder.resolveFile("dir2");
        assertTrue(dir2 == dir2_2);

        // check if the cache still holds the right instance
        final FileObject dir1_2 = scratchFolder.resolveFile("dir1");
        assertFalse(dir1 == dir1_2);
    }

    public void testClass() {
        assertTrue(getManager().getFilesCache() instanceof LRUFilesCache);
    }
}
