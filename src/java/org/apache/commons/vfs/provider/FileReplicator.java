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
package org.apache.commons.vfs.provider;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;

import java.io.File;

/**
 * Responsible for making local replicas of files.
 * <p/>
 * <p>A file replicator may also implement {@link VfsComponent}.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public interface FileReplicator
{
    /**
     * Creates a local copy of the file, and all its descendents.
     *
     * @param srcFile  The file to copy.
     * @param selector Selects the files to copy.
     * @return The local copy of the source file.
     * @throws FileSystemException If the source files does not exist, or on error copying.
     */
    File replicateFile(FileObject srcFile, FileSelector selector)
        throws FileSystemException;
}
