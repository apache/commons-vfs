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

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileContentInfo;
import org.apache.commons.vfs.FileContentInfoFactory;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.http.HttpFileObject;
import org.apache.commons.vfs.provider.URLFileName;
import org.apache.commons.vfs.impl.DefaultFileContentInfo;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.DavConstants;

/**
 * Description
 *
 * @author
 * @version $Revision:  $
 */
public class WebdavFileContentInfoFactory implements FileContentInfoFactory
{
    public FileContentInfo create(FileContent fileContent) throws FileSystemException
    {
        WebdavFileObject file = (WebdavFileObject) fileContent.getFile();

        String contentType = null;
        String contentEncoding = null;

        DavPropertyNameSet nameSet = new DavPropertyNameSet();
        nameSet.add(DavPropertyName.GETCONTENTTYPE);
        DavPropertySet propertySet = file.getProperties((URLFileName)file.getName(), nameSet, true);

        DavProperty property = propertySet.get(DavPropertyName.GETCONTENTTYPE);
        if ( property != null )
        {
            contentType = (String) property.getValue();
        }
        property = propertySet.get(WebdavFileObject.RESPONSE_CHARSET);
        if ( property != null )
        {
            contentEncoding = (String) property.getValue();
        }

        return new DefaultFileContentInfo(contentType, contentEncoding);
    }
}