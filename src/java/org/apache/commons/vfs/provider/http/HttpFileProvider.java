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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs.provider.GenericFileName;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


/**
 * An HTTP provider that uses commons-httpclient.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class HttpFileProvider
    extends AbstractOriginatingFileProvider
{
    final static Collection capabilities = Collections.unmodifiableCollection(Arrays.asList(new Capability[]
    {
        Capability.GET_TYPE,
        Capability.READ_CONTENT,
        Capability.URI,
        Capability.GET_LAST_MODIFIED,
        Capability.ATTRIBUTES,
        Capability.RANDOM_ACCESS_READ
    }));

    public HttpFileProvider()
    {
        super();
    }

    /**
     * Parses an abolute URI.
     */
    protected FileName parseUri(final String uri)
        throws FileSystemException
    {
        return GenericFileName.parseUri(uri, 80);
    }

    /**
     * Creates a {@link FileSystem}.
     */
    protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions)
        throws FileSystemException
    {
        // Create the file system
        final GenericFileName rootName = (GenericFileName) name;

        HttpClient httpClient = HttpClientFactory.createConnection(rootName.getHostName(),
            rootName.getPort(),
            rootName.getUserName(),
            rootName.getPassword(),
            fileSystemOptions);

        return new HttpFileSystem(rootName, httpClient, fileSystemOptions);
    }

    public FileSystemConfigBuilder getConfigBuilder()
    {
        return HttpFileSystemConfigBuilder.getInstance();
    }

    public Collection getCapabilities()
    {
        return capabilities;
    }
}
