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
package org.apache.commons.vfs.provider.tar;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.LayeredFileName;
import org.apache.commons.vfs.provider.UriParser;
import org.apache.commons.vfs.provider.FileNameParser;

/**
 * A parser for Tar file names.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class TarFileName extends LayeredFileName
{
    public TarFileName(final String scheme,
                       final FileName tarFileUri,
                       final String path)
    {
        super(scheme, tarFileUri, path);
    }

    /**
     * Builds the root URI for this file name.
     */
    protected void appendRootUri(final StringBuffer buffer)
    {
        /*
        buffer.append(getScheme());
        buffer.append(":");
        UriParser.appendEncoded(buffer, getOuterName().getURI(), TAR_URL_RESERVED_CHARS);
        buffer.append("!");
        */
    }

    /**
     * Factory method for creating name instances.
     */
    public FileName createName(final String path)
    {
        return null;
    }

}
