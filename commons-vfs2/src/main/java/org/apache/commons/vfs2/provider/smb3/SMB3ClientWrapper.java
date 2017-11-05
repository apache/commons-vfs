package org.apache.commons.vfs2.provider.smb3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.GenericFileName;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileAllInformation;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskEntry;
import com.hierynomus.smbj.share.DiskShare;

public class SMB3ClientWrapper extends SMBClient
{
	protected final FileSystemOptions fileSystemOptions;
	private final GenericFileName root;
	private SMBClient smbClient;
	private Connection connection;
	private Session session;
	private DiskShare diskShare;
	
	protected SMB3ClientWrapper(final GenericFileName root, final FileSystemOptions fileSystemOptions) throws FileSystemException
	{
		this.root = root;
		this.fileSystemOptions = fileSystemOptions;
		smbClient = new SMBClient();
		setupClient();
	}
	
	private void setupClient()
	{
		final GenericFileName rootName = getRoot();
		
		String userName = (rootName.getUserName().equals("") || rootName.getUserName() == null) ? "" : ((rootName.getUserName().contains(";") ? rootName.getUserName().substring(rootName.getUserName().indexOf(";")+1, rootName.getUserName().length()) : rootName.getUserName()));
		String password = rootName.getPassword();
		String authDomain = (rootName.getUserName().contains(";") ? rootName.getUserName().substring(0, rootName.getUserName().indexOf(";")) : null);
		AuthenticationContext authContext = new AuthenticationContext(userName, password.toCharArray(), authDomain);
		setupClient(rootName, authContext);

	}
	
	protected void setupClient(final GenericFileName rootName, final AuthenticationContext authContext)
	{
		smbClient = new SMBClient();
		try
		{
			connection = smbClient.connect(rootName.getHostName());
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		session = connection.authenticate(authContext);
		
		String share = extractShare(rootName);
		
		diskShare = (DiskShare) session.connectShare(share);
	}
	
	private String extractShare(final GenericFileName rootName)
	{
		if(rootName.getPath().equals("") || rootName.getPath().equals("/"))
		{
			return null;
		}
		String[] pathParts = (rootName.getPath().startsWith("/")) ? rootName.getPath().substring(1).split("/") : rootName.getPath().split("/");
		
		return pathParts[0];
	}
	
	public GenericFileName getRoot()
	{
		return root;
	}
	
	public FileAllInformation getFileInfo(String relPath)
	{
		try
		{
			return diskShare.getFileInformation(relPath);
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	public DiskEntry getDiskEntryWrite(String path)
	{
		DiskEntry diskEntryWrite = diskShare.open(path,
    			EnumSet.of(AccessMask.GENERIC_ALL),
    			EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
    			EnumSet.of(SMB2ShareAccess.FILE_SHARE_WRITE),
				SMB2CreateDisposition.FILE_OPEN_IF,
				EnumSet.of(SMB2CreateOptions.FILE_NO_COMPRESSION));
		
		return diskEntryWrite;
	}
	
	public void createFolder(String path)
	{
		DiskEntry de = diskShare.openDirectory(path, 
				EnumSet.of(AccessMask.GENERIC_WRITE),
    			EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
    			EnumSet.of(SMB2ShareAccess.FILE_SHARE_READ),
				SMB2CreateDisposition.FILE_CREATE,
				EnumSet.of(SMB2CreateOptions.FILE_DIRECTORY_FILE));
		
		de.close();
	}
	
	public DiskEntry getDiskEntryRead(String path)
	{
		DiskEntry diskEntryRead = diskShare.open(path, 
    			EnumSet.of(AccessMask.GENERIC_READ),
    			EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
    			EnumSet.of(SMB2ShareAccess.FILE_SHARE_READ),
				SMB2CreateDisposition.FILE_OPEN,
				EnumSet.of(SMB2CreateOptions.FILE_NO_COMPRESSION));
		
		return diskEntryRead;
	}
	
	public String[] getChildren(String path)
	{
		List<String> children = new ArrayList<String>();
		
		for(FileIdBothDirectoryInformation file : diskShare.list(path))
		{
			String name = file.getFileName();
			if (name.equals(".") || name.equals("..") || name.equals("./") || name.equals("../")) 
			{
                continue;
            }
			children.add(file.getFileName());
		}
		return children.toArray(new String[children.size()]);
	}
	
	public void delete(String path)
	{
		
		diskShare.rm(path);
	}
}
