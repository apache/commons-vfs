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
package org.apache.commons.vfs.provider.webdav;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.GenericFileName;
import org.apache.commons.vfs.util.MonitorInputStream;
import org.apache.commons.vfs.util.MonitorOutputStream;
import org.apache.util.HttpURL;
import org.apache.webdav.lib.WebdavResource;

/**
 * A WebDAV file.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1 $ $Date: 2003/02/15 00:17:06 $
 */
public class WebdavFileObject
    extends AbstractFileObject
    implements FileObject
{
    private WebdavResource resource;
    private HttpURL url;

    public WebdavFileObject( final GenericFileName name,
                             final AbstractFileSystem fileSystem ) throws FileSystemException
    {
        super( name, fileSystem );
    }

    /**
     * Determines the type of the file, returns null if the file does not
     * exist.
     *
     * @todo Pool and reuse the connections
     * @todo Bail if file is not a DAV resource
     */
    protected FileType doGetType() throws Exception
    {
        final GenericFileName name = (GenericFileName)getName();
        url = new HttpURL( name.getUserName(), name.getPassword(), name.getHostName(), name.getPort(), name.getPath() );
        resource = new WebdavResource( url, WebdavResource.NOACTION, 1 );

        // Determine whether the resource exists, and whether it is a DAV resource
        boolean ok = resource.optionsMethod();
        if ( !ok )
        {
            resource.getHttpURL().setPath( getName().getPath() + '/' );
            ok = resource.optionsMethod();
            if ( !ok )
            {
                return null;
            }
        }

        boolean exists = false;
        for ( Enumeration enum = resource.getAllowedMethods(); enum.hasMoreElements(); )
        {
            final String method = (String)enum.nextElement();
            if ( method.equals( "GET" ) )
            {
                exists = true;
                break;
            }
        }
        boolean isDavResource = false;
        for ( Enumeration enum = resource.getDavCapabilities(); enum.hasMoreElements(); )
        {
            isDavResource = true;
            break;
        }

        if ( !exists )
        {
            return null;
        }

        // Get the properties of the resource
        resource.setProperties( WebdavResource.DEFAULT, 1 );
        if ( resource.isCollection() )
        {
            return FileType.FOLDER;
        }
        else
        {
            return FileType.FILE;
        }
    }

    /**
     * Lists the children of the file.
     */
    protected String[] doListChildren() throws Exception
    {
        final String[] children = resource.list();
        if ( children == null )
        {
            throw new FileSystemException( "vfs.provider.webdav/list-children.error" );
        }
        return children;
    }

    /**
     * Creates this file as a folder.
     */
    protected void doCreateFolder() throws Exception
    {
        resource.getHttpURL().setPath( getName().getPath() + '/' );
        final boolean ok = resource.mkcolMethod();
        if ( !ok )
        {
            throw new FileSystemException( "vfs.provider.webdav/create-collection.error", resource.getStatusMessage() );
        }
    }

    /**
     * Deletes the file.
     */
    protected void doDelete() throws Exception
    {
        final boolean ok = resource.deleteMethod( url.getPath() );
        if ( !ok )
        {
            throw new FileSystemException( "vfs.provider.webdav/delete-file.error", resource.getStatusMessage() );
        }
    }

    /**
     * Creates an input stream to read the file content from.
     */
    protected InputStream doGetInputStream() throws Exception
    {
        return new WebdavInputStream( resource );
    }

    /**
     * Creates an output stream to write the file content to.
     */
    protected OutputStream doGetOutputStream() throws Exception
    {
        return new WebdavOutputStream();
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    protected long doGetContentSize() throws Exception
    {
        return resource.getGetContentLength();
    }

    /** An InputStream that reads from a Webdav resource. */
    private static class WebdavInputStream
        extends MonitorInputStream
    {
        private final WebdavResource resource;

        public WebdavInputStream( final WebdavResource resource )
            throws Exception
        {
            super( resource.getMethodData() );
            this.resource = resource;
        }

        /**
         * Called after the stream has been closed.
         */
        protected void onClose() throws IOException
        {
            // TODO - clean up
            //resource.close();
        }
    }

    /**
     * An OutputStream that writes to a Webdav resource.
     *
     * @todo Don't gather up the body in a ByteArrayOutputStream, write directly to connection
     */
    private class WebdavOutputStream
        extends MonitorOutputStream
    {
        public WebdavOutputStream()
        {
            super( new ByteArrayOutputStream() );
        }

        /**
         * Called after this stream is closed.
         */
        protected void onClose() throws IOException
        {
            final ByteArrayOutputStream outstr = (ByteArrayOutputStream)out;

            resource.getHttpURL().setPath( getName().getPath() );
            final boolean ok = resource.putMethod( outstr.toByteArray() );
            if ( !ok )
            {
                throw new FileSystemException( "vfs.provider.webdav/write-file.error", resource.getStatusMessage() );
            }
        }
    }
}
