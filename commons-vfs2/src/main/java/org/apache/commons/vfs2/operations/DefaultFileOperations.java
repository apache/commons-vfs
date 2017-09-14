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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;

/**
 *
 * @since 0.1
 */
public class DefaultFileOperations implements FileOperations {
    /**
     */
    private final FileSystemManager fsmanager;

    /**
     */
    private final FileObject fileObject;

    /**
     *
     * @param file The file.
     */
    public DefaultFileOperations(final FileObject file) {
        fileObject = file;

        fsmanager = file.getFileSystem().getFileSystemManager();
    }

    /**
     * @return The operation classes.
     * @throws FileSystemException If an error occurs.
     */
    @Override
    public Class<? extends FileOperation>[] getOperations() throws FileSystemException {

        final String scheme = fileObject.getURL().getProtocol();
        final FileOperationProvider[] providers = fsmanager.getOperationProviders(scheme);

        if (providers == null) {
            return null;
        }

        final List<Class<? extends FileOperation>> operations = new ArrayList<>();

        for (final FileOperationProvider provider : providers) {
            provider.collectOperations(operations, fileObject);
        }

        @SuppressWarnings("unchecked")
        final Class<? extends FileOperation>[] array = (Class<? extends FileOperation>[]) operations
                .toArray(new Class<?>[] {});
        return array;
    }

    /**
     * @param operationClass The Class that performs the operation.
     * @return The FileOperation.
     * @throws FileSystemException if an error occurs.
     */
    @Override
    public FileOperation getOperation(final Class<? extends FileOperation> operationClass) throws FileSystemException {

        final String scheme = fileObject.getURL().getProtocol();
        final FileOperationProvider[] providers = fsmanager.getOperationProviders(scheme);

        if (providers == null) {
            throw new FileSystemException("vfs.operation/operation-not-supported.error", operationClass);
        }

        FileOperation resultOperation = null;

        for (final FileOperationProvider provider : providers) {
            resultOperation = provider.getOperation(fileObject, operationClass);

            if (resultOperation != null) {
                break;
            }
        }

        if (resultOperation == null) {
            throw new FileSystemException("vfs.operation/operation-not-supported.error", operationClass);
        }

        return resultOperation;
    }

    /**
     * @param operationClass the operation's class.
     * @return true if the operation of specified class is supported for current FileObject and false otherwise.
     *
     * @throws FileSystemException if an error occurs.
     */
    @Override
    public boolean hasOperation(final Class<? extends FileOperation> operationClass) throws FileSystemException {
        final Class<? extends FileOperation>[] operations = getOperations();
        if (operations == null) {
            return false;
        }

        for (final Class<? extends FileOperation> operation : operations) {
            if (operationClass.isAssignableFrom(operation)) {
                return true;
            }
        }
        return false;
    }
}
