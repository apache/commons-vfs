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
package org.apache.commons.vfs2.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileContentInfo;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

/**
 * A helper class that determines the provider to use for a file.
 */
final class FileTypeMap {

    private final Map<String, String> mimeTypeMap = new HashMap<>();
    private final Map<String, String> extensionMap = new HashMap<>();

    /**
     * Adds a file name extension mapping.
     */
    public void addExtension(final String extension, final String scheme) {
        extensionMap.put(extension, scheme);
    }

    /**
     * Adds a MIME type mapping.
     */
    public void addMimeType(final String mimeType, final String scheme) {
        mimeTypeMap.put(mimeType, scheme);
    }

    /**
     * Removes all extensions and scheme mappings.
     */
    public void clear() {
        mimeTypeMap.clear();
        extensionMap.clear();
    }

    /**
     * Gets the scheme for the provider of a layered file system.
     * <p>
     * This will check the {@link FileContentInfo} or file extension.
     * </p>
     *
     * @param fileObject The file object to query.
     * @return Scheme supporting the file type or null (if unknown).
     * @throws FileSystemException if an error occurs.
     */
    public String getScheme(final FileObject fileObject) throws FileSystemException {
        // Check the file's mime type for a match
        final FileContent content = fileObject.getContent();
        final String mimeType = content.getContentInfo().getContentType();
        if (mimeType != null) {
            return mimeTypeMap.get(mimeType);
        }

        // no specific mime-type - if it is a file also check the extension
        if (!fileObject.isFile()) {
            return null; // VFS-490 folders don't use extensions for mime-type
        }
        return extensionMap.get(fileObject.getName().getExtension());
    }
}
