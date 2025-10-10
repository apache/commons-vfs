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

package org.apache.commons.vfs2.provider.gzip;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GzipTest {

    @Test
    public void testCreateGzipFileSystem() throws IOException {
        final File gzFile = new File("src/test/resources/test-data/好.txt.gz");
        @SuppressWarnings("resource") // global
        final FileSystemManager manager = VFS.getManager();

        try (FileObject localFileObject = manager.resolveFile(gzFile.getAbsolutePath());
                FileObject gzFileObjectDir = manager.createFileSystem(localFileObject);
                FileObject gzFileObject = gzFileObjectDir.resolveFile("好.txt")) {
            Assertions.assertInstanceOf(GzipFileObject.class, gzFileObjectDir);
            Assertions.assertTrue(gzFileObjectDir.isFolder());
            Assertions.assertInstanceOf(GzipFileObject.class, gzFileObject);
            Assertions.assertFalse(gzFileObject.isFolder());
            try (FileContent content = gzFileObject.getContent()) {
                Assertions.assertEquals("aaa", content.getString(StandardCharsets.UTF_8));
            }
        }
    }
}
