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
package org.apache.commons.vfs2.provider.zip;

import static org.apache.commons.vfs2.VfsTestUtils.getTestResource;

import java.io.File;

import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.jupiter.api.TestInstance;

/**
 * Tests for nested ZIP file systems (JUnit 5).
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NestedZipTest extends ProviderTestSuiteJunit5 {

    public NestedZipTest() throws Exception {
        super(new NestedZipTestConfig(), "", true);
    }

    /**
     * Configuration for nested ZIP provider tests.
     */
    private static class NestedZipTestConfig extends AbstractProviderTestConfig {

        @Override
        public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
            final File nestedFile = getTestResource("nested.zip");
            final String nestedUri = "zip:file:" + nestedFile.getAbsolutePath() + "!/";
            final FileObject nestedZip = manager.resolveFile(nestedUri);
            final FileObject testZip = nestedZip.resolveFile("test.zip");
            // Need to resolve as a ZIP file system
            return manager.resolveFile("zip:" + testZip.getURL().toString() + "!/");
        }

        @Override
        public void prepare(final DefaultFileSystemManager manager) throws Exception {
            manager.addProvider("zip", new ZipFileProvider());
        }
    }
}

