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
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.UserAuthenticationData;
import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.util.UserAuthenticatorUtils;
import org.apache.commons.vfs.provider.GenericFileName;
import org.apache.commons.vfs.provider.http.HttpFileProvider;
import org.apache.commons.vfs.provider.http.HttpClientFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * A provider for WebDAV.
 *
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 * @version $Revision$ $Date$
 */
public class WebdavFileProvider
    extends HttpFileProvider
{
    /** The authenticator types used by the WebDAV provider. */
    public static final UserAuthenticationData.Type[] AUTHENTICATOR_TYPES = new UserAuthenticationData.Type[]
        {
            UserAuthenticationData.USERNAME, UserAuthenticationData.PASSWORD
        };

    /** The capabilities of the WebDAV provider */
    protected static final Collection capabilities =
            Collections.unmodifiableCollection(Arrays.asList(new Capability[]
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
        Capability.ATTRIBUTES,
        Capability.RANDOM_ACCESS_READ,
        Capability.DIRECTORY_READ_CONTENT,
    }));

    public WebdavFileProvider()
    {
        super();

        setFileNameParser(WebdavFileNameParser.getInstance());
    }
    /**
     * Creates a {@link FileSystem}.  If you're looking at this method and wondering how to 
     * get a FileSystemOptions object bearing the proxy host and credentials configuration through 
     * to this method so it's used for resolving a {@link FileObject} in the FileSystem, then be sure
     * to use correct signature of the {@link FileSystemManager} resolveFile method.
     * @see org.apache.commons.vfs.impl.DefaultFileSystemManager#resolveFile(FileObject, String, FileSystemOptions)
     */
    @Override
    protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions)
        throws FileSystemException
    {
        // Create the file system
        final GenericFileName rootName = (GenericFileName) name;
        FileSystemOptions fsOpts = (fileSystemOptions == null) ? new FileSystemOptions() : fileSystemOptions;

        UserAuthenticationData authData = null;
        HttpClient httpClient;
        try
        {
            authData = UserAuthenticatorUtils.authenticate(fsOpts, AUTHENTICATOR_TYPES);

            httpClient = HttpClientFactory.createConnection(
                WebdavFileSystemConfigBuilder.getInstance(),
                "http",
                rootName.getHostName(),
                rootName.getPort(),
                UserAuthenticatorUtils.toString(UserAuthenticatorUtils.getData(authData,
                        UserAuthenticationData.USERNAME, UserAuthenticatorUtils.toChar(rootName.getUserName()))),
                UserAuthenticatorUtils.toString(UserAuthenticatorUtils.getData(authData,
                        UserAuthenticationData.PASSWORD, UserAuthenticatorUtils.toChar(rootName.getPassword()))),
                fsOpts);
        }
        finally
        {
            UserAuthenticatorUtils.cleanup(authData);
        }

        return new WebdavFileSystem(rootName, httpClient, fsOpts);
    }

    @Override
    public FileSystemConfigBuilder getConfigBuilder()
    {
        return WebdavFileSystemConfigBuilder.getInstance();
    }


    @Override
    public Collection getCapabilities()
    {
        return capabilities;
    }
}
