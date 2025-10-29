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
package org.apache.commons.vfs2.provider.ftp;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Duration;

import org.apache.commons.vfs2.AbstractProviderTestSuite;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.LastModifiedTests;
import org.junit.jupiter.api.Test;

public class FtpMdtmOffLastModifiedTests extends LastModifiedTests {

    /**
     * Tests getting the last modified time of a file.
     */
    @Test
    public void testGetLastModifiedFileInexactMatch() throws IOException {
        final String fileName = "file1.txt";
        getFileSystem().getFileSystemManager().getFilesCache().clear(getFileSystem());
        final FileObject readFolder = getReadFolder();
        final FileObject fileObject = readFolder.resolveFile(fileName);
        final long lastModifiedTimeMillis = fileObject.getContent().getLastModifiedTime();
        // now try to match
        final long lastModTimeAccuracyMillis = (long) readFolder.getFileSystem().getLastModTimeAccuracy();
        final FileTime lastModifiedTime = Files
            .getLastModifiedTime(Paths.get(getTestDirectory(), AbstractProviderTestSuite.READ_TESTS_FOLDER, fileName));
        assertDeltaMillis("getLastModified on File", lastModifiedTime.toMillis(), lastModifiedTimeMillis,
            Math.max(lastModTimeAccuracyMillis, Duration.ofMinutes(1).toMillis()));
    }

}
