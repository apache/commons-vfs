/*
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs.provider.ftp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;

import java.io.IOException;

/**
 * Create a FtpClient instance
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date$
 */
public class FtpClientFactory
{
    private FtpClientFactory()
    {
    }

    /**
     * Creates a new connection to the server.
     */
    public static FTPClient createConnection(String hostname, int port, String username, String password, String workingDirectory, FileSystemOptions fileSystemOptions) throws FileSystemException
    {
        // Determine the username and password to use
        if (username == null)
        {
            username = "anonymous";
        }

        if (password == null)
        {
            password = "anonymous";
        }

        try
        {
            final FTPClient client = new FTPClient();

            FTPFileEntryParserFactory myFactory = FtpFileSystemConfigBuilder.getInstance().getEntryParserFactory(fileSystemOptions);
            if (myFactory != null)
            {
                client.setParserFactory(myFactory);
            }

            try
            {
                client.connect(hostname, port);

                int reply = client.getReplyCode();
                if (!FTPReply.isPositiveCompletion(reply))
                {
                    throw new FileSystemException("vfs.provider.ftp/connect-rejected.error", hostname);
                }

                // Login
                if (!client.login(username, password))
                {
                    throw new FileSystemException("vfs.provider.ftp/login.error", new Object[]{hostname, username}, null);
                }

                // Set binary mode
                if (!client.setFileType(FTP.BINARY_FILE_TYPE))
                {
                    throw new FileSystemException("vfs.provider.ftp/set-binary.error", hostname);
                }

                // Change to root by default
                // All file operations a relative to the filesystem-root
                // String root = getRoot().getName().getPath();
                if (workingDirectory != null)
                {
                    if (!client.changeWorkingDirectory(workingDirectory))
                    {
                        throw new FileSystemException("vfs.provider/get-attributes-no-exist.error", "/");
                    }
                }

                Boolean passiveMode = FtpFileSystemConfigBuilder.getInstance().getPassiveMode(fileSystemOptions);
                if (passiveMode != null && passiveMode.booleanValue())
                {
                    client.enterLocalPassiveMode();
                }
            }
            catch (final IOException e)
            {
                if (client.isConnected())
                {
                    client.disconnect();
                }
                throw e;
            }

            return client;
        }
        catch (final Exception exc)
        {
            throw new FileSystemException("vfs.provider.ftp/connect.error", new Object[]{hostname}, exc);
        }
    }
}