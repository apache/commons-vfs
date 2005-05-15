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
package org.apache.commons.vfs.provider.smb;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.FileNameParser;
import org.apache.commons.vfs.provider.URLFileNameParser;
import org.apache.commons.vfs.provider.UriParser;
import org.apache.commons.vfs.provider.VfsComponentContext;

/**
 * Implementation for the webdav filesystem.
 * < p/>
 * Additionally encodes every character below space (' ')
 */
public class SmbFileNameParser extends URLFileNameParser
{
    private final static SmbFileNameParser INSTANCE = new SmbFileNameParser();

    public SmbFileNameParser()
    {
        super(139);
    }

    public static FileNameParser getInstance()
    {
        return INSTANCE;
    }

    public FileName parseUri(final VfsComponentContext context, final String filename) throws FileSystemException
    {
        final StringBuffer name = new StringBuffer();

        // Extract the scheme and authority parts
        final Authority auth = extractToPath(filename, name);

        // Decode and adjust separators
        UriParser.canonicalizePath(name, 0, name.length(), this);
        UriParser.fixSeparators(name);

        // Extract the share
        final String share = UriParser.extractFirstElement(name);
        if (share == null || share.length() == 0)
        {
            throw new FileSystemException("vfs.provider.smb/missing-share-name.error", filename);
        }

        // Normalise the path.  Do this after extracting the share name,
        // to deal with things like smb://hostname/share/..
        UriParser.normalisePath(name);
        final String path = name.toString();

        return new SmbFileName(auth.scheme,
            auth.hostName,
            auth.port,
            auth.userName,
            auth.password,
            share,
            path);
    }
}
