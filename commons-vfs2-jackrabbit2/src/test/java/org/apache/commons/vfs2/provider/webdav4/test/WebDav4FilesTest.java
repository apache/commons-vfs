/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.provider.webdav4.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.jupiter.api.Test;

/**
 * Tests https://issues.apache.org/jira/browse/VFS-810.
 */
public class WebDav4FilesTest {

    @Test
    public void testUrlWithAuthority() throws FileSystemException {
        @SuppressWarnings("resource")
        final FileSystemManager fileSystemManager = VFS.getManager();

        // TODO All lowercase input except the percent encoded '\' (%5C);
        // We end up converting back to lowercase, but OK per RFC.
        final String urlWithAuthority = "webdav4://alice%5C1234:secret@localhost:80";
        try (final FileObject file = fileSystemManager.resolveFile(urlWithAuthority)) {
            assertEquals("webdav4://alice%5c1234:secret@localhost/", file.getURL().toExternalForm());
        }
    }
}
