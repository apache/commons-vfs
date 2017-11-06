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
	private final AtomicReference<SMBClient> client = new AtomicReference<SMBClient>();
	
	protected SMB3FileSystem(FileName rootName, FileSystemOptions fileSystemOptions, SMBClient smbClient)
	{
		super(rootName, null, fileSystemOptions);
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
