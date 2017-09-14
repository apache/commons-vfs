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
import org.apache.commons.vfs2.provider.AbstractFileNameParser;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.VfsComponentContext;

/**
 * A name parser.
 */
public abstract class LocalFileNameParser extends AbstractFileNameParser {
    /**
     * Determines if a name is an absolute file name.
     *
     * @param name The file name.
     * @return true if the name is absolute, false otherwise.
     */
    public boolean isAbsoluteName(final String name) {
        // TODO - this is yucky
        final StringBuilder b = new StringBuilder(name);
        try {
            UriParser.fixSeparators(b);
            extractRootPrefix(name, b);
            return true;
        } catch (final FileSystemException e) {
            return false;
        }
    }

    /**
     * Pops the root prefix off a URI, which has had the scheme removed.
     *
     * @param name the URI to modify.
     * @param uri the whole URI for error reporting.
     * @return the root prefix extracted.
     * @throws FileSystemException if an error occurs.
     */
    protected abstract String extractRootPrefix(final String uri, final StringBuilder name) throws FileSystemException;

    @Override
    public FileName parseUri(final VfsComponentContext context, final FileName base, final String uri)
            throws FileSystemException {
        final StringBuilder name = new StringBuilder();

        // Extract the scheme
        String scheme = UriParser.extractScheme(uri, name);
        if (scheme == null) {
            scheme = "file";
        }

        // Remove encoding, and adjust the separators
        UriParser.canonicalizePath(name, 0, name.length(), this);

        UriParser.fixSeparators(name);

        // Extract the root prefix
        final String rootFile = extractRootPrefix(uri, name);

        // Normalise the path
        final FileType fileType = UriParser.normalisePath(name);

        final String path = name.toString();

        return createFileName(scheme, rootFile, path, fileType);
    }

    protected abstract FileName createFileName(String scheme, final String rootFile, final String path,
            final FileType type);
}
