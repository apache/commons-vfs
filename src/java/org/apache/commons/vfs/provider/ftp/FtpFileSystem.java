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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.VfsLog;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.GenericFileName;

import java.io.IOException;
import java.util.Collection;

/**
 * An FTP file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.6 $ $Date: 2002/07/05 04:08:19 $
 */
final class FtpFileSystem
    extends AbstractFileSystem
{
    private final static Log log = LogFactory.getLog(FtpFileSystem.class);

    private final String hostname;
    private final int port;
    private final String username;
    private final String password;

    // An idle client
    private FTPClient idleClient;

    public FtpFileSystem(final GenericFileName rootName, final FileSystemOptions fileSystemOptions)
    {
        super(rootName, null, fileSystemOptions);
        hostname = rootName.getHostName();
        port = rootName.getPort();

        // Determine the username and password to use
        if (rootName.getUserName() == null)
        {
            username = "anonymous";
        }
        else
        {
            username = rootName.getUserName();
        }
        if (rootName.getPassword() == null)
        {
            password = "anonymous";
        }
        else
        {
            password = rootName.getPassword();
        }
    }

    public void close()
    {
        // Clean up the connection
        if (idleClient != null)
        {
            closeConnection(idleClient);
        }

        super.close();
    }

    /**
     * Adds the capabilities of this file system.
     */
    protected void addCapabilities(final Collection caps)
    {
        caps.add(Capability.CREATE);
        caps.add(Capability.DELETE);
        caps.add(Capability.RENAME);
        caps.add(Capability.GET_TYPE);
        caps.add(Capability.LIST_CHILDREN);
        caps.add(Capability.READ_CONTENT);
        caps.add(Capability.SET_LAST_MODIFIED);
        caps.add(Capability.GET_LAST_MODIFIED);
        caps.add(Capability.URI);
        caps.add(Capability.WRITE_CONTENT);
        caps.add(Capability.APPEND_CONTENT);
    }

    /**
     * Cleans up the connection to the server.
     */
    private void closeConnection(final FTPClient client)
    {
        try
        {
            // Clean up
            if (client.isConnected())
            {
                client.disconnect();
            }
        }
        catch (final IOException e)
        {
            // getLogger().warn("vfs.provider.ftp/close-connection.error", e);
            VfsLog.warn(getLogger(), log, "vfs.provider.ftp/close-connection.error", e);
        }
    }

    /**
     * Creates an FTP client to use.
     */
    public FTPClient getClient() throws FileSystemException
    {
        if (idleClient == null)
        {
            return createConnection();
        }
        else
        {
            final FTPClient client = idleClient;
            idleClient = null;
            return client;
        }
    }

    /**
     * Returns an FTP client after use.
     */
    public void putClient(final FTPClient client)
    {
        if (idleClient == null)
        {
            // Hang on to client for later
            idleClient = client;
        }
        else
        {
            // Close the client
            closeConnection(client);
        }
    }

    /**
     * Creates a file object.
     */
    protected FileObject createFile(final FileName name)
        throws FileSystemException
    {
        return new FtpFileObject(name, this, getRootName());
    }

    /**
     * Creates a new connection to the server.
     */
    private FTPClient createConnection()
        throws FileSystemException
    {
        try
        {
            final FTPClient client = new FTPClient();

            /* as soon as commons-1.2 will be released
            FTPFileEntryParserFactory myFactory = FtpFileSystemConfigBuilder.getInstance().getFTPFileEntryParserFactory(getFileSystemOptions());
            if (myFactory != null)
            {
                client.setParserFactory(myFactory);
            }
            */

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
                String root = getRoot().getName().getPath();
                if (root != null)
                {
                    if (!client.changeWorkingDirectory(root))
                    {
                        throw new FileSystemException("vfs.provider/get-attributes-no-exist.error", "/");
                    }
                }
            }
            catch (final IOException e)
            {
                closeConnection(client);
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
