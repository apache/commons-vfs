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
package org.apache.commons.vfs2.provider.webdav4;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.apache.commons.vfs2.provider.http4.Http4FileProvider;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.HttpClientContext;

/**
 * A provider for WebDAV based on HTTP4.
 *
 * @since 2.5.0
 */
public class Webdav4FileProvider extends Http4FileProvider {

    /**
     * The authenticator types used by the WebDAV provider.
     *
     * @deprecated Might be removed in the next major version.
     */
    @Deprecated
    public static final UserAuthenticationData.Type[] AUTHENTICATOR_TYPES = new UserAuthenticationData.Type[] {
            UserAuthenticationData.USERNAME, UserAuthenticationData.PASSWORD };

    /** The capabilities of the WebDAV provider */
    public static final Collection<Capability> DEFAULT_CAPABILITIES =
            Collections.unmodifiableCollection(
                    Arrays.asList(
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
                            Capability.DIRECTORY_READ_CONTENT
                            )
                    );

    /** The capabilities of the WebDAV provider */
    protected static final Collection<Capability> capabilities = DEFAULT_CAPABILITIES;

    public Webdav4FileProvider() {
        super();

        setFileNameParser(Webdav4FileNameParser.getInstance());
    }

    /**
     * Creates a {@link FileSystem}.
     * <p>
     * If you're looking at this method and wondering how to get a FileSystemOptions object bearing the proxy host and
     * credentials configuration through to this method so it's used for resolving a
     * {@link org.apache.commons.vfs2.FileObject FileObject} in the FileSystem, then be sure to use correct signature of
     * the {@link org.apache.commons.vfs2.FileSystemManager FileSystemManager} resolveFile method.
     *
     * @see org.apache.commons.vfs2.impl.DefaultFileSystemManager#resolveFile(FileObject, String, FileSystemOptions)
     */
    @Override
    protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions)
            throws FileSystemException {
        // Create the file system
        final GenericFileName rootName = (GenericFileName) name;
        // TODO: need to check null to create a non-null here???
        final FileSystemOptions fsOpts = fileSystemOptions == null ? new FileSystemOptions() : fileSystemOptions;

        UserAuthenticationData authData = null;
        HttpClient httpClient = null;
        HttpClientContext httpClientContext = null;

        try {
            final Webdav4FileSystemConfigBuilder builder = Webdav4FileSystemConfigBuilder.getInstance();
            authData = UserAuthenticatorUtils.authenticate(fsOpts, AUTHENTICATOR_TYPES);
            httpClientContext = createHttpClientContext(builder, rootName, fsOpts, authData);
            httpClient = createHttpClient(builder, rootName, fsOpts);
        } finally {
            UserAuthenticatorUtils.cleanup(authData);
        }

        return new Webdav4FileSystem(rootName, fsOpts, httpClient, httpClientContext);
    }

    @Override
    public FileSystemConfigBuilder getConfigBuilder() {
        return Webdav4FileSystemConfigBuilder.getInstance();
    }

    @Override
    public Collection<Capability> getCapabilities() {
        return capabilities;
    }
}
