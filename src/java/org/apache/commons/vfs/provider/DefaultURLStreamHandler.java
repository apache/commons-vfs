/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider;

import java.io.IOException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;

/**
 * A default URL stream handler that will work for most file systems.
 *
 * @author <a href="mailto:brian@mmmanager.org">Brian Olsen</a>
 * @version $Revision: 1.5 $ $Date: 2002/07/05 04:08:17 $
 */
public class DefaultURLStreamHandler
    extends URLStreamHandler
{
    private final FileSystemProviderContext m_context;

    public DefaultURLStreamHandler( final FileSystemProviderContext context )
    {
        m_context = context;
    }

    protected URLConnection openConnection( final URL url )
        throws IOException
    {
        try
        {
            final FileObject entry = m_context.resolveFile( url.toExternalForm() );
            return new DefaultURLConnection( url, entry.getContent() );
        }
        catch( FileSystemException fse )
        {
            throw new ProtocolException( fse.getMessage() );
        }
    }

    protected void parseURL( final URL u,
                             final String spec,
                             final int start,
                             final int limit )
    {
        try
        {
            FileObject old = m_context.resolveFile( u.toExternalForm() );

            FileObject newURL;
            if( start > 0 && spec.charAt( start - 1 ) == ':' )
            {
                newURL = m_context.resolveFile( old, spec );
            }
            else
            {
                newURL = old.resolveFile( spec );
            }

            final String url = newURL.getName().getURI();
            final StringBuffer filePart = new StringBuffer();
            final String protocolPart = UriParser.extractScheme( url, filePart );

            setURL( u, protocolPart, null, -1, null, null, filePart.toString(), null, null );
        }
        catch( FileSystemException fse )
        {
            // This is rethrown to MalformedURLException in URL anyway
            throw new RuntimeException( fse.getMessage() );
        }
    }

    protected String toExternalForm( final URL u )
    {
        return u.getProtocol() + ":" + u.getFile();
    }
}
