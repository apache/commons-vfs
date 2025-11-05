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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;

/**
 * JUnit 5 tests for the ZIP file system with null charset configuration.
 * <p>
 * This class replaces {@code ZipProviderWithCharsetNullTestCase} with a pure JUnit 5 implementation.
 * </p>
 */
public class ZipProviderWithCharsetNullTest extends ProviderTestSuiteJunit5 {

    public ZipProviderWithCharsetNullTest() throws Exception {
        super(new ZipProviderWithCharsetNullTestConfig(), "", true);
    }

    /**
     * Configuration for ZIP provider tests with null charset.
     */
    private static class ZipProviderWithCharsetNullTestConfig extends AbstractProviderTestConfig {

        @Override
        public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
            final FileSystemOptions opts = new FileSystemOptions();
            final ZipFileSystemConfigBuilder builder = ZipFileSystemConfigBuilder.getInstance();
            // Tests null as the default.
            builder.setCharset(opts, null);

            final File zipFile = getTestResource("test.zip");
            final String uri = "zip:file:" + zipFile.getAbsolutePath() + "!/";
            final FileObject resolvedFile = manager.resolveFile(uri, opts);
            final FileSystem fileSystem = resolvedFile.getFileSystem();
            assertInstanceOf(ZipFileSystem.class, fileSystem);
            final ZipFileSystem zipFileSystem = (ZipFileSystem) fileSystem;
            assertEquals(StandardCharsets.UTF_8, zipFileSystem.getCharset());
            return resolvedFile;
        }

        @Override
        public void prepare(final DefaultFileSystemManager manager) throws Exception {
            manager.addProvider("zip", new ZipFileProvider());
            manager.addExtensionMap("zip", "zip");
            manager.addMimeTypeMap(MIME_TYPE_APPLICATION_ZIP, "zip");
        }
    }
}

