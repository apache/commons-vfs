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
package org.apache.commons.vfs.provider.zip;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.UriParser;
import org.apache.commons.vfs.provider.ParsedLayeredUri;

/**
 * A parser for Zip file names.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.4 $ $Date: 2002/07/05 04:08:19 $
 */
public class ZipFileNameParser
    extends UriParser
{
    private static final char[] ZIP_URL_RESERVED_CHARS = {'!'};

    /**
     * Parses an absolute URI, splitting it into its components.
     *
     * @param uriStr
     *          The URI.
     */
    public ParsedLayeredUri parseZipUri( final String uriStr )
        throws FileSystemException
    {
        final StringBuffer name = new StringBuffer();
        final ParsedLayeredUri uri = new ParsedLayeredUri();

        // Extract the scheme
        final String scheme = extractScheme( uriStr, name );
        uri.setScheme( scheme );

        // Extract the Zip file name
        final String zipName = extractZipName( name );
        uri.setOuterFileUri( zipName );

        // Decode and normalise the file name
        decode( name, 0, name.length() );
        normalisePath( name );
        uri.setPath( name.toString() );

        return uri;
    }

    /**
     * Assembles a root URI from the components of a parsed URI.
     */
    public String buildRootUri( final String scheme, final String outerFileUri )
    {
        final StringBuffer rootUri = new StringBuffer();
        rootUri.append( scheme );
        rootUri.append( ":" );
        appendEncoded( rootUri, outerFileUri, ZIP_URL_RESERVED_CHARS );
        rootUri.append( "!" );
        return rootUri.toString();
    }

    /**
     * Pops the root prefix off a URI, which has had the scheme removed.
     */
    private String extractZipName( final StringBuffer uri )
        throws FileSystemException
    {
        // Looking for <name>!<abspath>
        int maxlen = uri.length();
        int pos = 0;
        for ( ; pos < maxlen && uri.charAt( pos ) != '!'; pos++ )
        {
        }

        // Extract the name
        String prefix = uri.substring( 0, pos );
        if ( pos < maxlen )
        {
            uri.delete( 0, pos + 1 );
        }
        else
        {
            uri.setLength( 0 );
        }

        // Decode the name
        return decode( prefix );
    }
}
