/*
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs.tasks;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.util.Messages;
import org.apache.tools.ant.BuildException;

/**
 * An Ant task that deletes matching files.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.7 $ $Date: 2004/02/28 03:35:52 $
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
            srcFile.delete( Selectors.SELECT_ALL );
        }
        catch ( final Exception e )
        {
            throw new BuildException( e );
        }
    }
}
