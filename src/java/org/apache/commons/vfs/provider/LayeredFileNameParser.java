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
package org.apache.commons.vfs.provider;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;

/**
 * Implementation for layered filesystems.
 * <p/>
 * Additionally encodes the '!' character.
 */
public class LayeredFileNameParser extends AbstractFileNameParser
{
    private final static LayeredFileNameParser INSTANCE = new LayeredFileNameParser();

    public static LayeredFileNameParser getInstance()
    {
        return INSTANCE;
    }

    public boolean encodeCharacter(char ch)
    {
        return super.encodeCharacter(ch) || ch == '!';
    }

    public FileName parseUri(final VfsComponentContext context, FileName base, final String filename) throws FileSystemException
    {
        final StringBuffer name = new StringBuffer();

        // Extract the scheme
        final String scheme = UriParser.extractScheme(filename, name);

        // Extract the Layered file URI
        final String rootUriName = extractRootName(name);
        FileName rootUri = null;
        if (rootUriName != null)
        {
            rootUri = context.parseURI(rootUriName);
        }

        // Decode and normalise the path
        UriParser.canonicalizePath(name, 0, name.length(), this);
        FileType fileType = UriParser.normalisePath(name);
        final String path = name.toString();

        return new LayeredFileName(scheme, rootUri, path, fileType);
    }

    /**
     * Pops the root prefix off a URI, which has had the scheme removed.
     */
    protected String extractRootName(final StringBuffer uri)
        throws FileSystemException
    {
        // Looking for <name>!<abspath> (staring at the end)
        int maxlen = uri.length();
        int pos = maxlen - 1;
        for (; pos > 0 && uri.charAt(pos) != '!'; pos--)
        {
        }

        if (pos == 0 && uri.charAt(pos) != '!')
        {
            // not ! found, so take the whole path a root
            // e.g. zip:/my/zip/file.zip
            pos = maxlen;
        }

        // Extract the name
        String prefix = uri.substring(0, pos);
        if (pos < maxlen)
        {
            uri.delete(0, pos + 1);
        }
        else
        {
            uri.setLength(0);
        }

        return prefix;
    }

}
