/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002, 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.commons.vfs.provider.ftp;

import java.io.IOException;
import java.util.Collection;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.GenericFileName;

/**
 * An FTP file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.6 $ $Date: 2002/07/05 04:08:19 $
 */
final class FtpFileSystem
    extends AbstractFileSystem
{
    private final String hostname;
    private final int port;
    private final String username;
    private final String password;

    // An idle client
    private FTPClient idleClient;

    public FtpFileSystem( final GenericFileName rootName )
    {
        super( rootName, null );
        hostname = rootName.getHostName();
        port = rootName.getPort();

        // Determine the username and password to use
        if ( rootName.getUserName() == null )
        {
            username = "anonymous";
        }
        else
        {
            username = rootName.getUserName();
        }
        if ( rootName.getPassword() == null )
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
        if ( idleClient != null )
        {
            closeConnection( idleClient );
        }

        super.close();
    }

    /**
     * Adds the capabilities of this file system.
     */
    protected void addCapabilities( final Collection caps )
    {
        caps.add( Capability.CREATE );
        caps.add( Capability.DELETE );
        caps.add( Capability.GET_TYPE );
        caps.add( Capability.LIST_CHILDREN );
        caps.add( Capability.READ_CONTENT );
        caps.add( Capability.SET_LAST_MODIFIED );
        caps.add( Capability.GET_LAST_MODIFIED );
        caps.add( Capability.URI );
        caps.add( Capability.WRITE_CONTENT );
    }

    /**
     * Cleans up the connection to the server.
     */
    private void closeConnection( final FTPClient client )
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
     * Creates an FTP client to use.
     */
    public FTPClient getClient() throws FileSystemException
    {
        if ( idleClient == null )
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
    public void putClient( final FTPClient client )
    {
        if ( idleClient == null )
        {
            // Hang on to client for later
            idleClient = client;
        }
        else
        {
            // Close the client
            closeConnection( client );
        }
    }

    /**
     * Creates a file object.
     */
    protected FileObject createFile( final FileName name )
        throws FileSystemException
    {
        return new FtpFileObject( name, this, getRootName() );
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

            try
            {
                client.connect( hostname, port );

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
            catch ( final IOException e )
            {
                closeConnection( client );
                throw e;
            }

            return client;
        }
        catch ( final Exception exc )
        {
            throw new FileSystemException( "vfs.provider.ftp/connect.error", new Object[]{hostname}, exc );
        }
    }

}
