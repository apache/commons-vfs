/*
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
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
 * @version $Revision: 1.6 $ $Date: 2004/09/19 18:23:48 $
 */
public class FtpFileSystemConfigBuilder extends FileSystemConfigBuilder
{
    private final static FtpFileSystemConfigBuilder builder = new FtpFileSystemConfigBuilder();

    private final static String FACTORY_KEY = FTPFileEntryParserFactory.class.getName() + ".KEY";

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
    public void setFTPFileEntryParserFactory(FileSystemOptions opts, FTPFileEntryParserFactory factory)
    {
        setParam(opts, FTPFileEntryParserFactory.class.getName(), factory);
    }

    /**
     * @param opts
     * @return
     * @see #setFTPFileEntryParserFactory
     */
    public FTPFileEntryParserFactory getFTPFileEntryParserFactory(FileSystemOptions opts)
    {
        return (FTPFileEntryParserFactory) getParam(opts, FTPFileEntryParserFactory.class.getName());
    }

    /**
     * set the key for using as argument to FTPFileEntryParserFactory.<br />
     * If you use the default implementation in commons-net and you set a FQCN, this class will be instantiated and used for ftp-entry parsing.<br />
     *
     * @param opts
     * @param key
     */
    public void setFTPFileEntryParserFactoryKey(FileSystemOptions opts, String key)
    {
        setParam(opts, FACTORY_KEY, key);
    }

    /**
     * @param opts
     * @return
     * @see #setFTPFileEntryParserFactoryKey
     */
    public String getFTPFileEntryParserFactoryKey(FileSystemOptions opts)
    {
        return (String) getParam(opts, FACTORY_KEY);
    }

    protected Class getConfigClass()
    {
        return FtpFileSystem.class;
    }
}
