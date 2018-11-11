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
package org.apache.commons.vfs2.provider.tar.test;

import java.io.File;

import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.tar.TarFileProvider;
import org.apache.commons.vfs2.test.AbstractProviderTestConfig;
import org.apache.commons.vfs2.test.ProviderTestSuite;

import junit.framework.Test;

/**
 * Tests for the Tar file system.
 */
public class TarProviderTestCase extends AbstractProviderTestConfig {
    /**
     * Creates the test suite for the tar file system.
     */
    public static Test suite() throws Exception {
        return new ProviderTestSuite(new TarProviderTestCase(), true);
    }

    /**
     * Prepares the file system manager.
     */
    @Override
    public void prepare(final DefaultFileSystemManager manager) throws Exception {
        manager.addProvider("tar", new TarFileProvider());
        manager.addMimeTypeMap("application/x-tar", "tar");
    }

    /**
     * Returns the base folder for read tests.
     */
    @Override
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
        final File tarFile = AbstractVfsTestCase.getTestResource("test.tar");
        final String uri = "tar:file:" + tarFile.getAbsolutePath() + "!/";
        return manager.resolveFile(uri);
    }
}
