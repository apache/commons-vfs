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
package org.apache.commons.vfs.provider.url;

import java.util.Collection;
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * A File system backed by Java's URL API.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.15 $ $Date: 2004/02/28 03:35:52 $
 */
class UrlFileSystem
    extends AbstractFileSystem
    implements FileSystem
{
    public UrlFileSystem( final FileName rootName )
    {
        super( rootName, null );
    }

    /**
     * Creates a file object.
     */
    protected FileObject createFile( final FileName name )
    {
        return new UrlFileObject( this, name );
    }

    /**
     * Returns the capabilities of this file system.
     */
    protected void addCapabilities( final Collection caps )
    {
        caps.add( Capability.READ_CONTENT );
        caps.add( Capability.URI );
        caps.add( Capability.GET_LAST_MODIFIED );
    }
}
