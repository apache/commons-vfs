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
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs.provider.LocalFileProvider;
import org.apache.commons.vfs.util.Os;

/**
 * A file system provider, which uses direct file access.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.3 $ $Date: 2002/07/05 04:08:18 $
 */
public final class DefaultLocalFileSystemProvider
    extends AbstractOriginatingFileProvider
    implements LocalFileProvider
{
    private final LocalFileNameParser parser;

    public DefaultLocalFileSystemProvider()
    {
        if ( Os.isFamily( Os.OS_FAMILY_WINDOWS ) )
        {
            parser = new WindowsFileNameParser();
        }
        else
        {
            parser = new GenericFileNameParser();
        }
    }

    /**
     * Determines if a name is an absolute file name.
     */
    public boolean isAbsoluteLocalName( final String name )
    {
        return parser.isAbsoluteName( name );
    }

    /**
     * Finds a local file, from its local name.
     */
    public FileObject findLocalFile( final String name )
        throws FileSystemException
    {
        // TODO - tidy this up, no need to turn the name into an absolute URI,
        // and then straight back again
        return findFile( null, "file:" + name );
    }

    /**
     * Finds a local file.
     */
    public FileObject findLocalFile( final File file )
        throws FileSystemException
    {
        // TODO - tidy this up, should build file object straight from the file
        return findFile( null, "file:" + file.getAbsolutePath() );
    }

    /**
     * Parses a URI.
     */
    protected FileName parseUri( final String uri )
        throws FileSystemException
    {
        return LocalFileName.parseUri( uri, parser );
    }

    /**
     * Creates the filesystem.
     */
    protected FileSystem doCreateFileSystem( final FileName name )
        throws FileSystemException
    {
        // Build the name of the root file.
        final LocalFileName fileUri = (LocalFileName)name;
        final String rootFile = fileUri.getRootFile();

        // Create the file system
        final FileName rootName = fileUri.resolveName( FileName.ROOT_PATH );
        return new LocalFileSystem( rootName, rootFile );
    }
}
