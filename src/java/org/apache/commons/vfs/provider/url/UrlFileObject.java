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
package org.apache.commons.vfs.provider.url;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * A {@link FileObject} implementation backed by a {@link URL}.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 * @todo Implement set lastModified and get/set attribute
 * @todo Implement getOutputStream()
 */
public class UrlFileObject
    extends AbstractFileObject
    implements FileObject
{
    private URL url;

    protected UrlFileObject(final UrlFileSystem fs,
                            final FileName fileName)
    {
        super(fileName, fs);
    }

    /**
     * Attaches this file object to its file resource.  This method is called
     * before any of the doBlah() or onBlah() methods.  Sub-classes can use
     * this method to perform lazy initialisation.
     */
    protected void doAttach() throws Exception
    {
        if (url == null)
        {
            // url = new URL(getName().getURI());
            url = createURL(getName());
        }
    }

    protected URL createURL(final FileName name) throws MalformedURLException
    {
        return new URL(getName().getURI());
    }

    /**
     * Determines the type of the file.
     */
    protected FileType doGetType() throws Exception
    {
        try
        {
            // Attempt to connect & check status
            final URLConnection conn = url.openConnection();
            final InputStream in = conn.getInputStream();
            try
            {
                if (conn instanceof HttpURLConnection)
                {
                    final int status = ((HttpURLConnection) conn).getResponseCode();
                    // 200 is good, maybe add more later...
                    if (HttpURLConnection.HTTP_OK != status)
                    {
                        return FileType.IMAGINARY;
                    }
                }

                return FileType.FILE;
            }
            finally
            {
                in.close();
            }
        }
        catch (final FileNotFoundException e)
        {
            return FileType.IMAGINARY;
        }
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    protected long doGetContentSize() throws Exception
    {
        final URLConnection conn = url.openConnection();
        final InputStream in = conn.getInputStream();
        try
        {
            return conn.getContentLength();
        }
        finally
        {
            in.close();
        }
    }

    /**
     * Returns the last modified time of this file.
     */
    protected long doGetLastModifiedTime()
        throws Exception
    {
        final URLConnection conn = url.openConnection();
        final InputStream in = conn.getInputStream();
        try
        {
            return conn.getLastModified();
        }
        finally
        {
            in.close();
        }
    }

    /**
     * Lists the children of the file.
     */
    protected String[] doListChildren() throws Exception
    {
        throw new FileSystemException("Not implemented.");
    }

    /**
     * Creates an input stream to read the file content from.
     */
    protected InputStream doGetInputStream() throws Exception
    {
        return url.openStream();
    }
}
