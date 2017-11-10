package org.apache.commons.vfs2.provider.smb2;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.GenericFileName;

public class SMB2FileName extends GenericFileName
{
	private final String shareName;
	private String rootUri;
	
	
	protected SMB2FileName(String scheme, String hostName, int port, int defaultPort, String userName, String password,
			String path, FileType type, String shareName)
	{
		super(scheme, hostName, port, defaultPort, userName, password, path, type);
		this.shareName = shareName;
	}
	
	public String getShareName()
	{
		return shareName;
	}
	
	@Override
    public String getRootURI() {
		if(this.rootUri == null)
		{
			String uri = super.getRootURI();
			this.rootUri = uri + shareName;
		}
		return this.rootUri;
	}
	
	@Override
    public FileName getParent() {
		
		if(this.rootUri == null)
		{
			getRootURI();
		}
		
		if(getPath().replaceAll("/", "").equals(shareName))
		{
			return null;
		}
		else
		{
			SMB2FileName name = new SMB2FileName(this.getScheme(), this.getHostName(), this.getPort(), this.getDefaultPort(), this.getUserName(), this.getPassword(), getPath().substring(0,  getPath().lastIndexOf("/")), this.getType(), shareName);
			return name;
		}
    }
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
		if(!getPath().startsWith("/"))
		{
			sb.append('/');
		}
		sb.append(getPath());
		return sb.toString();
	}

}
