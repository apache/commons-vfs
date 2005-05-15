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
package org.apache.commons.vfs.provider.local;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileName;

/**
 * A parser for Windows file names.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class WindowsFileNameParser
    extends LocalFileNameParser
{
    /**
     * Pops the root prefix off a URI, which has had the scheme removed.
     */
    protected String extractRootPrefix(final String uri,
                                       final StringBuffer name)
        throws FileSystemException
    {
        return extractWindowsRootPrefix(uri, name);
    }

    protected FileName createFileName(String scheme, final String rootFile, final String path)
    {
        return new LocalFileName(scheme, rootFile, path);
    }

    /**
     * Extracts a Windows root prefix from a name.
     */
    private String extractWindowsRootPrefix(final String uri,
                                            final StringBuffer name)
        throws FileSystemException
    {
        // Looking for:
        // ('/'){0, 3} <letter> ':' '/'
        // ['/'] '//' <name> '/' <name> ( '/' | <end> )

        // Skip over first 3 leading '/' chars
        int startPos = 0;
        int maxlen = Math.min(3, name.length());
        for (; startPos < maxlen && name.charAt(startPos) == '/'; startPos++)
        {
        }
        if (startPos == maxlen)
        {
            // Too many '/'
            throw new FileSystemException("vfs.provider.local/not-absolute-file-name.error", uri);
        }
        name.delete(0, startPos);

        // Look for drive name
        String driveName = extractDrivePrefix(name);
        if (driveName != null)
        {
            return driveName;
        }

        // Look for UNC name
        if (startPos < 2)
        {
            throw new FileSystemException("vfs.provider.local/not-absolute-file-name.error", uri);
        }

        return "//" + extractUNCPrefix(uri, name);
    }

    /**
     * Extracts a drive prefix from a path.  Leading '/' chars have been removed.
     */
    private String extractDrivePrefix(final StringBuffer name)
    {
        // Looking for <letter> ':' '/'
        if (name.length() < 3)
        {
            // Too short
            return null;
        }
        char ch = name.charAt(0);
        if (ch == '/' || ch == ':')
        {
            // Missing drive letter
            return null;
        }
        if (name.charAt(1) != ':')
        {
            // Missing ':'
            return null;
        }
        if (name.charAt(2) != '/')
        {
            // Missing separator
            return null;
        }

        String prefix = name.substring(0, 2);
        name.delete(0, 2);
        return prefix;
    }

    /**
     * Extracts a UNC name from a path.  Leading '/' chars have been removed.
     */
    private String extractUNCPrefix(final String uri,
                                    final StringBuffer name)
        throws FileSystemException
    {
        // Looking for <name> '/' <name> ( '/' | <end> )

        // Look for first separator
        int maxpos = name.length();
        int pos = 0;
        for (; pos < maxpos && name.charAt(pos) != '/'; pos++)
        {
        }
        pos++;
        if (pos >= maxpos)
        {
            throw new FileSystemException("vfs.provider.local/missing-share-name.error", uri);
        }

        // Now have <name> '/'
        int startShareName = pos;
        for (; pos < maxpos && name.charAt(pos) != '/'; pos++)
        {
        }
        if (pos == startShareName)
        {
            throw new FileSystemException("vfs.provider.local/missing-share-name.error", uri);
        }

        // Now have <name> '/' <name> ( '/' | <end> )
        String prefix = name.substring(0, pos);
        name.delete(0, pos);
        return prefix;
    }
}
