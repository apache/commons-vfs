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
package org.apache.commons.vfs.provider.ftp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 * @version $Revision$ $Date$
 */
public class FtpFileSystem extends AbstractFileSystem
{
    private static final Log LOG = LogFactory.getLog(FtpFileSystem.class);

//    private final String hostname;
//    private final int port;
//    private final String username;
//    private final String password;

    // An idle client
    private FtpClient idleClient;

    public FtpFileSystem(final GenericFileName rootName, final FtpClient ftpClient, final FileSystemOptions fileSystemOptions)
    {
        super(rootName, null, fileSystemOptions);
        // hostname = rootName.getHostName();
        // port = rootName.getPort();

        idleClient = ftpClient;
    }

    @Override
    protected void doCloseCommunicationLink()
    {
        // Clean up the connection
        if (idleClient != null)
        {
            closeConnection(idleClient);
            idleClient = null;
        }
    }

    /**
     * Adds the capabilities of this file system.
     */
    @Override
    protected void addCapabilities(final Collection<Capability> caps)
    {
        caps.addAll(FtpFileProvider.capabilities);
    }

    /**
     * Cleans up the connection to the server.
     */
    private void closeConnection(final FtpClient client)
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
            VfsLog.warn(getLogger(), LOG, "vfs.provider.ftp/close-connection.error", e);
        }
    }

    /**
     * Creates an FTP client to use.
     * @return An FTPCleint.
     * @throws FileSystemException if an error occurs.
     */
    public FtpClient getClient() throws FileSystemException
    {
        synchronized (this)
            {
                if (idleClient == null || !idleClient.isConnected())
                {
                    idleClient = null;

                    return new FTPClientWrapper((GenericFileName) getRoot().getName(), getFileSystemOptions());
                }
                else
                {
                    final FtpClient client = idleClient;
                    idleClient = null;
                    return client;
                }
            }
    }

    /**
     * Returns an FTP client after use.
     * @param client The FTPClient.
     */
    public void putClient(final FtpClient client)
    {
        synchronized (this)
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
    }

    /**
     * Creates a file object.
     */
    @Override
    protected FileObject createFile(final FileName name)
        throws FileSystemException
    {
        return new FtpFileObject(name, this, getRootName());
    }
}
