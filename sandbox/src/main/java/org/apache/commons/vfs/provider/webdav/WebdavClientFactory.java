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
package org.apache.commons.vfs.provider.webdav;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.UserAuthenticator;
import org.apache.commons.vfs.UserAuthenticationData;
import org.apache.commons.vfs.util.UserAuthenticatorUtils;
import org.apache.webdav.lib.WebdavResource;

import java.io.IOException;

/**
 * Create a HttpClient instance
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date$
 */
public class WebdavClientFactory
{
    private WebdavClientFactory()
    {
    }

    /**
     * Creates a new connection to the server.
     */
    public static HttpClient createConnection(String hostname, int port, char[] username, char[] password, FileSystemOptions fileSystemOptions) throws FileSystemException
    {
        // Create an Http client
        HttpClient client;
        try
        {
            final HttpURL url = new HttpURL(
				UserAuthenticatorUtils.toString(username),
                UserAuthenticatorUtils.toString(password),
                hostname,
                port,
                "/");

            // WebdavResource resource = null;
            WebdavResource resource = new WebdavResource()
            {
            };

            if (fileSystemOptions != null)
            {
                String proxyHost = WebdavFileSystemConfigBuilder.getInstance().getProxyHost(fileSystemOptions);
                int proxyPort = WebdavFileSystemConfigBuilder.getInstance().getProxyPort(fileSystemOptions);

                if (proxyHost != null && proxyPort > 0)
                {
                    // resource = new WebdavResource(url, proxyHost, proxyPort);
                    resource.setProxy(proxyHost, proxyPort);
                }

                UserAuthenticator proxyAuth = WebdavFileSystemConfigBuilder.getInstance().getProxyAuthenticator(fileSystemOptions);
                if (proxyAuth != null)
                {
                    UserAuthenticationData authData = UserAuthenticatorUtils.authenticate(proxyAuth, new UserAuthenticationData.Type[]
                        {
                            UserAuthenticationData.USERNAME,
                            UserAuthenticationData.PASSWORD
                        });

                    if (authData != null)
                    {
                        final UsernamePasswordCredentials proxyCreds =
                            new UsernamePasswordCredentials(
                                UserAuthenticatorUtils.toString(UserAuthenticatorUtils.getData(authData, UserAuthenticationData.USERNAME, null)),
                                UserAuthenticatorUtils.toString(UserAuthenticatorUtils.getData(authData, UserAuthenticationData.PASSWORD, null)));

                        resource.setProxyCredentials(proxyCreds);
                    }
                }
            }

            /*
            if (resource == null)
            {
                resource = new WebdavResource(url);
            }
            resource.setProperties(WebdavResource.NOACTION, 1);
            */
            resource.setHttpURL(url, WebdavResource.NOACTION, 1);

            client = resource.retrieveSessionInstance();
            client.setHttpConnectionManager(new WebdavConnectionManager());
        }
        catch (final IOException e)
        {
            throw new FileSystemException("vfs.provider.webdav/connect.error", hostname, e);
        }

        return client;
    }
}