/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
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
