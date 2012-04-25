package org.apache.commons.vfs2.provider.ftp.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.ftpserver.ftplet.FtpException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MultipleConnectionTestCase
{

    @BeforeClass
    public static void setUpClass() throws FtpException, IOException
    {
        FtpProviderTestCase.setUpClass();
    }

    @AfterClass
    public static void tearDownClass() throws MalformedURLException, FtpException
    {
        FtpProviderTestCase.tearDownClass();
    }

    @Test
    public void testUnderlyingConnect() throws SocketException, IOException
    {
        FTPClient client1 = new FTPClient();
        FTPClient client2 = new FTPClient();
        try
        {
            final String hostname = "localhost";
            client1.connect(hostname, FtpProviderTestCase.getSocketPort());
            client2.connect(hostname, FtpProviderTestCase.getSocketPort());
        } finally
        {
            if (client1 != null)
            {
                client1.disconnect();
            }
            if (client2 != null)
            {
                client2.disconnect();
            }
        }
    }
}
