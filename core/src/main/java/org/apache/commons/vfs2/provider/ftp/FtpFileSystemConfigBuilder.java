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

import org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;

/**
 * The config builder for various ftp configuration options.
 */
public class FtpFileSystemConfigBuilder extends FileSystemConfigBuilder
{
    private static final String _PREFIX = FtpFileSystemConfigBuilder.class.getName();

    private static final FtpFileSystemConfigBuilder BUILDER = new FtpFileSystemConfigBuilder();

    private static final String CONNECT_TIMEOUT = _PREFIX + ".CONNECT_TIMEOUT";
    private static final String DATA_TIMEOUT = _PREFIX + ".DATA_TIMEOUT";
    private static final String DEFAULT_DATE_FORMAT = _PREFIX + ".DEFAULT_DATE_FORMAT";
    private static final String ENCODING = _PREFIX + ".ENCODING";
    private static final String FACTORY_KEY = FTPFileEntryParserFactory.class.getName() + ".KEY";
    private static final String FILE_TYPE = _PREFIX + ".FILE_TYPE";
    private static final String PASSIVE_MODE = _PREFIX + ".PASSIVE";

    private static final String RECENT_DATE_FORMAT = _PREFIX + ".RECENT_DATE_FORMAT";
    private static final String SERVER_LANGUAGE_CODE = _PREFIX + ".SERVER_LANGUAGE_CODE";
    private static final String SERVER_TIME_ZONE_ID = _PREFIX + ".SERVER_TIME_ZONE_ID";
    private static final String SHORT_MONTH_NAMES = _PREFIX + ".SHORT_MONTH_NAMES";
    private static final String SO_TIMEOUT = _PREFIX + ".SO_TIMEOUT";
    private static final String USER_DIR_IS_ROOT = _PREFIX + ".USER_DIR_IS_ROOT";

    /**
     * Gets the singleton instance.
     *
     * @return the singleton instance.
     */
    public static FtpFileSystemConfigBuilder getInstance()
    {
        return BUILDER;
    }

    private FtpFileSystemConfigBuilder()
    {
        super("ftp.");
    }
    
    /** @since 2.1 */
    protected FtpFileSystemConfigBuilder(String prefix) {
    	super(prefix);
    }

    @Override
    protected Class<? extends FileSystem> getConfigClass()
    {
        return FtpFileSystem.class;
    }

    /**
     * Gets the timeout in milliseconds to use for the socket connection.
     *
     * @param opts The FileSystemOptions.
     * @return The timeout in milliseconds to use for the socket connection.
     * @since 2.1
     */
    public Integer getConnectTimeout(final FileSystemOptions opts)
    {
        return getInteger(opts, CONNECT_TIMEOUT);
    }

    /**
     * @param opts The FileSystemOptions.
     * @return The encoding.
     * @since 2.0
     * */
    public String getControlEncoding(final FileSystemOptions opts)
    {
        return getString(opts, ENCODING);
    }

    /**
     * @param opts The FileSystemOptions.
     * @return The timeout for opening the data channel as an Integer.
     * @see #setDataTimeout
     */
    public Integer getDataTimeout(final FileSystemOptions opts)
    {
        return getInteger(opts, DATA_TIMEOUT);
    }

    /**
     * get the language code used by the server. see {@link org.apache.commons.net.ftp.FTPClientConfig}
     * for details and examples.
     * @param opts The FileSystemOptions
     * @return The default date format.
     */
    public String getDefaultDateFormat(final FileSystemOptions opts)
    {
        return getString(opts, DEFAULT_DATE_FORMAT);
    }

    /**
     * @param opts The FileSystemOptions.
     * @see #setEntryParser
     * @return the key to the EntryParser.
     */
    public String getEntryParser(final FileSystemOptions opts)
    {
        return getString(opts, FACTORY_KEY);
    }

    /**
     * @param opts The FlleSystemOptions.
     * @see #setEntryParserFactory
     * @return An FTPFileEntryParserFactory.
     */
    public FTPFileEntryParserFactory getEntryParserFactory(final FileSystemOptions opts)
    {
        return (FTPFileEntryParserFactory) getParam(opts, FTPFileEntryParserFactory.class.getName());
    }

    /**
     * Gets the file type parameter.
     *
     * @param opts The FileSystemOptions.
     * @return A FtpFileType
     * @since 2.1
     */
    public FtpFileType getFileType(final FileSystemOptions opts)
    {
        return getEnum(FtpFileType.class, opts, FILE_TYPE);
    }

    /**
     * @param opts The FileSystemOptions.
     * @return true if passive mode is set.
     * @see #setPassiveMode
     */
    public Boolean getPassiveMode(final FileSystemOptions opts)
    {
        return getBoolean(opts, PASSIVE_MODE);
    }

    /**
     * see {@link org.apache.commons.net.ftp.FTPClientConfig} for details and examples.
     * @param opts The FileSystemOptions.
     * @return The recent date format.
     */
    public String getRecentDateFormat(final FileSystemOptions opts)
    {
        return getString(opts, RECENT_DATE_FORMAT);
    }

    /**
     * get the language code used by the server. see {@link org.apache.commons.net.ftp.FTPClientConfig}
     * for details and examples.
     * @param opts The FilesystemOptions.
     * @return The language code of the server.
     */
    public String getServerLanguageCode(final FileSystemOptions opts)
    {
        return getString(opts, SERVER_LANGUAGE_CODE);
    }

    /**
     * see {@link org.apache.commons.net.ftp.FTPClientConfig} for details and examples.
     * @param opts The FileSystemOptions.
     * @return The server timezone id.
     */
    public String getServerTimeZoneId(final FileSystemOptions opts)
    {
        return getString(opts, SERVER_TIME_ZONE_ID);
    }

    /**
     * see {@link org.apache.commons.net.ftp.FTPClientConfig} for details and examples.
     * @param opts The FileSystemOptions.
     * @return An array of short month names.
     */
    public String[] getShortMonthNames(final FileSystemOptions opts)
    {
        return (String[]) getParam(opts, SHORT_MONTH_NAMES);
    }

    /**
     * @param opts The FileSystem options.
     * @return The timeout value.
     * @see #getDataTimeout
     * @since 2.0
     */
    public Integer getSoTimeout(final FileSystemOptions opts)
    {
        return getInteger(opts, SO_TIMEOUT);
    }

    /**
     * Returns {@link Boolean#TRUE} if VFS should treat the user directory as the root directory. Defaults to
     * <code>Boolean.TRUE</code> if the method {@link #setUserDirIsRoot(FileSystemOptions, boolean)} has not been
     * invoked.
     * 
     * @param opts
     *            The FileSystemOptions.
     * @return <code>Boolean.TRUE</code> if VFS treats the user directory as the root directory.
     * @see #setUserDirIsRoot
     */
    public Boolean getUserDirIsRoot(final FileSystemOptions opts)
    {
        return getBoolean(opts, USER_DIR_IS_ROOT, Boolean.TRUE);
    }

    /**
     * Sets the timeout for the initial control connection.
     * <p>
     * If you set the connectTimeout to {@code null} no connectTimeout will be set.
     * </p>
     *
     * @param opts The FileSystemOptions.
     * @param connectTimeout the timeout value in milliseconds
     * @since 2.1
     */
    public void setConnectTimeout(final FileSystemOptions opts, final Integer connectTimeout)
    {
        setParam(opts, CONNECT_TIMEOUT, connectTimeout);
    }

    /**
     * see {@link org.apache.commons.net.ftp.FTP#setControlEncoding} for details and examples.
     * @param opts The FileSystemOptions.
     * @param encoding the encoding to use
     * @since 2.0
     */
    public void setControlEncoding(final FileSystemOptions opts, final String encoding)
    {
        setParam(opts, ENCODING, encoding);
    }

    /**
     * set the data timeout for the ftp client.<br />
     * If you set the dataTimeout to {@code null} no dataTimeout will be set on the
     * ftp client.
     *
     * @param opts The FileSystemOptions.
     * @param dataTimeout The timeout value.
     */
    public void setDataTimeout(final FileSystemOptions opts, final Integer dataTimeout)
    {
        setParam(opts, DATA_TIMEOUT, dataTimeout);
    }

    /**
     * set the language code used by the server. see {@link org.apache.commons.net.ftp.FTPClientConfig}
     * for details and examples.
     * @param opts The FileSystemOptions.
     * @param defaultDateFormat The default date format.
     */
    public void setDefaultDateFormat(final FileSystemOptions opts, final String defaultDateFormat)
    {
        setParam(opts, DEFAULT_DATE_FORMAT, defaultDateFormat);
    }

    /**
     * set the FQCN of your FileEntryParser used to parse the directory listing from your server.<br />
     * <br />
     * <i>If you do not use the default commons-net FTPFileEntryParserFactory e.g. by using
     * {@link #setEntryParserFactory}
     * this is the "key" parameter passed as argument into your custom factory</i>
     *
     * @param opts The FileSystemOptions.
     * @param key The key.
     */
    public void setEntryParser(final FileSystemOptions opts, final String key)
    {
        setParam(opts, FACTORY_KEY, key);
    }

    /**
     * FTPFileEntryParserFactory which will be used for ftp-entry parsing.
     *
     * @param opts The FileSystemOptions.
     * @param factory instance of your factory
     */
    public void setEntryParserFactory(final FileSystemOptions opts, final FTPFileEntryParserFactory factory)
    {
        setParam(opts, FTPFileEntryParserFactory.class.getName(), factory);
    }

    /**
     * Sets the file type parameter.
     *
     * @param opts The FileSystemOptions.
     * @param ftpFileType A FtpFileType
     * @since 2.1
     */
    public void setFileType(final FileSystemOptions opts, final FtpFileType ftpFileType)
    {
        setParam(opts, FILE_TYPE, ftpFileType);
    }

    /**
     * enter into passive mode.
     *
     * @param opts The FileSystemOptions.
     * @param passiveMode true if passive mode should be used.
     */
    public void setPassiveMode(final FileSystemOptions opts, final boolean passiveMode)
    {
        setParam(opts, PASSIVE_MODE, passiveMode ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * see {@link org.apache.commons.net.ftp.FTPClientConfig} for details and examples.
     * @param opts The FileSystemOptions.
     * @param recentDateFormat The recent date format.
     */
    public void setRecentDateFormat(final FileSystemOptions opts, final String recentDateFormat)
    {
        setParam(opts, RECENT_DATE_FORMAT, recentDateFormat);
    }

    /**
     * set the language code used by the server. see {@link org.apache.commons.net.ftp.FTPClientConfig}
     * for details and examples.
     * @param opts The FileSystemOptions.
     * @param serverLanguageCode The servers language code.
     */
    public void setServerLanguageCode(final FileSystemOptions opts, final String serverLanguageCode)
    {
        setParam(opts, SERVER_LANGUAGE_CODE, serverLanguageCode);
    }

    /**
     * see {@link org.apache.commons.net.ftp.FTPClientConfig} for details and examples.
     * @param opts The FileSystemOptions.
     * @param serverTimeZoneId The server timezone id.
     */
    public void setServerTimeZoneId(final FileSystemOptions opts, final String serverTimeZoneId)
    {
        setParam(opts, SERVER_TIME_ZONE_ID, serverTimeZoneId);
    }

    /**
     * see {@link org.apache.commons.net.ftp.FTPClientConfig} for details and examples.
     * @param opts The FileSystemOptions.
     * @param shortMonthNames an array of short month name Strings.
     */
    public void setShortMonthNames(final FileSystemOptions opts, final String[] shortMonthNames)
    {
        String[] clone = null;
        if (shortMonthNames != null)
        {
            clone = new String[shortMonthNames.length];
            System.arraycopy(shortMonthNames, 0, clone, 0, shortMonthNames.length);
        }

        setParam(opts, SHORT_MONTH_NAMES, clone);
    }

    /**
     * Sets the socket timeout for the FTP client.<br />
     * If you set the socketTimeout to {@code null} no socketTimeout will be set on the
     * ftp client.
     *
     * @param opts The FileSystem options.
     * @param soTimeout The timeout value.
     * @since 2.0
     */
    public void setSoTimeout(final FileSystemOptions opts, final Integer soTimeout)
    {
        setParam(opts, SO_TIMEOUT, soTimeout);
    }

    /**
     * use user directory as root (do not change to fs root).
     *
     * @param opts The FileSystemOptions.
     * @param userDirIsRoot true if the user directory should be treated as the root.
     */
    public void setUserDirIsRoot(final FileSystemOptions opts, final boolean userDirIsRoot)
    {
        setParam(opts, USER_DIR_IS_ROOT, userDirIsRoot ? Boolean.TRUE : Boolean.FALSE);
    }

}
