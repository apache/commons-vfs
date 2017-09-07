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

package org.apache.commons.vfs2.provider.zip.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.Test;

public class ZipFileObjectTestCase {

    /**
     * Tests that we can read more than one file within a Zip file, especially after closing each FileObject.
     * 
     * @throws IOException
     */
    @Test
    public void testReadingFilesInZipFile() throws IOException {
        final File zipFile = new File("src/test/resources/test-data/read-xml-tests.zip");
        final File newZipFile = File.createTempFile(getClass().getSimpleName(), ".zip");
        newZipFile.deleteOnExit();
        FileUtils.copyFile(zipFile, newZipFile);
        final FileSystemManager manager = VFS.getManager();
        try (final FileObject zipFileObject = manager.resolveFile("zip:file:" + newZipFile.getAbsolutePath())) {
            try (final FileObject zipFileObject1 = zipFileObject.resolveFile("/read-xml-tests/file1.xml")) {
                try (final InputStream inputStream = zipFileObject1.getContent().getInputStream()) {
                    readAndAssert(inputStream, "1");
                }
            }
            try (final FileObject zipFileObject2 = zipFileObject.resolveFile("/read-xml-tests/file2.xml")) {
                try (final InputStream inputStream = zipFileObject2.getContent().getInputStream()) {
                    readAndAssert(inputStream, "2");
                }
            }
        }
        assertDelete(newZipFile);
    }

    private void assertDelete(final File fileObject) {
        Assert.assertTrue("Could not delete file", fileObject.delete());
    }

    private void readAndAssert(final InputStream inputStream, final String expectedId) throws IOException {
        final String string = IOUtils.toString(inputStream, "UTF-8");
        Assert.assertNotNull(string);
        Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() + "<Root" + expectedId
                + ">foo" + expectedId + "</Root" + expectedId + ">" + System.lineSeparator(), string);
    }
}
