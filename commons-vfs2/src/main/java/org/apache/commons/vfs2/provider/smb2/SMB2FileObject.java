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
package org.apache.commons.vfs2.provider.smb2;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.UriParser;

import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.smbj.share.DiskEntry;
import com.hierynomus.smbj.share.File;

/**
 * Class containing all its handles from the current Instance but NOT all
 * possible handles to the same file!!
 * <p>
 * Closing a stream (Input || Output) does not release the handle for the
 * current file. The handle itself must be closed! Otherwise the file got locked
 * up.
 * <p>
 * All methos accessing the FileSystem are declared a synchronized for
 * thread-safetyness
 */
public class SMB2FileObject extends AbstractFileObject<SMB2FileSystem>
{
	private final String relPathToShare;
	private FileAllInformation fileInfo;
	private FileName rootName;
	private DiskEntry diskEntryWrite;
	private DiskEntry diskEntryRead;
	private DiskEntry diskEntryFolderWrite;

	protected SMB2FileObject(AbstractFileName name, final SMB2FileSystem fs, final FileName rootName)
	{
		super(name, fs);
		String relPath = name.getURI().substring(rootName.getURI().length());
		// smb shares do not accept "/" --> it needs a "\" which is represented by "\\"
		relPathToShare = relPath.startsWith("/") ? relPath.substring(1).replace("/", "\\") : relPath.replace("/", "\\");
		this.rootName = rootName;
	}

	@Override
	protected long doGetContentSize() throws Exception
	{
		getFileInfo();
		return fileInfo.getStandardInformation().getEndOfFile();
	}

	@Override
	protected InputStream doGetInputStream() throws Exception
	{
		if (!getType().hasContent())
		{
			throw new FileSystemException("vfs.provider/read-not-file.error", getName());
		}
		if (diskEntryRead == null)
		{
			getDiskEntryRead();
		}
		InputStream is = ((File) diskEntryRead).getInputStream();

		// wrapped to override the close method. For further details see
		// SMB3InputStreamWrapper.class
		SMB2InputStreamWrapper inputStream = new SMB2InputStreamWrapper(is, this);
		return inputStream;
	}

	@Override
	protected FileType doGetType() throws Exception
	{
		synchronized (getFileSystem())
		{
			if (this.fileInfo == null)
			{
				// returns null if the diskShare cannot the file info's. Therefore : imaginary
				getFileInfo();
			}
			if (fileInfo == null)
			{
				return FileType.IMAGINARY;
			} else
			{
				return (fileInfo.getStandardInformation().isDirectory()) ? FileType.FOLDER : FileType.FILE;
			}
		}
	}

	private void getFileInfo()
	{
		if (fileInfo == null)
		{
			synchronized (getFileSystem())
			{
				SMB2FileSystem fileSystem = (SMB2FileSystem) getFileSystem();
				SMB2ClientWrapper client = (SMB2ClientWrapper) fileSystem.getClient();
				fileInfo = client.getFileInfo(getRelPathToShare());
			}

		}
	}

	@Override
	protected String[] doListChildren() throws Exception
	{
		// not using this method
		return null;
	}

	@Override
	public FileObject getParent() throws FileSystemException
	{
		synchronized (getFileSystem())
		{
			AbstractFileName name = (AbstractFileName) getName().getParent();

			// root folder has no parent
			if (name == null)
			{
				return null;
			}
			FileObject cachedFile = getFileSystem().getFileSystemManager().getFilesCache().getFile(getFileSystem(),
					name);
			if (cachedFile != null)
			{
				return cachedFile;
			} else
			{
				return new SMB2FileObject(name, (SMB2FileSystem) getFileSystem(), rootName);
			}
		}
	}

	@Override
	protected OutputStream doGetOutputStream(final boolean bAppend) throws Exception
	{
		if (diskEntryWrite == null)
		{
			getDiskEntryWrite();
		}

		return ((File) diskEntryWrite).getOutputStream();
	}

	@Override
	protected void doCreateFolder() throws Exception
	{
		try
		{
			synchronized (getFileSystem())
			{
				SMB2FileSystem fileSystem = (SMB2FileSystem) getFileSystem();
				SMB2ClientWrapper client = (SMB2ClientWrapper) fileSystem.getClient();
				client.createFolder(getRelPathToShare());
			}
		} catch (Exception e)
		{
			throw new FileSystemException("Exception thrown creating folder: " + e.getCause());
		}
	}

	@Override
	protected void endOutput() throws Exception
	{
		super.endOutput();
		closeAllHandles(); // also close the handles
	}

	private void getDiskEntryWrite() throws Exception
	{
		closeAllHandles();
		try
		{
			synchronized (getFileSystem())
			{
				SMB2FileSystem fileSystem = (SMB2FileSystem) getFileSystem();
				diskEntryWrite = fileSystem.getDiskEntryWrite(getRelPathToShare());
			}
		} catch (Exception e)
		{
			throw new FileSystemException("Exception thrown getting DiskEntry: " + e.getCause());
		}
	}

	private void getDiskEntryRead() throws Exception
	{
		try
		{
			synchronized (getFileSystem())
			{
				SMB2FileSystem fileSystem = (SMB2FileSystem) getFileSystem();
				diskEntryRead = fileSystem.getDiskEntryRead(getRelPathToShare());
			}
		} catch (Exception e)
		{
			throw new FileSystemException("Exception thrown getting DiskEntry: " + e.getCause());
		}
	}

	private void getDiskEntryFolderWrite() throws Exception
	{
		try
		{
			synchronized (getFileSystem())
			{
				SMB2FileSystem fileSystem = (SMB2FileSystem) getFileSystem();
				diskEntryFolderWrite = fileSystem.getDiskEntryFolderWrite(getRelPathToShare());
			}
		} catch (Exception e)
		{
			throw new FileSystemException("Exception thrown getting DiskEntry: " + e.getCause());
		}
	}

	public String getRelPathToShare()
	{
		return decodeOrGet(relPathToShare);
	}

	public String decodeOrGet(String s)
	{
		try
		{
			return UriParser.decode(s);
		} catch (FileSystemException e)
		{
			return s;
		}
	}

	public String encodeOrGet(String s)
	{
		return UriParser.encode(s);
	}

	@Override
	protected void doRename(final FileObject newFile) throws Exception
	{
		if (doGetType() == FileType.FOLDER)
		{
			if (diskEntryFolderWrite == null)
			{
				getDiskEntryFolderWrite();
			}
			SMB2FileObject fo = (SMB2FileObject) newFile;
			diskEntryFolderWrite.rename(fo.getRelPathToShare());
		} else
		{
			if (diskEntryWrite == null)
			{
				getDiskEntryWrite();
			}
			SMB2FileObject fo = (SMB2FileObject) newFile;
			diskEntryWrite.rename(fo.getRelPathToShare());

			// TODO maybo obsoloete
			closeAllHandles();
		}
	}

	@Override
	protected FileObject[] doListChildrenResolved() throws Exception
	{

		synchronized (getFileSystem())
		{
			if (getType() != FileType.FOLDER)
			{
				throw new FileSystemException("vfs.provider/list-children-not-folder.error", this);
			}

			List<FileObject> children = new ArrayList<FileObject>();

			SMB2FileSystem fileSystem = (SMB2FileSystem) getFileSystem();
			SMB2ClientWrapper client = (SMB2ClientWrapper) fileSystem.getClient();
			String[] childrenNames = client.getChildren(getRelPathToShare());

			for (int i = 0; i < childrenNames.length; i++)
			{
				children.add(fileSystem.getFileSystemManager().resolveFile(this, encodeOrGet(childrenNames[i])));
			}
			return children.toArray(new FileObject[children.size()]);
		}
	}

	@Override
	protected void doDelete() throws Exception
	{
		synchronized (getFileSystem())
		{
			if (diskEntryRead != null)
			{
				diskEntryRead.close();
			}
			endOutput();

			SMB2FileSystem fileSystem = (SMB2FileSystem) getFileSystem();
			SMB2ClientWrapper client = (SMB2ClientWrapper) fileSystem.getClient();
			client.delete(getRelPathToShare());
		}
	}

	@Override
	protected long doGetLastModifiedTime() throws Exception
	{
		getFileInfo();
		return fileInfo.getBasicInformation().getChangeTime().getWindowsTimeStamp();
	}

	// needs to be overridden to also close the file Handles when the FileObject is
	// closed. Otherwise files got locked up
	@Override
	public void close() throws FileSystemException
	{
		super.close();
		closeAllHandles();
	}

	private void closeAllHandles()
	{
		if (diskEntryRead != null)
		{
			diskEntryRead.close();
			diskEntryRead = null;
		}
		if (diskEntryWrite != null)
		{
			diskEntryWrite.close();
			diskEntryWrite = null;
		}
	}

	@Override
	protected void doDetach()
	{
		this.fileInfo = null;
	}

	public String toString()
	{
		return getName().toString();
	}

}
