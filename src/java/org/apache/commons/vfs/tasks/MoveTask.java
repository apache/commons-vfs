/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.tasks;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.Selectors;

/**
 * An Ant task that moves files.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1 $ $Date: 2002/10/23 10:58:52 $
 *
 * @todo Delete matching folders
 */
public class MoveTask
    extends CopyTask
{
    /**
     * Handles a single source file.
     */
    protected void handleFile( final FileObject srcFile,
                               final FileObject destFile )
        throws FileSystemException
    {
        super.handleFile( srcFile, destFile );
        log( "Deleting " + srcFile );
        srcFile.delete( Selectors.SELECT_SELF );
    }
}
