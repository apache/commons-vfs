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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FileObjectTest {

    StandardFileSystemManager fileSystemManager;

    {
        try {
            fileSystemManager = loadFileSystemManager();
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
    }

    interface IOExceptionThrowingFunction<T, R> {
        R apply(T t) throws IOException;
    }

    /**
     * Expected contents of test files
     */
    public static final String TEST_FILE_CONTENT = "aaa";

    /**
     * Test file paths
     */
    public static final String[] TEST_FILE_PATHS = new String[]{
            "src/test/resources/test-data/好.txt",
            "src/test/resources/test-data/1 1.txt",
    };

    private static StandardFileSystemManager loadFileSystemManager() throws FileSystemException {
        StandardFileSystemManager fileSystemManager = new StandardFileSystemManager();
        fileSystemManager.setLogger(null);
        fileSystemManager.init();
        fileSystemManager.setBaseFile(new File(System.getProperty("user.dir")));
        return fileSystemManager;
    }

    @Test
    public void testToFile() throws IOException {
        testToFile(fileObject -> fileObject.getPath().toFile());
    }

    @Test
    public void testToFile2() throws IOException {
        testToFile(FileObjectTest::toFile2);
    }

    @Test
    public void testEqualsURI() throws FileSystemException {
        for (String testFilePath : TEST_FILE_PATHS) {
            FileObject fileObject = fileSystemManager.resolveFile(testFilePath);
            assertNotNull(fileObject);
            File file = new File(testFilePath).getAbsoluteFile();
            URI fileURI = file.getAbsoluteFile().toURI();
            URI fileObjectURI = fileObject.getURI();
            assertEquals(fileObjectURI, fileURI);
        }
    }

    @Test
    public void testEqualsPaths() throws FileSystemException {
        for (String testFilePath : TEST_FILE_PATHS) {
            FileObject fileObject = fileSystemManager.resolveFile(testFilePath);
            assertNotNull(fileObject);
            File file = new File(testFilePath).getAbsoluteFile();
            Path filePath = file.toPath();
            Path fileObjectPath = fileObject.getPath();
            assertEquals(filePath, fileObjectPath);
        }
    }

    private void testToFile(IOExceptionThrowingFunction<FileObject, File> function) throws IOException {
        for (String testFilePath : TEST_FILE_PATHS) {
            testToFile(function, testFilePath);
        }
    }

    private void testToFile(IOExceptionThrowingFunction<FileObject, File> function, String testFilePath) throws IOException {
        FileObject fileObject = fileSystemManager.resolveFile(testFilePath);
        assertNotNull(fileObject);
        try (InputStream inputStream = fileObject.getContent().getInputStream()) {
            assertEquals(TEST_FILE_CONTENT, IOUtils.toString(inputStream, StandardCharsets.UTF_8));
        }
        File file = function.apply(fileObject);
        assertNotNull(file);
        assertEquals(TEST_FILE_CONTENT, FileUtils.readFileToString(file, StandardCharsets.UTF_8));
    }

    private static File toFile2(FileObject fileObject) throws FileSystemException {
        if (fileObject == null || !"file".equals(fileObject.getURL().getProtocol())) {
            return null;
        }
        return new File(fileObject.getName().getPathDecoded());
    }

    /**
     * please only invoke this when you want to figure out what special characters
     * is not allowed in Path but allowed in File.
     */
    @Ignore
    @Test
    public void testFull() throws Exception {
        String folderString = "target/testTempOutput/FileObjectTest/";
        File folderFile = new File(folderString);
        folderFile.mkdirs();
        for (char c = 0; c < Character.MAX_VALUE; c++) {
            String fileString = folderFile.getAbsolutePath() + '/' + c + ".txt";
            File file = new File(fileString);
            try {
                file.createNewFile();
            } catch (Exception ignored) {
                continue;
            }
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(TEST_FILE_CONTENT);
            }
            try {
                testToFile(fileObject -> fileObject.getPath().toFile(), file.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("fails to use FileObject on : c = " + (int) c + " draws = " + c);
            }
        }
    }
}
