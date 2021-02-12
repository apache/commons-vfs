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

import java.net.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemOptions;

/**
 * The config builder for various FTP configuration options.
 */
public class FtpFileSystemConfigBuilder extends FileSystemConfigBuilder {

    private static final String _PREFIX = FtpFileSystemConfigBuilder.class.getName();

    private static final FtpFileSystemConfigBuilder BUILDER = new FtpFileSystemConfigBuilder();

    private static final String AUTODETECT_UTF8 = _PREFIX + ".AUTODETECT_UTF8";
    private static final String CONNECT_TIMEOUT = _PREFIX + ".CONNECT_TIMEOUT";
    private static final String DATA_TIMEOUT = _PREFIX + ".DATA_TIMEOUT";
    private static final String DEFAULT_DATE_FORMAT = _PREFIX + ".DEFAULT_DATE_FORMAT";
    private static final String ENCODING = _PREFIX + ".ENCODING";
    private static final String FACTORY_KEY = FTPFileEntryParserFactory.class.getName() + ".KEY";
    private static final String FILE_TYPE = _PREFIX + ".FILE_TYPE";
    private static final String PASSIVE_MODE = _PREFIX + ".PASSIVE";
    private static final String PROXY = _PREFIX + ".PROXY";
    private static final String RECENT_DATE_FORMAT = _PREFIX + ".RECENT_DATE_FORMAT";
    private static final String REMOTE_VERIFICATION = _PREFIX + ".REMOTE_VERIFICATION";
    private static final String SERVER_LANGUAGE_CODE = _PREFIX + ".SERVER_LANGUAGE_CODE";
    private static final String SERVER_TIME_ZONE_ID = _PREFIX + ".SERVER_TIME_ZONE_ID";
    private static final String SHORT_MONTH_NAMES = _PREFIX + ".SHORT_MONTH_NAMES";
    private static final String SO_TIMEOUT = _PREFIX + ".SO_TIMEOUT";
    private static final String CONTROL_KEEP_ALIVE_TIMEOUT = _PREFIX + ".CONTROL_KEEP_ALIVE_TIMEOUT";
    private static final String CONTROL_KEEP_ALIVE_REPLY_TIMEOUT = _PREFIX + ".CONTROL_KEEP_ALIVE_REPLY_TIMEOUT";
    private static final String USER_DIR_IS_ROOT = _PREFIX + ".USER_DIR_IS_ROOT";
    private static final String TRANSFER_ABORTED_OK_REPLY_CODES = _PREFIX + ".TRANSFER_ABORTED_OK_REPLY_CODES";
    private static final String MDTM_LAST_MODIFED_TIME = _PREFIX + ".MDTM_LAST_MODIFED_TIME";

    /**
     * Gets the singleton instance.
     *
     * @return the singleton instance.
     */
    public static FtpFileSystemConfigBuilder getInstance() {
        return BUILDER;
    }

    public static List<Integer> getSaneTransferAbortedOkReplyCodes() {
        // See VFS-674, its accompanying PR and https://github.com/apache/commons-vfs/pull/51 as to why 426 and 550 are here
        return new ArrayList<>(Arrays.asList(FTPReply.TRANSFER_ABORTED, FTPReply.FILE_UNAVAILABLE));
    }

    private FtpFileSystemConfigBuilder() {
        super("ftp.");
    }

    /**
     * Create new config builder with specified prefix string.
     *
     * @param prefix prefix string to use for parameters of this config builder.
     * @since 2.1
     */
    protected FtpFileSystemConfigBuilder(final String prefix) {
        super(prefix);
    }

    /**
     * Gets whether to try to autodetect the server encoding (only UTF8 is supported).
     *
     * @param options The FileSystemOptions.
     * @return True if autodetection should be done.
     * @since 2.4
     */
    public Boolean getAutodetectUtf8(final FileSystemOptions options) {
        return getBoolean(options, AUTODETECT_UTF8);
    }

    @Override
    protected Class<? extends FileSystem> getConfigClass() {
        return FtpFileSystem.class;
    }

    /**
     * Gets the timeout in milliseconds to use for the socket connection.
     *
     * @param options The FileSystemOptions.
     * @return The timeout in milliseconds to use for the socket connection.
     * @since 2.1
     * @deprecated Use {@link #getConnectTimeoutDuration(FileSystemOptions)}.
     */
    @Deprecated
    public Integer getConnectTimeout(final FileSystemOptions options) {
        return getDurationInteger(options, CONNECT_TIMEOUT);
    }

    /**
     * Gets the timeout in milliseconds to use for the socket connection.
     *
     * @param options The FileSystemOptions.
     * @return The timeout in milliseconds to use for the socket connection.
     * @since 2.8.0
     */
    public Duration getConnectTimeoutDuration(final FileSystemOptions options) {
        return getDuration(options, CONNECT_TIMEOUT);
    }

    /**
     * @param options The FileSystemOptions.
     * @return The encoding.
     * @since 2.0
     */
    public String getControlEncoding(final FileSystemOptions options) {
        return getString(options, ENCODING);
    }

    /**
     * @param options The FileSystem options
     * @return The controlKeepAliveReplyTimeout value.
     * @since 2.8.0
     */
    public Duration getControlKeepAliveReplyTimeout(final FileSystemOptions options) {
        return getDuration(options, CONTROL_KEEP_ALIVE_REPLY_TIMEOUT);
    }

    /**
     * @param options The FileSystem options
     * @return The controlKeepAliveTimeout value.
     * @since 2.8.0
     */
    public Duration getControlKeepAliveTimeout(final FileSystemOptions options) {
        return getDuration(options, CONTROL_KEEP_ALIVE_TIMEOUT);
    }

    /**
     * @param options The FileSystemOptions.
     * @return The timeout for opening the data channel in milliseconds.
     * @see #setDataTimeout
     * @deprecated Use {@link #getDataTimeoutDuration(FileSystemOptions)}.
     */
    @Deprecated
    public Integer getDataTimeout(final FileSystemOptions options) {
        return getDurationInteger(options, DATA_TIMEOUT);
    }

    /**
     * Gets the timeout for opening the data channel.
     *
     * @param options The FileSystemOptions.
     * @return The timeout for opening the data channel.
     * @see #setDataTimeout
     * @since 2.8.0
     */
    public Duration getDataTimeoutDuration(final FileSystemOptions options) {
        return getDuration(options, DATA_TIMEOUT);
    }

    /**
     * Get the default date format used by the server. See {@link org.apache.commons.net.ftp.FTPClientConfig} for
     * details and examples.
     *
     * @param options The FileSystemOptions
     * @return The default date format.
     */
    public String getDefaultDateFormat(final FileSystemOptions options) {
        return getString(options, DEFAULT_DATE_FORMAT);
    }

    /**
     * @param options The FileSystemOptions.
     * @see #setEntryParser
     * @return the key to the EntryParser.
     */
    public String getEntryParser(final FileSystemOptions options) {
        return getString(options, FACTORY_KEY);
    }

    /**
     * @param options The FlleSystemOptions.
     * @see #setEntryParserFactory
     * @return An FTPFileEntryParserFactory.
     */
    public FTPFileEntryParserFactory getEntryParserFactory(final FileSystemOptions options) {
        return getParam(options, FTPFileEntryParserFactory.class.getName());
    }

    /**
     * Gets the file type parameter.
     *
     * @param options The FileSystemOptions.
     * @return A FtpFileType
     * @since 2.1
     */
    public FtpFileType getFileType(final FileSystemOptions options) {
        return getEnum(FtpFileType.class, options, FILE_TYPE);
    }

    /**
     * Gets the option to use FTP MDTM for {@link FileContent#getLastModifiedTime()}.
     *
     * @param options The FileSystemOptions.
     * @return true if MDTM should be used.
     * @since 2.8.0
     */
    public Boolean getMdtmLastModifiedTime(final FileSystemOptions options) {
        return getBoolean(options, MDTM_LAST_MODIFED_TIME);
    }

    /**
     * @param options The FileSystemOptions.
     * @return true if passive mode is set.
     * @see #setPassiveMode
     */
    public Boolean getPassiveMode(final FileSystemOptions options) {
        return getBoolean(options, PASSIVE_MODE);
    }

    /**
     * Gets the Proxy.
     *
     * @param options The FileSystemOptions.
     * @return the Proxy
     * @since 2.1
     */
    public Proxy getProxy(final FileSystemOptions options) {
        return getParam(options, PROXY);
    }

    /**
     * See {@link org.apache.commons.net.ftp.FTPClientConfig} for details and examples.
     *
     * @param options The FileSystemOptions.
     * @return The recent date format.
     */
    public String getRecentDateFormat(final FileSystemOptions options) {
        return getString(options, RECENT_DATE_FORMAT);
    }

    /**
     * Gets whether to use remote verification.
     *
     * @param options The FileSystemOptions.
     * @return True if remote verification should be done.
     */
    public Boolean getRemoteVerification(final FileSystemOptions options) {
        return getBoolean(options, REMOTE_VERIFICATION);
    }

    /**
     * Get the language code used by the server. See {@link org.apache.commons.net.ftp.FTPClientConfig} for details and
     * examples.
     *
     * @param options The FilesystemOptions.
     * @return The language code of the server.
     */
    public String getServerLanguageCode(final FileSystemOptions options) {
        return getString(options, SERVER_LANGUAGE_CODE);
    }

    /**
     * See {@link org.apache.commons.net.ftp.FTPClientConfig} for details and examples.
     *
     * @param options The FileSystemOptions.
     * @return The server timezone id.
     */
    public String getServerTimeZoneId(final FileSystemOptions options) {
        return getString(options, SERVER_TIME_ZONE_ID);
    }

    /**
     * See {@link org.apache.commons.net.ftp.FTPClientConfig} for details and examples.
     *
     * @param options The FileSystemOptions.
     * @return An array of short month names.
     */
    public String[] getShortMonthNames(final FileSystemOptions options) {
        return getParam(options, SHORT_MONTH_NAMES);
    }

    /**
     * Gets The so timeout duration in milliseconds.
     *
     * @param options The FileSystem options.
     * @return The so timeout duration in milliseconds.
     * @see #getDataTimeout
     * @since 2.0
     * @deprecated Use {@link #getSoTimeoutDuration(FileSystemOptions)}.
     */
    @Deprecated
    public Integer getSoTimeout(final FileSystemOptions options) {
        return getDurationInteger(options, SO_TIMEOUT);
    }

    /**
     * Gets The so timeout duration.
     *
     * @param options The FileSystem options.
     * @return The timeout value in milliseconds.
     * @see #getDataTimeout
     * @since 2.8.0
     */
    public Duration getSoTimeoutDuration(final FileSystemOptions options) {
        return getDuration(options, SO_TIMEOUT);
    }

    /**
     * @param options The FileSystem options.
     * @return The list of reply codes (apart from 200) that are considered as OK when prematurely
     * closing a stream.
     * @since 2.4
     */
    public List<Integer> getTransferAbortedOkReplyCodes(final FileSystemOptions options) {
        return getParam(options, TRANSFER_ABORTED_OK_REPLY_CODES);
    }

    /**
     * Returns {@link Boolean#TRUE} if VFS should treat the user directory as the root directory. Defaults to
     * {@code Boolean.TRUE} if the method {@link #setUserDirIsRoot(FileSystemOptions, boolean)} has not been
     * invoked.
     *
     * @param options The FileSystemOptions.
     * @return {@code Boolean.TRUE} if VFS treats the user directory as the root directory.
     * @see #setUserDirIsRoot
     */
    public Boolean getUserDirIsRoot(final FileSystemOptions options) {
        return getBoolean(options, USER_DIR_IS_ROOT, Boolean.TRUE);
    }

    /**
     * Sets whether to try to autodetect the server encoding (only UTF8 is supported).
     *
     * @param options The FileSystemOptions.
     * @param autodetectUTF8 true if autodetection should be done.
     * @since 2.4
     */
    public void setAutodetectUtf8(final FileSystemOptions options, final Boolean autodetectUTF8) {
        setParam(options, AUTODETECT_UTF8, autodetectUTF8);
    }

    /**
     * Sets the timeout for the initial control connection.
     * <p>
     * If you set the connectTimeout to {@code null} no connectTimeout will be set.
     * </p>
     *
     * @param options The FileSystemOptions.
     * @param duration the timeout duration in milliseconds
     * @since 2.8.0
     */
    public void setConnectTimeout(final FileSystemOptions options, final Duration duration) {
        setParam(options, CONNECT_TIMEOUT, duration);
    }

    /**
     * Sets the timeout for the initial control connection.
     * <p>
     * If you set the connectTimeout to {@code null} no connectTimeout will be set.
     * </p>
     *
     * @param options The FileSystemOptions.
     * @param duration the timeout duration.
     * @since 2.1
     * @deprecated Use {@link #setConnectTimeout(FileSystemOptions, Duration)}.
     */
    @Deprecated
    public void setConnectTimeout(final FileSystemOptions options, final Integer duration) {
        setConnectTimeout(options, Duration.ofMillis(duration));
    }

    /**
     * See {@link org.apache.commons.net.ftp.FTP#setControlEncoding} for details and examples.
     *
     * @param options The FileSystemOptions.
     * @param encoding the encoding to use
     * @since 2.0
     */
    public void setControlEncoding(final FileSystemOptions options, final String encoding) {
        setParam(options, ENCODING, encoding);
    }

    /**
     * Sets the control keep alive reply timeout for the FTP client.
     *
     * @param options The FileSystem options.
     * @param duration timeout duration.
     * @since 2.8.0
     */
    public void setControlKeepAliveReplyTimeout(final FileSystemOptions options, final Duration duration) {
        setParam(options, CONTROL_KEEP_ALIVE_REPLY_TIMEOUT, duration);
    }

    /**
     * Sets the control keep alive timeout for the FTP client.
     * <p>
     * Set the {@code controlKeepAliveTimeout} to ensure the socket be alive after download huge file.
     * </p>
     *
     * @param options The FileSystem options.
     * @param duration The timeout duration.
     * @since 2.8.0
     */
    public void setControlKeepAliveTimeout(final FileSystemOptions options, final Duration duration) {
        setParam(options, CONTROL_KEEP_ALIVE_TIMEOUT, duration);
    }

    /**
     * Set the data timeout for the FTP client.
     * <p>
     * If you set the {@code dataTimeout} to {@code null}, no dataTimeout will be set on the FTP client.
     * </p>
     *
     * @param options The FileSystemOptions.
     * @param duration The timeout duration.
     * @since 2.8.0
     */
    public void setDataTimeout(final FileSystemOptions options, final Duration duration) {
        setParam(options, DATA_TIMEOUT, duration);
    }

    /**
     * Set the data timeout for the FTP client.
     * <p>
     * If you set the {@code dataTimeout} to {@code null}, no dataTimeout will be set on the FTP client.
     * </p>
     *
     * @param options The FileSystemOptions.
     * @param duration The timeout value.
     * @deprecated Use {@link #setDataTimeout(FileSystemOptions, Duration)}.
     */
    @Deprecated
    public void setDataTimeout(final FileSystemOptions options, final Integer duration) {
        setDataTimeout(options, Duration.ofMillis(duration));
    }

    /**
     * Set the default date format used by the server. See {@link org.apache.commons.net.ftp.FTPClientConfig} for
     * details and examples.
     *
     * @param options The FileSystemOptions.
     * @param defaultDateFormat The default date format.
     */
    public void setDefaultDateFormat(final FileSystemOptions options, final String defaultDateFormat) {
        setParam(options, DEFAULT_DATE_FORMAT, defaultDateFormat);
    }

    /**
     * Set the FQCN of your FileEntryParser used to parse the directory listing from your server.
     * <p>
     * If you do not use the default commons-net FTPFileEntryParserFactory e.g. by using {@link #setEntryParserFactory}
     * this is the "key" parameter passed as argument into your custom factory.
     * </p>
     *
     * @param options The FileSystemOptions.
     * @param key The key.
     */
    public void setEntryParser(final FileSystemOptions options, final String key) {
        setParam(options, FACTORY_KEY, key);
    }

    /**
     * FTPFileEntryParserFactory which will be used for ftp-entry parsing.
     *
     * @param options The FileSystemOptions.
     * @param factory instance of your factory
     */
    public void setEntryParserFactory(final FileSystemOptions options, final FTPFileEntryParserFactory factory) {
        setParam(options, FTPFileEntryParserFactory.class.getName(), factory);
    }

    /**
     * Sets the file type parameter.
     *
     * @param options The FileSystemOptions.
     * @param ftpFileType A FtpFileType
     * @since 2.1
     */
    public void setFileType(final FileSystemOptions options, final FtpFileType ftpFileType) {
        setParam(options, FILE_TYPE, ftpFileType);
    }

    /**
     * Sets the option to use FTP MDTM for {@link FileContent#getLastModifiedTime()}.
     *
     * @param options The FileSystemOptions.
     * @param mdtm true if MDTM should be used.
     * @since 2.8.0
     */
    public void setMdtmLastModifiedTime(final FileSystemOptions options, final boolean mdtm) {
        setParam(options, MDTM_LAST_MODIFED_TIME, toBooleanObject(mdtm));
    }

    /**
     * Enter into passive mode.
     *
     * @param options The FileSystemOptions.
     * @param passiveMode true if passive mode should be used.
     */
    public void setPassiveMode(final FileSystemOptions options, final boolean passiveMode) {
        setParam(options, PASSIVE_MODE, toBooleanObject(passiveMode));
    }

    /**
     * Sets the Proxy.
     * <p>
     * You might need to make sure that {@link #setPassiveMode(FileSystemOptions, boolean) passive mode} is activated.
     * </p>
     *
     * @param options the FileSystem options.
     * @param proxy the Proxy
     * @since 2.1
     */
    public void setProxy(final FileSystemOptions options, final Proxy proxy) {
        setParam(options, PROXY, proxy);
    }

    /**
     * See {@link org.apache.commons.net.ftp.FTPClientConfig} for details and examples.
     *
     * @param options The FileSystemOptions.
     * @param recentDateFormat The recent date format.
     */
    public void setRecentDateFormat(final FileSystemOptions options, final String recentDateFormat) {
        setParam(options, RECENT_DATE_FORMAT, recentDateFormat);
    }

    /**
     * Sets whether to use remote verification.
     *
     * @param options The FileSystemOptions.
     * @param remoteVerification True if verification should be done.
     */
    public void setRemoteVerification(final FileSystemOptions options, final boolean remoteVerification) {
        setParam(options, REMOTE_VERIFICATION, remoteVerification);
    }

    /**
     * Set the language code used by the server. See {@link org.apache.commons.net.ftp.FTPClientConfig} for details and
     * examples.
     *
     * @param options The FileSystemOptions.
     * @param serverLanguageCode The servers language code.
     */
    public void setServerLanguageCode(final FileSystemOptions options, final String serverLanguageCode) {
        setParam(options, SERVER_LANGUAGE_CODE, serverLanguageCode);
    }

    /**
     * See {@link org.apache.commons.net.ftp.FTPClientConfig} for details and examples.
     *
     * @param options The FileSystemOptions.
     * @param serverTimeZoneId The server timezone id.
     */
    public void setServerTimeZoneId(final FileSystemOptions options, final String serverTimeZoneId) {
        setParam(options, SERVER_TIME_ZONE_ID, serverTimeZoneId);
    }

    /**
     * See {@link org.apache.commons.net.ftp.FTPClientConfig} for details and examples.
     *
     * @param options The FileSystemOptions.
     * @param shortMonthNames an array of short month name Strings.
     */
    public void setShortMonthNames(final FileSystemOptions options, final String[] shortMonthNames) {
        String[] clone = null;
        if (shortMonthNames != null) {
            clone = new String[shortMonthNames.length];
            System.arraycopy(shortMonthNames, 0, clone, 0, shortMonthNames.length);
        }

        setParam(options, SHORT_MONTH_NAMES, clone);
    }

    /**
     * Sets the socket timeout for the FTP client.
     * <p>
     * If you set the {@code soTimeout} to {@code null}, no socket timeout will be set on the FTP client.
     * </p>
     *
     * @param options The FileSystem options.
     * @param timeout The timeout value in milliseconds.
     * @since 2.8.0
     */
    public void setSoTimeout(final FileSystemOptions options, final Duration timeout) {
        setParam(options, SO_TIMEOUT, timeout);
    }

    /**
     * Sets the socket timeout for the FTP client.
     * <p>
     * If you set the {@code soTimeout} to {@code null}, no socket timeout will be set on the FTP client.
     * </p>
     *
     * @param options The FileSystem options.
     * @param timeout The timeout value in milliseconds.
     * @since 2.0
     * @deprecated Use {@link #setSoTimeout(FileSystemOptions, Duration)}.
     */
    @Deprecated
    public void setSoTimeout(final FileSystemOptions options, final Integer timeout) {
        setSoTimeout(options, Duration.ofMillis(timeout));
    }

    /**
     * Sets the list of reply codes that are considered as OK when prematurely closing a stream.
     * <p>
     * If you set the {@code replyCodes} to an empty list, all reply codes besides 200 will be
     * considered as an error.
     * </p>
     *
     * @param options The FileSystem options.
     * @param replyCodes The reply codes.
     * @since 2.4
     */
    public void setTransferAbortedOkReplyCodes(final FileSystemOptions options, final List<Integer> replyCodes) {
        setParam(options, TRANSFER_ABORTED_OK_REPLY_CODES, replyCodes);
    }

    /**
     * Use user directory as root (do not change to fs root).
     *
     * @param options The FileSystemOptions.
     * @param userDirIsRoot true if the user directory should be treated as the root.
     */
    public void setUserDirIsRoot(final FileSystemOptions options, final boolean userDirIsRoot) {
        setParam(options, USER_DIR_IS_ROOT, toBooleanObject(userDirIsRoot));
    }
}
