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

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;

/**
 * Create FTPSClient instances.
 *
 * @since 2.0
 */
public final class FtpsClientFactory
{
    private static final char[] ANON_CHAR_ARRAY = "anonymous".toCharArray();
    private static final int SHORT_MONTH_NAME_LEN = 40;

    /**
     * Creates a new connection to the server.
     * @param hostname The host name.
     * @param port The port.
     * @param username The user name for authentication.
     * @param password The user's password.
     * @param workingDirectory The directory to use.
     * @param fileSystemOptions The FileSystemOptions.
     * @return The FTPSClient.
     * @throws FileSystemException if an error occurs.
     */
    public static FTPSClient createConnection(final String hostname, final int port, char[] username, char[] password,
            final String workingDirectory, final FileSystemOptions fileSystemOptions) throws FileSystemException
    {
        // Determine the username and password to use
        if (username == null)
        {
            username = ANON_CHAR_ARRAY;
        }

        if (password == null)
        {
            password = ANON_CHAR_ARRAY;
        }

        try
        {

            final FTPSClient client = createFTPSClient(fileSystemOptions);

            final String key = FtpsFileSystemConfigBuilder.getInstance().getEntryParser(fileSystemOptions);
            if (key != null)
            {
                final FTPClientConfig config = createFTPClientConfig(fileSystemOptions, key);
                client.configure(config);
            }

            final FTPFileEntryParserFactory myFactory = FtpsFileSystemConfigBuilder.getInstance().getEntryParserFactory(
                    fileSystemOptions);
            if (myFactory != null)
            {
                client.setParserFactory(myFactory);
            }

            try
            {
                client.connect(hostname, port);

                // For VFS-412
                // String execPROT = FtpsFileSystemConfigBuilder.getInstance().getDataChannelProtectionLevel(
                // fileSystemOptions);
                // if (execPROT != null)
                // {
                // client.execPROT(execPROT);
                // }

                final int reply = client.getReplyCode();
                if (!FTPReply.isPositiveCompletion(reply))
                {
                    throw new FileSystemException("vfs.provider.ftp/connect-rejected.error", hostname);
                }

                // Login
                if (!client.login(UserAuthenticatorUtils.toString(username), UserAuthenticatorUtils.toString(password)))
                {
                    throw new FileSystemException("vfs.provider.ftp/login.error", hostname,
                            UserAuthenticatorUtils.toString(username));
                }

                // Set binary mode
                if (!client.setFileType(FTP.BINARY_FILE_TYPE))
                {
                    throw new FileSystemException("vfs.provider.ftp/set-binary.error", hostname);
                }

                // Set dataTimeout value
                final Integer dataTimeout = FtpsFileSystemConfigBuilder.getInstance().getDataTimeout(fileSystemOptions);
                if (dataTimeout != null)
                {
                    client.setDataTimeout(dataTimeout.intValue());
                }

                // Change to root by default
                // All file operations a relative to the filesystem-root
                // String root = getRoot().getName().getPath();

                final Boolean userDirIsRoot = FtpsFileSystemConfigBuilder.getInstance().getUserDirIsRoot(fileSystemOptions);
                if (workingDirectory != null && (userDirIsRoot == null || !userDirIsRoot.booleanValue()))
                {
                    if (!client.changeWorkingDirectory(workingDirectory))
                    {
                        throw new FileSystemException("vfs.provider.ftp/change-work-directory.error", workingDirectory);
                    }
                }

                final Boolean passiveMode = FtpsFileSystemConfigBuilder.getInstance().getPassiveMode(fileSystemOptions);
                if (passiveMode != null && passiveMode.booleanValue())
                {
                    client.enterLocalPassiveMode();
                }
            }
            catch (final IOException e)
            {
                if (client.isConnected())
                {
                    client.disconnect();
                }
                throw e;
            }

            return client;
        }
        catch (final Exception exc)
        {
            throw new FileSystemException("vfs.provider.sftp/connect.error", exc, hostname);
        }
    }

    private static FTPClientConfig createFTPClientConfig(final FileSystemOptions fileSystemOptions, final String key)
    {
        final FTPClientConfig config = new FTPClientConfig(key);

        final String serverLanguageCode = FtpsFileSystemConfigBuilder.getInstance().getServerLanguageCode(
                fileSystemOptions);
        if (serverLanguageCode != null)
        {
            config.setServerLanguageCode(serverLanguageCode);
        }
        final String defaultDateFormat = FtpsFileSystemConfigBuilder.getInstance().getDefaultDateFormat(
                fileSystemOptions);
        if (defaultDateFormat != null)
        {
            config.setDefaultDateFormatStr(defaultDateFormat);
        }
        final String recentDateFormat = FtpsFileSystemConfigBuilder.getInstance().getRecentDateFormat(
                fileSystemOptions);
        if (recentDateFormat != null)
        {
            config.setRecentDateFormatStr(recentDateFormat);
        }
        final String serverTimeZoneId = FtpsFileSystemConfigBuilder.getInstance().getServerTimeZoneId(
                fileSystemOptions);
        if (serverTimeZoneId != null)
        {
            config.setServerTimeZoneId(serverTimeZoneId);
        }
        final String[] shortMonthNames = FtpsFileSystemConfigBuilder.getInstance().getShortMonthNames(
                fileSystemOptions);
        if (shortMonthNames != null)
        {
            final StringBuilder shortMonthNamesStr = new StringBuilder(SHORT_MONTH_NAME_LEN);
            for (final String shortMonthName : shortMonthNames)
            {
                if (shortMonthNamesStr.length() > 0)
                {
                    shortMonthNamesStr.append("|");
                }
                shortMonthNamesStr.append(shortMonthName);
            }
            config.setShortMonthNames(shortMonthNamesStr.toString());
        }
        return config;
    }

    private static FTPSClient createFTPSClient(final FileSystemOptions fileSystemOptions)
            throws FileSystemException
    {
        if (FtpsFileSystemConfigBuilder.getInstance().getFtpsType(fileSystemOptions)
                .equals(FtpsFileSystemConfigBuilder.FTPS_TYPE_EXPLICIT))
        {
            return new FTPSClient();
        }
        else if (FtpsFileSystemConfigBuilder.getInstance().getFtpsType(fileSystemOptions)
                .equals(FtpsFileSystemConfigBuilder.FTPS_TYPE_IMPLICIT))
        {
            return new FTPSClient(true);
        }
        else
        {
            throw new FileSystemException("Invalid FTPS type of "
                    + FtpsFileSystemConfigBuilder.getInstance().getFtpsType(fileSystemOptions)
                    + " specified. Must be 'implicit' or 'explicit'");
        }
    }

    private FtpsClientFactory()
    {
    }
    }
