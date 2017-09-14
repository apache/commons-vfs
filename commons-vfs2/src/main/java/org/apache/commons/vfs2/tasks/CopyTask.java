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
 * An Ant task that copies matching files.
 * <p>
 * TODO - Copy folders that do not contain files.
 */
public class CopyTask extends AbstractSyncTask {
    private boolean overwrite;
    private boolean preserveLastModified = true;

    /**
     * Enable/disable overwriting of up-to-date files.
     *
     * @param overwrite true if the file should be overwritten.
     */
    public void setOverwrite(final boolean overwrite) {
        this.overwrite = overwrite;
    }

    /**
     * Enable/disable preserving last modified time of copied files.
     *
     * @param preserveLastModified true if the last modified time should be preserved.
     */
    public void setPreserveLastModified(final boolean preserveLastModified) {
        this.preserveLastModified = preserveLastModified;
    }

    /**
     * @return the current value of overwrite
     */
    public boolean isOverwrite() {
        return overwrite;
    }

    /**
     * @return the current value of preserveLastModified
     */
    public boolean isPreserveLastModified() {
        return preserveLastModified;
    }

    /**
     * Handles an out-of-date file.
     *
     * @param srcFile The source FileObject.
     * @param destFile The destination FileObject.
     */
    @Override
    protected void handleOutOfDateFile(final FileObject srcFile, final FileObject destFile) throws FileSystemException {
        log("Copying " + srcFile.getPublicURIString() + " to " + destFile.getPublicURIString());
        destFile.copyFrom(srcFile, Selectors.SELECT_SELF);
        if (preserveLastModified && srcFile.getFileSystem().hasCapability(Capability.GET_LAST_MODIFIED)
                && destFile.getFileSystem().hasCapability(Capability.SET_LAST_MODIFIED_FILE)) {
            final long lastModTime = srcFile.getContent().getLastModifiedTime();
            destFile.getContent().setLastModifiedTime(lastModTime);
        }
    }

    /**
     * Handles an up-to-date file.
     *
     * @param srcFile The source FileObject.
     * @param destFile The destination FileObject.
     */
    @Override
    protected void handleUpToDateFile(final FileObject srcFile, final FileObject destFile) throws FileSystemException {
        if (overwrite) {
            // Copy the file anyway
            handleOutOfDateFile(srcFile, destFile);
        }
    }
}
