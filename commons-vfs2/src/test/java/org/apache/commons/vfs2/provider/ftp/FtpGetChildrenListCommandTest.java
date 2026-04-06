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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.FileDepthSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VfsTestUtils;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.ftpserver.command.CommandFactoryFactory;
import org.apache.ftpserver.command.impl.LIST;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that {@link CacheStrategy#ON_RESOLVE} does not cause redundant FTP LIST
 * commands due to internal VFS navigation triggering cache refreshes.
 * <p>
 * With {@code ON_RESOLVE}, every {@code fileSystem.resolveFile()} call triggers
 * {@code refresh()} on the returned file. This is correct for external API calls
 * (the user explicitly resolving a file), but internal navigation methods like
 * {@code getParent()}, {@code getRoot()}, and child resolution in
 * {@code getChildren()} also call {@code resolveFile()}, unintentionally refreshing
 * cached files and clearing their state.
 * </p>
 * <p>
 * This was always wasteful, but became a severe regression after
 * {@code FtpFileObject.refresh()} was changed to unconditionally clear
 * {@code childMap} (to fix stale cached state on never-attached files).
 * That change turned the unnecessary refreshes into O(N) LIST commands:
 * each child's {@code getParent()} refreshes the parent, clearing its
 * {@code childMap}, forcing a new LIST for every child.
 * </p>
 */
public class FtpGetChildrenListCommandTest {

    private static final int FILE_COUNT = 50;
    private static final AtomicInteger listCommandCount = new AtomicInteger();
    private static final LIST defaultListCommand = new LIST();

    private File[] testFiles;

    @BeforeEach
    public void setUp() throws Exception {
        listCommandCount.set(0);

        // Create test files in the FTP home directory.
        final File testDir = new File(VfsTestUtils.getTestDirectory());
        testFiles = new File[FILE_COUNT];
        for (int i = 0; i < FILE_COUNT; i++) {
            testFiles[i] = new File(testDir, "amplification-test-" + i + ".txt");
            try (FileWriter w = new FileWriter(testFiles[i])) {
                w.write("content-" + i);
            }
        }

        // Start FTP server with a LIST command wrapper that counts invocations.
        final CommandFactoryFactory cmdFactory = new CommandFactoryFactory();
        cmdFactory.addCommand("LIST", (session, context, request) -> {
            listCommandCount.incrementAndGet();
            defaultListCommand.execute(session, context, request);
        });
        FtpProviderTest.setUpClass(VfsTestUtils.getTestDirectory(), null,
                cmdFactory.createCommandFactory());
    }

    @AfterEach
    public void tearDown() {
        FtpProviderTest.tearDownClass();
        if (testFiles != null) {
            for (final File f : testFiles) {
                f.delete();
            }
        }
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
     * Verifies that {@code findFiles()} on a directory with {@code N} children
     * issues exactly 1 LIST command. {@code findFiles()} internally calls
     * {@code getType()} on each child via {@code traverse()}, which is where
     * the cascade occurs.
     * <p>
     * With the bug, each child's {@code getType()} triggers a parent LIST via
     * the ON_RESOLVE cascade, producing ~N LIST commands.
     * Without the bug, the parent's {@code childMap} is reused across children,
     * resulting in exactly 1 LIST.
     * </p>
     */
    @Test
    public void testGetChildrenIssuesSingleList() throws Exception {
        try (final DefaultFileSystemManager manager = new DefaultFileSystemManager()) {
            manager.addProvider("ftp", new FtpFileProvider());
            manager.setCacheStrategy(CacheStrategy.ON_RESOLVE);
            manager.init();

            final FileSystemOptions options = createOptions();
            final FileObject root = manager.resolveFile(
                    FtpProviderTest.getConnectionUri(), options);

            listCommandCount.set(0);
            root.refresh();
            final FileObject[] children = root.findFiles(new FileDepthSelector(1, 1));
            assertNotNull(children, "findFiles should return results");
            assertTrue(children.length >= FILE_COUNT,
                    "Should find at least " + FILE_COUNT + " files, found: " + children.length);

            final int listCount = listCommandCount.get();

            // With the bug: LIST count ≈ FILE_COUNT (one per child due to cascade)
            // Without the bug: exactly 1 LIST (the getChildren() directory listing)
            assertEquals(1, listCount,
                    "Scan should produce exactly 1 LIST command, but produced "
                            + listCount + ". This indicates the ON_RESOLVE childMap "
                            + "amplification bug: each child's getParent() re-resolution "
                            + "refreshes the parent, clearing its childMap and forcing a new LIST.");
        }
    }
}
