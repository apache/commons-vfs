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
 * An Ant task that copies matching files.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 * @todo Copy folders that do not contain files
 */
public class CopyTask
    extends AbstractSyncTask
{
    private boolean overwrite = false;
    private boolean preserveLastModified = true;

    /**
     * Enable/disable overwriting of up-to-date files.
     */
    public void setOverwrite(boolean overwrite)
    {
        this.overwrite = overwrite;
    }

    /**
     * Enable/disable preserving last modified time of copied files.
     */
    public void setPreserveLastModified(boolean preserveLastModified)
    {
        this.preserveLastModified = preserveLastModified;
    }

    /**
     * @return the curent value of overwrite
     */
    public boolean isOverwrite()
    {
        return overwrite;
    }

    /**
     * @return the curent value of preserveLastModified
     */
    public boolean isPreserveLastModified()
    {
        return preserveLastModified;
    }

    /**
     * Handles an out-of-date file.
     */
    protected void handleOutOfDateFile(final FileObject srcFile,
                                       final FileObject destFile)
        throws FileSystemException
    {
        log("Copying " + srcFile + " to " + destFile);
        destFile.copyFrom(srcFile, Selectors.SELECT_SELF);
        if (preserveLastModified)
        {
            final long lastModTime = srcFile.getContent().getLastModifiedTime();
            destFile.getContent().setLastModifiedTime(lastModTime);
        }
    }

    /**
     * Handles an up-to-date file.
     */
    protected void handleUpToDateFile(final FileObject srcFile,
                                      final FileObject destFile)
        throws FileSystemException
    {
        if (overwrite)
        {
            // Copy the file anyway
            handleOutOfDateFile(srcFile, destFile);
        }
    }
}
