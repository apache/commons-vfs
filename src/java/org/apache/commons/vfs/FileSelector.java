/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs;

/**
 * This interface is used to select files when traversing a file hierarchy.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1 $ $Date: 2002/04/07 02:27:55 $
 */
public interface FileSelector
{
    /**
     * Determines if a file or folder should be selected.  This method is
     * called in depthwise order (that is, it is called for the children
     * of a folder before it is called for the folder itself).
     *
     * @param fileInfo the file or folder to select.
     * @return true if the file should be selected.
     */
    boolean includeFile( FileSelectInfo fileInfo )
        throws Exception;

    /**
     * Determines whether a folder should be traversed.  If this method returns
     * true, {@link #includeFile} is called for each of the children of
     * the folder, and each of the child folders is recursively traversed.
     *
     * <p>This method is called on a folder before {@link #includeFile}
     * is called.
     *
     * @param fileInfo the file or folder to select.
     *
     * @return true if the folder should be traversed.
     */
    boolean traverseDescendents( FileSelectInfo fileInfo )
        throws Exception;
}
