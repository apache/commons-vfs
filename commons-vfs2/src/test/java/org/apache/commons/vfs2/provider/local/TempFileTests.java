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
package org.apache.commons.vfs2.provider.local;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.vfs2.AbstractProviderTestCase;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.junit.jupiter.api.Test;

/**
 * Additional naming tests for local file system.
 */
public class TempFileTests extends AbstractProviderTestCase {

    /**
     * https://issues.apache.org/jira/browse/VFS-790.
     */
    @Test
    public void testLocalFile() throws Exception {
        final String prefix = "\u0074\u0065\u0074";
        final Path file = Files.createTempFile(prefix + "-", "-" + prefix);
        assertTrue(Files.exists(file));
        final URI uri = file.toUri();
        try (FileSystemManager manager = getManager()) {
            try (FileObject fileObject = manager.resolveFile(uri)) {
                try (FileContent sourceContent = fileObject.getContent()) {
                    assertEquals(sourceContent.getSize(), Files.size(file));
                }
            }
        }
    }

}
