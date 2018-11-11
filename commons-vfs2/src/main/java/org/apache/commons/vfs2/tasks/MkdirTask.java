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

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.util.Messages;
import org.apache.tools.ant.BuildException;

/**
 * An Ant task that creates a directory.
 */
public class MkdirTask extends VfsTask {
    private String dirName;

    /**
     * Sets the directory to create.
     *
     * @param dir The directory name.
     */
    public void setDir(final String dir) {
        dirName = dir;
    }

    /**
     * Executes the task.
     *
     * @throws BuildException if an exception occurs.
     */
    @Override
    public void execute() throws BuildException {
        if (dirName == null) {
            final String message = Messages.getString("vfs.tasks/no-directory-specified.error");
            throw new BuildException(message);
        }

        try {
            final FileObject dir = resolveFile(dirName);
            final String message = Messages.getString("vfs.tasks/mkdir.create-folder.info", dir);
            log(message);
            dir.createFolder();
        } catch (final FileSystemException e) {
            throw new BuildException(e);
        }
    }
}
