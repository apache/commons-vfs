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

import java.net.URI;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.HostFileNameParser;
import org.apache.commons.vfs2.provider.VfsComponentContext;

public class SMB2FileNameParser extends HostFileNameParser {

	private static final SMB2FileNameParser INSTANCE = new SMB2FileNameParser();
	
	private static final int PORT = 443;
	
	public SMB2FileNameParser()
	{
		super(PORT);
	}
	
	public static SMB2FileNameParser getInstance()
	{
		return INSTANCE;
	}
	
	protected String extractShareName(URI uri) throws FileSystemException
	{
		String s = uri.getPath().startsWith("/") ? uri.getPath().substring(1) : uri.getPath();
		String[] pathParts = s.split("/");
		String share  = pathParts[0];
		if(share == null || share.equals(""))
		{
			throw new FileSystemException("vfs.provider.smb2/missing-share-name.error", uri.toString());
		}
		
		return pathParts[0]; //TODO check share given by uri
	}
	
	@Override
	public FileName parseUri(final VfsComponentContext context, final FileName base, final String uri) throws FileSystemException
    {
		FileName parsedFileName = super.parseUri(context, base, uri);
		String share;
		if(base == null)
		{
			share = extractShareName(parseURIString(parsedFileName.toString()));
		}
		else
		{
			share = extractShareName(parseURIString(base.toString()));
		}

		StringBuilder sb = new StringBuilder();
		Authority auth = extractToPath(parsedFileName.toString(), sb);
		
		String path;
		
		if(sb.length() == 0 || (sb.length() == 1 && sb.charAt(0) == '/'))
		{
			//this must point to the share root
			path = "/" + share;
		}
		else
		{
			path = parsedFileName.getPath();
		}
		
		String relPathFromShare;
		try
		{
			relPathFromShare = removeShareFromAbsPath(path, share);
		}
		catch(Exception e)
		{
			throw new FileSystemException("vfs.provider.smb2/share-path-extraction.error", path, e.getCause());
		}
		
		SMB2FileName fileName = new SMB2FileName(auth.getScheme(), auth.getHostName(), auth.getPort(), PORT, auth.getUserName(), auth.getPassword(), relPathFromShare, parsedFileName.getType(), share);
		
		
		return fileName;
    }
	
	public URI parseURIString(String uriString) throws FileSystemException
	{
		try
		{
			return new URI(uriString);
		}
		catch (Exception e)
		{
			throw new FileSystemException("vfs.provider.url/badly-formed-uri.error", uriString, e.getCause());
		}
	}
	
	public String removeShareFromAbsPath(String path, String shareName) throws Exception
	{
		if(shareName == null || shareName.length() == 0)
		{
			throw new Exception("No path provided!");
		}
		
		String tmp = path.startsWith("/") ? path.substring(1) : path;
		
		if(!tmp.substring(0, shareName.length()).equals(shareName)) {
			throw new Exception("Share does not match the provided path!");
		}
		
		tmp = tmp.substring(shareName.length());
		
		if(tmp.equals("") || tmp.equals("/"))
		{
			return "";
		}
		return tmp;
	}
	
	
	
	

}