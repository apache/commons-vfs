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
package org.apache.commons.vfs2.provider.zip.test;

import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.zip.ZipFileProvider;
import org.apache.commons.vfs2.test.AbstractProviderTestConfig;
import org.apache.commons.vfs2.test.ProviderTestSuite;

import junit.framework.Test;

/**
 * Tests for the Zip file system, using a zip file nested inside another zip file.
 */
public class NestedZipTestCase extends AbstractProviderTestConfig {
    /**
     * Creates the test suite for nested zip files.
     */
    public static Test suite() throws Exception {
        return new ProviderTestSuite(new NestedZipTestCase(), true);
    }

    /**
     * Prepares the file system manager.
     */
    @Override
    public void prepare(final DefaultFileSystemManager manager) throws Exception {
        manager.addProvider("zip", new ZipFileProvider());
        manager.addExtensionMap("zip", "zip");
        manager.addMimeTypeMap("application/zip", "zip");
    }

    /**
     * Returns the base folder for tests.
     */
    @Override
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
        // Locate the base Zip file
        final String zipFilePath = AbstractVfsTestCase.getTestResource("nested.zip").getAbsolutePath();
        final String uri = "zip:file:" + zipFilePath + "!/test.zip";
        final FileObject zipFile = manager.resolveFile(uri);

        // Now build the nested file system
        final FileObject nestedFS = manager.createFileSystem(zipFile);
        return nestedFS.resolveFile("/");
    }
}
