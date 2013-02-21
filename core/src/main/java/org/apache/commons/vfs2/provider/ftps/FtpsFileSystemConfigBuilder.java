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

import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;

/**
 * The configuration builder for various FTPS configuration options.
 *
 * @since 2.0
 */
public final class FtpsFileSystemConfigBuilder extends FtpFileSystemConfigBuilder
{
    private static final String _PREFIX = FtpsFileSystemConfigBuilder.class.getName();

    private static final FtpsFileSystemConfigBuilder BUILDER =
        new FtpsFileSystemConfigBuilder();

    private static final String FTPS_TYPE = _PREFIX + ".FTPS_TYPE";
    private static final String PROT = _PREFIX + ".PROT";

    /**
     * FTPS implicit file type.
     */
    public static final String FTPS_TYPE_IMPLICIT = "implicit";

    /**
     * FTPS explicit file type.
     */
    public static final String FTPS_TYPE_EXPLICIT = "explicit";

    private FtpsFileSystemConfigBuilder()
    {
        super("ftps.");
    }

    /**
     * Gets the singleton builder.
     *
     * @return the singleton builder.
     */
    public static FtpsFileSystemConfigBuilder getInstance()
    {
        return BUILDER;
    }

    /**
     * Set FTPS security mode, either "implicit" or "explicit".
     *
     * <p>Note, that implicit mode is not standardized and considered as deprecated. Some unit tests for VFS fail with
     * implicit mode and it is not yet clear if its a problem with Commons VFS/Commons Net or our test server Apache
     * FTP/SSHD.</p>
     *  
     * @param opts The FileSystemOptions.
     * @param ftpsType The file type.
     * @see <a href="http://en.wikipedia.org/wiki/FTPS#Implicit">Wikipedia: FTPS/Implicit</a>
     */
    public void setFtpsType(final FileSystemOptions opts, final String ftpsType)
    {
        setParam(opts, FTPS_TYPE, ftpsType);
    }

    /**
     * Return the FTPS security mode. Defaults to "explicit" if not defined.
     *
     * @param opts The FileSystemOptions.
     * @return The file type.
     * @see #setFtpsType
     */
    public String getFtpsType(final FileSystemOptions opts)
    {
        return getString(opts, FTPS_TYPE, FtpsFileSystemConfigBuilder.FTPS_TYPE_EXPLICIT);
    }

	/**
	 * Gets the data channel protection level (PROT).
	 * 
	 * @param opts The FileSystemOptions.
	 * @return The PROT value.
	 * @see org.apache.commons.net.ftp.FTPSClient#execPROT(String)
	 * @since 2.1
	 */
	public FtpsDataChannelProtectionLevel getDataChannelProtectionLevel(final FileSystemOptions opts)
	{
		return (FtpsDataChannelProtectionLevel)getParam(opts, PROT);
	}

	/**
	 * Sets the data channel protection level (PROT).
	 * 
	 * @param opts The FileSystemOptions.
	 * @param prot The PROT value, {@code null} has no effect.
	 * @see org.apache.commons.net.ftp.FTPSClient#execPROT(String)
	 * @since 2.1
	 */
	public void setDataChannelProtectionLevel(final FileSystemOptions opts, final FtpsDataChannelProtectionLevel prot)
	{
		setParam(opts, PROT, prot);
	}
}
