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

import java.io.File;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.UriParser;
import org.apache.commons.vfs.provider.Uri;

/**
 * A name parser.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.5 $ $Date: 2002/03/09 10:31:30 $
 */
abstract class LocalFileNameParser
    extends UriParser
{
    public LocalFileNameParser()
    {
        super( new char[]{File.separatorChar, '/', '\\'} );
    }

    /**
     * Determines if a name is an absolute file name.
     */
    public boolean isAbsoluteName( final String name )
    {
        // TODO - this is yucky
        StringBuffer b = new StringBuffer( name );
        try
        {
            fixSeparators( b );
            extractRootPrefix( name, b );
            return true;
        }
        catch ( FileSystemException e )
        {
            return false;
        }
    }

    /**
     * Parses an absolute URI, splitting it into its components.
     *
     * @param uriStr The URI.
     */
    public Uri parseFileUri( final String uriStr )
        throws FileSystemException
    {
        final StringBuffer name = new StringBuffer();
        final LocalFileUri uri = new LocalFileUri();

        // Extract the scheme
        final String scheme = extractScheme( uriStr, name );
        uri.setScheme( scheme );

        // Remove encoding, and adjust the separators
        decode( name, 0, name.length() );
        fixSeparators( name );

        // Extract the root prefix
        final String rootFile = extractRootPrefix( uriStr, name );
        uri.setRootFile( rootFile );

        // Normalise the path
        normalisePath( name );
        uri.setPath( name.toString() );

        // Build the root URI
        final StringBuffer rootUri = new StringBuffer();
        rootUri.append( scheme );
        rootUri.append( "://" );
        rootUri.append( rootFile );
        uri.setContainerUri( rootUri.toString() );

        return uri;
    }

    /**
     * Pops the root prefix off a URI, which has had the scheme removed.
     */
    protected abstract String extractRootPrefix( final String uri,
                                                 final StringBuffer name )
        throws FileSystemException;

}
