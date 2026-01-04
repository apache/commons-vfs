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
package org.apache.commons.vfs2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests FileTypeSelector.
 */
public class FileTypeSelectorTest {

    private static FileObject baseFolder;

    /**
     * Creates a RAM FS.
     *
     * @throws Exception
     */
    @BeforeAll
    public static void setUpClass() throws Exception {
        baseFolder = VFS.getManager().resolveFile("ram://" + FileTypeSelectorTest.class.getName());
        baseFolder.resolveFile("root1.html").createFile();
        baseFolder.resolveFile("root2.html").createFile();
        baseFolder.resolveFile("f1/a.html").createFile();
        baseFolder.resolveFile("f2/b.html").createFile();
        baseFolder.resolveFile("f3/c.html").createFile();
        baseFolder.resolveFile("f4/").createFolder();
        baseFolder.resolveFile("f5/").createFolder();
        baseFolder.resolveFile("f6/f7").createFolder();
    }

    /**
     * Deletes RAM FS files.
     *
     * @throws Exception
     */
    @AfterAll
    public static void tearDownClass() throws Exception {
        if (baseFolder != null) {
            baseFolder.deleteAll();
        }
    }

    @ParameterizedTest(name = "FileType.{0} should find {1} items")
    @CsvSource({
        "FILE_OR_FOLDER, 0",
        "FILE, 5",
        "FOLDER, 8"
    })

    /** Test file type selector with different file types */
    public void testFileTypeSelector(final FileType fileType, final int expectedCount) throws Exception {
        final FileSelector selector = new FileTypeSelector(fileType);
        final FileObject[] foList = baseFolder.findFiles(selector);
        assertEquals(expectedCount, foList.length,
            () -> "FileType." + fileType + " should find " + expectedCount + " items");
    }

}
