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
package org.apache.commons.vfs2.util;

import java.lang.ref.WeakReference;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;

/**
 * Wrap a listener with a WeakReference.
 *
 * @since 2.0
 */
public class WeakRefFileListener implements FileListener {
    private final FileSystem fs;
    private final FileName name;
    private final WeakReference<FileListener> listener;

    protected WeakRefFileListener(final FileObject file, final FileListener listener) {
        this.fs = file.getFileSystem();
        this.name = file.getName();
        this.listener = new WeakReference<>(listener);
    }

    /**
     * This will install the {@code listener} at the given {@code file}.
     *
     * @param file The FileObject to listen on.
     * @param listener The FileListener
     */
    public static void installListener(final FileObject file, final FileListener listener) {
        final WeakRefFileListener weakListener = new WeakRefFileListener(file, listener);

        file.getFileSystem().addListener(file, new WeakRefFileListener(file, weakListener));
    }

    /**
     * returns the wrapped listener. If it is gone, the WeakRefFileListener wrapper will remove itself from the list of
     * listeners.
     *
     * @return The FileListener.
     * @throws Exception if an error occurs.
     */
    protected FileListener getListener() throws Exception {
        final FileListener listener = this.listener.get();
        if (listener == null) {
            final FileObject file = fs.resolveFile(name);
            file.getFileSystem().removeListener(file, this);
        }
        return listener;
    }

    /**
     * Called when a file is created.
     *
     * @param event The FileChangeEvent.
     * @throws Exception if an error occurs.
     */
    @Override
    public void fileCreated(final FileChangeEvent event) throws Exception {
        final FileListener listener = getListener();
        if (listener == null) {
            return;
        }
        listener.fileCreated(event);
    }

    /**
     * Called when a file is deleted.
     *
     * @param event The FileChangeEvent.
     * @throws Exception if an error occurs.
     */
    @Override
    public void fileDeleted(final FileChangeEvent event) throws Exception {
        final FileListener listener = getListener();
        if (listener == null) {
            return;
        }
        listener.fileDeleted(event);
    }

    /**
     * Called when a file is changed.
     * <p>
     * This will only happen if you monitor the file using {@link org.apache.commons.vfs2.FileMonitor}.
     *
     * @param event The FileChangeEvent.
     * @throws Exception if an error occurs.
     */
    @Override
    public void fileChanged(final FileChangeEvent event) throws Exception {
        final FileListener listener = getListener();
        if (listener == null) {
            return;
        }
        listener.fileChanged(event);
    }
}
