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
package org.apache.commons.vfs.provider.http;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.GenericFileName;

import java.util.Collection;

/**
 * An HTTP file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.5 $ $Date: 2004/05/01 18:14:28 $
 */
public class HttpFileSystem
    extends AbstractFileSystem
    implements FileSystem

{
    private HttpClient client;

    public HttpFileSystem(final GenericFileName rootName, final FileSystemOptions fileSystemOptions)
    {
        super(rootName, null, fileSystemOptions);
    }

    /**
     * Adds the capabilities of this file system.
     */
    protected void addCapabilities(final Collection caps)
    {
        caps.add(Capability.READ_CONTENT);
        caps.add(Capability.URI);
        caps.add(Capability.GET_LAST_MODIFIED);
        caps.add(Capability.ATTRIBUTES);
    }

    /**
     * Returns the client for this file system.
     */
    protected HttpClient getClient()
        throws FileSystemException
    {
        if (client == null)
        {
            // Create an Http client
            final GenericFileName rootName = (GenericFileName) getRootName();
            client = new HttpClient(new MultiThreadedHttpConnectionManager());
            final HostConfiguration config = new HostConfiguration();
            config.setHost(rootName.getHostName(), rootName.getPort());
            client.setHostConfiguration(config);
            final UsernamePasswordCredentials creds =
                new UsernamePasswordCredentials(rootName.getUserName(), rootName.getPassword());
            client.getState().setCredentials(null, rootName.getHostName(), creds);
        }
        return client;
    }

    /**
     * Creates a file object.  This method is called only if the requested
     * file is not cached.
     */
    protected FileObject createFile(final FileName name)
        throws Exception
    {
        return new HttpFileObject(name, this);
    }
}
