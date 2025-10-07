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
package org.apache.commons.vfs2.provider.tar;

import static org.apache.commons.vfs2.VfsTestUtils.getTestResource;

import java.io.File;

import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.bzip2.Bzip2FileProvider;
import org.junit.jupiter.api.TestInstance;

/**
 * Tests for nested TBZ2 file systems (JUnit 5).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NestedTbz2Test extends ProviderTestSuiteJunit5 {

    public NestedTbz2Test() throws Exception {
        super(new NestedTbz2TestConfig(), "", true);
    }

    /**
     * Configuration for nested TBZ2 provider tests.
     */
    private static class NestedTbz2TestConfig extends AbstractProviderTestConfig {

        @Override
        public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
            final File nestedFile = getTestResource("nested.tbz2");
            final String nestedUri = "tbz2:file:" + nestedFile.getAbsolutePath() + "!/";
            final FileObject nestedTbz2 = manager.resolveFile(nestedUri);
            final FileObject testTbz2 = nestedTbz2.resolveFile("test.tbz2");
            // Need to resolve as a TBZ2 file system
            return manager.resolveFile("tbz2:" + testTbz2.getURL().toString() + "!/");
        }

        @Override
        public void prepare(final DefaultFileSystemManager manager) throws Exception {
            manager.addProvider("tar", new TarFileProvider());
            manager.addProvider("tbz2", new TarFileProvider());
            manager.addProvider("bz2", new Bzip2FileProvider());
        }
    }
}

