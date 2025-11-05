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
package org.apache.commons.vfs2.provider.ram;

import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectoryFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;

/**
 * JUnit 5 tests for the RAM file system.
 * <p>
 * This class replaces {@link RamProviderTestCase} with a pure JUnit 5 implementation.
 * </p>
 */
public class RamProviderTest extends ProviderTestSuiteJunit5 {

    /** Logger */
    private static final Log log = LogFactory.getLog(RamProviderTest.class);

    private static final RamProviderTestConfig config = new RamProviderTestConfig();

    public RamProviderTest() throws Exception {
        super(config, "", false);
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            super.tearDown();
        } finally {
            // Cleanup the temporary test data directory
            config.cleanupTempTestDir();
        }
    }

    /**
     * Configuration for RAM provider tests.
     */
    private static class RamProviderTestConfig extends AbstractProviderTestConfig {

        private boolean inited;
        private File tempTestDir;

        /**
         * Returns the base folder for tests.
         */
        @Override
        public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
            if (!inited) {
                // Create a temporary isolated copy of the test data directory
                // to avoid race conditions with other tests (like LocalProviderTest)
                // that modify files in the shared target/test-classes/test-data directory
                tempTestDir = createTempTestDataCopy();

                // Import the test tree from our isolated copy
                final FileObject fo = manager.resolveFile("ram:/");
                final RamFileSystem fs = (RamFileSystem) fo.getFileSystem();
                fs.importTree(tempTestDir);
                fo.close();

                inited = true;
            }

            final String uri = "ram:/";
            return manager.resolveFile(uri);
        }

        /**
         * Creates a temporary copy of the test data directory.
         * This ensures test isolation and prevents race conditions with other tests.
         */
        private File createTempTestDataCopy() throws IOException {
            final File sourceDir = getTestDirectoryFile();
            final Path tempDir = Files.createTempDirectory("vfs-ram-test-");

            // Copy the entire test data directory to the temp location
            copyDirectory(sourceDir.toPath(), tempDir);

            return tempDir.toFile();
        }

        /**
         * Recursively copies a directory.
         */
        private void copyDirectory(final Path source, final Path target) throws IOException {
            Files.walk(source).forEach(sourcePath -> {
                try {
                    final Path targetPath = target.resolve(source.relativize(sourcePath));
                    if (Files.isDirectory(sourcePath)) {
                        if (!Files.exists(targetPath)) {
                            Files.createDirectory(targetPath);
                        }
                    } else {
                        Files.copy(sourcePath, targetPath);
                    }
                } catch (final IOException e) {
                    // Skip files that cannot be copied (e.g., permission issues)
                    log.warn("Skipping file that cannot be copied: " + sourcePath + " - " + e.getMessage());
                }
            });
        }

        /**
         * Cleans up the temporary test data directory.
         */
        private void cleanupTempTestDir() {
            if (tempTestDir != null && tempTestDir.exists()) {
                try {
                    Files.walk(tempTestDir.toPath())
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                } catch (final IOException e) {
                    log.warn("Failed to cleanup temp test directory: " + tempTestDir + " - " + e.getMessage());
                }
            }
        }

        /**
         * Prepares the file system manager.
         *
         * Imports test data from the disk.
         *
         * @throws Exception
         */
        @Override
        public void prepare(final DefaultFileSystemManager manager) throws Exception {
            try {
                manager.addProvider("ram", new RamFileProvider());
                manager.addProvider("file", new DefaultLocalFileProvider());
            } catch (final Exception e) {
                log.error(e);
                throw e;
            }
        }
    }
}

