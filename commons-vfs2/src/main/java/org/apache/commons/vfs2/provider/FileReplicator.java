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
package org.apache.commons.vfs2.provider;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;

/**
 * Responsible for making local replicas of files.
 * <p>
 * A file replicator may also implement {@link VfsComponent}.
 */
public interface FileReplicator {
    /**
     * Creates a local copy of the file, and all its descendants.
     *
     * @param srcFile The file to copy.
     * @param selector Selects the files to copy.
     * @return The local copy of the source file.
     * @throws FileSystemException If the source files does not exist, or on error copying.
     */
    File replicateFile(FileObject srcFile, FileSelector selector) throws FileSystemException;
}
