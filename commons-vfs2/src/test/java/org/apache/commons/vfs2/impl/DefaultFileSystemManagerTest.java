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

package org.apache.commons.vfs2.impl;

import java.nio.file.Paths;

import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.cache.NullFilesCache;
import org.apache.commons.vfs2.provider.ram.RamFileProvider;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests {@link DefaultFileSystemManager}.
 *
 * @since 2.5.0
 */
public class DefaultFileSystemManagerTest {

    /**
     * Tests {@link DefaultFileSystemManager#close()}.
     *
     * @throws FileSystemException
     */
    @Test
    public void test_close() throws FileSystemException {
        try (FileSystemManager fileSystemManager = new DefaultFileSystemManager()) {
            VFS.setManager(fileSystemManager);
            VFS.setManager(null);
        }
        Assert.assertNotNull(VFS.getManager());
        Assert.assertFalse(VFS.getManager().resolveFile(Paths.get("DoesNotExist.not").toUri()).exists());
    }

    @Test
    public void testAddAndRemoveProvider() throws FileSystemException {
        try (DefaultFileSystemManager fileSystemManager = new DefaultFileSystemManager()) {
            fileSystemManager.setFilesCache(new NullFilesCache());
            fileSystemManager.setCacheStrategy(CacheStrategy.MANUAL);

            final RamFileProvider provider = Mockito.spy(new RamFileProvider());
            fileSystemManager.addProvider("ram1", provider);
            fileSystemManager.addProvider("ram2", provider);
            Assert.assertNotNull(fileSystemManager.resolveFile("ram1:///"));
            Assert.assertNotNull(fileSystemManager.resolveFile("ram2:///"));

            fileSystemManager.removeProvider("ram1");
            Mockito.verify(provider, Mockito.never()).close();
            Assert.assertThrows(FileSystemException.class, () -> fileSystemManager.resolveFile("ram1:///"));
            Assert.assertNotNull(fileSystemManager.resolveFile("ram2:///"));

            fileSystemManager.removeProvider("ram2");
            Mockito.verify(provider).close();
            Assert.assertThrows(FileSystemException.class, () -> fileSystemManager.resolveFile("ram2:///"));
        }
    }
}
