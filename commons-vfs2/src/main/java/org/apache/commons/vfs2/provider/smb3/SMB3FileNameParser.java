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
			//TODO logger write whats wrong...
			e.printStackTrace();
		}
		if(uri.toString().equals("/"))
		{
			//TODO logger no share submitted
			return null;
		}
		String share = extractShareName(uri);
		
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