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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileObjectContentOutputStreamCloseTest {

    private static Path TEMP_FILE_PATH;

    @BeforeClass
    public static void before() throws IOException {
        TEMP_FILE_PATH = Files.createTempFile("org.apache.commons.vfs2", ".txt");
    }

    @AfterClass
    public static void after() {
        try {
            VFS.getManager().resolveFile(TEMP_FILE_PATH.toUri()).delete();
        } catch (Exception ignored) {

        }
    }

    @Test
    public void test() throws IOException {
        Path tempFilePath = Files.createTempFile("org.apache.commons.vfs2", ".txt");
        FileObject fileObject = VFS.getManager().resolveFile(tempFilePath.toUri());
        try (
                OutputStream outputStream = fileObject.getContent().getOutputStream();
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(bufferedOutputStream)
        ) {
            outputStreamWriter.write("org.apache.commons.vfs2");
        }
    }

}
