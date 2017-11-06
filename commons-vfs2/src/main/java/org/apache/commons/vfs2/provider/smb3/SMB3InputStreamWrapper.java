package org.apache.commons.vfs2.provider.smb3;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs2.FileObject;

public class SMB3InputStreamWrapper extends InputStream
{
	private InputStream is;
	private final FileObject fo;
	
	public SMB3InputStreamWrapper(InputStream is, final FileObject fo)
	{
		this.is = is;
		this.fo = fo;
	}

	@Override
	public int read() throws IOException
	{
		return is.read();
	}
	
	@Override
	public void close() throws IOException
	{
		is.close();
		fo.close();
	}
}
