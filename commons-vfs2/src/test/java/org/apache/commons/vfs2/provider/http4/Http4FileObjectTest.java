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
package org.apache.commons.vfs2.provider.http4;

import java.net.URI;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Http4FileObjectTest {

    private static final String HTTP4_URL = "http4://www.apache.org/licenses/LICENSE-2.0.txt";
    private static final String INTERNAL_HTTP4_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt";

    private static final String HTTP4S_URL = "http4s://www.apache.org/licenses/LICENSE-2.0.txt";
    private static final String INTERNAL_HTTP4S_URL = "https://www.apache.org/licenses/LICENSE-2.0.txt";

    @Test
    public void testHttp4FileObjectURLs() throws FileSystemException {
        final FileSystemManager fsm = VFS.getManager();
        final FileObject file = fsm.resolveFile(HTTP4_URL);

        assertEquals(HTTP4_URL, file.getURL().toString());
        assertTrue(file instanceof Http4FileObject);

        @SuppressWarnings("rawtypes")
        final Http4FileObject http4File = (Http4FileObject) file;
        assertEquals(URI.create(INTERNAL_HTTP4_URL), http4File.getInternalURI());
    }

    @Test
    public void testHttp4sFileObjectURLs() throws FileSystemException {
        final FileSystemManager fsm = VFS.getManager();
        final FileObject file = fsm.resolveFile(HTTP4S_URL);

        assertEquals(HTTP4S_URL, file.getURL().toString());
        assertTrue(file instanceof Http4FileObject);

        @SuppressWarnings("rawtypes")
        final Http4FileObject http4File = (Http4FileObject) file;
        assertEquals(URI.create(INTERNAL_HTTP4S_URL), http4File.getInternalURI());
    }
}
