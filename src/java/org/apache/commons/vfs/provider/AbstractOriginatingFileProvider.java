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
package org.apache.commons.vfs.provider;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;

/**
 * A {@link FileProvider} that handles physical files, such as the files in a
 * local fs, or on an FTP server.  An originating file system cannot be
 * layered on top of another file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.13 $ $Date: 2003/06/28 10:58:51 $
 */
public abstract class AbstractOriginatingFileProvider
    extends AbstractFileProvider
{
    /**
     * Locates a file object, by absolute URI.
     *
     * @param uri
     *          The absolute URI of the file to find.
     */
    public FileObject findFile( final FileObject baseFile,
                                final String uri ) throws FileSystemException
    {
        // Parse the URI
        final FileName name;
        try
        {
            name = parseUri( uri );
        }
        catch ( FileSystemException exc )
        {
            throw new FileSystemException( "vfs.provider/invalid-absolute-uri.error", uri, exc );
        }

        // Locate the file
        return findFile( name );
    }

    /**
     * Locates a file from its parsed URI.
     */
    private FileObject findFile( final FileName name )
        throws FileSystemException
    {
        // Check in the cache for the file system
        final FileName rootName = name.resolveName( FileName.ROOT_PATH );
        FileSystem fs = findFileSystem( rootName );
        if ( fs == null )
        {
            // Need to create the file system, and cache it
            fs = doCreateFileSystem( rootName );
            addFileSystem( rootName, fs );
        }

        // Locate the file
        return fs.resolveFile( name.getPath() );
    }

    /**
     * Parses an absolute URI.
     *
     * @return The name of the file.  This name is used to locate the file
     *         system in the cache, using the root URI.  This name is also
     *         passed to {@link #doCreateFileSystem} to create the file system.
     */
    protected abstract FileName parseUri( final String uri )
        throws FileSystemException;

    /**
     * Creates a {@link FileSystem}.  If the returned FileSystem implements
     * {@link VfsComponent}, it will be initialised.
     *
     * @param rootName The name of the root file of the file system to create.
     */
    protected abstract FileSystem doCreateFileSystem( final FileName rootName )
        throws FileSystemException;
}
