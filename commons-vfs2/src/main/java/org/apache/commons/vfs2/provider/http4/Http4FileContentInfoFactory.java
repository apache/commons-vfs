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
package org.apache.commons.vfs2.provider.http4;

import java.io.IOException;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileContentInfo;
import org.apache.commons.vfs2.FileContentInfoFactory;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileContentInfo;
import org.apache.commons.vfs2.util.FileObjectUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.protocol.HTTP;

/**
 * Creates {@code FileContentInfoFactory} instances for http4 provider.
 *
 * @since 2.3
 */
public class Http4FileContentInfoFactory implements FileContentInfoFactory {

    @SuppressWarnings("unchecked")
    @Override
    public FileContentInfo create(final FileContent fileContent) throws FileSystemException {
        String contentMimeType = null;
        String contentCharset = null;

        try (final Http4FileObject<Http4FileSystem> http4File = (Http4FileObject<Http4FileSystem>) FileObjectUtils
                .getAbstractFileObject(fileContent.getFile())) {
            final HttpResponse lastHeadResponse = http4File.getLastHeadResponse();

            final Header header = lastHeadResponse.getFirstHeader(HTTP.CONTENT_TYPE);

            if (header != null) {
                final ContentType contentType = ContentType.parse(header.getValue());
                contentMimeType = contentType.getMimeType();

                if (contentType.getCharset() != null) {
                    contentCharset = contentType.getCharset().name();
                }
            }

            return new DefaultFileContentInfo(contentMimeType, contentCharset);
        } catch (final IOException e) {
            throw new FileSystemException(e);
        }
    }
}
