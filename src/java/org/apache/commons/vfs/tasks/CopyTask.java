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
 * An Ant task that copies files.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1 $ $Date: 2002/10/23 10:58:52 $
 *
 * @todo Copy folders that do not contain files
 */
public class CopyTask
    extends AbstractSyncTask
{
    protected void handleFile( final FileObject srcFile,
                               final FileObject destFile )
        throws FileSystemException
    {
        log( "Copying " + srcFile + " to " + destFile );
        destFile.copyFrom( srcFile, Selectors.SELECT_SELF );
    }
}
