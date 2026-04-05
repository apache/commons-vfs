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

import java.io.File;
import java.time.Duration;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VfsTestUtils;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that {@link org.apache.commons.vfs2.provider.AbstractFileObject#refresh()} correctly clears cached state
 * even when the file was never attached.
 * <p>
 * Regression test for the bug in {@code AbstractFileObject.detach()} where the {@code if (attached)} guard
 * prevented clearing cached fields ({@code type}, {@code parent}, {@code children}) on objects that were never
 * attached. Provider-specific cached fields like {@code FtpFileObject.childMap} can be populated without going
 * through {@code attach()} (e.g. via {@code getChildFile()} → {@code doGetChildren()}), so {@code refresh()}
 * must clear cached state regardless of the {@code attached} flag.
 * </p>
 */
public class FtpRefreshCachedStateTest {

    @BeforeEach
    public void setUp() throws Exception {
        FtpProviderTest.setUpClass(VfsTestUtils.getTestDirectory(), null, null);
    }

    @AfterEach
    public void tearDown() {
        FtpProviderTest.tearDownClass();
    }

    private static FileSystemOptions createOptions() {
        final FileSystemOptions options = new FileSystemOptions();
        final FtpFileSystemConfigBuilder builder = FtpFileSystemConfigBuilder.getInstance();
        builder.setUserDirIsRoot(options, true);
        builder.setPassiveMode(options, true);
        builder.setConnectTimeout(options, Duration.ofSeconds(10));
        return options;
    }

    /**
     * Tests that {@code exists()} returns {@code false} for a file that was deleted from the FTP server,
     * even when the parent's cached {@code childMap} was populated without {@code attach()}.
     * <p>
     * The scenario: a file exists, its parent's {@code childMap} is populated (via {@code getChildFile()}
     * during the first {@code exists()} call), the file is then deleted on the server, and the parent's
     * {@code refresh()} must clear the stale {@code childMap} so the next {@code exists()} returns
     * {@code false}.
     * </p>
     * <p>
     * Before the fix, {@code refresh()} → {@code detach()} skipped cache clearing because the parent was
     * never explicitly attached ({@code attached == false}), leaving stale data in {@code childMap}.
     * </p>
     */
    @Test
    public void testExistsReturnsFalseAfterFileDeletedAndParentRefreshed() throws Exception {
        // Create a temporary file in the FTP home directory.
        final File ftpHome = new File(VfsTestUtils.getTestDirectory());
        final File tempFile = new File(ftpHome, "refresh-test-file.txt");
        tempFile.createNewFile();

        try (final DefaultFileSystemManager manager = new DefaultFileSystemManager()) {
            manager.addProvider("ftp", new FtpFileProvider());
            manager.init();

            final FileSystemOptions options = createOptions();
            final FileObject file = manager.resolveFile(
                FtpProviderTest.getConnectionUri() + "/refresh-test-file.txt", options);

            // Verify the file exists. This populates the parent's childMap
            // via setFTPFile() → getParent().getChildFile() → doGetChildren().
            Assertions.assertTrue(file.exists(), "File should exist on the FTP server");

            // Delete the file directly on the filesystem.
            Assertions.assertTrue(tempFile.delete(), "Temp file should be deleted");

            // Refresh the parent to clear its cached childMap, then refresh the file.
            // Before the fix, the parent's refresh() skipped clearing childMap because
            // the parent was never attached (attached == false).
            final FileObject parent = file.getParent();
            parent.refresh();
            file.refresh();

            // exists() must return false now that the file is deleted.
            Assertions.assertFalse(file.exists(),
                "exists() must return false after file is deleted and parent is refreshed");
        } finally {
            tempFile.delete();
        }
    }
}
