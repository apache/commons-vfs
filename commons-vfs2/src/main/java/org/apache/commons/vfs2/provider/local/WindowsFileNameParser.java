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
package org.apache.commons.vfs2.provider.local;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

/**
 * A parser for Windows file names.
 */
public class WindowsFileNameParser extends LocalFileNameParser {
    /**
     * Pops the root prefix off a URI, which has had the scheme removed.
     */
    @Override
    protected String extractRootPrefix(final String uri, final StringBuilder name) throws FileSystemException {
        return extractWindowsRootPrefix(uri, name);
    }

    @Override
    protected FileName createFileName(final String scheme, final String rootFile, final String path,
            final FileType type) {
        return new WindowsFileName(scheme, rootFile, path, type);
    }

    /**
     * Extracts a Windows root prefix from a name.
     */
    private String extractWindowsRootPrefix(final String uri, final StringBuilder name) throws FileSystemException {
        // Looking for:
        // ('/'){0, 3} <letter> ':' '/'
        // ['/'] '//' <name> '/' <name> ( '/' | <end> )

        // Skip over first 4 (unc) leading '/' chars
        int startPos = 0;
        final int maxlen = Math.min(4, name.length());
        for (; startPos < maxlen && name.charAt(startPos) == '/'; startPos++) {
        }
        if (startPos == maxlen && name.length() > (startPos + 1) && name.charAt(startPos + 1) == '/') {
            // Too many '/'
            throw new FileSystemException("vfs.provider.local/not-absolute-file-name.error", uri);
        }
        name.delete(0, startPos);

        // Look for drive name
        final String driveName = extractDrivePrefix(name);
        if (driveName != null) {
            return driveName;
        }

        // Look for UNC name
        if (startPos < 2) {
            throw new FileSystemException("vfs.provider.local/not-absolute-file-name.error", uri);
        }

        return "//" + extractUNCPrefix(uri, name);
    }

    /**
     * Extracts a drive prefix from a path. Leading '/' chars have been removed.
     */
    private String extractDrivePrefix(final StringBuilder name) {
        // Looking for <letter> ':' '/'
        if (name.length() < 3) {
            // Too short
            return null;
        }
        final char ch = name.charAt(0);
        if (ch == '/' || ch == ':') {
            // Missing drive letter
            return null;
        }
        if (name.charAt(1) != ':') {
            // Missing ':'
            return null;
        }
        if (name.charAt(2) != '/') {
            // Missing separator
            return null;
        }

        final String prefix = name.substring(0, 2);
        name.delete(0, 2);

        return prefix.intern();
    }

    /**
     * Extracts a UNC name from a path. Leading '/' chars have been removed.
     */
    private String extractUNCPrefix(final String uri, final StringBuilder name) throws FileSystemException {
        // Looking for <name> '/' <name> ( '/' | <end> )

        // Look for first separator
        final int maxpos = name.length();
        int pos = 0;
        for (; pos < maxpos && name.charAt(pos) != '/'; pos++) {
        }
        pos++;
        if (pos >= maxpos) {
            throw new FileSystemException("vfs.provider.local/missing-share-name.error", uri);
        }

        // Now have <name> '/'
        final int startShareName = pos;
        for (; pos < maxpos && name.charAt(pos) != '/'; pos++) {
        }
        if (pos == startShareName) {
            throw new FileSystemException("vfs.provider.local/missing-share-name.error", uri);
        }

        // Now have <name> '/' <name> ( '/' | <end> )
        final String prefix = name.substring(0, pos);
        name.delete(0, pos);
        return prefix;
    }
}
