/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider;

/**
 * A data container for information parsed from an absolute URI.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.3 $ $Date: 2002/04/07 02:27:56 $
 */
public class ParsedUri
{
    private String scheme;
    private String rootURI;
    private String path;
    private String userInfo;
    private String hostName;
    private String port;

    /** Returns the scheme. */
    public String getScheme()
    {
        return scheme;
    }

    /** Sets the scheme. */
    public void setScheme( String scheme )
    {
        this.scheme = scheme;
    }

    /** Returns the root URI, used to identify the file system. */
    public String getRootUri()
    {
        return rootURI;
    }

    /** Sets the root URI. */
    public void setRootUri( String rootPrefix )
    {
        rootURI = rootPrefix;
    }

    /** Returns the user info part of the URI. */
    public String getUserInfo()
    {
        return userInfo;
    }

    /** Sets the user info part of the URI. */
    public void setUserInfo( String userInfo )
    {
        this.userInfo = userInfo;
    }

    /** Returns the host name part of the URI. */
    public String getHostName()
    {
        return hostName;
    }

    /** Sets the host name part of the URI. */
    public void setHostName( String hostName )
    {
        this.hostName = hostName;
    }

    /** Returns the port part of the URI. */
    public String getPort()
    {
        return port;
    }

    /** Sets the port part of the URI. */
    public void setPort( String port )
    {
        this.port = port;
    }

    /** Returns the path part of the URI.  */
    public String getPath()
    {
        return path;
    }

    /** Sets the path part of the URI. */
    public void setPath( String absolutePath )
    {
        path = absolutePath;
    }
}
