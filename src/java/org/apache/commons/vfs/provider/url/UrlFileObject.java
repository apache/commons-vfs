/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.url;

import java.io.InputStream;
import java.net.URL;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileObject;

/**
 * A {@link FileObject} implementation backed by a {@link URL}.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.2 $ $Date: 2002/10/22 11:51:31 $
 */
class UrlFileObject
    extends AbstractFileObject
    implements FileObject
{
    private URL url;

    public UrlFileObject( final UrlFileSystem fs,
                          final FileName fileName )
    {
        super( fileName, fs );
    }

    /**
     * Attaches this file object to its file resource.  This method is called
     * before any of the doBlah() or onBlah() methods.  Sub-classes can use
     * this method to perform lazy initialisation.
     */
    protected void doAttach() throws Exception
    {
        if ( url == null )
        {
            url = new URL( getName().getURI() );
        }
    }

    /**
     * Determines the type of the file.
     */
    protected FileType doGetType() throws Exception
    {
        // TODO - implement this
        return FileType.FILE;
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    protected long doGetContentSize() throws Exception
    {
        return url.openConnection().getContentLength();
    }

    /**
     * Lists the children of the file.
     */
    protected String[] doListChildren() throws Exception
    {
        throw new FileSystemException( "Not implemented." );
    }

    /**
     * Creates an input stream to read the file content from.
     */
    protected InputStream doGetInputStream() throws Exception
    {
        return url.openStream();
    }
}
