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
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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
package org.apache.commons.vfs.provider;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileName;

/**
 * A file name that represents a 'generic' URI, as per RFC 2396.  Consists of
 * a scheme, userinfo (typically username and password), hostname, port, and
 * path.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.8 $ $Date: 2003/03/17 09:07:32 $
 */
public class GenericFileName
    extends AbstractFileName
{
    private final String userName;
    private final String hostName;
    private final int defaultPort;
    private final String password;
    private final int port;

    protected GenericFileName( final String scheme,
                               final String hostName,
                               final int port,
                               final int defaultPort,
                               final String userName,
                               final String password,
                               final String path )
    {
        super( scheme, path );
        this.hostName = hostName;
        this.defaultPort = defaultPort;
        this.password = password;
        this.userName = userName;
        if ( port > 0 )
        {
            this.port = port;
        }
        else
        {
            this.port = getDefaultPort();
        }
    }

    /** Returns the user name part of this name. */
    public String getUserName()
    {
        return userName;
    }

    /** Returns the password part of this name. */
    public String getPassword()
    {
        return password;
    }

    /** Returns the host name part of this name. */
    public String getHostName()
    {
        return hostName;
    }

    /** Returns the port part of this name. */
    public int getPort()
    {
        return port;
    }

    /** Returns the default port for this file name. */
    public int getDefaultPort()
    {
        return defaultPort;
    }

    /**
     * Factory method for creating name instances.
     */
    protected FileName createName( final String absPath )
    {
        return new GenericFileName( getScheme(),
                                    hostName,
                                    port,
                                    defaultPort,
                                    userName,
                                    password,
                                    absPath );
    }

    /**
     * Parses a generic URI.
     */
    public static GenericFileName parseUri( final String uri,
                                            final int defaultPort )
        throws FileSystemException
    {
        // FTP URI are generic URI (as per RFC 2396)
        final StringBuffer name = new StringBuffer();

        // Extract the scheme and authority parts
        final Authority auth = extractToPath( uri, name );

        // Decode and normalise the file name
        UriParser.decode( name, 0, name.length() );
        UriParser.normalisePath( name );
        final String path = name.toString();

        return new GenericFileName( auth.scheme,
                                    auth.hostName,
                                    auth.port,
                                    defaultPort, 
                                    auth.userName,
                                    auth.password,
                                    path );
    }

    /**
     * Extracts the scheme, userinfo, hostname and port components of a
     * generic URI.
     *
     * @param uri
     *          The absolute URI to parse.
     *
     * @param name
     *          Used to return the remainder of the URI.
     */
    protected static Authority extractToPath( final String uri,
                                              final StringBuffer name )
        throws FileSystemException
    {
        final Authority auth = new Authority();

        // Extract the scheme
        auth.scheme = UriParser.extractScheme( uri, name );

        // Expecting "//"
        if ( name.length() < 2 || name.charAt( 0 ) != '/' || name.charAt( 1 ) != '/' )
        {
            throw new FileSystemException( "vfs.provider/missing-double-slashes.error", uri );
        }
        name.delete( 0, 2 );

        // Extract userinfo, and split into username and password
        // TODO - need to decode username and password
        final String userInfo = extractUserInfo( name );
        final String userName;
        final String password;
        if ( userInfo != null )
        {
            int idx = userInfo.indexOf( ':' );
            if ( idx == -1 )
            {
                userName = userInfo;
                password = null;
            }
            else
            {
                userName = userInfo.substring( 0, idx );
                password = userInfo.substring( idx + 1 );
            }
        }
        else
        {
            userName = null;
            password = null;
        }
        auth.userName = userName;
        auth.password = password;

        // Extract hostname, and normalise (lowercase)
        final String hostName = extractHostName( name );
        if ( hostName == null )
        {
            throw new FileSystemException( "vfs.provider/missing-hostname.error", uri );
        }
        auth.hostName = hostName.toLowerCase();

        // Extract port
        auth.port = extractPort( name, uri );

        // Expecting '/' or empty name
        if ( name.length() > 0 && name.charAt( 0 ) != '/' )
        {
            throw new FileSystemException( "vfs.provider/missing-hostname-path-sep.error", uri );
        }

        return auth;
    }

    /**
     * Extracts the user info from a URI.  The scheme:// part has been removed
     * already.
     */
    protected static String extractUserInfo( final StringBuffer name )
    {
        final int maxlen = name.length();
        for ( int pos = 0; pos < maxlen; pos++ )
        {
            final char ch = name.charAt( pos );
            if ( ch == '@' )
            {
                // Found the end of the user info
                String userInfo = name.substring( 0, pos );
                name.delete( 0, pos + 1 );
                return userInfo;
            }
            if ( ch == '/' || ch == '?' )
            {
                // Not allowed in user info
                break;
            }
        }

        // Not found
        return null;
    }

    /**
     * Extracts the hostname from a URI.  The scheme://userinfo@ part has
     * been removed.
     */
    protected static String extractHostName( final StringBuffer name )
    {
        final int maxlen = name.length();
        int pos = 0;
        for ( ; pos < maxlen; pos++ )
        {
            final char ch = name.charAt( pos );
            if ( ch == '/' || ch == ';' || ch == '?' || ch == ':'
                || ch == '@' || ch == '&' || ch == '=' || ch == '+'
                || ch == '$' || ch == ',' )
            {
                break;
            }
        }
        if ( pos == 0 )
        {
            return null;
        }

        final String hostname = name.substring( 0, pos );
        name.delete( 0, pos );
        return hostname;
    }

    /**
     * Extracts the port from a URI.  The scheme://userinfo@hostname
     * part has been removed.
     * @return The port, or -1 if the URI does not contain a port.
     */
    private static int extractPort( final StringBuffer name, final String uri ) throws FileSystemException
    {
        if ( name.length() < 1 || name.charAt( 0 ) != ':' )
        {
            return -1;
        }

        final int maxlen = name.length();
        int pos = 1;
        for ( ; pos < maxlen; pos++ )
        {
            final char ch = name.charAt( pos );
            if ( ch < '0' || ch > '9' )
            {
                break;
            }
        }

        final String port = name.substring( 1, pos );
        name.delete( 0, pos );
        if ( port.length() == 0 )
        {
            throw new FileSystemException( "vfs.provider/missing-port.error", uri );
        }

        return Integer.parseInt( port );
    }

    /**
     * Builds the root URI for this file name.
     */
    protected void appendRootUri( final StringBuffer buffer )
    {
        buffer.append( getScheme() );
        buffer.append( "://" );
        if ( userName != null && userName.length() != 0 )
        {
            // TODO - need to encode username and password
            buffer.append( userName );
            if ( password != null && password.length() != 0 )
            {
                buffer.append( ':' );
                buffer.append( password );
            }
            buffer.append( '@' );
        }
        buffer.append( hostName );
        if ( port != getDefaultPort() )
        {
            buffer.append( ':' );
            buffer.append( port );
        }
    }

    /** Parsed authority info (scheme, hostname, userinfo, port) */
    protected static class Authority
    {
        public String scheme;
        public String hostName;
        public String userName;
        public String password;
        public int port;
    }
}
