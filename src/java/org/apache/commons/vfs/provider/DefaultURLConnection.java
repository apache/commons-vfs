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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        return content.getInputStream();
    }

    public OutputStream getOutputStream()
        throws IOException
    {
        return content.getOutputStream();
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
