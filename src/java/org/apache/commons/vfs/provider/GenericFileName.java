/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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

/**
 * A file name that represents a 'generic' URI, as per RFC 2396.  Consists of
 * a scheme, userinfo (typically username and password), hostname, port, and
 * path.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.2 $ $Date: 2003/01/24 00:20:03 $
 */
public class GenericFileName
    extends DefaultFileName
{
    private final String userInfo;
    private final String hostName;
    private final String port;

    protected GenericFileName( final String scheme,
                               final String rootUri,
                               final String hostName,
                               final String port,
                               final String userInfo,
                               final String path )
    {
        super( scheme, rootUri, path );
        this.hostName = hostName;
        this.port = port;
        this.userInfo = userInfo;
    }

    /** Returns the user info part of the URI. */
    public String getUserInfo()
    {
        return userInfo;
    }

    /** Returns the host name part of the URI. */
    public String getHostName()
    {
        return hostName;
    }

    /** Returns the port part of the URI. */
    public String getPort()
    {
        return port;
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

        // Extract userinfo
        auth.userInfo = extractUserInfo( name );

        // Extract hostname, and normalise (lowercase)
        final String hostName = extractHostName( name );
        if ( hostName == null )
        {
            throw new FileSystemException( "vfs.provider/missing-hostname.error", uri );
        }
        auth.hostName = hostName.toLowerCase();

        // Extract port
        final String port = extractPort( name );
        if ( port != null && port.length() == 0 )
        {
            throw new FileSystemException( "vfs.provider/missing-port.error", uri );
        }
        auth.port = port;

        // Expecting '/' or empty name
        if ( name.length() > 0 && name.charAt( 0 ) != '/' )
        {
            throw new FileSystemException( "vfs.provider/missing-hostname-path-sep.error", uri );
        }

        return auth;
    }

    /**
     * Extracts the user info from a URI.  The <scheme>:// part has been removed
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
     * Extracts the hostname from a URI.  The <scheme>://<userinfo>@ part has
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
     * Extracts the port from a URI.  The <scheme>://<userinfo>@<hostname>
     * part has been removed.
     */
    protected static String extractPort( final StringBuffer name )
    {
        if ( name.length() < 1 || name.charAt( 0 ) != ':' )
        {
            return null;
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
        return port;
    }

    /**
     * Assembles a generic URI, appending to the supplied StringBuffer.
     */
    protected static void appendRootUri( final Authority auth,
                                         final StringBuffer rootUri )
    {
        rootUri.append( auth.scheme );
        rootUri.append( "://" );
        final String userInfo = auth.userInfo;
        if ( userInfo != null && userInfo.length() != 0 )
        {
            rootUri.append( userInfo );
            rootUri.append( "@" );
        }
        rootUri.append( auth.hostName );
        final String port = auth.port;
        if ( port != null && port.length() > 0 )
        {
            rootUri.append( ":" );
            rootUri.append( port );
        }
    }

    /** Parsed authority info (scheme, hostname, userinfo, port) */
    protected static class Authority
    {
        public String scheme;
        public String hostName;
        public String userInfo;
        public String port;
    }
}
