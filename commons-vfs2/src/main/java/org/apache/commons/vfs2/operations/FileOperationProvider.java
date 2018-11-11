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
package org.apache.commons.vfs2.operations;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

/**
 * FileOperationProvider is responsible for dealing with FileOperation's.
 *
 * @since 0.1
 */
public interface FileOperationProvider {

    /**
     * Gather available operations for the specified FileObject and put them into specified operationsList.
     *
     * @param operationsList the list of available operations for the specified FileObject. The operationList contains
     *            classes of available operations, e.g. Class objects.
     * @param file the FileObject for which we want to get the list of available operations.
     *
     * @throws FileSystemException if list of operations cannot be retrieved.
     */
    void collectOperations(final Collection<Class<? extends FileOperation>> operationsList, final FileObject file)
            throws FileSystemException;

    /**
     * Get implementation for a given FileObject and FileOperation interface.
     *
     * @param file the FileObject for which we need a operation.
     * @param operationClass the Class which instance we are needed.
     * @return the required operation instance.
     *
     * @throws FileSystemException if operation cannot be retrieved.
     */
    FileOperation getOperation(final FileObject file, final Class<? extends FileOperation> operationClass)
            throws FileSystemException;
}
