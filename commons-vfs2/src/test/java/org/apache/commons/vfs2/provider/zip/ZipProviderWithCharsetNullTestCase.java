/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.nio.charset.StandardCharsets;

import junit.framework.Test;

import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.ProviderTestSuite;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.jupiter.api.Assertions;

/**
 * Tests for the Zip file system.
 */
public class ZipProviderWithCharsetNullTestCase extends AbstractProviderTestConfig {

    /**
     * Creates the test suite for the ZIP file system.
     */
    public static Test suite() throws Exception {
        return new ProviderTestSuite(new ZipProviderWithCharsetNullTestCase(), true);
    }

    /**
     * Returns the base folder for read tests.
     */
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
        Assertions.assertTrue(fileSystem instanceof ZipFileSystem);
        final ZipFileSystem zipFileSystem = (ZipFileSystem) fileSystem;
        Assertions.assertEquals(StandardCharsets.UTF_8, zipFileSystem.getCharset());
        return resolvedFile;
    }

    /**
     * Prepares the file system manager.
     */
    @Override
    public void prepare(final DefaultFileSystemManager manager) throws Exception {
        manager.addProvider("zip", new ZipFileProvider());
        manager.addExtensionMap("zip", "zip");
        manager.addMimeTypeMap(MIME_TYPE_APPLICATION_ZIP, "zip");
    }

}
