/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.tasks;

import org.apache.tools.ant.Task;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManagerFactory;
import org.apache.commons.vfs.FileSystemManager;

/**
 * Base class for the VFS Ant tasks.  Provides some utility methods.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1 $ $Date: 2002/10/23 10:58:12 $
 */
public class VfsTask
    extends Task
{
    private FileSystemManager manager;

    /**
     * Resolves a URI to a file.  Relative URI are resolved relative to the
     * project directory.
     *
     * @param uri The URI to resolve.
     */
    protected FileObject resolveFile( final String uri )
        throws FileSystemException
    {
        if ( manager == null )
        {
            manager = FileSystemManagerFactory.getManager();
        }
        return manager.resolveFile( getProject().getBaseDir(), uri );
    }
}
