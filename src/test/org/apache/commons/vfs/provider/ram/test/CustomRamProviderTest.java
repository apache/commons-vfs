/*
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.commons.vfs.provider.ram.test;

import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.ram.RamFileProvider;
import org.apache.commons.vfs.provider.ram.RamFileSystemConfigBuilder;

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

	FileSystemOptions predicated = new FileSystemOptions();

	protected void setUp() throws Exception
	{
		super.setUp();

		manager = new DefaultFileSystemManager();
		manager.addProvider("ram", new RamFileProvider());
		manager.init();

		// File Systems Options
		RamFileSystemConfigBuilder.getInstance().setMaxSize(zeroSized, 0);
		RamFileSystemConfigBuilder.getInstance().setMaxSize(smallSized, 10);
		FileSelector predicate = new FileSelector()
		{
			public boolean includeFile(FileSelectInfo fileInfo) throws Exception
			{
				return "txt".equals(fileInfo.getFile().getName().getExtension());
			}

			public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception
			{
				// not required
				return true;
			}
			
		};
		/*
		Predicate predicate = new Predicate()
		{
			public boolean evaluate(Object o)
			{
				RamFileObject file = (RamFileObject) o;
				return file.getName().getBaseName().endsWith("txt");
			}
		};
		*/
		RamFileSystemConfigBuilder.getInstance().setPredicate(predicated, predicate);
	}

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
		FileObject fo3 = manager.resolveFile("ram:/", smallSized);
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
			;
		}

	}

	public void testPredicatedFS() throws FileSystemException
	{
		FileObject predFo = null;
		try
		{
			predFo = manager.resolveFile("ram:/myfile.anotherExtension",
					predicated);
			predFo.createFile();
			fail("It should only accept files with .txt extensions");
		}
		catch (FileSystemException e)
		{
			// Do nothing
		}
		predFo = manager
				.resolveFile("ram:/myfile.anotherExtension", predicated);
		assertTrue(!predFo.exists());
	}

}
