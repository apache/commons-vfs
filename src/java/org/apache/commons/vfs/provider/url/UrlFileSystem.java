/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.url;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * A File system backed by Java's URL API.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.7 $ $Date: 2002/10/22 11:51:31 $
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
    protected FileObject createFile( final FileName name ) throws FileSystemException
    {
        return new UrlFileObject( this, name );
    }
}
