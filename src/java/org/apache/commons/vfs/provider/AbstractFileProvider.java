/*
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs.provider;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;

/**
 * A partial {@link FileProvider} implementation.  Takes care of managing the
 * file systems created by the provider.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.9 $ $Date: 2002/07/05 03:47:04 $
 */
public abstract class AbstractFileProvider
    extends AbstractVfsContainer
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
        fileSystems.clear();
        super.close();
    }

    /**
     * Creates a layered file system.  This method throws a 'not supported' exception.
     */
    public FileObject createFileSystem( final String scheme, final FileObject file )
        throws FileSystemException
    {
        // Can't create a layered file system
        throw new FileSystemException( "vfs.provider/not-layered-fs.error", scheme );
    }

    /**
     * Adds a file system to those cached by this provider.  The file system
     * may implement {@link VfsComponent}, in which case it is initialised.
     */
    protected void addFileSystem( final Object key, final FileSystem fs )
        throws FileSystemException
    {
        // Add to the cache
        addComponent( fs );
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
