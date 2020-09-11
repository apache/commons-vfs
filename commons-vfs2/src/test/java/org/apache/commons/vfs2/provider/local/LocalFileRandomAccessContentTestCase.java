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
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs2.util.RandomAccessMode;
import org.junit.Assert;
import org.junit.Test;

/**
 * @since 2.7.0
 */
public class LocalFileRandomAccessContentTestCase {

    private final int EOF = -1;

    /**
     * test LocalFileRandomAccessContent InputStream read one byte 0xff; see VFS-624
     **/
    @Test
    public void testInputStreamRead0xff() throws IOException {
        // open test file,this file has only one byte data 0xff
        final File file = new File("src/test/resources/test-data/0xff_file.txt");

        // read test data,first data should be 0xFF instead of -1. Will read -1 finally (EOF)
        try (InputStream in = new LocalFileRandomAccessContent(file, RandomAccessMode.READ).getInputStream()) {
            // read first data
            final int read = in.read();
            Assert.assertNotEquals(EOF, read);
            Assert.assertEquals(0xFF, read);

            // read EOF
            Assert.assertEquals(EOF, in.read());
        }
    }
}
