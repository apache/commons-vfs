/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.commons.vfs.provider.webdav;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.provider.GenericFileName;
import org.apache.commons.vfs.util.MonitorOutputStream;
import org.apache.webdav.lib.BaseProperty;
import org.apache.webdav.lib.WebdavResource;
import org.apache.webdav.lib.methods.DepthSupport;
import org.apache.webdav.lib.methods.OptionsMethod;
import org.apache.webdav.lib.methods.XMLResponseMethodBase;

/**
 * A WebDAV file.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.12 $ $Date: 2004/03/03 07:05:35 $
 */
public class WebdavFileObject
    extends AbstractFileObject
    implements FileObject
{
    private final WebDavFileSystem fileSystem;
    private WebdavResource resource;
    private HttpURL url;

    public WebdavFileObject( final GenericFileName name,
                             final WebDavFileSystem fileSystem )
    {
        super( name, fileSystem );
        this.fileSystem = fileSystem;
    }

    /**
     * Attaches this file object to its file resource.
     */
    protected void doAttach() throws Exception
    {
        final GenericFileName name = (GenericFileName)getName();
        url = new HttpURL( name.getUserName(), name.getPassword(), name.getHostName(), name.getPort(), name.getPath() );
        resource = new WebdavResource( fileSystem.getClient() ) { };
        resource.setHttpURL( url, WebdavResource.NOACTION, 1 );
    }

    /**
     * Determines the type of the file, returns null if the file does not
     * exist.
     *
     * @todo Shouldn't need 2 trips to the server to determine type.
     */
    protected FileType doGetType() throws Exception
    {
        // Determine whether the resource exists, and whether it is a DAV resource
        final OptionsMethod optionsMethod = new OptionsMethod( getName().getPath() );
        optionsMethod.setFollowRedirects( true );
        final int status = fileSystem.getClient().executeMethod( optionsMethod );
        if ( status < 200 || status > 299 )
        {
            return FileType.IMAGINARY;
        }
        resource.getHttpURL().setPath( optionsMethod.getPath() );

        // Resource exists if we can do a GET on it
        boolean exists = false;
        for ( Enumeration enum = optionsMethod.getAllowedMethods(); enum.hasMoreElements(); )
        {
            final String method = (String)enum.nextElement();
            if ( method.equals( "GET" ) )
            {
                exists = true;
                break;
            }
        }
        if ( !exists )
        {
            return FileType.IMAGINARY;
        }

        // Check if the resource is a DAV resource
        final boolean davResource = optionsMethod.getDavCapabilities().hasMoreElements();
        if ( !davResource )
        {
            // Assume a folder, and don't get the properties
            return FileType.FOLDER;
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
            throw new FileSystemException( "vfs.provider.webdav/list-children.error", resource.getStatusMessage() );
        }
        return children;
    }

    /**
     * Creates this file as a folder.
     */
    protected void doCreateFolder() throws Exception
    {
        // Adjust resource path
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
        return resource.getMethodData();
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

    /**
     * An OutputStream that writes to a Webdav resource.
     *
     * @todo Don't gather up the body in a ByteArrayOutputStream; need to write directly to connection
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

            // Adjust the resource path (this file object may have been a folder)
            resource.getHttpURL().setPath( getName().getPath() );
            final boolean ok = resource.putMethod( outstr.toByteArray() );
            if ( !ok )
            {
                throw new FileSystemException( "vfs.provider.webdav/write-file.error", resource.getStatusMessage() );
            }
        }
    }

    /**
     * Returns the last modified time of this file.  Is only called if
     * {@link #doGetType} does not return {@link FileType#IMAGINARY}.
     */
    protected long doGetLastModifiedTime() throws Exception
    {
        return resource.getGetLastModified();
    }

    /**
     * Returns the properties of the Webdav resource.
     */
    protected Map doGetAttributes() throws Exception
    {
        final Map attributes = new HashMap();
        final Enumeration e = resource.propfindMethod( DepthSupport.DEPTH_0 );
        while (e.hasMoreElements())
        {
            final XMLResponseMethodBase.Response response = (XMLResponseMethodBase.Response)e.nextElement();
            final Enumeration properties = response.getProperties();
            while ( properties.hasMoreElements() )
            {
                final BaseProperty property = (BaseProperty) properties.nextElement();
                attributes.put( property.getLocalName(), property.getPropertyAsString() );
            }
        }

        return attributes;
    }
}
