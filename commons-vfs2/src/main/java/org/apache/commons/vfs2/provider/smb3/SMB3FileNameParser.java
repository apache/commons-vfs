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

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.HostFileNameParser;
import org.apache.commons.vfs2.provider.VfsComponentContext;

public class SMB3FileNameParser extends HostFileNameParser {

	private static final SMB3FileNameParser INSTANCE = new SMB3FileNameParser();
	
	private static final int PORT = 443;
	
	public SMB3FileNameParser()
	{
		super(PORT);
	}
	
	public static SMB3FileNameParser getInstance()
	{
		return INSTANCE;
	}
	
	public FileName parseShareRoot(VfsComponentContext ctx, FileName name, String path) throws FileSystemException
	{
		URI uri = null;
		try
		{
			uri = new URI(name.getPath());
		} catch (URISyntaxException e)
		{
			throw new FileSystemException("URI invalid: FileSystem depends on it! " + e.getCause());
		}
		if(uri.toString().equals("/"))
		{
			//no share submitted, can not determine root
			return null;
		}
		String share = extractShareName(uri);
		
		//dunno why sometimes the path ends up with two "/" at the end
		while(path.endsWith("/") && path.length() > 0)
		{
			path = path.substring(0, path.length()-1);
		}
		
		String rootPathShare = path + "/" + share + "/";  //add '/' to the end so the share gets parsed as folder by the HostFileNameParser
		
		return parseUri(ctx, name, rootPathShare);
	}
	
	protected String extractShareName(URI uri)
	{
		String s = uri.getPath().startsWith("/") ? uri.getPath().substring(1) : uri.getPath();
		String[] pathParts = s.split("/");
		
		return pathParts[0];
	}

}