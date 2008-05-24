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
package org.apache.commons.vfs.provider.http;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.util.DateParser;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.vfs.*;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.provider.URLFileName;
import org.apache.commons.vfs.util.MonitorInputStream;
import org.apache.commons.vfs.util.RandomAccessMode;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * A file object backed by commons httpclient.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 * @todo status codes
 */
public class HttpFileObject
    extends AbstractFileObject
{
    private final HttpFileSystem fileSystem;
    private final String urlCharset;
    private HeadMethod method;

    protected HttpFileObject(final FileName name,
                             final HttpFileSystem fileSystem)
    {
        super(name, fileSystem);
        this.fileSystem = fileSystem;
        urlCharset = HttpFileSystemConfigBuilder.getInstance().getUrlCharset(getFileSystem().getFileSystemOptions());
    }

    /**
     * Detaches this file object from its file resource.
     */
    protected void doDetach()
        throws Exception
    {
        method = null;
    }

    /**
     * Determines the type of this file.  Must not return null.  The return
     * value of this method is cached, so the implementation can be expensive.
     */
    protected FileType doGetType()
        throws Exception
    {
        // Use the HEAD method to probe the file.
        method = new HeadMethod();
        setupMethod(method);
        final HttpClient client = fileSystem.getClient();
        final int status = client.executeMethod(method);
        method.releaseConnection();
        if (status == HttpURLConnection.HTTP_OK)
        {
            return FileType.FILE;
        }
        else if (status == HttpURLConnection.HTTP_NOT_FOUND
            || status == HttpURLConnection.HTTP_GONE)
        {
            return FileType.IMAGINARY;
        }
        else
        {
            throw new FileSystemException("vfs.provider.http/head.error", getName());
        }
    }

    /**
     * Lists the children of this file.
     */
    protected String[] doListChildren()
        throws Exception
    {
        throw new Exception("Not implemented.");
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    protected long doGetContentSize()
        throws Exception
    {
        final Header header = method.getResponseHeader("content-length");
        if (header == null)
        {
            // Assume 0 content-length
            return 0;
        }
        return Integer.parseInt(header.getValue());
    }

    /**
     * Returns the last modified time of this file.
     * <p/>
     * This implementation throws an exception.
     */
    protected long doGetLastModifiedTime()
        throws Exception
    {
        final Header header = method.getResponseHeader("last-modified");
        if (header == null)
        {
            throw new FileSystemException("vfs.provider.http/last-modified.error", getName());
        }
        return DateParser.parseDate(header.getValue()).getTime();
    }

    /**
     * Creates an input stream to read the file content from.  Is only called
     * if {@link #doGetType} returns {@link FileType#FILE}.
     * <p/>
     * <p>It is guaranteed that there are no open output streams for this file
     * when this method is called.
     * <p/>
     * <p>The returned stream does not have to be buffered.
     */
    protected InputStream doGetInputStream()
        throws Exception
    {
        final GetMethod getMethod = new GetMethod();
        setupMethod(getMethod);
        final int status = fileSystem.getClient().executeMethod(getMethod);
        if (status == HttpURLConnection.HTTP_NOT_FOUND)
        {
            throw new FileNotFoundException(getName());
        }
        if (status != HttpURLConnection.HTTP_OK)
        {
            throw new FileSystemException("vfs.provider.http/get.error", getName());
        }

        return new HttpInputStream(getMethod);
    }

    protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception
    {
        return new HttpRandomAccesContent(this, mode);
    }

    /**
     * Prepares a Method object.
     */
    void setupMethod(final HttpMethod method) throws FileSystemException, URIException
    {
        String pathEncoded = ((URLFileName) getName()).getPathQueryEncoded(urlCharset);
        method.setPath(pathEncoded);
        method.setFollowRedirects(true);
        method.setRequestHeader("User-Agent", "Jakarta-Commons-VFS");
    }

    protected String encodePath(final String decodedPath) throws URIException
    {
        String pathEncoded = URIUtil.encodePath(decodedPath);
        return pathEncoded;
    }

    /**
     * An InputStream that cleans up the HTTP connection on close.
     */
    static class HttpInputStream
        extends MonitorInputStream
    {
        private final GetMethod method;

        public HttpInputStream(final GetMethod method)
            throws IOException
        {
            super(method.getResponseBodyAsStream());
            this.method = method;
        }

        /**
         * Called after the stream has been closed.
         */
        protected void onClose()
            throws IOException
        {
            method.releaseConnection();
        }
    }


    protected FileContentInfoFactory getFileContentInfoFactory()
    {
        return new HttpFileContentInfoFactory();
    }

    HeadMethod getHeadMethod()
    {
        return method;
    }

    /*
    protected Map doGetAttributes() throws Exception
    {
        TreeMap map = new TreeMap();

        Header contentType = method.getResponseHeader("content-type");
        if (contentType != null)
        {
            HeaderElement[] element = contentType.getValues();
            if (element != null && element.length > 0)
            {
                map.put("content-type", element[0].getName());
            }
        }

        map.put("content-encoding", method.getResponseCharSet());
        return map;
    }
    */
}
