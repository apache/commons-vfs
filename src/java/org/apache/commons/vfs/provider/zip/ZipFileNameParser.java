/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
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
