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

import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.UserAuthenticator;
import org.apache.commons.httpclient.Cookie;

/**
 * Configuration options for HTTP
 * 
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date$
 */
public class HttpFileSystemConfigBuilder extends FileSystemConfigBuilder
{
    private final static HttpFileSystemConfigBuilder builder = new HttpFileSystemConfigBuilder();

    public static HttpFileSystemConfigBuilder getInstance()
    {
        return builder;
    }

    private HttpFileSystemConfigBuilder()
    {
    }

    /**
     * Set the charset used for url encoding<br>
     *
     * @param chaset the chaset
     */
    public void setUrlCharset(FileSystemOptions opts, String chaset)
    {
        setParam(opts, "urlCharset", chaset);
    }

    /**
     * Set the charset used for url encoding<br>
     *
     * @return the chaset
     */
    public String getUrlCharset(FileSystemOptions opts)
    {
        return (String) getParam(opts, "urlCharset");
    }

    /**
     * Set the proxy to use for http connection.<br>
     * You have to set the ProxyPort too if you would like to have the proxy relly used.
     *
     * @param proxyHost the host
     * @see #setProxyPort
     */
    public void setProxyHost(FileSystemOptions opts, String proxyHost)
    {
        setParam(opts, "proxyHost", proxyHost);
    }

    /**
     * Set the proxy-port to use for http connection
     * You have to set the ProxyHost too if you would like to have the proxy relly used.
     *
     * @param proxyPort the port
     * @see #setProxyHost
     */
    public void setProxyPort(FileSystemOptions opts, int proxyPort)
    {
        setParam(opts, "proxyPort", new Integer(proxyPort));
    }

    /**
     * Get the proxy to use for http connection
     * You have to set the ProxyPort too if you would like to have the proxy relly used.
     *
     * @return proxyHost
     * @see #setProxyPort
     */
    public String getProxyHost(FileSystemOptions opts)
    {
        return (String) getParam(opts, "proxyHost");
    }

    /**
     * Get the proxy-port to use for http the connection
     * You have to set the ProxyHost too if you would like to have the proxy relly used.
     *
     * @return proxyPort: the port number or 0 if it is not set
     * @see #setProxyHost
     */
    public int getProxyPort(FileSystemOptions opts)
    {
        if (!hasParam(opts, "proxyPort"))
        {
            return 0;
        }

        return ((Number) getParam(opts, "proxyPort")).intValue();
    }

    /**
     * Set the proxy authenticator where the system should get the credentials from
     */
    public void setProxyAuthenticator(FileSystemOptions opts, UserAuthenticator authenticator)
    {
        setParam(opts, "proxyAuthenticator", authenticator);
    }

    /**
     * Get the proxy authenticator where the system should get the credentials from
     */
    public UserAuthenticator getProxyAuthenticator(FileSystemOptions opts)
    {
        return (UserAuthenticator) getParam(opts, "proxyAuthenticator");
    }

    /**
     * The cookies to add to the reqest
     */
    public void setCookies(FileSystemOptions opts, Cookie[] cookies)
    {
        setParam(opts, "cookies", cookies);
    }

    /**
     * The cookies to add to the reqest
     */
    public Cookie[] getCookies(FileSystemOptions opts)
    {
        return (Cookie[]) getParam(opts, "cookies");
    }
    
    protected Class getConfigClass()
    {
        return HttpFileSystem.class;
    }
}
