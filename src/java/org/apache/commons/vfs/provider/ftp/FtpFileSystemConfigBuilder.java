/*
 * Copyright 2002-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory;
import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemOptions;

/**
 * The config builder for various ftp configuration options
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date$
 */
public class FtpFileSystemConfigBuilder extends FileSystemConfigBuilder
{
    private final static FtpFileSystemConfigBuilder builder = new FtpFileSystemConfigBuilder();

    private final static String FACTORY_KEY = FTPFileEntryParserFactory.class.getName() + ".KEY";
    private final static String PASSIVE_MODE = FTPFileEntryParserFactory.class.getName() + ".PASSIVE";
    private final static String USER_DIR_IS_ROOT = FTPFileEntryParserFactory.class.getName() + ".USER_DIR_IS_ROOT";

    public static FtpFileSystemConfigBuilder getInstance()
    {
        return builder;
    }

    private FtpFileSystemConfigBuilder()
    {
    }

    /**
     * FTPFileEntryParserFactory which will be used for ftp-entry parsing
     *
     * @param opts
     * @param factory instance of your factory
     */
    public void setEntryParserFactory(FileSystemOptions opts, FTPFileEntryParserFactory factory)
    {
        setParam(opts, FTPFileEntryParserFactory.class.getName(), factory);
    }

    /**
     * @param opts
     * @return
     * @see #setEntryParserFactory
     */
    public FTPFileEntryParserFactory getEntryParserFactory(FileSystemOptions opts)
    {
        return (FTPFileEntryParserFactory) getParam(opts, FTPFileEntryParserFactory.class.getName());
    }

    /**
     * set the FQCN of your FileEntryParser used to parse the directory listing from your server.<br />
     * <br />
     * <i>If you do not use the default commons-net FTPFileEntryParserFactory e.g. by using {@link #setEntryParserFactory}
     * this is the "key" parameter passed as argument into your custom factory</i>
     *
     * @param opts
     * @param key
     */
    public void setEntryParser(FileSystemOptions opts, String key)
    {
        setParam(opts, FACTORY_KEY, key);
    }

    /**
     * @param opts
     * @return
     * @see #setEntryParser
     */
    public String getEntryParser(FileSystemOptions opts)
    {
        return (String) getParam(opts, FACTORY_KEY);
    }

    protected Class getConfigClass()
    {
        return FtpFileSystem.class;
    }

    /**
     * enter into passive mode
     *
     * @param opts
     * @param passiveMode
     */
    public void setPassiveMode(FileSystemOptions opts, boolean passiveMode)
    {
        setParam(opts, PASSIVE_MODE, passiveMode ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * @param opts
     * @return
     * @see #setPassiveMode
     */
    public Boolean getPassiveMode(FileSystemOptions opts)
    {
        return (Boolean) getParam(opts, PASSIVE_MODE);
    }

    /**
     * use user directory as root (do not change to fs root)
     *
     * @param opts
     * @param userDirIsRoot
     */
    public void setUserDirIsRoot(FileSystemOptions opts, boolean userDirIsRoot)
    {
        setParam(opts, USER_DIR_IS_ROOT, userDirIsRoot ? Boolean.TRUE : Boolean.FALSE);
    }

    /**
     * @param opts
     * @return
     * @see #setUserDirIsRoot
     */
    public Boolean getUserDirIsRoot(FileSystemOptions opts)
    {
        return (Boolean) getParam(opts, USER_DIR_IS_ROOT);
    }
}
