/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.ftp;

import java.io.IOException;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * An FTP file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.6 $ $Date: 2002/07/05 04:08:19 $
 */
final class FtpFileSystem
    extends AbstractFileSystem
{
    private final FTPClient client;

    public FtpFileSystem( final FileName rootName,
                          final String hostname,
                          final String username,
                          final String password )
        throws FileSystemException
    {
        super( rootName, null );
        try
        {
            client = new FTPClient();
            client.connect( hostname );

            int reply = client.getReplyCode();
            if ( !FTPReply.isPositiveCompletion( reply ) )
            {
                throw new FileSystemException( "vfs.provider.ftp/connect-rejected.error", hostname );
            }

            // Login
            if ( !client.login( username, password ) )
            {
                throw new FileSystemException( "vfs.provider.ftp/login.error", new Object[]{hostname, username}, null );
            }

            // Set binary mode
            if ( !client.setFileType( FTP.BINARY_FILE_TYPE ) )
            {
                throw new FileSystemException( "vfs.provider.ftp/set-binary.error", hostname );
            }
        }
        catch ( final Exception exc )
        {
            closeConnection();
            throw new FileSystemException( "vfs.provider.ftp/connect.error", new Object[]{hostname}, exc );
        }
    }

    public void close()
    {
        // Clean up the connection
        closeConnection();

        super.close();
    }

    /**
     * Cleans up the connection to the server.
     */
    private void closeConnection()
    {
        try
        {
            // Clean up
            if ( client.isConnected() )
            {
                client.disconnect();
            }
        }
        catch ( final IOException e )
        {
            getLogger().warn( "vfs.provider.ftp/close-connection.error", e );
        }
    }

    /**
     * Returns an FTP client to use.
     */
    public FTPClient getClient()
    {
        // TODO - connect on demand, and garbage collect connections
        return client;
    }

    /**
     * Creates a file object.
     */
    protected FileObject createFile( FileName name )
        throws FileSystemException
    {
        return new FtpFileObject( name, this );
    }
}
