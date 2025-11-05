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
package org.apache.commons.vfs2.provider.bzip2;

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


import static org.apache.commons.vfs2.VfsTestUtils.getTestResource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.compressed.CompressedFileFileObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Bzip2Test {

    @Test
    public void testBZip2() throws IOException {
        final File testResource = getTestResource("bla.txt.bz2");
        try (FileObject bz2FileObject = VFS.getManager().resolveFile("bz2://" + testResource)) {
            Assertions.assertTrue(bz2FileObject.exists());
            Assertions.assertTrue(bz2FileObject.isFolder());
            try (FileObject fileObjectDir = bz2FileObject.resolveFile("bla.txt")) {
                Assertions.assertTrue(fileObjectDir.exists());
                Assertions.assertTrue(bz2FileObject.isFolder());
                try (FileObject fileObject = fileObjectDir.resolveFile("bla.txt")) {
                    Assertions.assertTrue(fileObject.exists());
                    Assertions.assertFalse(fileObject.isFolder());
                    Assertions.assertTrue(fileObject.isFile());
                    try (FileContent content = fileObject.getContent()) {
                        Assertions.assertEquals(CompressedFileFileObject.SIZE_UNDEFINED, content.getSize());
                        // blows up, Commons Compress?
                        final String string = content.getString(StandardCharsets.UTF_8);
                        Assertions.assertEquals(26, string.length());
                        Assertions.assertEquals("Hallo, dies ist ein Test.\n", string);
                    }
                }
            }
        }
    }

}
