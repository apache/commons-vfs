/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * A proxy for URLs that are supported by the standard stream handler factory.
 *
 * @author <a href="mailto:brian@mmmanager.org">Brian Olsen</a>
 * @version $Revision: 1.3 $ $Date: 2002/07/05 04:08:18 $
 */
class URLStreamHandlerProxy
    extends URLStreamHandler
{
    protected URLConnection openConnection( final URL url )
        throws IOException
    {
        final URL proxyURL = new URL( url.toExternalForm() );
        return proxyURL.openConnection();
    }

    protected void parseURL( final URL u,
                             final String spec,
                             final int start,
                             final int limit )
    {
        try
        {
            final URL url = new URL( u, spec );
            setURL( u, url.getProtocol(), url.getHost(),
                    url.getPort(), url.getAuthority(), url.getUserInfo(),
                    url.getFile(), url.getQuery(), url.getRef() );
        }
        catch( MalformedURLException mue )
        {
            //We retrow this as a simple runtime exception.
            //It is retrown in URL as a MalformedURLException anyway.
            throw new RuntimeException( mue.getMessage() );
        }
    }
}
