/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.impl;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.apache.commons.logging.Log;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.FileReplicator;
import org.apache.commons.vfs2.provider.VfsComponent;
import org.apache.commons.vfs2.provider.VfsComponentContext;

/**
 * A file replicator that wraps another file replicator, performing the replication as a privileged action.
 */
public class PrivilegedFileReplicator implements FileReplicator, VfsComponent {

    /**
     * An action that closes the wrapped replicator.
     */
    private final class CloseAction implements PrivilegedAction<Object> {

        /**
         * Performs the action.
         */
        @Override
        public Object run() {
            replicatorComponent.close();
            return null;
        }
    }

    /**
     * An action that initializes the wrapped replicator.
     */
    private final class InitAction implements PrivilegedExceptionAction<Object> {

        /**
         * Performs the action.
         */
        @Override
        public Object run() throws Exception {
            replicatorComponent.init();
            return null;
        }
    }

    /**
     * An action that replicates a file using the wrapped replicator.
     */
    private final class ReplicateAction implements PrivilegedExceptionAction<File> {
        private final FileObject srcFile;
        private final FileSelector selector;

        ReplicateAction(final FileObject srcFile, final FileSelector selector) {
            this.srcFile = srcFile;
            this.selector = selector;
        }

        /**
         * Performs the action.
         *
         * @throws Exception if an error occurs.
         */
        @Override
        public File run() throws Exception {
            // TODO - Do not pass the selector through. It is untrusted
            // TODO - Need to determine which files can be read
            return replicator.replicateFile(srcFile, selector);
        }
    }

    private final FileReplicator replicator;

    private final VfsComponent replicatorComponent;

    /**
     * Constructs a new instance.
     *
     * @param replicator The replicator.
     */
    public PrivilegedFileReplicator(final FileReplicator replicator) {
        this.replicator = replicator;
        if (replicator instanceof VfsComponent) {
            replicatorComponent = (VfsComponent) replicator;
        } else {
            replicatorComponent = null;
        }
    }

    /**
     * Closes the replicator.
     */
    @Override
    public void close() {
        if (replicatorComponent != null) {
            AccessController.doPrivileged(new CloseAction());
        }
    }

    /**
     * Initializes the component.
     *
     * @throws FileSystemException if an error occurs.
     */
    @Override
    public void init() throws FileSystemException {
        if (replicatorComponent != null) {
            try {
                AccessController.doPrivileged(new InitAction());
            } catch (final PrivilegedActionException e) {
                throw new FileSystemException("vfs.impl/init-replicator.error", e);
            }
        }
    }

    /**
     * Creates a local copy of the file, and all its descendants.
     *
     * @param srcFile The source FileObject.
     * @param selector The file selector.
     * @return The replicated file.
     * @throws FileSystemException if an error occurs.
     */
    @Override
    public File replicateFile(final FileObject srcFile, final FileSelector selector) throws FileSystemException {
        try {
            final ReplicateAction action = new ReplicateAction(srcFile, selector);
            return AccessController.doPrivileged(action);
        } catch (final PrivilegedActionException e) {
            throw new FileSystemException("vfs.impl/replicate-file.error", e, srcFile.getName());
        }
    }

    /**
     * Sets the context for the replicator.
     *
     * @param context The component context.
     */
    @Override
    public void setContext(final VfsComponentContext context) {
        if (replicatorComponent != null) {
            replicatorComponent.setContext(context);
        }
    }

    /**
     * Sets the Logger to use for the component.
     *
     * @param logger The logger.
     */
    @Override
    public void setLogger(final Log logger) {
        if (replicatorComponent != null) {
            replicatorComponent.setLogger(logger);
        }
    }
}
