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
package org.apache.commons.vfs2.provider.local;

import java.io.File;
import java.net.URI;

import org.apache.commons.vfs2.AbstractProviderTestCase;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.junit.Test;

/**
 * Additional naming tests for local file system.
 */
public class TempFileTests extends AbstractProviderTestCase {

    /**
     * https://issues.apache.org/jira/browse/VFS-790
     */
    @Test
    public void testLocalFile() throws Exception {
        final String prefix = "\u0074\u0065\u0074";
        final File file = File.createTempFile(prefix + "-", "-" + prefix);
        assertTrue(file.exists());
        final URI uri = file.toURI();
        try (final FileSystemManager manager = getManager()) {
            try (final FileObject fileObject = manager.resolveFile(uri)) {
                try (final FileContent sourceContent = fileObject.getContent()) {
                    assertEquals(sourceContent.getSize(), file.length());
                }
            }
        }
    }
}
