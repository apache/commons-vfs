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
package org.apache.commons.vfs2.provider.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;

/**
 * A wrapper to the FTPClient to allow automatic reconnect on connection loss.<br />
 * I decided to not to use eg. noop() to determine the state of the connection to avoid
 * unnecesary server round-trips.
 */
class FTPClientWrapper implements FtpClient
{
    private final GenericFileName root;
    private final FileSystemOptions fileSystemOptions;

    private FTPClient ftpClient;

    FTPClientWrapper(final GenericFileName root, final FileSystemOptions fileSystemOptions) throws FileSystemException
    {
        this.root = root;
        this.fileSystemOptions = fileSystemOptions;
        getFtpClient(); // fail-fast
    }

    public GenericFileName getRoot()
    {
        return root;
    }

    public FileSystemOptions getFileSystemOptions()
    {
        return fileSystemOptions;
    }

    private FTPClient createClient() throws FileSystemException
    {
        final GenericFileName rootName = getRoot();

        UserAuthenticationData authData = null;
        try
        {
            authData = UserAuthenticatorUtils.authenticate(fileSystemOptions, FtpFileProvider.AUTHENTICATOR_TYPES);

            return FtpClientFactory.createConnection(rootName.getHostName(),
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

    private FTPClient getFtpClient() throws FileSystemException
    {
        if (ftpClient == null)
        {
            ftpClient = createClient();
        }

        return ftpClient;
    }

    public boolean isConnected() throws FileSystemException
    {
        return ftpClient != null && ftpClient.isConnected();
    }

    public void disconnect() throws IOException
    {
        try
        {
            getFtpClient().disconnect();
        }
        finally
        {
            ftpClient = null;
        }
    }

    public FTPFile[] listFiles(String relPath) throws IOException
    {
        try
        {
            // VFS-210: return getFtpClient().listFiles(relPath);
            FTPFile[] files = listFilesInDirectory(relPath);
            return files;
        }
        catch (IOException e)
        {
            disconnect();

            FTPFile[] files = listFilesInDirectory(relPath);
            return files;
        }
    }

    private FTPFile[] listFilesInDirectory(String relPath) throws IOException
    {
        FTPFile[] files;

        // VFS-307: no check if we can simply list the files, this might fail if there are spaces in the path
        files = getFtpClient().listFiles(relPath);
        if (FTPReply.isPositiveCompletion(getFtpClient().getReplyCode()))
        {
            return files;
        }

        // VFS-307: now try the hard way by cd'ing into the directory, list and cd back
        // if VFS is required to fallback here the user might experience a real bad FTP performance
        // as then every list requires 4 ftp commands.
        String workingDirectory = null;
        if (relPath != null)
        {
            workingDirectory = getFtpClient().printWorkingDirectory();
            if (!getFtpClient().changeWorkingDirectory(relPath))
            {
                return null;
            }
        }

        files = getFtpClient().listFiles();

        if (relPath != null && !getFtpClient().changeWorkingDirectory(workingDirectory))
        {
            throw new FileSystemException("vfs.provider.ftp.wrapper/change-work-directory-back.error",
                    workingDirectory);
        }
        return files;
    }

    public boolean removeDirectory(String relPath) throws IOException
    {
        try
        {
            return getFtpClient().removeDirectory(relPath);
        }
        catch (IOException e)
        {
            disconnect();
            return getFtpClient().removeDirectory(relPath);
        }
    }

    public boolean deleteFile(String relPath) throws IOException
    {
        try
        {
            return getFtpClient().deleteFile(relPath);
        }
        catch (IOException e)
        {
            disconnect();
            return getFtpClient().deleteFile(relPath);
        }
    }

    public boolean rename(String oldName, String newName) throws IOException
    {
        try
        {
            return getFtpClient().rename(oldName, newName);
        }
        catch (IOException e)
        {
            disconnect();
            return getFtpClient().rename(oldName, newName);
        }
    }

    public boolean makeDirectory(String relPath) throws IOException
    {
        try
        {
            return getFtpClient().makeDirectory(relPath);
        }
        catch (IOException e)
        {
            disconnect();
            return getFtpClient().makeDirectory(relPath);
        }
    }

    public boolean completePendingCommand() throws IOException
    {
        if (ftpClient != null)
        {
            return getFtpClient().completePendingCommand();
        }

        return true;
    }

    public InputStream retrieveFileStream(String relPath) throws IOException
    {
        try
        {
            return getFtpClient().retrieveFileStream(relPath);
        }
        catch (IOException e)
        {
            disconnect();
            return getFtpClient().retrieveFileStream(relPath);
        }
    }

    public InputStream retrieveFileStream(String relPath, long restartOffset) throws IOException
    {
        try
        {
            FTPClient client = getFtpClient();
            client.setRestartOffset(restartOffset);
            return client.retrieveFileStream(relPath);
        }
        catch (IOException e)
        {
            disconnect();

            FTPClient client = getFtpClient();
            client.setRestartOffset(restartOffset);
            return client.retrieveFileStream(relPath);
        }
    }

    public OutputStream appendFileStream(String relPath) throws IOException
    {
        try
        {
            return getFtpClient().appendFileStream(relPath);
        }
        catch (IOException e)
        {
            disconnect();
            return getFtpClient().appendFileStream(relPath);
        }
    }

    public OutputStream storeFileStream(String relPath) throws IOException
    {
        try
        {
            return getFtpClient().storeFileStream(relPath);
        }
        catch (IOException e)
        {
            disconnect();
            return getFtpClient().storeFileStream(relPath);
        }
    }

    public boolean abort() throws IOException
    {
        try
        {
            // imario@apache.org: 2005-02-14
            // it should be better to really "abort" the transfer, but
            // currently I didnt manage to make it work - so lets "abort" the hard way.
            // return getFtpClient().abort();

            disconnect();
            return true;
        }
        catch (IOException e)
        {
            disconnect();
        }
        return true;
    }

    public String getReplyString() throws IOException
    {
        return getFtpClient().getReplyString();
    }
}
