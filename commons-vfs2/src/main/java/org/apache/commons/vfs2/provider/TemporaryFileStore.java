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

import org.apache.commons.vfs2.FileSystemException;

/**
 * Manages a repository of temporary local files.
 */
public interface TemporaryFileStore {
    /**
     * Allocates a new temporary file. The file (and all its descendants) will be deleted when this store is closed.
     *
     * @param basename The name of the file.
     * @return The temporary file.
     * @throws FileSystemException if an error occurs.
     */
    File allocateFile(String basename) throws FileSystemException;
}
