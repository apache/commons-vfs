/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs;

/**
 * Several standard file selectors.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1 $ $Date: 2002/10/23 10:56:32 $
 */
public interface Selectors
{
    /**
     * A {@link FileSelector} that selects only the base file/folder.
     */
    FileSelector SELECT_SELF = new FileDepthSelector( 0, 0 );

    /**
     * A {@link FileSelector} that selects the base file/folder and its
     * direct children.
     */
    FileSelector SELECT_SELF_AND_CHILDREN = new FileDepthSelector( 0, 1 );

    /**
     * A {@link FileSelector} that selects only the direct children
     * of the base folder.
     */
    FileSelector SELECT_CHILDREN = new FileDepthSelector( 1, 1 );

    /**
     * A {@link FileSelector} that selects all the descendents of the
     * base folder, but does not select the base folder itself.
     */
    FileSelector EXCLUDE_SELF = new FileDepthSelector( 1, Integer.MAX_VALUE );

    /**
     * A {@link FileSelector} that only files (not folders).
     */
    FileSelector SELECT_FILES = new FileTypeSelector( FileType.FILE );

    /**
     * A {@link FileSelector} that only folders (not files).
     */
    FileSelector SELECT_FOLDERS = new FileTypeSelector( FileType.FOLDER );

    /**
     * A {@link FileSelector} that selects the base file/folder, plus all
     * its descendents.
     */
    FileSelector SELECT_ALL = new AllFileSelector();
}
