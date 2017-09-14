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
package org.apache.commons.vfs2.test;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.util.RandomAccessMode;

/**
 * Random read-only test case for file providers.
 *
 * @version $Id$
 */
public class ProviderRandomReadTests extends AbstractProviderTestCase {
    private static final String TEST_DATA = "This is a test file.";

    /**
     * Returns the capabilities required by the tests of this test case.
     */
    @Override
    protected Capability[] getRequiredCaps() {
        return new Capability[] { Capability.GET_TYPE, Capability.RANDOM_ACCESS_READ };
    }

    /**
     * Read a file
     */
    public void testRandomRead() throws Exception {
        FileObject file = null;
        try {
            file = getReadFolder().resolveFile("file1.txt");
            final RandomAccessContent ra = file.getContent().getRandomAccessContent(RandomAccessMode.READ);

            // read first byte
            byte c = ra.readByte();
            assertEquals(c, TEST_DATA.charAt(0));
            assertEquals("fp", ra.getFilePointer(), 1);

            // start at pos 4
            ra.seek(3);
            c = ra.readByte();
            assertEquals(c, TEST_DATA.charAt(3));
            assertEquals("fp", ra.getFilePointer(), 4);

            c = ra.readByte();
            assertEquals(c, TEST_DATA.charAt(4));
            assertEquals("fp", ra.getFilePointer(), 5);

            // restart at pos 4
            ra.seek(3);
            c = ra.readByte();
            assertEquals(c, TEST_DATA.charAt(3));
            assertEquals("fp", ra.getFilePointer(), 4);

            c = ra.readByte();
            assertEquals(c, TEST_DATA.charAt(4));
            assertEquals("fp", ra.getFilePointer(), 5);

            // advance to pos 11
            ra.seek(10);
            c = ra.readByte();
            assertEquals(c, TEST_DATA.charAt(10));
            assertEquals("fp", ra.getFilePointer(), 11);

            c = ra.readByte();
            assertEquals(c, TEST_DATA.charAt(11));
            assertEquals("fp", ra.getFilePointer(), 12);
        } finally {
            if (file != null) {
                file.close();
            }
        }
    }
}
