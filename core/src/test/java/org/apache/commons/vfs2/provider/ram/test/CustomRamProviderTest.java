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
package org.apache.commons.vfs2.provider.ram.test;

import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.ram.RamFileProvider;
import org.apache.commons.vfs2.provider.ram.RamFileSystemConfigBuilder;

/**
 * Custom tests
 *
 * @author edgar poce
 * @version
 *
 */
public class CustomRamProviderTest extends TestCase
{
    DefaultFileSystemManager manager;

    FileSystemOptions zeroSized = new FileSystemOptions();

    FileSystemOptions smallSized = new FileSystemOptions();

    FileSystemOptions defaultRamFs = new FileSystemOptions();

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        manager = new DefaultFileSystemManager();
        manager.addProvider("ram", new RamFileProvider());
        manager.init();

        // File Systems Options
        RamFileSystemConfigBuilder.getInstance().setMaxSize(zeroSized, 0);
        RamFileSystemConfigBuilder.getInstance().setMaxSize(smallSized, 10);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        manager.close();
    }

    public void testSmallFS() throws Exception
    {

        // Default FS
        FileObject fo1 = manager.resolveFile("ram:/");
        FileObject fo2 = manager.resolveFile("ram:/");
        assertTrue("Both files should exist in the same fs instance.", fo1
                .getFileSystem() == fo2.getFileSystem());

        // Small FS
        FileObject fo3 = manager.resolveFile("ram:/fo3", smallSized);
        FileObject fo4 = manager.resolveFile("ram:/", smallSized);
        assertTrue("Both files should exist in different fs instances.", fo3
                .getFileSystem() == fo4.getFileSystem());
        assertTrue("These file shouldn't be in the same file system.", fo1
                .getFileSystem() != fo3.getFileSystem());

        fo3.createFile();
        try
        {
            OutputStream os = fo3.getContent().getOutputStream();
            os.write(new byte[10]);
            os.close();
        }
        catch (FileSystemException e)
        {
            fail("It shouldn't save such a small file");
        }

        try
        {
            OutputStream os = fo3.getContent().getOutputStream();
            os.write(new byte[11]);
            os.close();
            fail("It shouldn't save such a big file");
        }
        catch (FileSystemException e)
        {
            // exception awaited
        }

    }

    /**
     *
     * Checks root folder exists
     *
     * @throws FileSystemException
     */
    public void testRootFolderExists() throws FileSystemException {
        FileObject root = manager.resolveFile("ram:///", defaultRamFs);
        assertTrue(root.getType().hasChildren());

        try {
            root.delete();
            fail();
        } catch (FileSystemException e) {

        }

    }


}
