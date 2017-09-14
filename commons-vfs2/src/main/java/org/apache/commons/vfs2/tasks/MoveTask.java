/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.tasks;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.Selectors;

/**
 * An Ant task that moves matching files.
 * <p>
 * TODO - Delete matching folders.
 */
public class MoveTask extends CopyTask {
    private boolean tryRename;

    /**
     * Enable/disable move/rename of file (if possible).
     *
     * @param tryRename true if the file should be renamed.
     */
    public void setTryRename(final boolean tryRename) {
        this.tryRename = tryRename;
    }

    /**
     * Handles a single source file.
     */
    @Override
    protected void handleOutOfDateFile(final FileObject srcFile, final FileObject destFile) throws FileSystemException {
        if (!tryRename || !srcFile.canRenameTo(destFile)) {
            super.handleOutOfDateFile(srcFile, destFile);

            log("Deleting " + srcFile.getPublicURIString());
            srcFile.delete(Selectors.SELECT_SELF);
        } else {
            log("Rename " + srcFile.getPublicURIString() + " to " + destFile.getPublicURIString());
            srcFile.moveTo(destFile);
            if (!isPreserveLastModified()
                    && destFile.getFileSystem().hasCapability(Capability.SET_LAST_MODIFIED_FILE)) {
                destFile.getContent().setLastModifiedTime(System.currentTimeMillis());
            }
        }
    }
}
