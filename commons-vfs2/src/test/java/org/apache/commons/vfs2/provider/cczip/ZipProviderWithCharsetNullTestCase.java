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
package org.apache.commons.vfs2.provider.cczip;

import java.io.File;

import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.test.AbstractProviderTestConfig;
import org.apache.commons.vfs2.test.ProviderTestSuite;
import org.junit.Assert;

import junit.framework.Test;

/**
 * Tests for the Zip file system.
 */
public class ZipProviderWithCharsetNullTestCase extends AbstractProviderTestConfig {
    /**
     * Creates the test suite for the zip file system.
     */
    public static Test suite() throws Exception {
        return new ProviderTestSuite(new ZipProviderWithCharsetNullTestCase(), true);
    }

    /**
     * Prepares the file system manager.
     */
    @Override
    public void prepare(final DefaultFileSystemManager manager) throws Exception {
        manager.addProvider("commonscompresszip", new CommonsCompressZipFileProvider());
        manager.addExtensionMap("zip", "commonscompresszip");
        manager.addMimeTypeMap("application/zip", "commonscompresszip");
    }

    /**
     * Returns the base folder for read tests.
     */
    @Override
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
        final FileSystemOptions opts = new FileSystemOptions();
        final CommonsCompressZipFileSystemConfigBuilder builder = CommonsCompressZipFileSystemConfigBuilder.getInstance();
        // Tests null as the default.
        builder.setCharset(opts, null);

        final File zipFile = AbstractVfsTestCase.getTestResource("test.zip");
        final String uri = "commonscompresszip:file:" + zipFile.getAbsolutePath() + "!/";
        final FileObject resolvedFile = manager.resolveFile(uri, opts);
        final FileSystem fileSystem = resolvedFile.getFileSystem();
        Assert.assertTrue(fileSystem instanceof CommonsCompressZipFileSystem);
        final CommonsCompressZipFileSystem zipFileSystem = (CommonsCompressZipFileSystem) fileSystem;
        Assert.assertNull(zipFileSystem.getCharset());
        return resolvedFile;
    }
}
