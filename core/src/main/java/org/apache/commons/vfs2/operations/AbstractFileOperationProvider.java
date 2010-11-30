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
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;


/**
 *
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 * @since 0.1
 */
public abstract class AbstractFileOperationProvider implements FileOperationProvider
{

    /**
     * Available operations. Operations could be registered for different schemes.
     * Some operations can work only for "file" scheme, other - for "svnhttp(s)",
     * "svn", "svnssh", but not for "file", etc. The Map has scheme as a key and
     * Colleaction of operations that are available for that scheme.
     */
    private final Collection<Class<? extends FileOperation>> operations =
        new ArrayList<Class<? extends FileOperation>>();

    /**
     * Gather available operations for the specified FileObject and put them into
     * specified operationsList.
     *
     * @param operationsList
     *            the list of available operations for the specivied FileObject.
     *            The operationList contains classes of available operations, e.g.
     *            Class objects.
     * @param file
     *            the FileObject for which we want to get the list of available
     *            operations.
     * @throws org.apache.commons.vfs2.FileSystemException
     *             if list of operations cannto be retrieved.
     */
    public final void collectOperations(final Collection<Class<? extends FileOperation>> operationsList,
            final FileObject file) throws FileSystemException
    {
        doCollectOperations(operations, operationsList, file);
    }

    /**
     *
     * @throws FileSystemException
     */
    protected abstract void doCollectOperations(
            final Collection<Class<? extends FileOperation>> availableOperations,
            final Collection<Class<? extends FileOperation>> resultList,
            final FileObject file) throws FileSystemException;

    /**
     * @param file
     *            the FileObject for which we need a operation.
     * @param operationClass
     *            the Class which instance we are needed.
     * @return the requried operation instance.
     * @throws org.apache.commons.vfs2.FileSystemException
     *             if operation cannot be retrieved.
     */
    public final FileOperation getOperation(FileObject file, Class<? extends FileOperation> operationClass)
            throws FileSystemException
    {
        Class<? extends FileOperation> implementation = lookupOperation(operationClass);

        FileOperation operationInstance = instantiateOperation(file, implementation);

        return operationInstance;
    }

    /**
     *
     * @param operationClass
     * @return
     * @throws FileSystemException
     */
    protected abstract FileOperation instantiateOperation(final FileObject file,
            final Class<? extends FileOperation> operationClass) throws FileSystemException;

    /**
     *
     * @param operationClass
     * @return never returns null
     */
    protected final Class<? extends FileOperation> lookupOperation(final Class<? extends FileOperation> operationClass)
            throws FileSystemException
    {
        // check validity of passed class
        if (!FileOperation.class.isAssignableFrom(operationClass))
        {
            throw new FileSystemException("vfs.operation/wrong-type.error", operationClass);
        }

        // find appropriate class
        Class<? extends FileOperation> foundClass = null;
        Iterator<Class<? extends FileOperation>> iterator = operations.iterator();
        while (iterator.hasNext())
        {
            Class<? extends FileOperation> operation = iterator.next();
            if (operationClass.isAssignableFrom(operation))
            {
                foundClass = operation;
                break;
            }
        }

        if (foundClass == null)
        {
            throw new FileSystemException("vfs.operation/not-found.error", operationClass);
        }

        return foundClass;
    }

    /**
     *
     * @param operationClass
     * @throws FileSystemException
     */
    protected final void addOperation(final Class<? extends FileOperation> operationClass)
            throws FileSystemException
    {
        // check validity of passed class
        if (!FileOperation.class.isAssignableFrom(operationClass))
        {
            throw new FileSystemException("vfs.operation/cant-register.error", operationClass);
        }

        // ok, lets add it to the list
        operations.add(operationClass);
    }
}
