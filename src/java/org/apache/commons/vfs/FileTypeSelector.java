/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs;

/**
 * A {@link FileSelector} that selects files of a particular type.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1 $ $Date: 2002/10/23 10:56:32 $
 */
public class FileTypeSelector
    implements FileSelector
{
    private final FileType type;

    public FileTypeSelector( final FileType type )
    {
        this.type = type;
    }

    /**
     * Determines if a file or folder should be selected.
     */
    public boolean includeFile( final FileSelectInfo fileInfo )
        throws FileSystemException
    {
        return ( fileInfo.getFile().getType() == type );
    }

    /**
     * Determines whether a folder should be traversed.
     */
    public boolean traverseDescendents( final FileSelectInfo fileInfo )
    {
        return true;
    }
}
