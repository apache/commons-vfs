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
package org.apache.commons.vfs.provider.tar.test;

import junit.framework.Test;
import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.tar.TarFileProvider;
import org.apache.commons.vfs.test.AbstractProviderTestConfig;
import org.apache.commons.vfs.test.ProviderTestConfig;
import org.apache.commons.vfs.test.ProviderTestSuite;

import java.io.File;

/**
 * Tests for the Tar file system.
 */
public class Tbz2ProviderTestCase
    extends AbstractProviderTestConfig
    implements ProviderTestConfig
{
    /**
     * Creates the test suite for the tar file system.
     */
    public static Test suite() throws Exception
    {
        return new ProviderTestSuite(new Tbz2ProviderTestCase());
    }

    /**
     * Prepares the file system manager.
     */
    public void prepare(final DefaultFileSystemManager manager) throws Exception
    {
        manager.addProvider("tbz2", new TarFileProvider());
        manager.addProvider("tar", new TarFileProvider());
    }

    /**
     * Returns the base folder for read tests.
     */
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception
    {
        final File tarFile = AbstractVfsTestCase.getTestResource("test.tbz2");
        final String uri = "tbz2:" + tarFile.getAbsolutePath() + "!/";
        return manager.resolveFile(uri);
    }
}
