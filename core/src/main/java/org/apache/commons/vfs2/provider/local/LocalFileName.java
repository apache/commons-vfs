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
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.UriParser;

/**
 * A local file URI.
 */
public class LocalFileName extends AbstractFileName
{
    // URI Characters that are possible in local filenames, but must be escaped
    // for proper URI handling.
    //
    // How reserved URI chars were selected:
    //
    //  URIs can contain :, /, ?, #, @
    //      See http://download.oracle.com/javase/6/docs/api/java/net/URI.html
    //          http://tools.ietf.org/html/rfc3986#section-2.2
    //
    //  Since : and / occur before the path, only chars after path are escaped (i.e., # and ?)
    //  ? is a reserved filesystem character for Windows and Unix, so can't be part of a filename.
    //  Therefore only # is a reserved char in a URI as part of the path that can be in the filename.
    private static final char[] RESERVED_URI_CHARS = {'#'};


    private final String rootFile;

    protected LocalFileName(final String scheme,
                            final String rootFile,
                            final String path,
                            final FileType type)
    {
        super(scheme, path, type);
        this.rootFile = rootFile.intern();
    }

    /**
     * Returns the root file for this file.
     * @return The root file name.
     */
    public String getRootFile()
    {
        return rootFile;
    }

    /**
     * Factory method for creating name instances.
     * @param path The file path.
     * @param type The file type.
     * @return The FileName.
     */
    @Override
    public FileName createName(final String path, final FileType type)
    {
        return new LocalFileName(getScheme(), rootFile, path, type);
    }

    /**
     * Returns the absolute URI of the file.
     * @return The absolute URI of the file.
     */
    @Override
    public String getURI()
    {
        String uri = super.getURI();

        if (uri != null && uri.length() > 0)
        {
            try
            {
                // VFS-325: Handle URI special characters in filename
                // Decode the base uri and re-encode with URI special characters
                uri = UriParser.decode(uri);

                uri = UriParser.encode(uri, RESERVED_URI_CHARS);
            }
            catch (final FileSystemException e)
            {
                // Default to base uri value
            }
        }

        return uri;
    }

    /**
     * returns a "friendly path", this is a path without a password.
     * @return The "friendly" URI.
     */
    @Override
    public String getFriendlyURI()
    {
        String uri = super.getFriendlyURI();

        if (uri != null && uri.length() > 0)
        {
            try
            {
                // VFS-325: Handle URI special characters in filename
                // Decode the base uri and re-encode with URI special characters
                uri = UriParser.decode(uri);

                uri = UriParser.encode(uri, RESERVED_URI_CHARS);
            }
            catch (final FileSystemException e)
            {
                // Default to base uri value
            }
        }

        return uri;
    }

    /**
     * Returns the decoded URI of the file.
     * @return the FileName as a URI.
     */
    @Override
    public String toString()
    {
        try
        {
            return UriParser.decode(super.getURI());
        }
        catch (final FileSystemException e)
        {
            return super.getURI();
        }
    }



    /**
     * Builds the root URI for this file name.
     */
    @Override
    protected void appendRootUri(final StringBuilder buffer, final boolean addPassword)
    {
        buffer.append(getScheme());
        buffer.append("://");
        buffer.append(rootFile);
    }
}
