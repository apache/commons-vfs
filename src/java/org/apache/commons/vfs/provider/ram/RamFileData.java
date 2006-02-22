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
package org.apache.commons.vfs.provider.ram;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;

/**
 * RAM File Object Data
 * 
 * @author <a href="mailto:edgarpoce@gmail.com">Edgar Poce </a>
 */
class RamFileData implements Serializable
{
	/**
	 * File Name
	 */
	private FileName name;

	/**
	 * File Type
	 */
	private FileType type;

	/**
	 * Bytes
	 */
	private byte[] buffer;

	/**
	 * Last modified time
	 */
	private long lastModified;

	/**
	 * Children
	 */
	private Collection children;

	/**
	 * Constructor
	 */
	public RamFileData(FileName name)
	{
		super();
		this.clear();
		if (name == null)
		{
			throw new IllegalArgumentException("name can not be null");
		}
		this.name = name;
	}

	/**
	 * @return Returns the buffer.
	 */
	public byte[] getBuffer()
	{
		return buffer;
	}

	/**
	 * @param buffer
	 */
	public void setBuffer(byte[] buffer)
	{
		this.buffer = buffer;
	}

	/**
	 * @return Returns the lastModified.
	 */
	public long getLastModified()
	{
		return lastModified;
	}

	/**
	 * @param lastModified
	 *            The lastModified to set.
	 */
	public void setLastModified(long lastModified)
	{
		this.lastModified = lastModified;
	}

	/**
	 * @return Returns the type.
	 */
	public FileType getType()
	{
		return type;
	}

	/**
	 * @param type
	 *            The type to set.
	 */
	public void setType(FileType type)
	{
		this.type = type;
	}

	/**
	 * 
	 */
	public void clear()
	{
		this.buffer = new byte[0];
		this.lastModified = System.currentTimeMillis();
		this.type = FileType.IMAGINARY;
		this.children = Collections.synchronizedCollection(new ArrayList());
		this.name = null;
	}

	/**
	 * @return Returns the name.
	 */
	public FileName getName()
	{
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return this.name.toString();
	}

	/**
	 * Add a child
	 * 
	 * @param data
	 */
	public void addChild(RamFileData data) throws FileSystemException
	{
		if (!this.getType().equals(FileType.FOLDER))
		{
			throw new FileSystemException(
					"A child can only be added in a folder");
		}

		if (data == null)
		{
			throw new FileSystemException("No child can be null");
		}

		if (this.children.contains(data))
		{
			throw new FileSystemException("Child already exists. " + data);
		}

		this.children.add(data);
	}

	/**
	 * Remove a child
	 * 
	 * @param data
	 * @throws FileSystemException
	 */
	public void removeChild(RamFileData data) throws FileSystemException
	{
		if (!this.getType().equals(FileType.FOLDER))
		{
			throw new FileSystemException(
					"A child can only be removed from a folder");
		}
		if (!this.children.contains(data))
		{
			throw new FileSystemException("Child not found. " + data);
		}
		this.children.remove(data);
	}

	/**
	 * @return Returns the children.
	 */
	public Collection getChildren()
	{
		if (name == null)
		{
			throw new IllegalStateException("Data is clear");
		}
		return children;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o)
	{
		RamFileData data = (RamFileData) o;
		return this.getName().equals(data.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		return this.getName().hashCode();
	}

	public boolean hasChildren(RamFileData data)
	{
		return this.children.contains(data);
	}

	/**
	 * @return Returns the size of the buffer
	 */
	public int size()
	{
		return buffer.length;
	}

	/**
	 * Resize the buffer
	 * 
	 * @param newSize
	 */
	public void resize(int newSize)
	{
		int size = this.size();
		byte[] newBuf = new byte[newSize];
		System.arraycopy(this.buffer, 0, newBuf, 0, size);
		this.buffer = newBuf;
	}

}
