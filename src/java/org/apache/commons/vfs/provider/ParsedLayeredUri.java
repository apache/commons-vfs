/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider;

/**
 * A data container for information parsed from an absolute URI.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1 $ $Date: 2002/08/22 08:00:38 $
 */
public class ParsedLayeredUri
{
    private String scheme;
    private String outerFileUri;
    private String path;

    public String getScheme()
    {
        return scheme;
    }

    public void setScheme( final String scheme )
    {
        this.scheme = scheme;
    }

    public String getOuterFileUri()
    {
        return outerFileUri;
    }

    public void setOuterFileUri( final String outerFileUri )
    {
        this.outerFileUri = outerFileUri;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath( final String path )
    {
        this.path = path;
    }
}
