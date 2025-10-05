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

    public RamProviderTest() throws Exception {
        super(new RamProviderTestConfig(), "", false);
    }

    /**
     * Configuration for RAM provider tests.
     */
    private static class RamProviderTestConfig extends AbstractProviderTestConfig {

        private boolean inited;

        /**
         * Returns the base folder for tests.
         */
        @Override
        public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
            if (!inited) {
                // Import the test tree
                final FileObject fo = manager.resolveFile("ram:/");
                final RamFileSystem fs = (RamFileSystem) fo.getFileSystem();
                fs.importTree(getTestDirectoryFile());
                fo.close();

                inited = true;
            }

            final String uri = "ram:/";
            return manager.resolveFile(uri);
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

