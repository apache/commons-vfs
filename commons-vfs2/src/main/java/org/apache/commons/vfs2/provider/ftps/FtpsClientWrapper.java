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
package org.apache.commons.vfs2.provider.ftps;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.apache.commons.vfs2.provider.ftp.FTPClientWrapper;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;

/**
 * A wrapper to the FTPSClient to allow automatic reconnect on connection loss.
 * <p>
 * The only difference to the {@link FTPClientWrapper} is the creation of a {@link FTPSClient} instead of a
 * {@link FTPClient}.
 *
 * @since 2.0
 */
class FtpsClientWrapper extends FTPClientWrapper {
    FtpsClientWrapper(final GenericFileName root, final FileSystemOptions fileSystemOptions)
            throws FileSystemException {
        super(root, fileSystemOptions);
    }

    @Override
    protected FTPClient createClient(final GenericFileName rootName, final UserAuthenticationData authData)
            throws FileSystemException {
        return FtpsClientFactory.createConnection(rootName.getHostName(), rootName.getPort(),
                UserAuthenticatorUtils.getData(authData, UserAuthenticationData.USERNAME,
                        UserAuthenticatorUtils.toChar(rootName.getUserName())),
                UserAuthenticatorUtils.getData(authData, UserAuthenticationData.PASSWORD,
                        UserAuthenticatorUtils.toChar(rootName.getPassword())),
                rootName.getPath(), getFileSystemOptions());
    }
}
