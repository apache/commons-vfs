package org.apache.commons.vfs2.provider.smb3;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;

import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.share.DiskEntry;

public class SMB3FileSystem extends AbstractFileSystem
{

	//private SMBClient smbClient;
	
	private final AtomicReference<SMBClient> client = new AtomicReference<SMBClient>();
	
	protected SMB3FileSystem(FileName rootName, FileSystemOptions fileSystemOptions, SMBClient smbClient)
	{
		super(rootName, null, fileSystemOptions);
		//this.smbClient = smbClient;
		client.set(smbClient);
	}

	@Override
	protected FileObject createFile(AbstractFileName name) throws Exception
	{
		return new SMB3FileObject(name, this, getRootName());
	}

	@Override
	protected void addCapabilities(Collection<Capability> caps)
	{
		caps.addAll(SMB3FileProvider.capabilities);
	}
	
	public SMBClient getClient()
	{	
		return (SMB3ClientWrapper) client.get();
		//return smbClient;
	}
	
	public DiskEntry getDiskEntryWrite(String path)
	{
		return ((SMB3ClientWrapper) client.get()).getDiskEntryWrite(path);
	}
	
	public DiskEntry getDiskEntryRead(String path)
	{
		return ((SMB3ClientWrapper) client.get()).getDiskEntryRead(path);
	}

}
