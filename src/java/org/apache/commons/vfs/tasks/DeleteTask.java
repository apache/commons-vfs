/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.commons.vfs.util.Messages;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.Selectors;

/**
 * An Ant task that deletes matching files.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1 $ $Date: 2002/10/23 10:58:52 $
 *
 * @todo Allow selector to be specified.
 */
public class DeleteTask
    extends VfsTask
{
    private String file;

    /**
     * Sets the file/folder to delete.
     * @param file
     */
    public void setFile( final String file )
    {
        this.file = file;
    }

    /**
     * Executes this task.
     */
    public void execute() throws BuildException
    {
        if ( file == null )
        {
            final String message = Messages.getString( "vfs.tasks/delete.no-source-files.error" );
            throw new BuildException( message );
        }

        try
        {
            final FileObject srcFile = resolveFile( file );
            log( "Deleting " + srcFile );
            //srcFile.delete( Selectors.SELECT_ALL );
        }
        catch ( final Exception e )
        {
            throw new BuildException( e );
        }
    }
}
