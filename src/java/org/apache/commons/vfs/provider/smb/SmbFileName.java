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
package org.apache.commons.vfs.provider.smb;

import org.apache.commons.vfs.provider.GenericFileName;
import org.apache.commons.vfs.provider.UriParser;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileName;

/**
 * An SMB URI.  Adds a share name to the generic URI.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.6 $ $Date: 2003/02/15 00:07:45 $
 */
public class SmbFileName
    extends GenericFileName
{
    private static final int DEFAULT_PORT = 139;

    private final String share;

    private SmbFileName( final String scheme,
                         final String hostName,
                         final int port,
                         final String userName,
                         final String password,
                         final String share,
                         final String path )
    {
        super( scheme, hostName, port, DEFAULT_PORT, userName, password, path );
        this.share = share;
    }

    /**
     * Parses an SMB URI.
     */
    public static SmbFileName parseUri( final String uri )
        throws FileSystemException
    {
        final StringBuffer name = new StringBuffer();

        // Extract the scheme and authority parts
        final Authority auth = extractToPath( uri, name );

        // Decode and adjust separators
        UriParser.decode( name, 0, name.length() );
        UriParser.fixSeparators( name );

        // Extract the share
        final String share = UriParser.extractFirstElement( name );
        if ( share == null || share.length() == 0 )
        {
            throw new FileSystemException( "vfs.provider.smb/missing-share-name.error", uri );
        }

        // Normalise the path.  Do this after extracting the share name,
        // to deal with things like smb://hostname/share/..
        UriParser.normalisePath( name );
        final String path = name.toString();

        return new SmbFileName( auth.scheme,
                                auth.hostName,
                                auth.port,
                                auth.userName,
                                auth.password,
                                share,
                                path );
    }

    /**
     * Returns the share name.
     */
    public String getShare()
    {
        return share;
    }

    /**
     * Builds the root URI for this file name.
     */
    protected void appendRootUri( final StringBuffer buffer )
    {
        super.appendRootUri( buffer );
        buffer.append( '/' );
        buffer.append( share );
    }

    /**
     * Factory method for creating name instances.
     */
    protected FileName createName( final String path )
    {
        return new SmbFileName( getScheme(),
                                getHostName(),
                                getPort(),
                                getUserName(),
                                getPassword(),
                                share,
                                path );
    }
}
