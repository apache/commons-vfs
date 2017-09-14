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
package org.apache.commons.vfs2.provider.local.test;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.test.AbstractProviderTestCase;

/**
 * Additional URL tests for local file system.
 *
 * @version $Revision$
 */
public class UrlTests extends AbstractProviderTestCase {
    /**
     * Tests FindFiles with a filename that has a hash sign in it.
     */
    public void testHashFindFiles() throws Exception {
        final FileSystemManager fsManager = VFS.getManager();

        final FileObject[] foList = getBaseFolder().findFiles(Selectors.SELECT_FILES);

        boolean hashFileFound = false;
        for (final FileObject fo : foList) {
            if (fo.getURL().toString().contains("test-hash")) {
                hashFileFound = true;

                assertEquals(fo.toString(), UriParser.decode(fo.getURL().toString()));
            }
        }

        if (!hashFileFound) {
            fail("Test hash file containing 'test-hash' not found");
        }
    }

    /**
     * Tests resolution of an absolute file name.
     */
    public void testHashURL() throws Exception {
        final FileObject file = getReadFolder().resolveFile("test-hash-#test.txt");

        assertEquals(file.toString(), UriParser.decode(file.getURL().toString()));
    }
}
