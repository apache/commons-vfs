package org.apache.commons.vfs2.provider.smb3;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;

import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.smbj.share.DiskEntry;
import com.hierynomus.smbj.share.File;

public class SMB3FileObject extends AbstractFileObject<SMB3FileSystem>
{
	private final String relPathToShare;
	private FileAllInformation fileInfo;
	private FileName rootName;
	private DiskEntry diskEntryWrite;
	private DiskEntry diskEntryRead;

	protected SMB3FileObject(AbstractFileName name, final SMB3FileSystem fs, final FileName rootName)
	{
		super(name, fs);
		String relPath = name.getURI().substring(rootName.getURI().length());
		relPathToShare = relPath.startsWith("/") ? relPath.substring(1).replace("/", "\\") : relPath.replace("/", "\\");
		this.rootName = rootName;
	}

	@Override
	protected long doGetContentSize() throws Exception
	{
		if(fileInfo == null)
		{
			getFileInfo();
		}
		return fileInfo.getStandardInformation().getEndOfFile();
	}

	@Override
	protected InputStream doGetInputStream() throws Exception
	{
		if (!getType().hasContent()) {
            throw new FileSystemException("vfs.provider/read-not-file.error", getName());
        }
		if (diskEntryRead == null)
		{
			getDiskEntryRead();
		}
		InputStream is = ((File) diskEntryRead).getInputStream();
		SMB3InputStreamWrapper inputStream = new SMB3InputStreamWrapper(is, this);
		return inputStream;
	}

	@Override
	protected FileType doGetType() throws Exception
	{
		synchronized (getFileSystem())
		{
			if (this.fileInfo == null)
			{
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
		synchronized (getFileSystem())
		{
			SMB3FileSystem fileSystem = (SMB3FileSystem) getFileSystem();
			SMB3ClientWrapper client = (SMB3ClientWrapper) fileSystem.getClient();
			fileInfo = client.getFileInfo(relPathToShare);
		}
	}

	@Override
	protected String[] doListChildren() throws Exception
	{
		// TODO Auto-generated method stub
		return null;
	}

	// make sure to return null if child is in share - root
	@Override
	public FileObject getParent() throws FileSystemException
	{
		if (getName().getBaseName().equals(relPathToShare))
		{
			// return null;

			// test
			synchronized (getFileSystem())
			{
				AbstractFileName name = (AbstractFileName) getName().getParent();
				return new SMB3FileObject(name, (SMB3FileSystem) getFileSystem(), rootName);
			}

		}

		synchronized (getFileSystem())
		{
			AbstractFileName name = (AbstractFileName) getName().getParent();
			return new SMB3FileObject(name, (SMB3FileSystem) getFileSystem(), rootName);
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
				SMB3FileSystem fileSystem = (SMB3FileSystem) getFileSystem();
				SMB3ClientWrapper client = (SMB3ClientWrapper) fileSystem.getClient();
				client.createFolder(relPathToShare);
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
		closeAllHandles(); //force resolve
	}

	private void getDiskEntryWrite() throws Exception
	{
		closeAllHandles();
		try
		{
			synchronized (getFileSystem())
			{
				SMB3FileSystem fileSystem = (SMB3FileSystem) getFileSystem();
				diskEntryWrite = fileSystem.getDiskEntryWrite(relPathToShare);
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
				SMB3FileSystem fileSystem = (SMB3FileSystem) getFileSystem();
				diskEntryRead = fileSystem.getDiskEntryRead(relPathToShare);
				// TODO check SmbPath to conversion / --> \\
			}
		} catch (Exception e)
		{
			throw new FileSystemException("Exception thrown getting DiskEntry: " + e.getCause());
		}
	}
	
	public String getRelPathToShare()
	{
		return relPathToShare;
	}

	@Override
	protected void doRename(final FileObject newFile) throws Exception
	{
		if (diskEntryWrite == null)
		{
			getDiskEntryWrite();
		}
		SMB3FileObject fo = (SMB3FileObject) newFile;
		diskEntryWrite.rename(fo.getRelPathToShare());
		
		//TODO maybo obsoloete
		closeAllHandles();
	}

	@Override
    protected FileObject[] doListChildrenResolved() throws Exception {
       
		synchronized (getFileSystem())
		{
			List<FileObject> children = new ArrayList<FileObject>();
			
			SMB3FileSystem fileSystem = (SMB3FileSystem) getFileSystem();
			SMB3ClientWrapper client = (SMB3ClientWrapper) fileSystem.getClient();
			String[] childrenNames = client.getChildren(relPathToShare);
			
			for(int i = 0; i < childrenNames.length; i++)
			{
				//String currentFolderURI = getName().getURI().endsWith("/") ? getName().getURI() : getName().getURI() + "/";
				//String childPath = currentFolderURI + childrenNames[i];
				children.add(fileSystem.getFileSystemManager().resolveFile(this, childrenNames[i]));
			}
			return children.toArray(new FileObject[children.size()]);
		}
    }
	
	@Override
	protected void doDelete() throws Exception 
	{
		synchronized (getFileSystem())
		{
			if(diskEntryRead != null)
			{
				diskEntryRead.close();
			}
			endOutput();
			
			SMB3FileSystem fileSystem = (SMB3FileSystem) getFileSystem();
			SMB3ClientWrapper client = (SMB3ClientWrapper) fileSystem.getClient();
			client.delete(relPathToShare);
		}
    }
	
	@Override
	public void close() throws FileSystemException
	{
		super.close();
		closeAllHandles();
	}
	
	private void closeAllHandles()
	{
		if(diskEntryRead != null)
		{
			diskEntryRead.close();
			diskEntryRead = null;
		}
		if(diskEntryWrite != null)
		{
			diskEntryWrite.close();
			diskEntryWrite = null;
		}
	}
	
}
