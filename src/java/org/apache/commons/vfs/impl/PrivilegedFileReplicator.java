/*
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
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
package org.apache.commons.vfs.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.FileReplicator;
import org.apache.commons.vfs.provider.VfsComponent;
import org.apache.commons.vfs.provider.VfsComponentContext;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * A file replicator that wraps another file replicator, performing
 * the replication as a privileged action.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class PrivilegedFileReplicator
    implements FileReplicator, VfsComponent
{
    private final FileReplicator replicator;
    private final VfsComponent replicatorComponent;

    public PrivilegedFileReplicator(FileReplicator replicator)
    {
        this.replicator = replicator;
        if (replicator instanceof VfsComponent)
        {
            replicatorComponent = (VfsComponent) replicator;
        }
        else
        {
            replicatorComponent = null;
        }
    }

    /**
     * Sets the Logger to use for the component.
     */
    public void setLogger(final Log logger)
    {
        if (replicatorComponent != null)
        {
            replicatorComponent.setLogger(logger);
        }
    }

    /**
     * Sets the context for the replicator.
     */
    public void setContext(final VfsComponentContext context)
    {
        if (replicatorComponent != null)
        {
            replicatorComponent.setContext(context);
        }
    }

    /**
     * Initialises the component.
     */
    public void init() throws FileSystemException
    {
        if (replicatorComponent != null)
        {
            try
            {
                AccessController.doPrivileged(new InitAction());
            }
            catch (final PrivilegedActionException e)
            {
                throw new FileSystemException("vfs.impl/init-replicator.error", null, e);
            }
        }
    }

    /**
     * Closes the replicator.
     */
    public void close()
    {
        if (replicatorComponent != null)
        {
            AccessController.doPrivileged(new CloseAction());
        }
    }

    /**
     * Creates a local copy of the file, and all its descendents.
     */
    public File replicateFile(FileObject srcFile, FileSelector selector)
        throws FileSystemException
    {
        try
        {
            final ReplicateAction action = new ReplicateAction(srcFile, selector);
            return (File) AccessController.doPrivileged(action);
        }
        catch (final PrivilegedActionException e)
        {
            throw new FileSystemException("vfs.impl/replicate-file.error", new Object[]{srcFile.getName()}, e);
        }
    }

    /**
     * An action that initialises the wrapped replicator.
     */
    private class InitAction implements PrivilegedExceptionAction
    {
        /**
         * Performs the action.
         */
        public Object run() throws Exception
        {
            replicatorComponent.init();
            return null;
        }
    }

    /**
     * An action that replicates a file using the wrapped replicator.
     */
    private class ReplicateAction implements PrivilegedExceptionAction
    {
        private final FileObject srcFile;
        private final FileSelector selector;

        public ReplicateAction(final FileObject srcFile,
                               final FileSelector selector)
        {
            this.srcFile = srcFile;
            this.selector = selector;
        }

        /**
         * Performs the action.
         */
        public Object run() throws Exception
        {
            // TODO - Do not pass the selector through.  It is untrusted
            // TODO - Need to determine which files can be read
            return replicator.replicateFile(srcFile, selector);
        }
    }

    /**
     * An action that closes the wrapped replicator.
     */
    private class CloseAction implements PrivilegedAction
    {
        /**
         * Performs the action.
         */
        public Object run()
        {
            replicatorComponent.close();
            return null;
        }
    }
}
