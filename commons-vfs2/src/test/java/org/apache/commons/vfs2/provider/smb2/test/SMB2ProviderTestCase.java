package org.apache.commons.vfs2.provider.smb2.test;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.smb2.SMB2FileProvider;
import org.apache.commons.vfs2.test.AbstractProviderTestConfig;
import org.apache.commons.vfs2.test.ProviderTestConfig;
import org.apache.commons.vfs2.test.ProviderTestSuite;

import junit.framework.Test;
import junit.framework.TestSuite;

public class SMB2ProviderTestCase extends AbstractProviderTestConfig implements ProviderTestConfig
{
	private final static String HOSTNAME = "192.168.2.35";
	private final static String USERINFO = "Administrator:2Ha17qnp3y";
	private final static String SHARENAME = "share";
	private final static int PORT = 443;

	final TestSuite suite = new TestSuite();

	public static Test suite() throws Exception
	{
		return new ProviderTestSuite(new SMB2ProviderTestCase());
	}

	public void prepare(final DefaultFileSystemManager manager) throws FileSystemException
	{
		manager.addProvider("smb2", new SMB2FileProvider());
	}

	public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception
	{
		return manager.resolveFile(buildURI().toString());
	}

	private URI buildURI() throws URISyntaxException
	{
		return new URI("smb2", USERINFO, HOSTNAME, PORT, ("/" + SHARENAME), null, null);
	}

}
