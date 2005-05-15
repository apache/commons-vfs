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
package org.apache.commons.vfs.provider.webdav;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs.provider.GenericFileName;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * A provider for WebDAV.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class WebdavFileProvider
    extends AbstractOriginatingFileProvider
{
    protected final static Collection capabilities = Collections.unmodifiableCollection(Arrays.asList(new Capability[]
    {
        Capability.CREATE,
        Capability.DELETE,
        Capability.RENAME,
        Capability.GET_TYPE,
        Capability.LIST_CHILDREN,
        Capability.READ_CONTENT,
        Capability.URI,
        Capability.WRITE_CONTENT,
        Capability.GET_LAST_MODIFIED,
        Capability.ATTRIBUTES
    }));

    public WebdavFileProvider()
    {
        super();

        setFileNameParser(WebdavFileNameParser.getInstance());
    }

    /**
     * Creates a filesystem.
     */
    protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions)
        throws FileSystemException
    {
        // Create the file system
        final GenericFileName rootName = (GenericFileName) name;

        HttpClient httpClient = WebdavClientFactory.createConnection(rootName.getHostName(),
            rootName.getPort(),
            rootName.getUserName(),
            rootName.getPassword(),
            fileSystemOptions);

        return new WebDavFileSystem(rootName, httpClient, fileSystemOptions);
    }

    public Collection getCapabilities()
    {
        return capabilities;
    }
}
