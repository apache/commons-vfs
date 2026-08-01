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
package org.apache.commons.vfs2.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link DefaultFileReplicator}.
 */
public class DefaultFileReplicatorTest {

    private File allocateTempDir() throws FileSystemException {
        final DefaultFileReplicator replicator = new DefaultFileReplicator();
        replicator.init();
        return replicator.allocateFile("test.txt").getParentFile();
    }

    @Test
    public void testTempDirIsNotSharedByName() throws Exception {
        final File tempDir = allocateTempDir();
        try {
            assertNotEquals(new File(FileUtils.getTempDirectoryPath(), "vfs_cache").getAbsoluteFile(), tempDir,
                    "the replica directory must not use a name another user can guess and pre-create");
            assertTrue(tempDir.isDirectory(), tempDir + " was not created");
        } finally {
            FileUtils.deleteQuietly(tempDir);
        }
    }

    @Test
    public void testTempDirIsOwnerOnly() throws Exception {
        assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));
        final File tempDir = allocateTempDir();
        try {
            assertEquals(PosixFilePermissions.fromString("rwx------"),
                    Files.getPosixFilePermissions(tempDir.toPath()));
        } finally {
            FileUtils.deleteQuietly(tempDir);
        }
    }
}
