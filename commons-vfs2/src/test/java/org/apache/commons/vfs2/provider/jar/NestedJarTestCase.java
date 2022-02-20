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
package org.apache.commons.vfs2.provider.jar;

import static org.apache.commons.vfs2.VfsTestUtils.getTestResource;

import java.io.File;

import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.ProviderTestSuite;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;

import junit.framework.Test;

/**
 * Tests for the Zip file system.
 */
public class NestedJarTestCase extends AbstractProviderTestConfig {
    /**
     * Creates the test suite for nested jar files.
     */
    public static Test suite() throws Exception {
        return new ProviderTestSuite(new NestedJarTestCase(), true);
    }

    /**
     * Returns the base folder for tests.
     */
    @Override
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
        // Locate the Jar file
        final File outerFile = getTestResource("nested.jar");
        final String uri = "jar:file:" + outerFile.getAbsolutePath() + "!/test.jar";
        final FileObject jarFile = manager.resolveFile(uri);

        // Now build the nested file system
        final FileObject nestedFS = manager.createFileSystem(jarFile);
        return nestedFS.resolveFile("/");
    }

    /**
     * Prepares the file system manager.
     */
    @Override
    public void prepare(final DefaultFileSystemManager manager) throws Exception {
        manager.addProvider("jar", new JarFileProvider());
        manager.addExtensionMap("jar", "jar");
    }
}
