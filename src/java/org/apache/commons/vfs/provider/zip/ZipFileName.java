/*
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
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
package org.apache.commons.vfs.provider.zip;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.LayeredFileName;
import org.apache.commons.vfs.provider.UriParser;
import org.apache.commons.vfs.provider.FileNameParser;

/**
 * A parser for Zip file names.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class ZipFileName
    extends LayeredFileName
{
    private static final char[] ZIP_URL_RESERVED_CHARS = {'!'};

    public ZipFileName(final String scheme,
                       final FileName zipFileUri,
                       final String path)
    {
        super(scheme, zipFileUri, path);
    }

    /**
     * Builds the root URI for this file name.
     */
    protected void appendRootUri(final StringBuffer buffer)
    {
        buffer.append(getScheme());
        buffer.append(":");
        buffer.append(getOuterName().getURI());
        buffer.append("!");
    }

    /**
     * Factory method for creating name instances.
     */
    public FileName createName(final String path)
    {
        return new ZipFileName(getScheme(), getOuterName(), path);
    }
}
