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
package org.apache.commons.vfs2.provider.ftps.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.ftpserver.ftplet.FtpException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MultipleConnectionTestCase
{

    @BeforeClass
    public static void setUpClass() throws FtpException, IOException
    {
        FtpsProviderTestCase_Disabled.setUpClass();
    }

    @AfterClass
    public static void tearDownClass() throws MalformedURLException, FtpException
    {
        FtpsProviderTestCase_Disabled.tearDownClass();
    }

    private FTPSClient init(final FTPSClient client)
    {
        client.enterLocalPassiveMode();
        return client;
    }

    private FileObject resolveRoot() throws FileSystemException
    {
        return VFS.getManager().resolveFile(FtpsProviderTestCase_Disabled.getConnectionUri(),
                new FtpsProviderTestCase_Disabled().getFileSystemOptions());
    }

    @Test
    public void testConnectRoot() throws SocketException, IOException
    {
        resolveRoot();
        resolveRoot();
    }

    @Test
    public void testUnderlyingConnect() throws SocketException, IOException
    {
        final FTPSClient client1 = this.init(new FTPSClient(true));
        final FTPSClient client2 = this.init(new FTPSClient(true));
        try
        {
            final String hostname = "localhost";
            client1.connect(hostname, FtpsProviderTestCase_Disabled.getSocketPort());
            client2.connect(hostname, FtpsProviderTestCase_Disabled.getSocketPort());
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
