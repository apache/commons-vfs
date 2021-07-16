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
package org.apache.commons.vfs2.provider.tar;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.Test;


public class TarFileObjectTestCase {

    private void testReadSpecialNameFileInFile(final String testFilePath, final String scheme) throws FileSystemException {

        final File testFile = new File(testFilePath);
        final String[] fileNames = {"file.txt", "file^.txt", "file~.txt", "file?.txt", "file@.txt", "file$.txt",
                                    "file*.txt", "file&.txt", "file#.txt", "file%.txt", "file!.txt"};
        final FileSystemManager manager = VFS.getManager();
        final String baseUrl = scheme + ":file:" + testFile.getAbsolutePath();

        // test
        try (final FileObject fileObject = manager.resolveFile(baseUrl)) {
            // test getChildren() number equal
            Assert.assertEquals(fileObject.getChildren().length, fileNames.length);

            // test getChild(String)
            for (final String fileName : fileNames) {
                Assert.assertNotNull("can't read file " + fileName, fileObject.getChild(fileName));
            }
        }
    }

    /**
     * Test read file with special name in a tar file
     */
    @Test
    public void testReadSpecialNameFileInTarFile() throws FileSystemException {

        testReadSpecialNameFileInFile("src/test/resources/test-data/special_fileName.tar", "tar");
    }

    /**
     * Test read file with special name in a tbz2 file
     */
    @Test
    public void testReadSpecialNameFileInTbz2File() throws FileSystemException {

        testReadSpecialNameFileInFile("src/test/resources/test-data/special_fileName.tbz2", "tbz2");
    }

    /**
     * Test read file with special name in a tgz file
     */
    @Test
    public void testReadSpecialNameFileInTgzFile() throws FileSystemException {

        testReadSpecialNameFileInFile("src/test/resources/test-data/special_fileName.tgz", "tgz");
    }
}
