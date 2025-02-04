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
package org.apache.commons.vfs2.provider;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

/**
 * Implementation for layered file systems.
 * <p>
 * Additionally encodes the '!' character.
 * </p>
 */
public class LayeredFileNameParser extends AbstractFileNameParser {

    private static final LayeredFileNameParser INSTANCE = new LayeredFileNameParser();

    /**
     * Gets the singleton instance.
     *
     * @return the singleton instance.
     */
    public static LayeredFileNameParser getInstance() {
        return INSTANCE;
    }

    /**
     * Constructs a new instance.
     */
    public LayeredFileNameParser() {
        // empty
    }

    /**
     * Determines if a character should be encoded.
     *
     * @param ch The character to check.
     * @return true if the character should be encoded.
     */
    @Override
    public boolean encodeCharacter(final char ch) {
        return super.encodeCharacter(ch) || ch == LayeredFileName.LAYER_SEPARATOR;
    }

    /**
     * Pops the root prefix off a URI, which has had the scheme removed.
     *
     * @param uri string builder which gets modified.
     * @return the extracted root name.
     */
    protected String extractRootName(final StringBuilder uri) {
        // Looking for <name>!<abspath> (staring at the end)
        final int maxlen = uri.length();
        int pos = maxlen - 1;
        while (pos > 0 && uri.charAt(pos) != LayeredFileName.LAYER_SEPARATOR) {
            pos--;
        }

        if (pos == 0 && uri.charAt(pos) != LayeredFileName.LAYER_SEPARATOR) {
            // not ! found, so take the whole path a root
            // e.g. zip:/my/zip/file.zip
            pos = maxlen;
        }

        // Extract the name
        final String prefix = uri.substring(0, pos);
        if (pos < maxlen) {
            uri.delete(0, pos + 1);
        } else {
            uri.setLength(0);
        }

        return prefix;
    }

    /**
     * Parses the base and name into a FileName.
     *
     * @param context The component context.
     * @param baseFileName The base FileName.
     * @param fileName name The target file name.
     * @return The constructed FileName.
     * @throws FileSystemException if an error occurs.
     */
    @Override
    public FileName parseUri(final VfsComponentContext context, final FileName baseFileName, final String fileName)
            throws FileSystemException {
        final StringBuilder name = new StringBuilder();

        // Extract the scheme
        final String scheme = UriParser.extractScheme(context.getFileSystemManager().getSchemes(), fileName, name);

        // Extract the Layered file URI
        final String rootUriName = extractRootName(name);
        FileName rootUri = null;
        if (rootUriName != null) {
            rootUri = context.parseURI(rootUriName);
        }

        // Decode and normalise the path
        UriParser.canonicalizePath(name, 0, name.length(), this);
        UriParser.fixSeparators(name);
        final FileType fileType = UriParser.normalisePath(name);
        final String path = name.toString();

        return new LayeredFileName(scheme, rootUri, path, fileType);
    }

}
