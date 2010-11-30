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
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 * @version $Revision: 262 $ $Date: 2006-12-20T09:14:53.055649Z $
 * @since 2.0
 */
public class WeakRefFileListener implements FileListener
{
    private final FileSystem fs;
    private final FileName name;
    private final WeakReference<FileListener> listener;

    protected WeakRefFileListener(final FileObject file, final FileListener listener)
    {
        this.fs = file.getFileSystem();
        this.name = file.getName();
        this.listener = new WeakReference<FileListener>(listener);
    }

    /**
     * This will install the <code>listener</code> at the given <code>file</code>.
     * @param file The FileObject to listen on.
     * @param listener The FileListener
     */
    public static void installListener(final FileObject file, final FileListener listener)
    {
        WeakRefFileListener weakListener = new WeakRefFileListener(file, listener);

        file.getFileSystem().addListener(file, new WeakRefFileListener(file, weakListener));
    }

    /**
     * returns the wrapped listener. If it is gone, the WeakRefFileListener wrapper will
     * remove itself from the list of listeners.
     * @return The FileListener.
     * @throws Exception if an error occurs.
     */
    protected FileListener getListener() throws Exception
    {
        FileListener listener = this.listener.get();
        if (listener == null)
        {
            FileObject file = fs.resolveFile(name);
            file.getFileSystem().removeListener(file, this);
        }
        return listener;
    }

    /**
     * Called when a file is created.
     * @param event The FileChangeEvent.
     * @throws Exception if an error occurs.
     */
    public void fileCreated(final FileChangeEvent event) throws Exception
    {
        FileListener listener = getListener();
        if (listener == null)
        {
            return;
        }
        listener.fileCreated(event);
    }

    /**
     * Called when a file is deleted.
     * @param event The FileChangeEvent.
     * @throws Exception if an error occurs.
     */
    public void fileDeleted(final FileChangeEvent event) throws Exception
    {
        FileListener listener = getListener();
        if (listener == null)
        {
            return;
        }
        listener.fileDeleted(event);
    }

    /**
     * Called when a file is changed.
     * <p/>
     * This will only happen if you monitor the file using {@link org.apache.commons.vfs2.FileMonitor}.
     * @param event The FileChangeEvent.
     * @throws Exception if an error occurs.
     */
    public void fileChanged(FileChangeEvent event) throws Exception
    {
        FileListener listener = getListener();
        if (listener == null)
        {
            return;
        }
        listener.fileChanged(event);
    }
}
