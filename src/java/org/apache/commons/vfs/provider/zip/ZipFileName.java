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
package org.apache.commons.vfs.provider.zip;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.LayeredFileName;
import org.apache.commons.vfs.provider.UriParser;

/**
 * A parser for Zip file names.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.6 $ $Date: 2003/10/13 08:44:28 $
 */
public class ZipFileName
    extends LayeredFileName
{
    private static final char[] ZIP_URL_RESERVED_CHARS = {'!'};

    public ZipFileName( final String scheme,
                        final String zipFileUri,
                        final String path )
    {
        super( scheme, zipFileUri, path );
    }

    /**
     * Builds the root URI for this file name.
     */
    protected void appendRootUri( final StringBuffer buffer )
    {
        buffer.append( getScheme() );
        buffer.append( ":" );
        UriParser.appendEncoded( buffer, getOuterUri(), ZIP_URL_RESERVED_CHARS );
        buffer.append( "!" );
    }

    /**
     * Factory method for creating name instances.
     */
    protected FileName createName( final String path )
    {
        return new ZipFileName( getScheme(), getOuterUri(), path );
    }

    /**
     * Parses a Zip URI.
     */
    public static ZipFileName parseUri( final String uri )
        throws FileSystemException
    {
        final StringBuffer name = new StringBuffer();

        // Extract the scheme
        final String scheme = UriParser.extractScheme( uri, name );

        // Extract the Zip file URI
        final String zipUri = extractZipName( name );

        // Decode and normalise the path
        UriParser.decode( name, 0, name.length() );
        UriParser.normalisePath( name );
        final String path = name.toString();

        return new ZipFileName( scheme, zipUri, path );
    }

    /**
     * Pops the root prefix off a URI, which has had the scheme removed.
     */
    private static String extractZipName( final StringBuffer uri )
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
        return UriParser.decode( prefix );
    }
}
