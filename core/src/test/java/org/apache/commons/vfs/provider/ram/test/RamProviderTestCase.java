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
package org.apache.commons.vfs.provider.ram.test;

import java.io.File;

import junit.framework.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs.provider.ram.RamFileProvider;
import org.apache.commons.vfs.provider.ram.RamFileSystem;
import org.apache.commons.vfs.test.AbstractProviderTestConfig;
import org.apache.commons.vfs.test.ProviderTestConfig;
import org.apache.commons.vfs.test.ProviderTestSuite;
import org.apache.commons.AbstractVfsTestCase;

/**
 * Tests for the RAM file system.
 */
public class RamProviderTestCase extends AbstractProviderTestConfig implements
        ProviderTestConfig
{
    private boolean inited = false;

    /** logger */
    private static Log log = LogFactory.getLog(RamProviderTestCase.class);

    /**
     * Creates the test suite for the ram file system.
     */
    public static Test suite() throws Exception
    {
        return new ProviderTestSuite(new RamProviderTestCase());
    }

    /**
     * Prepares the file system manager.
     * 
     * Imports test data from the disk.
     * 
     * @throws Exception
     * 
     */
    @Override
    public void prepare(final DefaultFileSystemManager manager)
            throws Exception
    {
        try
        {
            manager.addProvider("ram", new RamFileProvider());
            manager.addProvider("file", new DefaultLocalFileProvider());
        }
        catch (Exception e)
        {
            log.error(e);
            throw e;
        }
    }

    /**
     * Returns the base folder for tests.
     */
    @Override
    public FileObject getBaseTestFolder(final FileSystemManager manager)
            throws Exception
    {
        if (!inited)
        {
            // Import the test tree
            FileObject fo = manager.resolveFile("ram:/");
            RamFileSystem fs = (RamFileSystem) fo.getFileSystem();
            fs.importTree(new File(AbstractVfsTestCase.getTestDirectory()));
            fo.close();
            
            inited=true;
        }

        final String uri = "ram:/";
        return manager.resolveFile(uri);
    }
}
