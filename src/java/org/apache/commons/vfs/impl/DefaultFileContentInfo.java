/*
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs.impl;

import org.apache.commons.vfs.FileContentInfo;

public class DefaultFileContentInfo implements FileContentInfo
{
    private final String contentType;
    private final String contentEncoding;

    public DefaultFileContentInfo(final String contentType, final String contentEncoding)
    {
        this.contentType = contentType;
        this.contentEncoding = contentEncoding;
    }

    public String getContentType()
    {
        return contentType;
    }

    public String getContentEncoding()
    {
        return contentEncoding;
    }
}