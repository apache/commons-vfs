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
package org.apache.commons.vfs.provider.ftp;

import org.apache.commons.vfs.provider.GenericFileName;
import org.apache.commons.vfs.provider.UriParser;
import org.apache.commons.vfs.FileSystemException;

/**
 * An FTP URI.  Splits userinfo (see {@link #getUserInfo}) into username and
 * password.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.2 $ $Date: 2003/01/24 00:20:04 $
 */
class FtpFileName
    extends GenericFileName
{
    private final String userName;
    private final String password;

    private FtpFileName( final String scheme,
                         final String rootUri,
                         final String hostName,
                         final String port,
                         final String userInfo,
                         final String userName,
                         final String password,
                         final String path )
    {
        super( scheme, rootUri, hostName, port, userInfo, path );
        this.password = password;
        this.userName = userName;
    }

    /**
     * Parses an FTP URI.
     */
    public static FtpFileName parseUri( final String uri )
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

        // Drop the port if it is 21
        final String port = auth.port;
        if ( port != null && port.equals( "21" ) )
        {
            auth.port = null;
        }

        // Split up the userinfo into a username and password
        // TODO - push this into GenericFileName
        final String userInfo = auth.userInfo;
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

        // Now build the root URI
        final StringBuffer buffer = new StringBuffer();
        appendRootUri( auth, buffer );
        final String rootUri = buffer.toString();

        return new FtpFileName( auth.scheme,
                                rootUri,
                                auth.hostName,
                                auth.port,
                                auth.userInfo,
                                userName,
                                password,
                                path );
    }

    /**
     * Returns the user name.
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * Returns the password.
     */
    public String getPassword()
    {
        return password;
    }
}
