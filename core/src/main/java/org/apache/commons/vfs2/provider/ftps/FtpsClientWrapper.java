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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.apache.commons.vfs2.provider.ftp.FtpClient;
import org.apache.commons.vfs2.provider.ftp.FtpFileProvider;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;


/**
 * A wrapper to the FTPClient to allow automatic reconnect on connection loss.<br />
 * I decided to not to use eg. noop() to determine the state of the connection to avoid unnecesary server round-trips.
 * @since 2.0
 */
class FtpsClientWrapper implements FtpClient
{
    private final GenericFileName root;
    private final FileSystemOptions fileSystemOptions;

    private FTPSClient ftpClient = null;

    FtpsClientWrapper(final GenericFileName root, final FileSystemOptions fileSystemOptions) throws FileSystemException
    {
        this.root = root;
        this.fileSystemOptions = fileSystemOptions;
        getFtpsClient(); // fail-fast
    }

    public GenericFileName getRoot()
    {
        return root;
    }

    public FileSystemOptions getFileSystemOptions()
    {
        return fileSystemOptions;
    }

    private FTPSClient createClient() throws FileSystemException
    {
        final GenericFileName rootName = getRoot();

        UserAuthenticationData authData = null;
        try
        {
            authData = UserAuthenticatorUtils.authenticate(fileSystemOptions, FtpFileProvider.AUTHENTICATOR_TYPES);

            return FtpsClientFactory.createConnection(rootName.getHostName(),
                rootName.getPort(),
                UserAuthenticatorUtils.getData(authData, UserAuthenticationData.USERNAME,
                                               UserAuthenticatorUtils.toChar(rootName.getUserName())),
                UserAuthenticatorUtils.getData(authData, UserAuthenticationData.PASSWORD,
                                               UserAuthenticatorUtils.toChar(rootName.getPassword())),
                rootName.getPath(),
                getFileSystemOptions());
        }
        finally
        {
            UserAuthenticatorUtils.cleanup(authData);
        }
    }

    private FTPSClient getFtpsClient() throws FileSystemException
    {
        if (ftpClient == null)
        {
            ftpClient = createClient();
        }

        return ftpClient;
    }

    @Override
    public boolean isConnected() throws FileSystemException
    {
        return getFtpsClient().isConnected();
    }

    @Override
    public void disconnect() throws IOException
    {
        try
        {
            getFtpsClient().disconnect();
        }
        finally
        {
            ftpClient = null;
        }
    }

    @Override
    public FTPFile[] listFiles(final String relPath) throws IOException
    {
        try
        {
            return getFtpsClient().listFiles(relPath);
        }
        catch (final IOException e)
        {
            disconnect();
            return getFtpsClient().listFiles(relPath);
        }
    }

    @Override
    public boolean removeDirectory(final String relPath) throws IOException
    {
        try
        {
            return getFtpsClient().removeDirectory(relPath);
        }
        catch (final IOException e)
        {
            disconnect();
            return getFtpsClient().removeDirectory(relPath);
        }
    }

    @Override
    public boolean deleteFile(final String relPath) throws IOException
    {
        try
        {
            return getFtpsClient().deleteFile(relPath);
        }
        catch (final IOException e)
        {
            disconnect();
            return getFtpsClient().deleteFile(relPath);
        }
    }

    @Override
    public boolean rename(final String oldName, final String newName) throws IOException
    {
        try
        {
            return getFtpsClient().rename(oldName, newName);
        }
        catch (final IOException e)
        {
            disconnect();
            return getFtpsClient().rename(oldName, newName);
        }
    }

    @Override
    public boolean makeDirectory(final String relPath) throws IOException
    {
        try
        {
            return getFtpsClient().makeDirectory(relPath);
        }
        catch (final IOException e)
        {
            disconnect();
            return getFtpsClient().makeDirectory(relPath);
        }
    }

    @Override
    public boolean completePendingCommand() throws IOException
    {
        if (ftpClient != null)
        {
            return getFtpsClient().completePendingCommand();
        }

        return true;
    }

    @Override
    public InputStream retrieveFileStream(final String relPath) throws IOException
    {
        try
        {
            return getFtpsClient().retrieveFileStream(relPath);
        }
        catch (final IOException e)
        {
            disconnect();
            return getFtpsClient().retrieveFileStream(relPath);
        }
    }

    @Override
    public InputStream retrieveFileStream(final String relPath, final long restartOffset) throws IOException
    {
        try
        {
            final FTPSClient client = getFtpsClient();
            client.setRestartOffset(restartOffset);
            return client.retrieveFileStream(relPath);
        }
        catch (final IOException e)
        {
            disconnect();

            final FTPSClient client = getFtpsClient();
            client.setRestartOffset(restartOffset);
            return client.retrieveFileStream(relPath);
        }
    }

    @Override
    public OutputStream appendFileStream(final String relPath) throws IOException
    {
        try
        {
            return getFtpsClient().appendFileStream(relPath);
        }
        catch (final IOException e)
        {
            disconnect();
            return getFtpsClient().appendFileStream(relPath);
        }
    }

    @Override
    public OutputStream storeFileStream(final String relPath) throws IOException
    {
        try
        {
            return getFtpsClient().storeFileStream(relPath);
        }
        catch (final IOException e)
        {
            disconnect();
            return getFtpsClient().storeFileStream(relPath);
        }
    }

    @Override
    public boolean abort() throws IOException
    {
        try
        {
            // imario@apache.org: 2005-02-14
            // it should be better to really "abort" the transfer, but
            // currently I didnt manage to make it work - so lets "abort" the hard way.
            // return getFtpsClient().abort();

            disconnect();
            return true;
        }
        catch (final IOException e)
        {
            disconnect();
        }
        return true;
    }

    @Override
    public String getReplyString() throws IOException
    {
        return getFtpsClient().getReplyString();
    }
}
