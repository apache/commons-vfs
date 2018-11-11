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
package org.apache.commons.vfs2.provider.webdav;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileContentInfo;
import org.apache.commons.vfs2.FileContentInfoFactory;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileContentInfo;
import org.apache.commons.vfs2.provider.URLFileName;
import org.apache.commons.vfs2.util.FileObjectUtils;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;

/**
 * Determines the content information for files accessed via WebDAV.
 *
 * @since 2.0
 */
public class WebdavFileContentInfoFactory implements FileContentInfoFactory {
    @Override
    public FileContentInfo create(final FileContent fileContent) throws FileSystemException {
        final WebdavFileObject file = (WebdavFileObject) FileObjectUtils.getAbstractFileObject(fileContent.getFile());

        String contentType = null;
        String contentEncoding = null;

        final DavPropertyNameSet nameSet = new DavPropertyNameSet();
        nameSet.add(DavPropertyName.GETCONTENTTYPE);
        final DavPropertySet propertySet = file.getProperties((URLFileName) file.getName(), nameSet, true);

        DavProperty property = propertySet.get(DavPropertyName.GETCONTENTTYPE);
        if (property != null) {
            contentType = (String) property.getValue();
        }
        property = propertySet.get(WebdavFileObject.RESPONSE_CHARSET);
        if (property != null) {
            contentEncoding = (String) property.getValue();
        }

        return new DefaultFileContentInfo(contentType, contentEncoding);
    }
}
