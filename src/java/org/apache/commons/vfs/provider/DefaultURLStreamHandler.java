/*
 * Copyright 2002-2005 The Apache Software Foundation.
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

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.FileType;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * A default URL stream handler that will work for most file systems.
 *
 * @author <a href="mailto:brian@mmmanager.org">Brian Olsen</a>
 * @version $Revision$ $Date$
 */
public class DefaultURLStreamHandler
    extends URLStreamHandler
{
    private final VfsComponentContext context;
    private final FileSystemOptions fileSystemOptions;

    public DefaultURLStreamHandler(final VfsComponentContext context)
    {
        this(context, null);
    }

    public DefaultURLStreamHandler(final VfsComponentContext context, final FileSystemOptions fileSystemOptions)
    {
        this.context = context;
        this.fileSystemOptions = fileSystemOptions;
    }

    protected URLConnection openConnection(final URL url)
        throws IOException
    {
        final FileObject entry = context.resolveFile(url.toExternalForm(), fileSystemOptions);
        return new DefaultURLConnection(url, entry.getContent());
    }

    protected void parseURL(final URL u,
                            final String spec,
                            final int start,
                            final int limit)
    {
        try
        {
            FileObject old = context.resolveFile(u.toExternalForm(), fileSystemOptions);

            FileObject newURL;
            if (start > 0 && spec.charAt(start - 1) == ':')
            {
                newURL = context.resolveFile(old, spec, fileSystemOptions);
            }
            else
            {
                if (old.getType() == FileType.FILE && old.getParent() != null)
                {
                    // for files we have to resolve relative
                    newURL = old.getParent().resolveFile(spec);
                }
                else
                {
                    newURL = old.resolveFile(spec);
                }
            }

            final String url = newURL.getName().getURI();
            final StringBuffer filePart = new StringBuffer();
            final String protocolPart = UriParser.extractScheme(url, filePart);

            setURL(u, protocolPart, null, -1, null, null, filePart.toString(), null, null);
        }
        catch (FileSystemException fse)
        {
            // This is rethrown to MalformedURLException in URL anyway
            throw new RuntimeException(fse.getMessage());
        }
    }

    protected String toExternalForm(final URL u)
    {
        return u.getProtocol() + ":" + u.getFile();
    }
}
