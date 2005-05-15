/*
 * Copyright 2002-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs.test;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.RandomAccessContent;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.util.RandomAccessMode;

/**
 * RanomdRead/Write test cases for file providers.
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date$
 */
public class ProviderRandomReadWriteTests
    extends AbstractProviderTestCase
{
    private final String TEST_DATA = "This is a test file.";

    /**
     * Returns the capabilities required by the tests of this test case.
     */
    protected Capability[] getRequiredCaps()
    {
        return new Capability[]
        {
            Capability.GET_TYPE,
            Capability.CREATE,
            Capability.RANDOM_ACCESS_READ,
            Capability.RANDOM_ACCESS_WRITE
        };
    }

    /**
     * Sets up a scratch folder for the test to use.
     */
    protected FileObject createScratchFolder() throws Exception
    {
        FileObject scratchFolder = getWriteFolder();

        // Make sure the test folder is empty
        scratchFolder.delete(Selectors.EXCLUDE_SELF);
        scratchFolder.createFolder();

        return scratchFolder;
    }

    /**
     * Read a file
     */
    public void testRandomWrite() throws Exception
    {
        FileObject file = null;
        try
        {
            file = createScratchFolder().resolveFile("random_write.txt");
            file.createFile();
            RandomAccessContent ra = file.getContent().getRandomAccessContent(RandomAccessMode.READWRITE);

            // write first byte
            ra.writeByte(TEST_DATA.charAt(0));

            // start at pos 4
            ra.seek(3);
            ra.writeByte(TEST_DATA.charAt(3));
            ra.writeByte(TEST_DATA.charAt(4));

            // restart at pos 4 (but overwrite with different content)
            ra.seek(3);
            ra.writeByte(TEST_DATA.charAt(7));
            ra.writeByte(TEST_DATA.charAt(8));

            // advance to pos 11
            ra.seek(10);
            ra.writeByte(TEST_DATA.charAt(10));
            ra.writeByte(TEST_DATA.charAt(11));

            // now read
            ra.seek(0);
            assertEquals(ra.readByte(), TEST_DATA.charAt(0));

            ra.seek(3);
            assertEquals(ra.readByte(), TEST_DATA.charAt(7));
            assertEquals(ra.readByte(), TEST_DATA.charAt(8));

            ra.seek(10);
            assertEquals(ra.readByte(), TEST_DATA.charAt(10));
            assertEquals(ra.readByte(), TEST_DATA.charAt(11));
        }
        finally
        {
            if (file != null)
            {
                file.close();
            }
        }
    }
}
