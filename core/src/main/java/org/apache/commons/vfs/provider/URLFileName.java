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
package org.apache.commons.vfs.provider;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;

public class URLFileName extends GenericFileName
{
    private final String queryString;

    public URLFileName(final String scheme,
                       final String hostName,
                       final int port,
                       final int defaultPort,
                       final String userName,
                       final String password,
                       final String path,
                       final FileType type,
                       final String queryString)
    {
        super(scheme, hostName, port, defaultPort, userName, password, path, type);
        this.queryString = queryString;
    }

    /**
     * get the query string
     *
     * @return the query string part of the filename
     */
    public String getQueryString()
    {
        return queryString;
    }

    /**
     * get the path and query string e.g. /path/servlet?param1=true
     *
     * @return the path and its query string
     */
    public String getPathQuery()
    {
        StringBuffer sb = new StringBuffer(250);
        sb.append(getPath());
        sb.append("?");
        sb.append(getQueryString());

        return sb.toString();
    }

    /**
     * get the path encoded suitable for url like filesystem e.g. (http, webdav)
     *
     * @param charset the charset used for the path encoding
     */
    public String getPathQueryEncoded(String charset) throws URIException, FileSystemException
    {
        if (getQueryString() == null)
        {
            if (charset != null)
            {
                return URIUtil.encodePath(getPathDecoded(), charset);
            }
            else
            {
                return URIUtil.encodePath(getPathDecoded());
            }
        }

        StringBuffer sb = new StringBuffer(250);
        if (charset != null)
        {
            sb.append(URIUtil.encodePath(getPathDecoded(), charset));
        }
        else
        {
            sb.append(URIUtil.encodePath(getPathDecoded()));
        }
        sb.append("?");
        sb.append(getQueryString());
        return sb.toString();
    }

    public FileName createName(final String absPath, FileType type)
    {
        return new URLFileName(getScheme(),
            getHostName(),
            getPort(),
            getDefaultPort(),
            getUserName(),
            getPassword(),
            absPath,
            type,
            getQueryString());
    }

    /**
     * append query string to the uri
     *
     * @return the uri
     */
    protected String createURI()
    {
        if (getQueryString() != null)
        {
            StringBuffer sb = new StringBuffer(250);
            sb.append(super.createURI());
            sb.append("?");
            sb.append(getQueryString());

            return sb.toString();
        }

        return super.createURI();
    }

    public String getURIEncoded(String charset) throws FileSystemException, URIException
    {
        StringBuffer sb = new StringBuffer(80);
        appendRootUri(sb, true);
        sb.append(getPathQueryEncoded(charset));
        return sb.toString();
    }
}
