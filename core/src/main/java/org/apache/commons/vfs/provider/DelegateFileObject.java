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
package org.apache.commons.vfs.provider;

import org.apache.commons.vfs.FileChangeEvent;
import org.apache.commons.vfs.FileListener;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileContentInfo;
import org.apache.commons.vfs.RandomAccessContent;
import org.apache.commons.vfs.util.WeakRefFileListener;
import org.apache.commons.vfs.util.RandomAccessMode;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A file backed by another file.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @author Gary D. Gregory
 * @version $Revision$ $Date$
 * @todo Extract subclass that overlays the children
 */
public class DelegateFileObject
	extends AbstractFileObject
	implements FileListener
{
	private FileObject file;
	private final Set children = new HashSet();
	private boolean ignoreEvent;

	public DelegateFileObject(final FileName name,
							  final AbstractFileSystem fileSystem,
							  final FileObject file) throws FileSystemException
	{
		super(name, fileSystem);
		this.file = file;
		if (file != null)
		{
			WeakRefFileListener.installListener(file, this);
		}
	}

	/**
	 * get access to the delegated file
	 */
	public FileObject getDelegateFile()
	{
		return file;
	}

	/**
	 * Adds a child to this file.
	 */
	public void attachChild(final FileName baseName, final FileType type) throws Exception
	{
		final FileType oldType = doGetType();
		if (children.add(baseName.getBaseName()))
		{
			childrenChanged(baseName, type);
		}
		maybeTypeChanged(oldType);
	}

	/**
	 * Attaches or detaches the target file.
	 */
	public void setFile(final FileObject file) throws Exception
	{
		final FileType oldType = doGetType();

		if (file != null)
		{
			WeakRefFileListener.installListener(file, this);
		}
		this.file = file;
		maybeTypeChanged(oldType);
	}

	/**
	 * Checks whether the file's type has changed, and fires the appropriate
	 * events.
	 */
	private void maybeTypeChanged(final FileType oldType) throws Exception
	{
		final FileType newType = doGetType();
		if (oldType == FileType.IMAGINARY && newType != FileType.IMAGINARY)
		{
			handleCreate(newType);
		}
		else if (oldType != FileType.IMAGINARY && newType == FileType.IMAGINARY)
		{
			handleDelete();
		}
	}

	/**
	 * Determines the type of the file, returns null if the file does not
	 * exist.
	 */
	protected FileType doGetType() throws FileSystemException
	{
		if (file != null)
		{
			return file.getType();
		}
		else if (children.size() > 0)
		{
			return FileType.FOLDER;
		}
		else
		{
			return FileType.IMAGINARY;
		}
	}

	/**
	 * Determines if this file can be read.
	 */
	protected boolean doIsReadable() throws FileSystemException
	{
		if (file != null)
		{
			return file.isReadable();
		}
		else
		{
			return true;
		}
	}

	/**
	 * Determines if this file can be written to.
	 */
	protected boolean doIsWriteable() throws FileSystemException
	{
		if (file != null)
		{
			return file.isWriteable();
		}
		else
		{
			return false;
		}
	}

	/**
	 * Determines if this file is hidden.
	 */
	protected boolean doIsHidden() throws FileSystemException
	{
		if (file != null)
		{
			return file.isHidden();
		}
		else
		{
			return false;
		}
	}

	/**
	 * Lists the children of the file.
	 */
	protected String[] doListChildren() throws Exception
	{
		if (file != null)
		{
			final FileObject[] children = file.getChildren();
			final String[] childNames = new String[children.length];
			for (int i = 0; i < children.length; i++)
			{
				childNames[i] = children[i].getName().getBaseName();
			}
			return childNames;
		}
		else
		{
			return (String[]) children.toArray(new String[children.size()]);
		}
	}

	/**
	 * Creates this file as a folder.
	 */
	protected void doCreateFolder() throws Exception
	{
		ignoreEvent = true;
		try
		{
			file.createFolder();
		}
		finally
		{
			ignoreEvent = false;
		}
	}

	/**
	 * Deletes the file.
	 */
	protected void doDelete() throws Exception
	{
		ignoreEvent = true;
		try
		{
			file.delete();
		}
		finally
		{
			ignoreEvent = false;
		}
	}

	/**
	 * Returns the size of the file content (in bytes).  Is only called if
	 * {@link #doGetType} returns {@link FileType#FILE}.
	 */
	protected long doGetContentSize() throws Exception
	{
		return file.getContent().getSize();
	}

	/**
	 * Returns the attributes of this file.
	 */
	protected Map doGetAttributes()
		throws Exception
	{
		return file.getContent().getAttributes();
	}

	/**
	 * Sets an attribute of this file.
	 */
	protected void doSetAttribute(final String atttrName,
								  final Object value)
		throws Exception
	{
		file.getContent().setAttribute(atttrName, value);
	}

	/**
	 * Returns the certificates of this file.
	 */
	protected Certificate[] doGetCertificates() throws Exception
	{
		return file.getContent().getCertificates();
	}

	/**
	 * Returns the last-modified time of this file.
	 */
	protected long doGetLastModifiedTime() throws Exception
	{
		return file.getContent().getLastModifiedTime();
	}

	/**
	 * Sets the last-modified time of this file.
	 */
	protected boolean doSetLastModTime(final long modtime)
		throws Exception
	{
		file.getContent().setLastModifiedTime(modtime);
		return true;
	}

	/**
	 * Creates an input stream to read the file content from.
	 */
	protected InputStream doGetInputStream() throws Exception
	{
		return file.getContent().getInputStream();
	}

	/**
	 * Creates an output stream to write the file content to.
	 */
	protected OutputStream doGetOutputStream(boolean bAppend) throws Exception
	{
		return file.getContent().getOutputStream(bAppend);
	}

	/**
	 * Called when a file is created.
	 */
	public void fileCreated(final FileChangeEvent event) throws Exception
	{
		if (event.getFile() != file)
		{
			return;
		}
		if (!ignoreEvent)
		{
			handleCreate(file.getType());
		}
	}

	/**
	 * Called when a file is deleted.
	 */
	public void fileDeleted(final FileChangeEvent event) throws Exception
	{
		if (event.getFile() != file)
		{
			return;
		}
		if (!ignoreEvent)
		{
			handleDelete();
		}
	}

	/**
	 * Called when a file is changed.
	 * <p/>
	 * This will only happen if you monitor the file using {@link org.apache.commons.vfs.FileMonitor}.
	 */
	public void fileChanged(FileChangeEvent event) throws Exception
	{
		if (event.getFile() != file)
		{
			return;
		}
		if (!ignoreEvent)
		{
			handleChanged();
		}
	}

	/**
	 * Close the delegated file
	 */
	public void close() throws FileSystemException
	{
		super.close();

		if (file != null)
		{
			file.close();
		}
	}

	/**
	 * refresh file informations
	 */
	public void refresh() throws FileSystemException
	{
		super.refresh();
		if (file != null)
		{
			file.refresh();
		}
	}

	protected FileContentInfo doGetContentInfo() throws Exception
	{
		return file.getContent().getContentInfo();
	}

	/**
	 * Renames the file.
	 */
	protected void doRename(FileObject newFile)
		throws Exception
	{
		file.moveTo(((DelegateFileObject) newFile).file);
	}

	/**
	 * Removes an attribute of this file.
	 */
	protected void doRemoveAttribute(final String atttrName)
		throws Exception
	{
		file.getContent().removeAttribute(atttrName);
	}

	/**
	 * Creates access to the file for random i/o.
	 */
	protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception
	{
		return file.getContent().getRandomAccessContent(mode);
	}
}
