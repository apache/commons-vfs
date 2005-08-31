/*
 * Copyright 2002-2005 The Apache Software Foundation.
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
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.GenericFileName;

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
    private final String domain;
    private String uriWithoutAuth;

    protected SmbFileName(
        final String scheme,
        final String hostName,
        final int port,
        final String userName,
        final String password,
        final String domain,
        final String share,
        final String path,
        final FileType type)
    {
        super(scheme, hostName, port, DEFAULT_PORT, userName, password, path, type);
        this.share = share;
        this.domain = domain;
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
     * put domain before username if both are set
     */
    protected void appendCredentials(StringBuffer buffer)
    {
        if (getDomain() != null && getDomain().length() != 0 &&
            getUserName() != null && getUserName().length() != 0)
        {
            buffer.append(getDomain());
            buffer.append("\\");
        }
        super.appendCredentials(buffer);
    }

    /**
     * Factory method for creating name instances.
     */
    public FileName createName(final String path, FileType type)
    {
        return new SmbFileName(
            getScheme(),
            getHostName(),
            getPort(),
            getUserName(),
            getPassword(),
            domain,
            share,
            path,
            type);
    }

    /**
     * Construct the path suitable for SmbFile when used with NtlmPasswordAuthentication
     */
    public String getUriWithoutAuth() throws FileSystemException
    {
        if (uriWithoutAuth != null)
        {
            return uriWithoutAuth;
        }

        StringBuffer sb = new StringBuffer(120);
        sb.append(getScheme());
        sb.append("://");
        sb.append(getHostName());
        sb.append("/");
        sb.append(getShare());
        sb.append(getPathDecoded());
        uriWithoutAuth = sb.toString();
        return uriWithoutAuth;
    }

    /**
     * returns the domain name
     */
    public String getDomain()
    {
        return domain;
    }
}
