/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.commons.vfs.impl;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;

import java.util.HashMap;
import java.util.Map;

/**
 * A helper class that determines the provider to use for a file.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
class FileTypeMap
{
    private final Map mimeTypeMap = new HashMap();
    private final Map extensionMap = new HashMap();

    /**
     * Adds a MIME type mapping.
     */
    public void addMimeType(final String mimeType, final String scheme)
    {
        mimeTypeMap.put(mimeType, scheme);
    }

    /**
     * Adds a filename extension mapping.
     */
    public void addExtension(final String extension, final String scheme)
    {
        extensionMap.put(extension, scheme);
    }

    /**
     * Finds the provider to use to create a filesystem from a given file.
     */
    public String getScheme(final FileObject file) throws FileSystemException
    {
        // Check the file's mime type for a match
        final FileContent content = file.getContent();
        // final String mimeType = (String) content.getAttribute("content-type");
        final String mimeType = (String) content.getContentInfo().getContentType();
        if (mimeType != null)
        {
            return (String) mimeTypeMap.get(mimeType);
        }

        // Check the file's extension for a match
        final String extension = file.getName().getExtension();
        return (String) extensionMap.get(extension);
    }
}
