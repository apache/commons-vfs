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
package org.apache.commons.vfs2.provider.test;

import java.io.File;

import junit.framework.Test;

import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.test.AbstractProviderTestConfig;
import org.apache.commons.vfs2.test.ProviderTestSuite;

/**
 * Test cases for the virtual file system provider.
 */
public class VirtualProviderTestCase extends AbstractProviderTestConfig {
    public static Test suite() throws Exception {
        final ProviderTestSuite testSuite = new ProviderTestSuite(new VirtualProviderTestCase());
        testSuite.addTests(JunctionTests.class);
        return testSuite;
    }

    /**
     * Returns the base folder for tests.
     */
    @Override
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
        final File baseDir = AbstractVfsTestCase.getTestDirectoryFile();
        final FileObject baseFile = manager.toFileObject(baseDir);
        return manager.createVirtualFileSystem(baseFile);
    }
}
