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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.GenericFileName;
import org.apache.webdav.lib.WebdavResource;

import java.io.IOException;
import java.util.Collection;

/**
 * A WebDAV file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.12 $ $Date: 2004/05/03 19:48:49 $
 */
class WebDavFileSystem
    extends AbstractFileSystem
    implements FileSystem
{
    private HttpClient client;

    public WebDavFileSystem(final FileSystemManager manager, final GenericFileName rootName, final FileSystemOptions fileSystemOptions)
    {
        super(manager, rootName, null, fileSystemOptions);
    }

    /**
     * Adds the capabilities of this file system.
     */
    protected void addCapabilities(final Collection caps)
    {
        caps.add(Capability.CREATE);
        caps.add(Capability.DELETE);
        caps.add(Capability.RENAME);
        caps.add(Capability.GET_TYPE);
        caps.add(Capability.LIST_CHILDREN);
        caps.add(Capability.READ_CONTENT);
        caps.add(Capability.URI);
        caps.add(Capability.WRITE_CONTENT);
        caps.add(Capability.GET_LAST_MODIFIED);
        caps.add(Capability.ATTRIBUTES);
    }

    /**
     * Returns the client for this file system.
     */
    protected HttpClient getClient() throws FileSystemException
    {
        if (client == null)
        {
            // Create an Http client
            try
            {
                final GenericFileName rootName = (GenericFileName) getRootName();
                final HttpURL url = new HttpURL(rootName.getUserName(),
                    rootName.getPassword(),
                    rootName.getHostName(),
                    rootName.getPort(),
                    "/");
                final WebdavResource resource = new WebdavResource(url, WebdavResource.NOACTION, 1);
                client = resource.retrieveSessionInstance();
            }
            catch (final IOException e)
            {
                throw new FileSystemException("vfs.provider.webdav/create-client.error", getRootName(), e);
            }
        }
        return client;
    }

    /**
     * Creates a file object.  This method is called only if the requested
     * file is not cached.
     */
    protected FileObject createFile(final FileName name)
    {
        final GenericFileName fileName = (GenericFileName) name;
        return new WebdavFileObject(fileName, this);
    }
}
