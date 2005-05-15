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
import org.apache.commons.vfs.Selectors;

/**
 * An Ant task that moves matching files.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 * @todo Delete matching folders
 */
public class MoveTask
    extends CopyTask
{
    private boolean tryRename = false;

    /**
     * Enable/disable move/rename of file (if possible)
     */
    public void setTryRename(boolean tryRename)
    {
        this.tryRename = tryRename;
    }

    /**
     * Handles a single source file.
     */
    protected void handleOutOfDateFile(final FileObject srcFile,
                                       final FileObject destFile)
        throws FileSystemException
    {
        if (!tryRename || !srcFile.canRenameTo(destFile))
        {
            super.handleOutOfDateFile(srcFile, destFile);

            log("Deleting " + srcFile);
            srcFile.delete(Selectors.SELECT_SELF);
        }
        else
        {
            log("Rename " + srcFile + " to " + destFile);
            srcFile.moveTo(destFile);
            if (!isPreserveLastModified())
            {
                destFile.getContent().setLastModifiedTime(System.currentTimeMillis());
            }
        }
    }
}
