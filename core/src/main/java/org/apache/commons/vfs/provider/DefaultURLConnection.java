/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileSystemException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * A default URL connection that will work for most file systems.
 *
 * @author <a href="mailto:brian@mmmanager.org">Brian Olsen</a>
 * @version $Revision$ $Date$
 */
public final class DefaultURLConnection
    extends URLConnection
{
    private final FileContent content;

    public DefaultURLConnection(final URL url,
                                final FileContent content)
    {
        super(url);
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


    public long getLastModified()
    {
        try
        {
            return content.getLastModifiedTime();
        }
        catch (FileSystemException fse)
        {
            // Ignore the exception
        }

        return -1;
    }

    public int getContentLength()
    {
        try
        {
            return (int) content.getSize();
        }
        catch (FileSystemException fse)
        {
            // Ignore the exception
        }

        return -1;
    }

    public String getContentType()
    {
        try
        {
            return content.getContentInfo().getContentType();
        }
        catch (FileSystemException e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String getContentEncoding()
    {
        try
        {
            return content.getContentInfo().getContentEncoding();
        }
        catch (FileSystemException e)
        {
            throw new RuntimeException(e.getMessage());
        }
    }

    /*
    public String getHeaderField(String name)
    {
        try
        {
            if (content.getFile().getFileSystem().hasCapability(Capability.ATTRIBUTES))
            {
                String value = (String) content.getAttribute(name);
                if (value != null)
                {
                    return value;
                }
            }

            return null;
        }
        catch (FileSystemException e)
        {
            throw new RuntimeException(e);
        }
    }
    */
}
