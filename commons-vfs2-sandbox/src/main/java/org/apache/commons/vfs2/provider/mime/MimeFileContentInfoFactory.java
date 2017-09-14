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
package org.apache.commons.vfs2.provider.mime;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.ContentType;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileContentInfo;
import org.apache.commons.vfs2.FileContentInfoFactory;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileContentInfo;

/**
 * Get access to the content info stuff for mime objects.
 */
public class MimeFileContentInfoFactory implements FileContentInfoFactory {
    public FileContentInfo create(final FileContent fileContent) throws FileSystemException {
        final MimeFileObject mimeFile = (MimeFileObject) fileContent.getFile();
        final Part part = mimeFile.getPart();

        String contentTypeString = null;
        String charset = null;

        try {
            // special handling for multipart
            if (mimeFile.isMultipart()) {
                // get the original content type, but ...
                contentTypeString = part.getContentType();

                // .... we deliver the preamble instead of an inupt string
                // the preamble will be delivered in UTF-8 - fixed
                charset = MimeFileSystem.PREAMBLE_CHARSET;
            }
        } catch (final MessagingException e) {
            throw new FileSystemException(e);
        }

        if (contentTypeString == null) {
            // normal message ... get the content type
            try {
                contentTypeString = part.getContentType();
            } catch (final MessagingException e) {
                throw new FileSystemException(e);
            }
        }

        ContentType contentType;
        try {
            contentType = new ContentType(contentTypeString);
        } catch (final MessagingException e) {
            throw new FileSystemException(e);
        }

        if (charset == null) {
            // charset might already be set by the multipart message stuff, else
            // extract it from the contentType now
            charset = contentType.getParameter("charset"); // NON-NLS
        }

        return new DefaultFileContentInfo(contentType.getBaseType(), charset);
    }
}
