/*
 * Copyright 2002-2005 The Apache Software Foundation.
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
package org.apache.commons.vfs.provider.http;

import org.apache.commons.vfs.provider.FileNameParser;
import org.apache.commons.vfs.provider.URLFileNameParser;

/**
 * Implementation for the webdav filesystem.
 * < p/>
 * Additionally encodes every character below space (' ')
 */
public class HttpFileNameParser extends URLFileNameParser
{
    private final static HttpFileNameParser INSTANCE = new HttpFileNameParser();

    public HttpFileNameParser()
    {
        super(80);
    }

    public boolean encodeCharacter(char ch)
    {
        return super.encodeCharacter(ch) || ch < ' ';
    }

    public static FileNameParser getInstance()
    {
        return INSTANCE;
    }
}
