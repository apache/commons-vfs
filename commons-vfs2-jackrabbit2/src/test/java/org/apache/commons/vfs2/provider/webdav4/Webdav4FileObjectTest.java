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
package org.apache.commons.vfs2.provider.webdav4;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Test;

public class Webdav4FileObjectTest {

    private static final String WEBDAV4_URL = "webdav4://www.apache.org/licenses/LICENSE-2.0.txt";
    private static final String INTERNAL_WEBDAV4_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt";

    private static final String WEBDAV4S_URL = "webdav4s://www.apache.org/licenses/LICENSE-2.0.txt";
    private static final String INTERNAL_WEBDAV4S_URL = "https://www.apache.org/licenses/LICENSE-2.0.txt";

    @Test
    public void testWebdav4FileObjectURLs() throws FileSystemException {
        final FileSystemManager fsm = VFS.getManager();
        try (final FileObject file = fsm.resolveFile(WEBDAV4_URL)) {

            assertEquals(WEBDAV4_URL, file.getURL().toString());
            assertTrue(file instanceof Webdav4FileObject);

            final Webdav4FileObject webdav4File = (Webdav4FileObject) file;
            assertEquals(URI.create(INTERNAL_WEBDAV4_URL), webdav4File.getInternalURI());
        }
    }

    @Test
    public void testWebdav4sFileObjectURLs() throws FileSystemException {
        final FileSystemManager fsm = VFS.getManager();
        try (final FileObject file = fsm.resolveFile(WEBDAV4S_URL)) {

            assertEquals(WEBDAV4S_URL, file.getURL().toString());
            assertTrue(file instanceof Webdav4FileObject);

            final Webdav4FileObject webdav4File = (Webdav4FileObject) file;
            assertEquals(URI.create(INTERNAL_WEBDAV4S_URL), webdav4File.getInternalURI());
        }
    }
}
