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

import java.net.URI;

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
     * Creates a FileName.
     *
     * @param scheme The scheme.
     * @param rootFile the root file.
     * @param path the path.
     * @param fileType the file type.
     * @return a FileName.
     */
    protected abstract FileName createFileName(String scheme, String rootFile, String path, FileType fileType);

    /**
     * Pops the root prefix off a URI, which has had the scheme removed.
     *
     * @param name the URI to modify.
     * @param uri the whole URI for error reporting.
     * @return the root prefix extracted.
     * @throws FileSystemException if an error occurs.
     */
    protected abstract String extractRootPrefix(String uri, StringBuilder name) throws FileSystemException;

    private String[] getSchemes(final VfsComponentContext context, final FileName base, final String uri) {
        if (context == null) {
            return new String[] {base != null ? base.getScheme() : URI.create(uri).getScheme()};
        }
        return context.getFileSystemManager().getSchemes();
    }

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

    @Override
    public FileName parseUri(final VfsComponentContext context, final FileName base, final String uri)
            throws FileSystemException {
        final StringBuilder nameBuilder = new StringBuilder();

        // Extract the scheme
        String scheme = UriParser.extractScheme(getSchemes(context, base, uri), uri, nameBuilder);
        if (scheme == null && base != null) {
            scheme = base.getScheme();
        }
        if (scheme == null) {
            scheme = "file";
        }

        // Remove encoding, and adjust the separators
        UriParser.canonicalizePath(nameBuilder, 0, nameBuilder.length(), this);

        UriParser.fixSeparators(nameBuilder);

        // Extract the root prefix
        final String rootFile = extractRootPrefix(uri, nameBuilder);

        // Normalise the path
        final FileType fileType = UriParser.normalisePath(nameBuilder);

        final String path = nameBuilder.toString();

        return createFileName(scheme, rootFile, path, fileType);
    }
}
