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
package org.apache.commons.vfs2.provider.sftp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;

import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.FileDepthSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VfsTestUtils;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that {@link CacheStrategy#ON_RESOLVE} does not cause stale children
 * when scanning SFTP directories, and that refreshing a directory produces
 * fresh child objects.
 * <p>
 * Companion to the FTP test {@code FtpGetChildrenListCommandTest}. Verifies
 * that the {@code resolveFileInternal()} fix and the in-place metadata
 * propagation in {@code doListChildrenResolved()} work correctly for the
 * SFTP provider.
 * </p>
 */
public class SftpGetChildrenListCommandTest {

    private static final int FILE_COUNT = 10;

    private File[] testFiles;

    @BeforeEach
    public void setUp() throws Exception {
        SftpTestServerHelper.startServer();

        final File testDir = VfsTestUtils.getTestDirectoryFile();
        testFiles = new File[FILE_COUNT];
        for (int i = 0; i < FILE_COUNT; i++) {
            testFiles[i] = new File(testDir, "sftp-list-test-" + i + ".txt");
            try (FileWriter w = new FileWriter(testFiles[i])) {
                w.write("content-" + i);
            }
        }
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        SftpTestServerHelper.stopServer();
        if (testFiles != null) {
            for (final File f : testFiles) {
                f.delete();
            }
        }
    }

    /**
     * Verifies that refreshing an SFTP directory and calling {@code findFiles()}
     * returns fresh children that reflect filesystem changes.
     * <p>
     * After the initial listing, a new file is added on disk. After
     * {@code refresh()}, {@code findFiles()} must see the new file.
     * This proves that {@code doListChildrenResolved()} propagates fresh
     * metadata to cached child objects for SFTP: cached children receive
     * updated {@code attrs} and their type is cleared for re-evaluation.
     * </p>
     */
    @Test
    public void testGetChildrenRefreshReturnsNewFiles() throws Exception {
        try (final DefaultFileSystemManager manager = new DefaultFileSystemManager()) {
            manager.addProvider("sftp", new SftpFileProvider());
            manager.setCacheStrategy(CacheStrategy.ON_RESOLVE);
            manager.init();

            final FileSystemOptions options = new FileSystemOptions();
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(options, "no");

            final String uri = SftpTestServerHelper.getConnectionUri();
            final FileObject root = manager.resolveFile(uri, options);

            // First scan
            root.refresh();
            final FileObject[] children1 = root.findFiles(new FileDepthSelector(1, 1));
            assertNotNull(children1, "findFiles should return results");
            final int firstCount = children1.length;
            assertTrue(firstCount >= FILE_COUNT,
                    "Should find at least " + FILE_COUNT + " files, found: " + firstCount);

            // Add a new file on disk
            final File newFile = new File(VfsTestUtils.getTestDirectoryFile(),
                    "sftp-list-test-new.txt");
            try (FileWriter w = new FileWriter(newFile)) {
                w.write("new content");
            }

            try {
                // Second scan after refresh — must see the new file
                root.refresh();
                final FileObject[] children2 = root.findFiles(new FileDepthSelector(1, 1));
                assertNotNull(children2, "Second findFiles should return results");

                assertEquals(firstCount + 1, children2.length,
                        "After refresh, findFiles should see the newly added file. "
                                + "First scan: " + firstCount + ", second scan: " + children2.length
                                + ". If equal, metadata propagation in doListChildrenResolved() is not working.");
            } finally {
                newFile.delete();
            }
        }
    }
}
