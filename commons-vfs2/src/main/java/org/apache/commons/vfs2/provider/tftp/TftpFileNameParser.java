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
package org.apache.commons.vfs2.provider.tftp;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.FileNameParser;
import org.apache.commons.vfs2.provider.GenericURLFileNameParser;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.VfsComponentContext;

/**
 * Implementation for tftp. set default port to 2049
 */
public class TftpFileNameParser extends GenericURLFileNameParser {
    private static final TftpFileNameParser INSTANCE = new TftpFileNameParser();
    private static final int TFTP_PORT = 2049;

    /**
     * Creates a new instance with the default port 2049.
     */
    public TftpFileNameParser() {
        super(TFTP_PORT);
    }

    /**
     * Gets the singleton instance.
     *
     * @return the singleton instance.
     */
    public static FileNameParser getInstance() {
        return INSTANCE;
    }

    @Override
    public FileName parseUri(final VfsComponentContext context, final FileName base, final String filename)
            throws FileSystemException {
        final StringBuilder name = new StringBuilder();

        // Extract the scheme and authority parts
        final Authority auth = extractToPath(context, filename, name);

        // extract domain
        String username = auth.getUserName();
        final String domain = extractDomain(username);
        if (domain != null) {
            username = username.substring(domain.length() + 1);
        }

        // Decode and adjust separators
        UriParser.canonicalizePath(name, 0, name.length(), this);
        UriParser.fixSeparators(name);

        // Extract the share
        final String share = UriParser.extractFirstElement(name);
        if (share == null || share.isEmpty()) {
            throw new FileSystemException("vfs.provider.tftp/missing-share-name.error", filename);
        }

        // Normalise the path. Do this after extracting the share name,
        // to deal with things like tftp://hostname/share/..
        final FileType fileType = UriParser.normalisePath(name);
        final String path = name.toString();

        return new TftpFileName(auth.getScheme(), auth.getHostName(), auth.getPort(), username, auth.getPassword(),
                domain, share, path, fileType);
    }

    private String extractDomain(final String username) {
        if (username == null) {
            return null;
        }

        for (int i = 0; i < username.length(); i++) {
            if (username.charAt(i) == '\\') {
                return username.substring(0, i);
            }
        }

        return null;
    }
}
