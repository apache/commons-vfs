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
package org.apache.commons.vfs.provider;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileName;

/**
 * A {@link FileProvider} that is layered on top of another, such as the
 * contents of a zip or tar file.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.2 $ $Date: 2002/10/23 11:59:40 $
 */
public abstract class AbstractLayeredFileProvider
    extends AbstractFileSystemProvider
    implements FileProvider
{
    /**
     * Locates a file object, by absolute URI.
     */
    public FileObject findFile( final FileObject baseFile,
                                final String uri ) throws FileSystemException
    {
        // Split the URI up into its parts
        final ParsedLayeredUri parsedUri = parseUri( uri );

        // Make the URI canonical

        // Resolve the outer file name
        final String fileName = parsedUri.getOuterFileUri();
        final FileObject file = getContext().resolveFile( baseFile, fileName );

        // Create the file system
        final FileObject rootFile = createFileSystem( parsedUri.getScheme(), file );

        // Resolve the file
        return rootFile.resolveFile( parsedUri.getPath() );
    }

    /**
     * Creates a layered file system.
     */
    public FileObject createFileSystem( final String scheme,
                                        final FileObject file )
        throws FileSystemException
    {
        // Check if cached
        final FileName rootName = file.getName();
        FileSystem fs = findFileSystem( rootName );
        if ( fs == null )
        {
            // Create the file system
            fs = doCreateFileSystem( scheme, file );
            addFileSystem( rootName, fs );
        }
        return fs.getRoot();
    }

    /**
     * Creates a layered file system.  This method is called if the file system
     * is not cached.
     * @param scheme The URI scheme.
     * @param file The file to create the file system on top of.
     * @return The file system.
     */
    protected abstract FileSystem doCreateFileSystem( String scheme,
                                                      FileObject file )
        throws FileSystemException;

    /**
     * Parses an absolute URI.
     * @param uri The URI to parse.
     */
    protected abstract ParsedLayeredUri parseUri( String uri )
        throws FileSystemException;
}
