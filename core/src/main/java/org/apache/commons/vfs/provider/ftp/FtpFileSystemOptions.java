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
package org.apache.commons.vfs.provider.ftp;

import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.DefaultFileSystemOptions;
import org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory;

/**
 * FTP File System Options
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 */
public class FtpFileSystemOptions extends DefaultFileSystemOptions
{
    // Why are these keys prefixed with a class name? FileSystemOptionsKey does that automatically.
    private static final String FACTORY_KEY = FTPFileEntryParserFactory.class.getName() + ".KEY";
    private static final String PASSIVE_MODE = FtpFileSystemConfigBuilder.class.getName() + ".PASSIVE";
    private static final String USER_DIR_IS_ROOT = FtpFileSystemConfigBuilder.class.getName() + ".USER_DIR_IS_ROOT";
    private static final String DATA_TIMEOUT = FtpFileSystemConfigBuilder.class.getName() + ".DATA_TIMEOUT";

    private static final String SERVER_LANGUAGE_CODE =
            FtpFileSystemConfigBuilder.class.getName() + ".SERVER_LANGUAGE_CODE";
    private static final String DEFAULT_DATE_FORMAT =
            FtpFileSystemConfigBuilder.class.getName() + ".DEFAULT_DATE_FORMAT";
    private static final String RECENT_DATE_FORMAT =
            FtpFileSystemConfigBuilder.class.getName() + ".RECENT_DATE_FORMAT";
    private static final String SERVER_TIME_ZONE_ID =
            FtpFileSystemConfigBuilder.class.getName() + ".SERVER_TIME_ZONE_ID";
    private static final String SHORT_MONTH_NAMES =
            FtpFileSystemConfigBuilder.class.getName() + ".SHORT_MONTH_NAMES";

    public FtpFileSystemOptions()
    {
        this("ftp.");
    }

    protected FtpFileSystemOptions(String scheme)
    {
        super(scheme);
    }

    public static FtpFileSystemOptions getInstance(FileSystemOptions opts)
    {
        return FileSystemOptions.makeSpecific(FtpFileSystemOptions.class, opts);
    }
        /**
     * FTPFileEntryParserFactory which will be used for ftp-entry parsing.
     *
     * @param factory instance of your factory
     */
    public void setEntryParserFactory(FTPFileEntryParserFactory factory)
    {
        setParam(FTPFileEntryParserFactory.class.getName(), factory);
    }

    /**
     * @see #setEntryParserFactory
     * @return An FTPFileEntryParserFactory.
     */
    public FTPFileEntryParserFactory getEntryParserFactory()
    {
        return (FTPFileEntryParserFactory) getParam(FTPFileEntryParserFactory.class.getName());
    }

    /**
     * set the FQCN of your FileEntryParser used to parse the directory listing from your server.<br />
     * <br />
     * <i>If you do not use the default commons-net FTPFileEntryParserFactory e.g. by using
     * {@link #setEntryParserFactory}
     * this is the "key" parameter passed as argument into your custom factory</i>
     *
     * @param key The key.
     */
    public void setEntryParser(String key)
    {
        setParam(FACTORY_KEY, key);
    }

    /**
     * @see #setEntryParser
     * @return the key to the EntryParser.
     */
    public String getEntryParser()
    {
        return getString(FACTORY_KEY);
    }

    /**
     * enter into passive mode.
     *
     * @param passiveMode true if passive mode should be used.
     */
    public void setPassiveMode(boolean passiveMode)
    {
        setParam(PASSIVE_MODE, passiveMode ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * @return true if passive mode is set.
     * @see #setPassiveMode
     */
    public Boolean getPassiveMode()
    {
        return getBoolean(PASSIVE_MODE);
    }

    /**
     * use user directory as root (do not change to fs root).
     *
     * @param userDirIsRoot true if the user directory should be treated as the root.
     */
    public void setUserDirIsRoot(boolean userDirIsRoot)
    {
        setParam(USER_DIR_IS_ROOT, userDirIsRoot ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * @return true if the user directory is treated as the root.
     * @see #setUserDirIsRoot
     */
    public Boolean getUserDirIsRoot()
    {
        return getBoolean(USER_DIR_IS_ROOT);
    }

    /**
     * @return The timeout as an Integer.
     * @see #setDataTimeout
     */
    public Integer getDataTimeout()
    {
        return getInteger(DATA_TIMEOUT);
    }

    /**
     * set the data timeout for the ftp client.<br />
     * If you set the dataTimeout to <code>null</code> no dataTimeout will be set on the
     * ftp client.
     *
     * @param dataTimeout The timeout value.
     */
    public void setDataTimeout(Integer dataTimeout)
    {
        setParam(DATA_TIMEOUT, dataTimeout);
    }

    /**
     * get the language code used by the server. see {@link org.apache.commons.net.ftp.FTPClientConfig}
     * for details and examples.
     * @return The language code of the server.
     */
    public String getServerLanguageCode()
    {
        return getString(SERVER_LANGUAGE_CODE);
    }

    /**
     * set the language code used by the server. see {@link org.apache.commons.net.ftp.FTPClientConfig}
     * for details and examples.
     * @param serverLanguageCode The servers language code.
     */
    public void setServerLanguageCode(String serverLanguageCode)
    {
        setParam(SERVER_LANGUAGE_CODE, serverLanguageCode);
    }

    /**
     * get the language code used by the server. see {@link org.apache.commons.net.ftp.FTPClientConfig}
     * for details and examples.
     * @return The default date format.
     */
    public String getDefaultDateFormat()
    {
        return getString(DEFAULT_DATE_FORMAT);
    }

    /**
     * set the language code used by the server. see {@link org.apache.commons.net.ftp.FTPClientConfig}
     * for details and examples.
     * @param defaultDateFormat The default date format.
     */
    public void setDefaultDateFormat(String defaultDateFormat)
    {
        setParam(DEFAULT_DATE_FORMAT, defaultDateFormat);
    }

    /**
     * see {@link org.apache.commons.net.ftp.FTPClientConfig} for details and examples.
     * @return The recent date format.
     */
    public String getRecentDateFormat()
    {
        return getString(RECENT_DATE_FORMAT);
    }

    /**
     * see {@link org.apache.commons.net.ftp.FTPClientConfig} for details and examples.
     * @param recentDateFormat The recent date format.
     */
    public void setRecentDateFormat(String recentDateFormat)
    {
        setParam(RECENT_DATE_FORMAT, recentDateFormat);
    }

    /**
     * see {@link org.apache.commons.net.ftp.FTPClientConfig} for details and examples.
     * @return The server timezone id.
     */
    public String getServerTimeZoneId()
    {
        return getString(SERVER_TIME_ZONE_ID);
    }

    /**
     * see {@link org.apache.commons.net.ftp.FTPClientConfig} for details and examples.
     * @param serverTimeZoneId The server timezone id.
     */
    public void setServerTimeZoneId(String serverTimeZoneId)
    {
        setParam(SERVER_TIME_ZONE_ID, serverTimeZoneId);
    }

    /**
     * see {@link org.apache.commons.net.ftp.FTPClientConfig} for details and examples.
     * @return An array of short month names.
     */
    public String[] getShortMonthNames()
    {
        return (String[]) getParam(SHORT_MONTH_NAMES);
    }

    /**
     * see {@link org.apache.commons.net.ftp.FTPClientConfig} for details and examples.
     * @param shortMonthNames an array of short month name Strings.
     */
    public void setShortMonthNames(String[] shortMonthNames)
    {
        String[] clone = null;
        if (shortMonthNames != null)
        {
            clone = new String[shortMonthNames.length];
            System.arraycopy(shortMonthNames, 0, clone, 0, shortMonthNames.length);
        }

        setParam(SHORT_MONTH_NAMES, clone);
    }
}
