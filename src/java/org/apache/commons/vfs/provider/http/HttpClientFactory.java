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
package org.apache.commons.vfs.provider.http;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;

/**
 * Create a HttpClient instance
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date$
 */
public class HttpClientFactory
{
    private HttpClientFactory()
    {
    }

    /**
     * Creates a new connection to the server.
     */
    public static HttpClient createConnection(String hostname, int port, String username, String password, FileSystemOptions fileSystemOptions) throws FileSystemException
    {
        HttpClient client;
        try
        {
            client = new HttpClient(new MultiThreadedHttpConnectionManager());
            final HostConfiguration config = new HostConfiguration();
            config.setHost(hostname, port);

            if (fileSystemOptions != null)
            {
                String proxyHost = HttpFileSystemConfigBuilder.getInstance().getProxyHost(fileSystemOptions);
                int proxyPort = HttpFileSystemConfigBuilder.getInstance().getProxyPort(fileSystemOptions);

                if (proxyHost != null && proxyPort > 0)
                {
                    config.setProxy(proxyHost, proxyPort);
                }
            }

            client.setHostConfiguration(config);

            if (username != null)
            {
                final UsernamePasswordCredentials creds =
                    new UsernamePasswordCredentials(username, password);
                client.getState().setCredentials(null, hostname, creds);
            }

            client.executeMethod(new HeadMethod());
        }
        catch (final Exception exc)
        {
            throw new FileSystemException("vfs.provider.http/connect.error", new Object[]{hostname}, exc);
        }

        return client;
    }
}