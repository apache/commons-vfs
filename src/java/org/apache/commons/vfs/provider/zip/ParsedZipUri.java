/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.zip;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.provider.ParsedUri;

/**
 * A parsed Zip URI.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.4 $ $Date: 2002/07/05 04:08:19 $
 */
class ParsedZipUri
    extends ParsedUri
{
    private String zipFileName;
    private FileObject zipFile;

    public String getZipFileName()
    {
        return zipFileName;
    }

    public void setZipFileName( final String zipFileName )
    {
        this.zipFileName = zipFileName;
    }

    public FileObject getZipFile()
    {
        return zipFile;
    }

    public void setZipFile( final FileObject zipFile )
    {
        this.zipFile = zipFile;
    }
}
