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
package org.apache.commons.vfs2.provider.ram;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.junit.Assert;
import org.junit.Test;

/**
 * @since 2.7.0
 */
public class RamFileRandomAccessContentTestCase {

    private final int EOF = -1;

    @Test
    public void testInputStreamRead0xff() throws IOException {

        // create ram file to test
        final FileObject file = VFS.getManager().resolveFile("ram://file");
        file.createFile();

        // write test data,a single byte 0xFF
        try (OutputStream out = file.getContent().getOutputStream()) {
            out.write(0xFF);
        }

        // read test data,first data should be 0xFF instead of -1. Will read -1 finally (EOF)
        try (InputStream in = new RamFileRandomAccessContent((RamFileObject) file, RandomAccessMode.READ).getInputStream()) {
            // read first data
            final int read = in.read();
            Assert.assertNotEquals(EOF, read);
            Assert.assertEquals(0xFF, read);

            // read EOF
            Assert.assertEquals(EOF, in.read());
        }
    }
}
