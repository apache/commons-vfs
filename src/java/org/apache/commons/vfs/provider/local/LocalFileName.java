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
package org.apache.commons.vfs.provider.local;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileName;
import org.apache.commons.vfs.provider.UriParser;

/**
 * A local file URI.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class LocalFileName
    extends AbstractFileName
{
    private final String rootFile;

    protected LocalFileName(final String scheme,
                            final String rootFile,
                            final String path)
    {
        super(scheme, path);
        this.rootFile = rootFile;
    }

    /**
     * Parses an absolute file URI.
     *
     * @todo Make parser a static field
     */
    public static LocalFileName parseUri(final String uri, final boolean uriEncoded,
                                         final LocalFileNameParser parser)
        throws FileSystemException
    {
        final StringBuffer name = new StringBuffer();

        // Extract the scheme
        final String scheme = UriParser.extractScheme(uri, name);

        // Remove encoding, and adjust the separators
        if (uriEncoded)
        {
            UriParser.decode(name, 0, name.length());
        }
        UriParser.fixSeparators(name);

        // Extract the root prefix
        final String rootFile = parser.extractRootPrefix(uri, name);

        // Normalise the path
        UriParser.normalisePath(name);
        final String path = name.toString();

        return new LocalFileName(scheme, rootFile, path);
    }

    /**
     * Returns the root file for this file.
     */
    public String getRootFile()
    {
        return rootFile;
    }

    /**
     * Factory method for creating name instances.
     */
    protected FileName createName(final String path)
    {
        return new LocalFileName(getScheme(), rootFile, path);
    }

    /**
     * Builds the root URI for this file name.
     */
    protected void appendRootUri(final StringBuffer buffer)
    {
        buffer.append(getScheme());
        buffer.append("://");
        buffer.append(rootFile);
    }
}
