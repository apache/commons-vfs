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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.time.Duration;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VfsTestUtils;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that {@link FtpFileObject} uses CWD to check directory existence before
 * falling back to the expensive parent LIST command.
 * <p>
 * Verifies that:
 * <ul>
 *   <li>Directories are correctly detected via CWD (no parent LIST needed)</li>
 *   <li>Files are correctly detected via parent LIST fallback</li>
 *   <li>Non-existent paths return {@link FileType#IMAGINARY}</li>
 *   <li>Working directory is preserved after all operations</li>
 *   <li>Directory timestamps are lazily fetched via parent LIST when requested</li>
 * </ul>
 */
public class FtpCwdOptimizationTest {

    private File testDir;
    private File nestedDir;
    private File fileInRoot;
    private File fileInNested;

    @BeforeEach
    public void setUp() throws Exception {
        FtpProviderTest.setUpClass(VfsTestUtils.getTestDirectory(), null, null);

        testDir = new File(VfsTestUtils.getTestDirectory());
        final File cwdOptDir = new File(testDir, "cwdOptDir");
        nestedDir = new File(cwdOptDir, "nested");
        nestedDir.mkdirs();
        fileInRoot = new File(testDir, "rootfile.txt");
        fileInNested = new File(cwdOptDir, "file.txt");
        try (FileWriter w = new FileWriter(fileInRoot)) {
            w.write("root content");
        }
        try (FileWriter w = new FileWriter(fileInNested)) {
            w.write("nested content");
        }
    }

    @AfterEach
    public void tearDown() {
        FtpProviderTest.tearDownClass();
        if (fileInNested != null) fileInNested.delete();
        if (fileInRoot != null) fileInRoot.delete();
        if (nestedDir != null) nestedDir.delete();
        new File(testDir, "cwdOptDir").delete();
    }

    private static FileSystemOptions createOptions() {
        final FileSystemOptions options = new FileSystemOptions();
        final FtpFileSystemConfigBuilder builder = FtpFileSystemConfigBuilder.getInstance();
        builder.setPassiveMode(options, true);
        builder.setConnectTimeout(options, Duration.ofSeconds(10));
        return options;
    }

    @Test
    public void testNestedDirectoryExistsViaOptimization() throws Exception {
        try (final DefaultFileSystemManager manager = new DefaultFileSystemManager()) {
            manager.addProvider("ftp", new FtpFileProvider());
            manager.init();
            final FileObject dir = manager.resolveFile(
                FtpProviderTest.getConnectionUri() + "/cwdOptDir/nested", createOptions());
            assertTrue(dir.exists(), "Nested directory should exist");
            assertEquals(FileType.FOLDER, dir.getType(), "Nested directory should be FOLDER");
        }
    }

    @Test
    public void testFileDetectedViaListFallback() throws Exception {
        try (final DefaultFileSystemManager manager = new DefaultFileSystemManager()) {
            manager.addProvider("ftp", new FtpFileProvider());
            manager.init();
            final FileObject file = manager.resolveFile(
                FtpProviderTest.getConnectionUri() + "/cwdOptDir/file.txt", createOptions());
            assertTrue(file.exists(), "File should exist");
            assertEquals(FileType.FILE, file.getType(), "file.txt should be FILE");
        }
    }

    @Test
    public void testNonExistentPathIsImaginary() throws Exception {
        try (final DefaultFileSystemManager manager = new DefaultFileSystemManager()) {
            manager.addProvider("ftp", new FtpFileProvider());
            manager.init();
            final FileObject ghost = manager.resolveFile(
                FtpProviderTest.getConnectionUri() + "/cwdOptDir/doesNotExist", createOptions());
            assertFalse(ghost.exists(), "Non-existent path should not exist");
            assertEquals(FileType.IMAGINARY, ghost.getType(), "Non-existent path should be IMAGINARY");
        }
    }

    @Test
    public void testWorkingDirectoryPreservedAfterMixedChecks() throws Exception {
        try (final DefaultFileSystemManager manager = new DefaultFileSystemManager()) {
            manager.addProvider("ftp", new FtpFileProvider());
            manager.init();
            final FileSystemOptions options = createOptions();

            final FileObject rootFile = manager.resolveFile(
                FtpProviderTest.getConnectionUri() + "/rootfile.txt", options);
            assertTrue(rootFile.exists(), "rootfile.txt should exist initially");

            final FileObject dir = manager.resolveFile(
                FtpProviderTest.getConnectionUri() + "/cwdOptDir/nested", options);
            assertTrue(dir.exists(), "nested dir should exist");

            final FileObject nestedFile = manager.resolveFile(
                FtpProviderTest.getConnectionUri() + "/cwdOptDir/file.txt", options);
            assertTrue(nestedFile.exists(), "file.txt should exist");

            final FileObject ghost = manager.resolveFile(
                FtpProviderTest.getConnectionUri() + "/cwdOptDir/doesNotExist", options);
            assertFalse(ghost.exists(), "ghost should not exist");

            rootFile.refresh();
            assertTrue(rootFile.exists(),
                "rootfile.txt must still be accessible after mixed CWD/LIST operations");
            assertEquals(FileType.FILE, rootFile.getType());
        }
    }

    /**
     * Tests that requesting the last modified time on a CWD-resolved directory
     * does not throw NPE and returns a valid timestamp. The CWD optimization
     * creates a synthetic FTPFile with no timestamp; the timestamp must be lazily
     * fetched via parent LIST when first requested.
     */
    @Test
    public void testDirectoryTimestampLazilyFetched() throws Exception {
        try (final DefaultFileSystemManager manager = new DefaultFileSystemManager()) {
            manager.addProvider("ftp", new FtpFileProvider());
            manager.init();
            final FileObject dir = manager.resolveFile(
                FtpProviderTest.getConnectionUri() + "/cwdOptDir/nested", createOptions());
            assertTrue(dir.exists(), "Nested directory should exist");

            // Must not throw NPE, and should return a real timestamp from the
            // lazy parent LIST fallback (not 0/DEFAULT_TIMESTAMP).
            final long lastModified = dir.getContent().getLastModifiedTime();
            assertTrue(lastModified > 0,
                "Directory timestamp should be fetched lazily via parent LIST, not default 0");
        }
    }
}
