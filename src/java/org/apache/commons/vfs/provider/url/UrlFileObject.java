/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002, 2003 The Apache Software Foundation.  All rights
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
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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
package org.apache.commons.vfs.provider.url;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileObject;

/**
 * A {@link FileObject} implementation backed by a {@link URL}.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.9 $ $Date: 2003/06/28 11:16:33 $
 *
 * @todo Implement set lastModified and get/set attribute
 * @todo Implement getOutputStream()
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
        try
        {
            // Attempt to connect & check status
            final URLConnection conn = url.openConnection();
            final InputStream in = conn.getInputStream();
            try
            {
                if (conn instanceof HttpURLConnection)
                {
                    final int status = ((HttpURLConnection)conn).getResponseCode();
                    // 200 is good, maybe add more later...
                    if ( HttpURLConnection.HTTP_OK != status)
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
        catch ( final FileNotFoundException e )
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
