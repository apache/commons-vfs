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
package org.apache.commons.vfs.provider.webdav;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.GenericFileName;
import org.apache.commons.vfs.provider.http.ThreadLocalHttpConnectionManager;

import java.util.Collection;

/**
 * A WebDAV file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class WebDavFileSystem
    extends AbstractFileSystem
    implements FileSystem
{
    private final HttpClient client;

    protected WebDavFileSystem(final GenericFileName rootName, final HttpClient client, final FileSystemOptions fileSystemOptions)
    {
        super(rootName, null, fileSystemOptions);

        this.client = client;
    }

    /**
     * Adds the capabilities of this file system.
     */
    protected void addCapabilities(final Collection caps)
    {
        caps.addAll(WebdavFileProvider.capabilities);
    }

    /**
     * Returns the client for this file system.
     */
    protected HttpClient getClient()
    {
        return client;
    }

	public void closeCommunicationLink()
	{
		if (getClient() != null)
		{
			HttpConnectionManager mgr = getClient().getHttpConnectionManager();
			if (mgr instanceof ThreadLocalHttpConnectionManager)
			{
				((ThreadLocalHttpConnectionManager) mgr).releaseLocalConnection();
			}
		}
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
