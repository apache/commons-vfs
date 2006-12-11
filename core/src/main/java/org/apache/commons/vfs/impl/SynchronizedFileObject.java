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
package org.apache.commons.vfs.impl;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.NameScope;

import java.util.List;

/**
 * This decorator synchronize all access to the FileObject
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date$
 */
public class SynchronizedFileObject extends DecoratedFileObject
{
	public SynchronizedFileObject(FileObject fileObject)
	{
		super(fileObject);
	}

	public void close() throws FileSystemException
    {
        synchronized(this)
        {
		    super.close();
        }
    }

	public void copyFrom(FileObject srcFile, FileSelector selector) throws FileSystemException
	{
        synchronized(this)
        {
    		super.copyFrom(srcFile, selector);
        }
    }

	public void createFile() throws FileSystemException
	{
        synchronized(this)
        {
    		super.createFile();
        }
    }

	public void createFolder() throws FileSystemException
	{
        synchronized(this)
        {
    		super.createFolder();
        }
    }

	public boolean delete() throws FileSystemException
	{
        synchronized(this)
        {
    		return super.delete();
        }
    }

	public int delete(FileSelector selector) throws FileSystemException
	{
        synchronized(this)
        {
    		return super.delete(selector);
        }
    }

	public boolean exists() throws FileSystemException
	{
        synchronized(this)
        {
    		return super.exists();
        }
    }

	public void findFiles(FileSelector selector, boolean depthwise, List selected) throws FileSystemException
	{
        synchronized(this)
        {
    		super.findFiles(selector, depthwise, selected);
        }
    }

	public FileObject[] findFiles(FileSelector selector) throws FileSystemException
	{
        synchronized(this)
        {
    		return super.findFiles(selector);
        }
    }

	public FileObject getChild(String name) throws FileSystemException
	{
        synchronized(this)
        {
    		return super.getChild(name);
        }
    }

	public FileObject[] getChildren() throws FileSystemException
	{
        synchronized(this)
        {
    		return super.getChildren();
        }
    }

	public FileContent getContent() throws FileSystemException
	{
        synchronized(this)
        {
    		return super.getContent();
        }
    }

	public FileType getType() throws FileSystemException
	{
        synchronized(this)
        {
    		return super.getType();
        }
    }

	public boolean isHidden() throws FileSystemException
	{
        synchronized(this)
        {
    		return super.isHidden();
        }
    }

	public boolean isReadable() throws FileSystemException
	{
        synchronized(this)
        {
    		return super.isReadable();
        }
    }

	public boolean isWriteable() throws FileSystemException
	{
        synchronized(this)
        {
    		return super.isWriteable();
        }
    }

	public void moveTo(FileObject destFile) throws FileSystemException
	{
        synchronized(this)
        {
    		super.moveTo(destFile);
        }
    }

	public FileObject resolveFile(String name, NameScope scope) throws FileSystemException
	{
        synchronized(this)
        {
    		return super.resolveFile(name, scope);
        }
    }

	public FileObject resolveFile(String path) throws FileSystemException
	{
        synchronized(this)
        {
    		return super.resolveFile(path);
        }
    }
}
