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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileObject;

/**
 * A partial {@link FileProvider} implementation.  Takes care of managing the
 * file systems created by the provider.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.9 $ $Date: 2002/07/05 03:47:04 $
 */
public abstract class AbstractFileSystemProvider
    extends AbstractVfsComponent
    implements FileProvider
{
    /**
     * The cached file systems.  This is a mapping from root URI to
     * FileSystem object.
     */
    private final Map fileSystems = new HashMap();

    /**
     * Closes the file systems created by this provider.
     */
    public void close()
    {
        // Close all the filesystems created by this provider
        for ( Iterator iterator = fileSystems.values().iterator(); iterator.hasNext(); )
        {
            Object fileSystem = iterator.next();
            if ( fileSystem instanceof VfsComponent )
            {
                final VfsComponent vfsComponent = (VfsComponent)fileSystem;
                vfsComponent.close();
            }
        }
        fileSystems.clear();
    }

    /**
     * Creates a layered file system.
     */
    public FileObject createFileSystem( final String scheme, final FileObject file )
        throws FileSystemException
    {
        // Can't create a layered file system
        throw new FileSystemException( "vfs.provider/not-layered-fs.error", scheme );
    }
    
    /**
     * Adds a file system to those cached by this provider.
     */
    protected void addFileSystem( final Object key, final FileSystem fs )
        throws FileSystemException
    {
        // Initialise
        if ( fs instanceof VfsComponent )
        {
            VfsComponent vfsComponent = (VfsComponent)fs;
            vfsComponent.setLogger( getLogger() );
            vfsComponent.setContext( getContext() );
            vfsComponent.init();
        }

        // Add to the cache
        fileSystems.put( key, fs );
    }

    /**
     * Locates a cached file system
     * @return The provider, or null if it is not cached.
     */
    protected FileSystem findFileSystem( final Object key )
    {
        return (FileSystem)fileSystems.get( key );
    }
}
