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
package org.apache.commons.vfs2.provider.tftp;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs2.provider.FileProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * A provider for tftp
 */
public class TftpFileProvider extends AbstractOriginatingFileProvider implements FileProvider {
    /**
     * Authentication data supported by this provider.
     */
    public static final UserAuthenticationData.Type[] AUTHENTICATOR_TYPES = new UserAuthenticationData.Type[]{
            UserAuthenticationData.USERNAME, UserAuthenticationData.PASSWORD, UserAuthenticationData.DOMAIN};

    static final Collection<Capability> capabilities = Collections
            .unmodifiableCollection(Arrays.asList(Capability.CREATE, Capability.DELETE, Capability.RENAME, Capability.GET_TYPE,
                    Capability.GET_LAST_MODIFIED, Capability.SET_LAST_MODIFIED_FILE,
                    Capability.SET_LAST_MODIFIED_FOLDER, Capability.LIST_CHILDREN, Capability.READ_CONTENT,
                    Capability.URI, Capability.WRITE_CONTENT, Capability.APPEND_CONTENT, Capability.RANDOM_ACCESS_READ,
                    Capability.RANDOM_ACCESS_WRITE));

    public TftpFileProvider() {
        setFileNameParser(TftpFileNameParser.getInstance());
    }

    /**
     * Creates the file system.
     */
    @Override
    protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions)
            throws FileSystemException {
        return new TftpFileSystem(name, fileSystemOptions);
    }

    @Override
    public Collection<Capability> getCapabilities() {
        return capabilities;
    }
}
