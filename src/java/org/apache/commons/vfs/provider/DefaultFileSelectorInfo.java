/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;

/**
 * A default {@link FileSelectInfo} implementation.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.2 $ $Date: 2002/07/05 04:08:17 $
 */
final class DefaultFileSelectorInfo
    implements FileSelectInfo
{
    private FileObject baseFolder;
    private FileObject file;
    private int depth;

    public FileObject getBaseFolder()
    {
        return baseFolder;
    }

    public void setBaseFolder( final FileObject baseFolder )
    {
        this.baseFolder = baseFolder;
    }

    public FileObject getFile()
    {
        return file;
    }

    public void setFile( final FileObject file )
    {
        this.file = file;
    }

    public int getDepth()
    {
        return depth;
    }

    public void setDepth( final int depth )
    {
        this.depth = depth;
    }
}
