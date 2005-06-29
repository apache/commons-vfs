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
package org.apache.commons.vfs.provider.url;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileNameParser;
import org.apache.commons.vfs.provider.URLFileName;
import org.apache.commons.vfs.provider.UriParser;
import org.apache.commons.vfs.provider.VfsComponentContext;
import org.apache.commons.vfs.provider.URLFileNameParser;
import org.apache.commons.vfs.provider.local.LocalFileNameParser;
import org.apache.commons.vfs.provider.local.GenericFileNameParser;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * Implementation for any java.net.url based filesystem.<br />
 * Composite of URLFilenameParser and GenericFilenameParser
 *
 * @author imario@apache.org
 * @version $Revision$ $Date$
 */
public class UrlFileNameParser extends AbstractFileNameParser
{
    private URLFileNameParser url = new URLFileNameParser(80);
    private GenericFileNameParser generic = new GenericFileNameParser();

    public UrlFileNameParser()
    {
        super();
    }

    public boolean encodeCharacter(char ch)
    {
        return super.encodeCharacter(ch) || ch == '?';
    }

    public FileName parseUri(final VfsComponentContext context, final FileName base, final String filename) throws FileSystemException
    {
        if (isUrlBased(base, filename))
        {
            return url.parseUri(context, base, filename);
        }

        return generic.parseUri(context, base, filename);
    }

    /**
     * Guess is the given filename is a url with host or not. VFS treats such urls differently.<br />
     * A filename is url-based if the base is a <code>URLFileName</code> or there are only 2 slashes
     * after the scheme.<br/>
     * e.g: http://host/path, file:/path/to/file, file:///path/to/file
     *
     */
    protected boolean isUrlBased(final FileName base, final String filename)
    {
        if (base instanceof URLFileName)
        {
            return true;
        }

        int nuofSlash = countSlashes(filename);
        return nuofSlash == 2;
    }

    /**
     * This method counts the slashes after the scheme.
     *
     * @param filename
     * @return nuof slashes
     */
    protected int countSlashes(final String filename)
    {
        int state = 0;
        int nuofSlash = 0;
        for (int pos = 0; pos<filename.length(); pos++)
        {
            char c = filename.charAt(pos);
            if (state == 0)
            {
                if (c >= 'a' && c <= 'z')
                {
                    continue;
                }
                if (c == ':')
                {
                    state++;
                    continue;
                }
            }
            else if (state == 1)
            {
                if (c == '/')
                {
                    nuofSlash++;
                }
                else
                {
                    return nuofSlash;
                }
            }
        }
        return nuofSlash;
    }
}
