/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.ftp;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.UriParser;

/**
 * A parser for FTP URI.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.5 $ $Date: 2002/07/05 04:08:18 $
 */
class FtpFileNameParser
    extends UriParser
{
    /**
     * Parses an absolute URI, splitting it into its components.
     */
    public ParsedFtpUri parseFtpUri( final String uriStr )
        throws FileSystemException
    {
        final ParsedFtpUri uri = new ParsedFtpUri();

        // FTP URI are generic URI (as per RFC 2396)
        parseGenericUri( uriStr, uri );

        // Adjust the hostname to lower-case
        final String hostname = uri.getHostName().toLowerCase();
        uri.setHostName( hostname );

        // Drop the port if it is 21
        final String port = uri.getPort();
        if( port != null && port.equals( "21" ) )
        {
            uri.setPort( null );
        }

        // Split up the userinfo into a username and password
        final String userInfo = uri.getUserInfo();
        if( userInfo != null )
        {
            int idx = userInfo.indexOf( ':' );
            if( idx == -1 )
            {
                uri.setUserName( userInfo );
            }
            else
            {
                String userName = userInfo.substring( 0, idx );
                String password = userInfo.substring( idx + 1 );
                uri.setUserName( userName );
                uri.setPassword( password );
            }
        }

        // Now build the root URI
        final StringBuffer rootUri = new StringBuffer();
        appendRootUri( uri, rootUri );
        uri.setRootUri( rootUri.toString() );

        return uri;
    }
}
