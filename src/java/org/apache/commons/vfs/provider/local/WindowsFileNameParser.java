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
