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
package org.apache.commons.vfs.cache;

import junit.framework.Test;
import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FilesCache;
import org.apache.commons.vfs.test.AbstractProviderTestConfig;
import org.apache.commons.vfs.test.CacheTestSuite;
import org.apache.commons.vfs.test.ProviderTestConfig;

import java.io.File;

/**
 * Tests the NullFilesCache
 * 
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date$
 */
public class LRUFilesCacheTestCase
    extends AbstractProviderTestConfig
    implements ProviderTestConfig
{
    public static Test suite() throws Exception
    {
        CacheTestSuite suite = new CacheTestSuite(new LRUFilesCacheTestCase());
        suite.addTests(LRUFilesCacheTests.class);
        return suite;
    }

    public FilesCache getFilesCache()
    {
        return new LRUFilesCache(5);
    }

    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception
    {
        final File testDir = AbstractVfsTestCase.getTestDirectoryFile();
        return manager.toFileObject(testDir);
    }
}
