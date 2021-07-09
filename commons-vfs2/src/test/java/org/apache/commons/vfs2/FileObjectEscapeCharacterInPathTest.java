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

package org.apache.commons.vfs2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.junit.Test;

public class FileObjectEscapeCharacterInPathTest {

    private static final String REL_PATH_GREAT = "src/test/resources/test-data/好.txt";

    private static final String REL_PATH_SPACE = "src/test/resources/test-data/1 1.txt";

    /**
     * Expected contents of test files.
     */
    public static final String TEST_FILE_CONTENT = "aaa";

    /**
     * Test file paths.
     */
    public static final String[] TEST_FILE_PATHS = new String[] {REL_PATH_SPACE, REL_PATH_GREAT};

    private static StandardFileSystemManager loadFileSystemManager() throws FileSystemException {
        StandardFileSystemManager fileSystemManager = new StandardFileSystemManager();
        fileSystemManager.setLogger(null);
        fileSystemManager.init();
        fileSystemManager.setBaseFile(SystemUtils.getUserDir());
        return fileSystemManager;
    }

    private static File toFile2(FileObject fileObject) throws FileSystemException {
        if (fileObject == null || !"file".equals(fileObject.getURL().getProtocol())) {
            return null;
        }
        return new File(fileObject.getName().getPathDecoded());
    }

    @SuppressWarnings("resource")
    private void testProviderGetPath(String relPathStr) throws URISyntaxException {
        FileSystems.getDefault().provider().getPath(new URI(Paths.get(relPathStr).toAbsolutePath().toUri().toString()));
    }

    /**
     * Tests a path with the Chinese character 好.
     */
    @Test
    public void testProviderGetPathGreat() throws URISyntaxException {
        testProviderGetPath(REL_PATH_GREAT);
    }

    /**
     * Tests a path with the space character.
     */
    @Test
    public void testProviderGetPathSpace() throws URISyntaxException {
        testProviderGetPath(REL_PATH_SPACE);
    }

    @Test
    public void testToFile() throws IOException {
        testToFile(fileObject -> fileObject.getPath().toFile());
    }

    private void testToFile(FailableFunction<FileObject, File, IOException> function) throws IOException {
        for (String testFilePath : TEST_FILE_PATHS) {
            try (FileSystemManager fileSystemManager = loadFileSystemManager();
                FileObject fileObject = fileSystemManager.resolveFile(testFilePath)) {
                assertNotNull(fileObject);
                try (final FileContent content = fileObject.getContent();
                    InputStream inputStream = content.getInputStream()) {
                    assertEquals(TEST_FILE_CONTENT, IOUtils.toString(inputStream, StandardCharsets.UTF_8));
                }
                File file = function.apply(fileObject);
                assertNotNull(file);
                assertEquals(TEST_FILE_CONTENT, FileUtils.readFileToString(file, StandardCharsets.UTF_8));
            }
        }
    }

    @Test
    public void testToFile2() throws IOException {
        testToFile(FileObjectEscapeCharacterInPathTest::toFile2);
    }
}
