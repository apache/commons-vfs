/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.commons.vfs.provider.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.util.DateParser;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.util.MonitorInputStream;

/**
 * A file object backed by commons httpclient.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.3 $ $Date: 2003/10/13 08:44:27 $
 *
 * @todo status codes
 */
public class HttpFileObject
    extends AbstractFileObject
{
    private final HttpFileSystem fileSystem;
    private HeadMethod method;

    public HttpFileObject( final FileName name,
                           final HttpFileSystem fileSystem )
    {
        super( name, fileSystem );
        this.fileSystem = fileSystem;
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
        setupMethod( method );
        final HttpClient client = fileSystem.getClient();
        final int status = client.executeMethod( method );
        method.releaseConnection();
        if ( status == HttpURLConnection.HTTP_OK )
        {
            return FileType.FILE;
        }
        else if ( status == HttpURLConnection.HTTP_NOT_FOUND
                  || status == HttpURLConnection.HTTP_GONE )
        {
            return FileType.IMAGINARY;
        }
        else
        {
            throw new FileSystemException( "vfs.provider.http/head.error", getName() );
        }
    }

    /**
     * Lists the children of this file.
     */
    protected String[] doListChildren()
        throws Exception
    {
        throw new Exception( "Not implemented." );
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    protected long doGetContentSize()
        throws Exception
    {
        final Header header = method.getResponseHeader( "content-length" );
        if ( header == null )
        {
            // Assume 0 content-length
            return 0;
        }
        return Integer.parseInt( header.getValue() );
    }

    /**
     * Returns the last modified time of this file.
     *
     * This implementation throws an exception.
     */
    protected long doGetLastModifiedTime()
        throws Exception
    {
        final Header header = method.getResponseHeader( "last-modified" );
        if ( header == null )
        {
            throw new FileSystemException( "vfs.provider.http/last-modified.error", getName() );
        }
        return DateParser.parseDate( header.getValue() ).getTime();
    }

    /**
     * Creates an input stream to read the file content from.  Is only called
     * if {@link #doGetType} returns {@link FileType#FILE}.
     *
     * <p>It is guaranteed that there are no open output streams for this file
     * when this method is called.
     *
     * <p>The returned stream does not have to be buffered.
     */
    protected InputStream doGetInputStream()
        throws Exception
    {
        final GetMethod getMethod = new GetMethod();
        setupMethod( getMethod );
        final int status = fileSystem.getClient().executeMethod( getMethod );
        if ( status != HttpURLConnection.HTTP_OK )
        {
            throw new FileSystemException( "vfs.provider.http/get.error", getName() );
        }

        return new HttpInputStream( getMethod );
    }

    /**
     * Prepares a Method object.
     */
    private void setupMethod( final HttpMethod method )
    {
        method.setPath( getName().getPath() );
        method.setFollowRedirects( true );
        method.setRequestHeader( "User-Agent", "Jakarta-Commons-VFS" );
    }

    /** An InputStream that cleans up the HTTP connection on close. */
    private static class HttpInputStream
        extends MonitorInputStream
    {
        private final GetMethod method;

        public HttpInputStream( final GetMethod method )
            throws IOException
        {
            super( method.getResponseBodyAsStream() );
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
}
