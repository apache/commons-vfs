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
package org.apache.commons.vfs2.provider.webdav4.test;

import junit.framework.TestCase;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests https://issues.apache.org/jira/browse/VFS-810
 */
public class WebDav4FilesTestCase extends TestCase {

    /**
     * Tests https://issues.apache.org/jira/browse/VFS-810
     */
    @Test
    public void testUrlWithAuthority() throws FileSystemException {
        final String urlWithAuthority = "webdav4://alice\\1234:secret@localhost:80";

        final FileSystemManager fileSystemManager = VFS.getManager();

        final FileObject file = fileSystemManager.resolveFile(urlWithAuthority);
        Assert.assertEquals("webdav4://alice\\1234:secret@localhost/", file.getURL().toExternalForm());
    }
}
