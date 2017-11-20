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
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskEntry;
import com.hierynomus.smbj.share.DiskShare;

/**
 * A wrapper to the SMBClient for bundling the related client & connection instances.
 * <p>
 * The SMBClient ALWAYS needs a share. The share is part of the rootURI provided by the FileNameParser
 */
public class SMB2ClientWrapper extends SMBClient
{
	private static final SmbConfig CONFIG = SmbConfig.builder()
			.withDfsEnabled(true)
			.withMultiProtocolNegotiate(true)
			.build();
	
	protected final FileSystemOptions fileSystemOptions;
	private final GenericFileName root;
	private SMBClient smbClient;
	private Connection connection;
	private Session session;
	private DiskShare diskShare;
	
	protected SMB2ClientWrapper(final GenericFileName root, final FileSystemOptions fileSystemOptions) throws FileSystemException
	{
		this.root = root;
		this.fileSystemOptions = fileSystemOptions;
		smbClient = new SMBClient(CONFIG);
		setupClient();
	}
	
	private void setupClient() throws FileSystemException
	{
		final GenericFileName rootName = getRoot();
		
		//the relevant data to authenticate a connection
		String userName = (rootName.getUserName().equals("") || rootName.getUserName() == null) ? "" : ((rootName.getUserName().contains(";") ? rootName.getUserName().substring(rootName.getUserName().indexOf(";")+1, rootName.getUserName().length()) : rootName.getUserName()));
		String password = rootName.getPassword();
		String authDomain = (rootName.getUserName().contains(";") ? rootName.getUserName().substring(0, rootName.getUserName().indexOf(";")) : null);
		
		//if username == "" the client tries to authenticate "anonymously". It's also possible to summit "guets" as username
		AuthenticationContext authContext = new AuthenticationContext(userName, password.toCharArray(), authDomain);

		//a connection stack is: SMBClient > Connection > Session > DiskShare
		try
		{
			connection = smbClient.connect(rootName.getHostName());
			session = connection.authenticate(authContext);
			String share = ((SMB2FileName) rootName).getShareName();
			diskShare = (DiskShare) session.connectShare(share);
		} catch (Exception e)
		{
			throw new FileSystemException("Error while creation a connection: " + e.getCause());
		}
		
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
	
	//create a WRITE handle on the file
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
	
	//creates a folder and immediately closes the handle
	public void createFolder(String path)
	{
		DiskEntry de = getDiskEntryFolderWrite(path);
		de.close();
	}
	
	public DiskEntry getDiskEntryFolderWrite(String path)
	{
		DiskEntry de = diskShare.openDirectory(path, 
				EnumSet.of(AccessMask.GENERIC_ALL),
    			EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
    			EnumSet.of(SMB2ShareAccess.FILE_SHARE_READ),
				SMB2CreateDisposition.FILE_OPEN_IF,
				EnumSet.of(SMB2CreateOptions.FILE_DIRECTORY_FILE));
		
		return de;
	}
	
	
	
	//creates a READ handle for the file
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
		FileAllInformation info = null;
		try
		{
			info = diskShare.getFileInformation(path);
		}
		catch(Exception e)
		{
			//file or folder does not exist
			return;
		}
		if(info.getStandardInformation().isDirectory())
		{
			diskShare.rmdir(path, true);
		}
		else
		{
			diskShare.rm(path);
		}
	}
	
	
}
