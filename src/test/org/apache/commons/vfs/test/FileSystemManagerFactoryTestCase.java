/*
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs.test;

import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;

import java.io.File;

/**
 * Test cases for the VFS factory.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class FileSystemManagerFactoryTestCase
    extends AbstractVfsTestCase
{
    /**
     * Sanity test.
     */
    public void testDefaultInstance() throws Exception
    {
        // Locate the default manager
        final FileSystemManager manager = VFS.getManager();

        // Lookup a test jar file
        final File jarFile = getTestResource("test.jar");
        FileObject file = manager.toFileObject(jarFile);
        assertNotNull(file);
        assertTrue(file.exists());
        assertSame(FileType.FILE, file.getType());

        // Expand it
        file = manager.createFileSystem(file);
        assertNotNull(file);
        assertTrue(file.exists());
        assertSame(FileType.FOLDER, file.getType());
    }

}
