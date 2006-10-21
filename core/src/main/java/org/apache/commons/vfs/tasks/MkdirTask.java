/*
 * Copyright 2002-2005 The Apache Software Foundation.
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
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.util.Messages;
import org.apache.tools.ant.BuildException;

/**
 * An Ant task that creates a directory.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class MkdirTask
    extends VfsTask
{
    private String dirName;

    /**
     * Sets the directory to create.
     *
     * @param dir
     */
    public void setDir(final String dir)
    {
        dirName = dir;
    }

    /**
     * Executes the task.
     */
    public void execute() throws BuildException
    {
        if (dirName == null)
        {
            final String message = Messages.getString("vfs.tasks/no-directory-specified.error");
            throw new BuildException(message);
        }

        try
        {
            final FileObject dir = resolveFile(dirName);
            final String message = Messages.getString("vfs.tasks/mkdir.create-folder.info", dir);
            log(message);
            dir.createFolder();
        }
        catch (final FileSystemException e)
        {
            throw new BuildException(e);
        }
    }
}
