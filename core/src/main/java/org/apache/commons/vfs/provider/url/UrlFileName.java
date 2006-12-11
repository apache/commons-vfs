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
package org.apache.commons.vfs.provider.url;

import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.URLFileName;

/**
 * Created by IntelliJ IDEA.
 * User: im
 * Date: 28.06.2005
 * Time: 16:00:19
 * To change this template use File | Settings | File Templates.
 */
public class UrlFileName extends URLFileName
{
    public UrlFileName(final String scheme, final String hostName, final int port, final int defaultPort, final String userName, final String password, final String path, final FileType type, final String queryString)
    {
        super(scheme, hostName, port, defaultPort, userName, password, path, type, queryString);
    }

    protected void appendRootUri(final StringBuffer buffer, boolean addPassword)
    {
        if (getHostName() != null && !"".equals(getHostName()))
        {
            super.appendRootUri(buffer, addPassword);
            return;
        }

        buffer.append(getScheme());
        buffer.append(":");
    }
}
