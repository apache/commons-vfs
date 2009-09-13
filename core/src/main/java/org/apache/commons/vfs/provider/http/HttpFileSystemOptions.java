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
package org.apache.commons.vfs.provider.http;

import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.UserAuthenticator;
import org.apache.commons.vfs.DefaultFileSystemOptions;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

/**
 * HTTP File System Options
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 */
public class HttpFileSystemOptions extends DefaultFileSystemOptions
{
    private static final int DEFAULT_MAX_HOST_CONNECTIONS = 5;

    private static final int DEFAULT_MAX_CONNECTIONS = 50;

    public HttpFileSystemOptions()
    {
        this("http.");
    }

    protected HttpFileSystemOptions(String scheme)
    {
        super(scheme);
    }

    public static HttpFileSystemOptions getInstance(FileSystemOptions opts)
    {
        return FileSystemOptions.makeSpecific(HttpFileSystemOptions.class, opts);
    }

    /**
     * Set the charset used for url encoding<br>
     *
     * @param chaset the chaset
     */
    public void setUrlCharset(String chaset)
    {
        setParam("urlCharset", chaset);
    }

    /**
     * Set the charset used for url encoding<br>
     *
     * @return the chaset
     */
    public String getUrlCharset()
    {
        return getString("urlCharset");
    }

    /**
     * Set the proxy to use for http connection.<br>
     * You have to set the ProxyPort too if you would like to have the proxy relly used.
     *
     * @param proxyHost the host
     * @see #setProxyPort
     */
    public void setProxyHost(String proxyHost)
    {
        setParam("proxyHost", proxyHost);
    }

    /**
     * Set the proxy-port to use for http connection
     * You have to set the ProxyHost too if you would like to have the proxy relly used.
     *
     * @param proxyPort the port
     * @see #setProxyHost
     */
    public void setProxyPort(int proxyPort)
    {
        setParam("proxyPort", new Integer(proxyPort));
    }

    /**
     * Get the proxy to use for http connection
     * You have to set the ProxyPort too if you would like to have the proxy relly used.
     *
     * @return proxyHost
     * @see #setProxyPort
     */
    public String getProxyHost()
    {
        return getString("proxyHost");
    }

    /**
     * Get the proxy-port to use for http the connection
     * You have to set the ProxyHost too if you would like to have the proxy relly used.
     *
     * @return proxyPort: the port number or 0 if it is not set
     * @see #setProxyHost
     */
    public int getProxyPort()
    {
        return getInteger("proxyPort", 0);
    }

    /**
     * Set the proxy authenticator where the system should get the credentials from
     */
    public void setProxyAuthenticator(UserAuthenticator authenticator)
    {
        setParam("proxyAuthenticator", authenticator);
    }

    /**
     * Get the proxy authenticator where the system should get the credentials from
     */
    public UserAuthenticator getProxyAuthenticator()
    {
        return (UserAuthenticator) getParam("proxyAuthenticator");
    }

    /**
     * The cookies to add to the reqest
     */
    public void setCookies(Cookie[] cookies)
    {
        setParam("cookies", cookies);
    }

    /**
     * The cookies to add to the reqest
     */
    public Cookie[] getCookies()
    {
        return (Cookie[]) getParam("cookies");
    }

    /**
     * The maximum number of connections allowed
     */
    public void setMaxTotalConnections(int maxTotalConnections)
    {
        setParam(HttpConnectionManagerParams.MAX_TOTAL_CONNECTIONS, new Integer(maxTotalConnections));
    }

    /**
     * Retrieve the maximum number of connections allowed.
     * @return The maximum number of connections allowed.
     */
    public int getMaxTotalConnections()
    {
        return getInteger(HttpConnectionManagerParams.MAX_TOTAL_CONNECTIONS, DEFAULT_MAX_CONNECTIONS);
    }

    /**
     * The maximum number of connections allowed to any host
     */
    public void setMaxConnectionsPerHost(int maxHostConnections)
    {
        setParam(HttpConnectionManagerParams.MAX_HOST_CONNECTIONS, new Integer(maxHostConnections));
    }

    /**
     * Retrieve the maximum number of connections allowed per host.
     * @return The maximum number of connections allowed per host.
     */
    public int getMaxConnectionsPerHost()
    {
        return getInteger(HttpConnectionManagerParams.MAX_HOST_CONNECTIONS, DEFAULT_MAX_HOST_CONNECTIONS);
    }
}
