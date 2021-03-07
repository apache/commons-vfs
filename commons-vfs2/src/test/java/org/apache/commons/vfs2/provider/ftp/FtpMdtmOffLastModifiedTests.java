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

package org.apache.commons.vfs2.provider.ftp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Duration;

import org.apache.commons.vfs2.AbstractTestSuite;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.LastModifiedTests;
import org.junit.Test;

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
            .getLastModifiedTime(Paths.get(getTestDirectory(), AbstractTestSuite.READ_TESTS_FOLDER, fileName));
        assertDeltaMillis("getLastModified on File", lastModifiedTime.toMillis(), lastModifiedTimeMillis,
            Math.max(lastModTimeAccuracyMillis, Duration.ofMinutes(1).toMillis()));
    }

}
