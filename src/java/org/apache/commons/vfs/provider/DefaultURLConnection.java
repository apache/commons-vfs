/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileSystemException;

/**
 * A default URL connection that will work for most file systems.
 *
 * @author <a href="mailto:brian@mmmanager.org">Brian Olsen</a>
 * @version $Revision: 1.3 $ $Date: 2002/07/05 04:08:17 $
 */
public final class DefaultURLConnection
    extends URLConnection
{
    private final FileContent content;

    public DefaultURLConnection( final URL url,
                                 final FileContent content )
    {
        super( url );
        this.content = content;
    }

    public void connect()
    {
        connected = true;
    }

    public InputStream getInputStream()
        throws IOException
    {
        try
        {
            return content.getInputStream();
        }
        catch ( FileSystemException fse )
        {
            throw new ProtocolException( fse.getMessage() );
        }
    }

    public OutputStream getOutputStream()
        throws IOException
    {
        try
        {
            return content.getOutputStream();
        }
        catch ( FileSystemException fse )
        {
            throw new ProtocolException( fse.getMessage() );
        }
    }

    public int getContentLength()
    {
        try
        {
            return (int)content.getSize();
        }
        catch ( FileSystemException fse )
        {
        }

        return -1;
    }

}
