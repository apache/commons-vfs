/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.ftp;

import org.apache.commons.vfs.provider.ParsedUri;

/**
 * A parsed FTP URI.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.3 $ $Date: 2002/07/05 04:08:19 $
 */
public final class ParsedFtpUri
    extends ParsedUri
{
    private String m_userName;
    private String m_password;

    public String getUserName()
    {
        return m_userName;
    }

    public void setUserName( String userName )
    {
        m_userName = userName;
    }

    public String getPassword()
    {
        return m_password;
    }

    public void setPassword( String password )
    {
        m_password = password;
    }
}
