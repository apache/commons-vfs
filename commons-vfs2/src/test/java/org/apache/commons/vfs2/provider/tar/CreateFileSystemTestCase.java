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
import java.io.IOException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.Test;

/**
 * test use DefaultFileSystemManager.createFileSystem method to create tar,tgz,tbz2 file system
 *
 * @since 2.7.0
 **/
public class CreateFileSystemTestCase {

    private FileObject createFileSystem(final String testFilePath) throws IOException {

        final File testFile = new File(testFilePath);
        final FileSystemManager manager = VFS.getManager();

        // create fileSystem and return fileObject
        try (FileObject localFileObject = manager.resolveFile(testFile.getAbsolutePath())) {
            return manager.createFileSystem(localFileObject);
        }
    }

    @Test
    public void testTarFile() throws IOException {

        final String testFilePath = "src/test/resources/test-data/test.tar";
        try (FileObject fileObject = createFileSystem(testFilePath)) {
            Assert.assertTrue(fileObject instanceof TarFileObject);
        }
    }

    @Test
    public void testTbz2File() throws IOException {

        final String testFilePath = "src/test/resources/test-data/test.tbz2";
        try (FileObject fileObject = createFileSystem(testFilePath)) {
            Assert.assertTrue(fileObject instanceof TarFileObject);
        }
    }

    @Test
    public void testTgzFile() throws IOException {

        final String testFilePath = "src/test/resources/test-data/test.tgz";
        try (FileObject fileObject = createFileSystem(testFilePath)) {
            Assert.assertTrue(fileObject instanceof TarFileObject);
        }
    }
}
