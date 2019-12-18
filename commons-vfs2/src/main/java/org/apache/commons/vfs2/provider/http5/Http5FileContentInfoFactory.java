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
package org.apache.commons.vfs2.provider.http5;

import java.io.IOException;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileContentInfo;
import org.apache.commons.vfs2.FileContentInfoFactory;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileContentInfo;
import org.apache.commons.vfs2.util.FileObjectUtils;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpResponse;

/**
 * Creates {@code FileContentInfoFactory} instances for http5 provider.
 *
 * @since 2.5.0
 */
public class Http5FileContentInfoFactory implements FileContentInfoFactory {

    @SuppressWarnings("unchecked")
    @Override
    public FileContentInfo create(final FileContent fileContent) throws FileSystemException {
        String contentMimeType = null;
        String contentCharset = null;

        try (final Http5FileObject<Http5FileSystem> http4File = (Http5FileObject<Http5FileSystem>) FileObjectUtils
                .getAbstractFileObject(fileContent.getFile())) {
            final HttpResponse lastHeadResponse = http4File.getLastHeadResponse();

            final Header header = lastHeadResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE);

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
