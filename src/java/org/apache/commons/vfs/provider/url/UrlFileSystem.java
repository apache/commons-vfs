/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.url;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.FileSystem;

/**
 * A File system backed by Java's URL API.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.6 $ $Date: 2002/08/22 02:42:46 $
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
        try
        {
            final URL url = new URL( name.getURI() );
            return new UrlFileObject( this, name, url );
        }
        catch ( MalformedURLException e )
        {
            throw new FileSystemException( e );
        }
    }
}
