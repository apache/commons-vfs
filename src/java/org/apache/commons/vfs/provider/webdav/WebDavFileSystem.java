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

import java.util.Collection;
import java.io.IOException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.GenericFileName;
import org.apache.util.HttpURL;
import org.apache.webdav.lib.WebdavResource;

/**
 * A WebDAV file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.4 $ $Date: 2003/02/21 13:08:59 $
 */
class WebDavFileSystem
    extends AbstractFileSystem
    implements FileSystem
{
    private HttpClient client;

    public WebDavFileSystem( final GenericFileName rootName )
    {
        super( rootName, null );
    }

    /**
     * Adds the capabilities of this file system.
     */
    protected void addCapabilities( final Collection caps )
    {
        caps.add( Capability.CREATE );
        caps.add( Capability.DELETE );
        caps.add( Capability.GET_TYPE );
        caps.add( Capability.LIST_CHILDREN );
        caps.add( Capability.READ_CONTENT );
        caps.add( Capability.URI );
        caps.add( Capability.WRITE_CONTENT );
    }

    /**
     * Returns the client for this file system.
     */
    protected HttpClient getClient() throws FileSystemException
    {
        if ( client == null )
        {
            // Create an Http client
            try
            {
                final GenericFileName rootName = (GenericFileName)getRootName();
                final HttpURL url = new HttpURL( rootName.getUserName(),
                                                 rootName.getPassword(),
                                                 rootName.getHostName(),
                                                 rootName.getPort(),
                                                 "/" );
                final WebdavResource resource = new WebdavResource( url, WebdavResource.NOACTION, 1 );
                client = resource.retrieveSessionInstance();
            }
            catch ( final IOException e )
            {
                throw new FileSystemException( "vfs.provider.webdav/create-client.error", getRootName(), e );
            }
        }
        return client;
    }

    /**
     * Creates a file object.  This method is called only if the requested
     * file is not cached.
     */
    protected FileObject createFile( final FileName name )
    {
        final GenericFileName fileName = (GenericFileName)name;
        return new WebdavFileObject( fileName, this );
    }
}
