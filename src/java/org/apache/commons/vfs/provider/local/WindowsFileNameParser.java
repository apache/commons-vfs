/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.local;

import org.apache.commons.vfs.FileSystemException;

/**
 * A parser for Windows file names.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.3 $ $Date: 2002/07/05 04:08:18 $
 */
final class WindowsFileNameParser
    extends LocalFileNameParser
{
    /**
     * Pops the root prefix off a URI, which has had the scheme removed.
     */
    protected String extractRootPrefix( final String uri,
                                        final StringBuffer name )
        throws FileSystemException
    {
        return extractWindowsRootPrefix( uri, name );
    }

    /**
     * Extracts a Windows root prefix from a name.
     */
    private String extractWindowsRootPrefix( final String uri,
                                             final StringBuffer name )
        throws FileSystemException
    {
        // Looking for:
        // ('/'){0, 3} <letter> ':' '/'
        // ['/'] '//' <name> '/' <name> ( '/' | <end> )

        // Skip over first 3 leading '/' chars
        int startPos = 0;
        int maxlen = Math.min( 3, name.length() );
        for ( ; startPos < maxlen && name.charAt( startPos ) == '/'; startPos++ )
        {
        }
        if ( startPos == maxlen )
        {
            // Too many '/'
            throw new FileSystemException( "vfs.provider.local/not-absolute-file-name.error", uri );
        }
        name.delete( 0, startPos );

        // Look for drive name
        String driveName = extractDrivePrefix( name );
        if ( driveName != null )
        {
            return driveName;
        }

        // Look for UNC name
        if ( startPos < 2 )
        {
            throw new FileSystemException( "vfs.provider.local/not-absolute-file-name.error", uri );
        }

        return "//" + extractUNCPrefix( uri, name );
    }

    /**
     * Extracts a drive prefix from a path.  Leading '/' chars have been removed.
     */
    private String extractDrivePrefix( final StringBuffer name )
    {
        // Looking for <letter> ':' '/'
        if ( name.length() < 3 )
        {
            // Too short
            return null;
        }
        char ch = name.charAt( 0 );
        if ( ch == '/' || ch == ':' )
        {
            // Missing drive letter
            return null;
        }
        if ( name.charAt( 1 ) != ':' )
        {
            // Missing ':'
            return null;
        }
        if ( name.charAt( 2 ) != '/' )
        {
            // Missing separator
            return null;
        }

        String prefix = name.substring( 0, 2 );
        name.delete( 0, 2 );
        return prefix;
    }

    /**
     * Extracts a UNC name from a path.  Leading '/' chars have been removed.
     */
    private String extractUNCPrefix( final String uri,
                                     final StringBuffer name )
        throws FileSystemException
    {
        // Looking for <name> '/' <name> ( '/' | <end> )

        // Look for first separator
        int maxpos = name.length();
        int pos = 0;
        for ( ; pos < maxpos && name.charAt( pos ) != '/'; pos++ )
        {
        }
        pos++;
        if ( pos >= maxpos )
        {
            throw new FileSystemException( "vfs.provider.local/missing-share-name.error", uri );
        }

        // Now have <name> '/'
        int startShareName = pos;
        for ( ; pos < maxpos && name.charAt( pos ) != '/'; pos++ )
        {
        }
        if ( pos == startShareName )
        {
            throw new FileSystemException( "vfs.provider.local/missing-share-name.error", uri );
        }

        // Now have <name> '/' <name> ( '/' | <end> )
        String prefix = name.substring( 0, pos );
        name.delete( 0, pos );
        return prefix;
    }
}
