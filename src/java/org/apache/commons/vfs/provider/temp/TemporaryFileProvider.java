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
package org.apache.commons.vfs.provider.temp;

import java.io.File;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystemProvider;
import org.apache.commons.vfs.provider.DefaultFileName;
import org.apache.commons.vfs.provider.FileProvider;
import org.apache.commons.vfs.provider.Uri;
import org.apache.commons.vfs.provider.UriParser;
import org.apache.commons.vfs.provider.local.LocalFileSystem;

/**
 * A provider for temporary files.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.2 $ $Date: 2002/10/31 10:40:58 $
 */
public class TemporaryFileProvider
    extends AbstractFileSystemProvider
    implements FileProvider
{
    private final UriParser parser = new UriParser();
    private File rootFile;

    public TemporaryFileProvider( final File rootFile )
    {
        this.rootFile = rootFile;
    }

    public TemporaryFileProvider()
    {
    }

    /**
     * Locates a file object, by absolute URI.
     */
    public FileObject findFile( final FileObject baseFile, final String uri )
        throws FileSystemException
    {
        // Parse the name
        final Uri parsedUri = parseUri( uri );

        // Create the temp file system
        FileSystem filesystem = findFileSystem( this );
        if ( filesystem == null )
        {
            if ( rootFile == null )
            {
                rootFile = getContext().getTemporaryFileStore().allocateFile( "tempfs" );
            }
            final FileName rootName = new DefaultFileName( parser, parsedUri.getContainerUri(), "/" );
            filesystem = new LocalFileSystem( rootName, rootFile.getAbsolutePath() );
            addFileSystem( this, filesystem );
        }

        // Find the file
        return filesystem.resolveFile( parsedUri.getPath() );
    }

    /** Parses an absolute URI into its parts. */
    private Uri parseUri( final String uri ) throws FileSystemException
    {
        final StringBuffer buffer = new StringBuffer( uri );
        final Uri parsedUri = new Uri();
        final String scheme = parser.extractScheme( uri, buffer );
        parsedUri.setScheme( scheme );
        parser.decode( buffer, 0, buffer.length() );
        parser.normalisePath( buffer );
        parsedUri.setPath( buffer.toString() );
        parsedUri.setContainerUri( scheme + ":" );
        return parsedUri;
    }
}
