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
package org.apache.commons.vfs2.test;

import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.impl.VirtualFileSystem;
import org.apache.commons.vfs2.provider.ram.RamFileObject;
import org.apache.commons.vfs2.util.FileObjectUtils;

/**
 * Test the cache stragey
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 */
public class ProviderCacheStrategyTests
    extends AbstractProviderTestCase
{
    /**
     * Returns the capabilities required by the tests of this test case.
     */
    @Override
    protected Capability[] getRequiredCaps()
    {
        return new Capability[]
        {
            Capability.CREATE,
            Capability.GET_TYPE,
            Capability.LIST_CHILDREN,
        };
    }

    /**
     * Test the manual cache strategy
     */
    public void testManualCache() throws Exception
    {
        FileObject scratchFolder = getWriteFolder();
        if (FileObjectUtils.isInstanceOf(getBaseFolder(), RamFileObject.class) ||
            scratchFolder.getFileSystem() instanceof VirtualFileSystem)
        {
            // cant check ram filesystem as every manager holds its own ram filesystem data
            return;
        }

        scratchFolder.delete(Selectors.EXCLUDE_SELF);

        DefaultFileSystemManager fs = createManager();
        fs.setCacheStrategy(CacheStrategy.MANUAL);
        fs.init();
        FileObject foBase2 = getBaseTestFolder(fs);

        FileObject cachedFolder = foBase2.resolveFile(scratchFolder.getName().getPath());

        FileObject[] fos = cachedFolder.getChildren();
        assertContainsNot(fos, "file1.txt");

        scratchFolder.resolveFile("file1.txt").createFile();

        fos = cachedFolder.getChildren();
        assertContainsNot(fos, "file1.txt");

        cachedFolder.refresh();
        fos = cachedFolder.getChildren();
        assertContains(fos, "file1.txt");
    }

    /**
     * Test the on_resolve strategy
     */
    public void testOnResolveCache() throws Exception
    {
        FileObject scratchFolder = getWriteFolder();
        if (FileObjectUtils.isInstanceOf(getBaseFolder(), RamFileObject.class) ||
            scratchFolder.getFileSystem() instanceof VirtualFileSystem)
        {
            // cant check ram filesystem as every manager holds its own ram filesystem data
            return;
        }

        scratchFolder.delete(Selectors.EXCLUDE_SELF);

        DefaultFileSystemManager fs = createManager();
        fs.setCacheStrategy(CacheStrategy.ON_RESOLVE);
        fs.init();
        FileObject foBase2 = getBaseTestFolder(fs);

        FileObject cachedFolder = foBase2.resolveFile(scratchFolder.getName().getPath());

        FileObject[] fos = cachedFolder.getChildren();
        assertContainsNot(fos, "file1.txt");

        scratchFolder.resolveFile("file1.txt").createFile();

        fos = cachedFolder.getChildren();
        assertContainsNot(fos, "file1.txt");

        cachedFolder = foBase2.resolveFile(scratchFolder.getName().getPath());
        fos = cachedFolder.getChildren();
        assertContains(fos, "file1.txt");
    }

    /**
     * Test the on_call strategy
     */
    public void testOnCallCache() throws Exception
    {
        FileObject scratchFolder = getWriteFolder();
        if (FileObjectUtils.isInstanceOf(getBaseFolder(), RamFileObject.class) ||
            scratchFolder.getFileSystem() instanceof VirtualFileSystem)
        {
            // cant check ram filesystem as every manager holds its own ram filesystem data
            return;
        }

        scratchFolder.delete(Selectors.EXCLUDE_SELF);

        DefaultFileSystemManager fs = createManager();
        fs.setCacheStrategy(CacheStrategy.ON_CALL);
        fs.init();
        FileObject foBase2 = getBaseTestFolder(fs);

        FileObject cachedFolder = foBase2.resolveFile(scratchFolder.getName().getPath());

        FileObject[] fos = cachedFolder.getChildren();
        assertContainsNot(fos, "file1.txt");

        scratchFolder.resolveFile("file1.txt").createFile();

        fos = cachedFolder.getChildren();
        assertContains(fos, "file1.txt");
    }

    public void assertContainsNot(FileObject[] fos, String string)
    {
        for (int i = 0; i<fos.length; i++)
        {
            if (string.equals(fos[i].getName().getBaseName()))
            {
                fail(string + " should not be seen");
            }
        }
    }

    public void assertContains(FileObject[] fos, String string)
    {
        for (int i = 0; i<fos.length; i++)
        {
            if (string.equals(fos[i].getName().getBaseName()))
            {
                return;
            }
        }

        fail(string + " should be seen");
    }
}
