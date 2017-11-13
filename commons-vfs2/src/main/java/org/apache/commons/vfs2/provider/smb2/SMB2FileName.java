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

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.GenericFileName;

/**
 * Using an explicit fileName for SMB2 since the uri must contain a share name.  
 * <p>
 * The share name belongs to the rootURI, whereas the AbsPath must not contain the share
 */
public class SMB2FileName extends GenericFileName
{
	private final String shareName;
	private String rootUri;
	private String uri;

	protected SMB2FileName(String scheme, String hostName, int port, int defaultPort, String userName, String password,
			String path, FileType type, String shareName)
	{
		super(scheme, hostName, port, defaultPort, userName, password, path, type);
		this.shareName = shareName;
		createURI();
	}

	public String getShareName()
	{
		return shareName;
	}

	@Override
	public String getFriendlyURI()
	{
		return createURI(false, false);
	}

	@Override
	public String getURI()
	{
		if (uri == null)
		{
			uri = createURI();
		}
		return uri;
	}

	protected String createURI()
	{
		return createURI(false, true);
	}

	//the share needs to be inserted since it has been extracted from absPath (getPath())
	private String createURI(final boolean useAbsolutePath, final boolean usePassword)
	{
		StringBuilder sb = new StringBuilder();
		appendRootUri(sb, usePassword);
		if(sb.charAt(sb.length() -1 ) != '/')
		{
			sb.append('/');
		}
		sb.append(shareName);
		
		if(!(getPath() == null || getPath().equals("/")))
		{
			if(!getPath().startsWith("/"))
			{
				sb.append('/');
			}
			sb.append(getPath());
		}

		return sb.toString();
	}

	@Override
	public String getRootURI()
	{
		if (this.rootUri == null)
		{
			String uri = super.getRootURI();
			this.rootUri = uri + shareName;
		}
		return this.rootUri;
	}

	@Override
	public FileName getParent()
	{

		if (this.rootUri == null)
		{
			getRootURI();
		}

		if (getPath().replaceAll("/", "").equals(shareName) || getPath().equals("/") || getPath().equals(""))
		{
			return null; //if this method is called from the root name, return null because there is no parent
		} 
		else
		{
			SMB2FileName name = new SMB2FileName(this.getScheme(), this.getHostName(), this.getPort(),
					this.getDefaultPort(), this.getUserName(), this.getPassword(),
					getPath().substring(0, getPath().lastIndexOf("/")), this.getType(), shareName);
			return name;
		}
	}

	//inserting the share name since it has been extracted from the absPath
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getScheme());
		sb.append("://");
		sb.append(getUserName());
		sb.append(':');
		sb.append(getPassword());
		sb.append('@');
		sb.append(getHostName());
		sb.append("/");
		sb.append(shareName);
		if (!getPath().startsWith("/"))
		{
			sb.append('/');
		}
		sb.append(getPath());
		return sb.toString();
	}

}
