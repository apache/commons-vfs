/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs;

/**
 * A {@link FileSelector} which selects all files in a particular depth range.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.2 $ $Date: 2002/07/05 04:08:17 $
 */
public final class FileDepthSelector
    implements FileSelector
{
    private final int minDepth;
    private final int maxDepth;

    public FileDepthSelector( int minDepth, int maxDepth )
    {
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
    }

    /**
     * Determines if a file or folder should be selected.
     */
    public boolean includeFile( final FileSelectInfo fileInfo )
        throws FileSystemException
    {
        final int depth = fileInfo.getDepth();
        return minDepth <= depth && depth <= maxDepth;
    }

    /**
     * Determines whether a folder should be traversed.
     */
    public boolean traverseDescendents( final FileSelectInfo fileInfo )
        throws FileSystemException
    {
        return fileInfo.getDepth() < maxDepth;
    }
}
