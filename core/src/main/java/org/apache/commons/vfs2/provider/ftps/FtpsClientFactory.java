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


import javax.net.ssl.TrustManager;

import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.ftp.FtpClientFactory;

/**
 * Create FTPSClient instances.
 *
 * @since 2.0
 */
public final class FtpsClientFactory
{
    private FtpsClientFactory()
    {
    }

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
    	final FtpsConnectionFactory factory = new FtpsConnectionFactory(FtpsFileSystemConfigBuilder.getInstance());
		return factory.createConnection(hostname, port, username, password, workingDirectory, fileSystemOptions);
    }
    
    private static final class FtpsConnectionFactory extends FtpClientFactory.ConnectionFactory<FTPSClient, FtpsFileSystemConfigBuilder> {

		private FtpsConnectionFactory(final FtpsFileSystemConfigBuilder builder)
		{
			super(builder);
		}

		@Override
		protected FTPSClient createClient(final FileSystemOptions fileSystemOptions) throws FileSystemException
		{
			final FTPSClient client;
	        if (builder.getFtpsType(fileSystemOptions).equals(FtpsFileSystemConfigBuilder.FTPS_TYPE_EXPLICIT))
	        {
	            client = new FTPSClient();
	        }
	        else if (builder.getFtpsType(fileSystemOptions).equals(FtpsFileSystemConfigBuilder.FTPS_TYPE_IMPLICIT))
	        {
	            client = new FTPSClient(true);
	        }
	        else
	        {
	            throw new FileSystemException("Invalid FTPS type of "
	                    + FtpsFileSystemConfigBuilder.getInstance().getFtpsType(fileSystemOptions)
	                    + " specified. Must be 'implicit' or 'explicit'");
	        }
	        
			final TrustManager trustManager = TrustManagerUtils.getValidateServerCertificateTrustManager();
			client.setTrustManager(trustManager);
	        return client;
		}
    }
}
