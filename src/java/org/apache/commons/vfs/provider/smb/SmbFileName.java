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
package org.apache.commons.vfs.provider.smb;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.GenericFileName;
import org.apache.commons.vfs.provider.UriParser;

/**
 * An SMB URI.  Adds a share name to the generic URI.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class SmbFileName
    extends GenericFileName
{
    private static final int DEFAULT_PORT = 139;

    private final String share;

    private SmbFileName(final String scheme,
                        final String hostName,
                        final int port,
                        final String userName,
                        final String password,
                        final String share,
                        final String path)
    {
        super(scheme, hostName, port, DEFAULT_PORT, userName, password, path);
        this.share = share;
    }

    /**
     * Parses an SMB URI.
     */
    public static SmbFileName parseUri(final String uri)
        throws FileSystemException
    {
        final StringBuffer name = new StringBuffer();

        // Extract the scheme and authority parts
        final Authority auth = extractToPath(uri, name);

        // Decode and adjust separators
        UriParser.decode(name, 0, name.length());
        UriParser.fixSeparators(name);

        // Extract the share
        final String share = UriParser.extractFirstElement(name);
        if (share == null || share.length() == 0)
        {
            throw new FileSystemException("vfs.provider.smb/missing-share-name.error", uri);
        }

        // Normalise the path.  Do this after extracting the share name,
        // to deal with things like smb://hostname/share/..
        UriParser.normalisePath(name);
        final String path = name.toString();

        return new SmbFileName(auth.scheme,
            auth.hostName,
            auth.port,
            auth.userName,
            auth.password,
            share,
            path);
    }

    /**
     * Returns the share name.
     */
    public String getShare()
    {
        return share;
    }

    /**
     * Builds the root URI for this file name.
     */
    protected void appendRootUri(final StringBuffer buffer)
    {
        super.appendRootUri(buffer);
        buffer.append('/');
        buffer.append(share);
    }

    /**
     * Factory method for creating name instances.
     */
    protected FileName createName(final String path)
    {
        return new SmbFileName(getScheme(),
            getHostName(),
            getPort(),
            getUserName(),
            getPassword(),
            share,
            path);
    }
}
