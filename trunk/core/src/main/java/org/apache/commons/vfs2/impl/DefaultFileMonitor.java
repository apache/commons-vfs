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
package org.apache.commons.vfs2.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileMonitor;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.AbstractFileSystem;

/**
 * A polling {@link FileMonitor} implementation.
 * <p>
 * The DefaultFileMonitor is a Thread based polling file system monitor with a 1
 * second delay.
 *
 * <h2>Design:</h2>
 *
 * There is a Map of monitors known as FileMonitorAgents. With the thread running,
 * each FileMonitorAgent object is asked to "check" on the file it is
 * responsible for. To do this check, the cache is cleared.
 * <ul>
 * <li>If the file existed before the refresh and it no longer exists, a delete
 * event is fired.</li>
 * <li>If the file existed before the refresh and it still exists, check the
 * last modified timestamp to see if that has changed.</li>
 * <li>If it has, fire a change event.</li>
 * </ul>
 * With each file delete, the FileMonitorAgent of the parent is asked to
 * re-build its
 * list of children, so that they can be accurately checked when there are new
 * children.
 * <p>
 * New files are detected during each "check" as each file does a check for new
 * children.
 * If new children are found, create events are fired recursively if recursive
 * descent is enabled.
 * <p>
 * For performance reasons, added a delay that increases as the number of files
 * monitored
 * increases. The default is a delay of 1 second for every 1000 files processed.
 *
 * <h2>Example usage:</h2><pre>
 * FileSystemManager fsManager = VFS.getManager();
 * FileObject listendir = fsManager.resolveFile("/home/username/monitored/");
 *
 * DefaultFileMonitor fm = new DefaultFileMonitor(new CustomFileListener());
 * fm.setRecursive(true);
 * fm.addFile(listendir);
 * fm.start();</pre>
 * <i>(where CustomFileListener is a class that implements the FileListener
 * interface.)</i>
 */
public class DefaultFileMonitor implements Runnable, FileMonitor
{
    private static final Log LOG = LogFactory.getLog(DefaultFileMonitor.class);

    private static final long DEFAULT_DELAY = 1000;

    private static final int DEFAULT_MAX_FILES = 1000;

    /**
     * Map from FileName to FileObject being monitored.
     */
    private final Map<FileName, FileMonitorAgent> monitorMap = new HashMap<FileName, FileMonitorAgent>();

    /**
     * The low priority thread used for checking the files being monitored.
     */
    private Thread monitorThread;

    /**
     * File objects to be removed from the monitor map.
     */
    private final Stack<FileObject> deleteStack = new Stack<FileObject>();

    /**
     * File objects to be added to the monitor map.
     */
    private final Stack<FileObject> addStack = new Stack<FileObject>();

    /**
     * A flag used to determine if the monitor thread should be running.
     */
    private volatile boolean shouldRun = true; // used for inter-thread communication

    /**
     * A flag used to determine if adding files to be monitored should be recursive.
     */
    private boolean recursive;

    /**
     * Set the delay between checks
     */
    private long delay = DEFAULT_DELAY;

    /**
     * Set the number of files to check until a delay will be inserted
     */
    private int checksPerRun = DEFAULT_MAX_FILES;

    /**
     * A listener object that if set, is notified on file creation and deletion.
     */
    private final FileListener listener;

    public DefaultFileMonitor(final FileListener listener)
    {
        this.listener = listener;
    }

    /**
     * Access method to get the recursive setting when adding files for monitoring.
     * @return true if monitoring is enabled for children.
     */
    public boolean isRecursive()
    {
        return this.recursive;
    }

    /**
     * Access method to set the recursive setting when adding files for monitoring.
     * @param newRecursive true if monitoring should be enabled for children.
     */
    public void setRecursive(final boolean newRecursive)
    {
        this.recursive = newRecursive;
    }

    /**
     * Access method to get the current FileListener object notified when there
     * are changes with the files added.
     * @return The FileListener.
     */
    FileListener getFileListener()
    {
        return this.listener;
    }

    /**
     * Adds a file to be monitored.
     * @param file The FileObject to monitor.
     */
    @Override
    public void addFile(final FileObject file)
    {
        doAddFile(file);
        try
        {
            // add all direct children too
            if (file.getType().hasChildren())
            {
                // Traverse the children
                final FileObject[] children = file.getChildren();
                for (final FileObject element : children)
                {
                    doAddFile(element);
                }
            }
        }
        catch (final FileSystemException fse)
        {
            LOG.error(fse.getLocalizedMessage(), fse);
        }
    }

    /**
     * Adds a file to be monitored.
     * @param file The FileObject to add.
     */
    private void doAddFile(final FileObject file)
    {
        synchronized (this.monitorMap)
        {
            if (this.monitorMap.get(file.getName()) == null)
            {
                this.monitorMap.put(file.getName(), new FileMonitorAgent(this,
                    file));

                try
                {
                    if (this.listener != null)
                    {
                        file.getFileSystem().addListener(file, this.listener);
                    }

                    if (file.getType().hasChildren() && this.recursive)
                    {
                        // Traverse the children
                        final FileObject[] children = file.getChildren();
                        for (final FileObject element : children)
                        {
                            this.addFile(element); // Add depth first
                        }
                    }

                }
                catch (final FileSystemException fse)
                {
                    LOG.error(fse.getLocalizedMessage(), fse);
                }

            }
        }
    }

    /**
     * Removes a file from being monitored.
     * @param file The FileObject to remove from monitoring.
     */
    @Override
    public void removeFile(final FileObject file)
    {
        synchronized (this.monitorMap)
        {
            final FileName fn = file.getName();
            if (this.monitorMap.get(fn) != null)
            {
                FileObject parent;
                try
                {
                    parent = file.getParent();
                }
                catch (final FileSystemException fse)
                {
                    parent = null;
                }

                this.monitorMap.remove(fn);

                if (parent != null)
                { // Not the root
                    final FileMonitorAgent parentAgent =
                        this.monitorMap.get(parent.getName());
                    if (parentAgent != null)
                    {
                        parentAgent.resetChildrenList();
                    }
                }
            }
        }
    }

    /**
     * Queues a file for removal from being monitored.
     * @param file The FileObject to be removed from being monitored.
     */
    protected void queueRemoveFile(final FileObject file)
    {
        this.deleteStack.push(file);
    }

    /**
     * Get the delay between runs.
     * @return The delay period.
     */
    public long getDelay()
    {
        return delay;
    }

    /**
     * Set the delay between runs.
     * @param delay The delay period.
     */
    public void setDelay(final long delay)
    {
        if (delay > 0)
        {
            this.delay = delay;
        }
        else
        {
            this.delay = DEFAULT_DELAY;
        }
    }

    /**
     * get the number of files to check per run.
     * @return The number of files to check per iteration.
     */
    public int getChecksPerRun()
    {
        return checksPerRun;
    }

    /**
     * set the number of files to check per run.
     * a additional delay will be added if there are more files to check
     *
     * @param checksPerRun a value less than 1 will disable this feature
     */
    public void setChecksPerRun(final int checksPerRun)
    {
        this.checksPerRun = checksPerRun;
    }

    /**
     * Queues a file for addition to be monitored.
     * @param file The FileObject to add.
     */
    protected void queueAddFile(final FileObject file)
    {
        this.addStack.push(file);
    }

    /**
     * Starts monitoring the files that have been added.
     */
    public void start()
    {
        if (this.monitorThread == null)
        {
            this.monitorThread = new Thread(this);
            this.monitorThread.setDaemon(true);
            this.monitorThread.setPriority(Thread.MIN_PRIORITY);
        }
        this.monitorThread.start();
    }

    /**
     * Stops monitoring the files that have been added.
     */
    public void stop()
    {
        this.shouldRun = false;
    }

    /**
     * Asks the agent for each file being monitored to check its file for changes.
     */
    @Override
    public void run()
    {
        mainloop:
        while (!monitorThread.isInterrupted() && this.shouldRun)
        {
            // For each entry in the map
            Object[] fileNames;
            synchronized (this.monitorMap)
            {
                fileNames = this.monitorMap.keySet().toArray();
            }
            for (int iterFileNames = 0; iterFileNames < fileNames.length;
                 iterFileNames++)
            {
                final FileName fileName = (FileName) fileNames[iterFileNames];
                FileMonitorAgent agent;
                synchronized (this.monitorMap)
                {
                    agent = this.monitorMap.get(fileName);
                }
                if (agent != null)
                {
                    agent.check();
                }

                if (getChecksPerRun() > 0 && (iterFileNames + 1) % getChecksPerRun() == 0)
                {
                    try
                    {
                        Thread.sleep(getDelay());
                    }
                    catch (final InterruptedException e)
                    {
                        // Woke up.
                    }
                }

                if (monitorThread.isInterrupted() || !this.shouldRun)
                {
                    continue mainloop;
                }
            }

            while (!this.addStack.empty())
            {
                this.addFile(this.addStack.pop());
            }

            while (!this.deleteStack.empty())
            {
                this.removeFile(this.deleteStack.pop());
            }

            try
            {
                Thread.sleep(getDelay());
            }
            catch (final InterruptedException e)
            {
                continue;
            }
        }

        this.shouldRun = true;
    }

    /**
     * File monitor agent.
     */
    private static final class FileMonitorAgent
    {
        private final FileObject file;
        private final DefaultFileMonitor fm;

        private boolean exists;
        private long timestamp;
        private Map<FileName, Object> children;

        private FileMonitorAgent(final DefaultFileMonitor fm, final FileObject file)
        {
            this.fm = fm;
            this.file = file;

            this.refresh();
            this.resetChildrenList();

            try
            {
                this.exists = this.file.exists();
            }
            catch (final FileSystemException fse)
            {
                this.exists = false;
                this.timestamp = -1;
            }

            if (this.exists)
            {
                try
                {
                    this.timestamp = this.file.getContent().getLastModifiedTime();
                }
                catch (final FileSystemException fse)
                {
                    this.timestamp = -1;
                }
            }
        }

        private void resetChildrenList()
        {
            try
            {
                if (this.file.getType().hasChildren())
                {
                    this.children = new HashMap<FileName, Object>();
                    final FileObject[] childrenList = this.file.getChildren();
                    for (final FileObject element : childrenList)
                    {
                        this.children.put(element.getName(), new
                            Object()); // null?
                    }
                }
            }
            catch (final FileSystemException fse)
            {
                this.children = null;
            }
        }


        /**
         * Clear the cache and re-request the file object
         */
        private void refresh()
        {
            try
            {
                this.file.refresh();
            }
            catch (final FileSystemException fse)
            {
                LOG.error(fse.getLocalizedMessage(), fse);
            }
        }


        /**
         * Recursively fires create events for all children if recursive descent is
         * enabled. Otherwise the create event is only fired for the initial
         * FileObject.
         * @param child The child to add.
         */
        private void fireAllCreate(final FileObject child)
        {
            // Add listener so that it can be triggered
            if (this.fm.getFileListener() != null)
            {
                child.getFileSystem().addListener(child, this.fm.getFileListener());
            }

            ((AbstractFileSystem) child.getFileSystem()).fireFileCreated(child);

            // Remove it because a listener is added in the queueAddFile
            if (this.fm.getFileListener() != null)
            {
                child.getFileSystem().removeListener(child,
                    this.fm.getFileListener());
            }

            this.fm.queueAddFile(child); // Add

            try
            {
                if (this.fm.isRecursive() && child.getType().hasChildren())
                {
                    final FileObject[] newChildren = child.getChildren();
                    for (final FileObject element : newChildren)
                    {
                        fireAllCreate(element);
                    }
                }
            }
            catch (final FileSystemException fse)
            {
                LOG.error(fse.getLocalizedMessage(), fse);
            }
        }

        /**
         * Only checks for new children. If children are removed, they'll
         * eventually be checked.
         */
        private void checkForNewChildren()
        {
            try
            {
                if (this.file.getType().hasChildren())
                {
                    final FileObject[] newChildren = this.file.getChildren();
                    if (this.children != null)
                    {
                        // See which new children are not listed in the current children map.
                        final Map<FileName, Object> newChildrenMap = new HashMap<FileName, Object>();
                        final Stack<FileObject> missingChildren = new Stack<FileObject>();

                        for (int i = 0; i < newChildren.length; i++)
                        {
                            newChildrenMap.put(newChildren[i].getName(), new
                                Object()); // null ?
                            // If the child's not there
                            if
                                (!this.children.containsKey(newChildren[i].getName()))
                            {
                                missingChildren.push(newChildren[i]);
                            }
                        }

                        this.children = newChildrenMap;

                        // If there were missing children
                        if (!missingChildren.empty())
                        {

                            while (!missingChildren.empty())
                            {
                                final FileObject child = missingChildren.pop();
                                this.fireAllCreate(child);
                            }
                        }

                    }
                    else
                    {
                        // First set of children - Break out the cigars
                        if (newChildren.length > 0)
                        {
                            this.children = new HashMap<FileName, Object>();
                        }
                        for (final FileObject element : newChildren)
                        {
                            this.children.put(element.getName(), new
                                Object()); // null?
                            this.fireAllCreate(element);
                        }
                    }
                }
            }
            catch (final FileSystemException fse)
            {
                LOG.error(fse.getLocalizedMessage(), fse);
            }
        }

        private void check()
        {
            this.refresh();

            try
            {
                // If the file existed and now doesn't
                if (this.exists && !this.file.exists())
                {
                    this.exists = this.file.exists();
                    this.timestamp = -1;

                    // Fire delete event

                    ((AbstractFileSystem)
                        this.file.getFileSystem()).fireFileDeleted(this.file);

                    // Remove listener in case file is re-created. Don't want to fire twice.
                    if (this.fm.getFileListener() != null)
                    {
                        this.file.getFileSystem().removeListener(this.file,
                            this.fm.getFileListener());
                    }

                    // Remove from map
                    this.fm.queueRemoveFile(this.file);
                }
                else if (this.exists && this.file.exists())
                {

                    // Check the timestamp to see if it has been modified
                    if (this.timestamp != this.file.getContent().getLastModifiedTime())
                    {
                        this.timestamp = this.file.getContent().getLastModifiedTime();
                        // Fire change event

                        // Don't fire if it's a folder because new file children
                        // and deleted files in a folder have their own event triggered.
                        if (!this.file.getType().hasChildren())
                        {
                            ((AbstractFileSystem)
                                this.file.getFileSystem()).fireFileChanged(this.file);
                        }
                    }

                }
                else if (!this.exists && this.file.exists())
                {
                    this.exists = this.file.exists();
                    this.timestamp = this.file.getContent().getLastModifiedTime();
                    // Don't fire if it's a folder because new file children
                    // and deleted files in a folder have their own event triggered.
                    if (!this.file.getType().hasChildren())
                    {
                        ((AbstractFileSystem)
                                this.file.getFileSystem()).fireFileCreated(this.file);
                    }
                }

                this.checkForNewChildren();

            }
            catch (final FileSystemException fse)
            {
                LOG.error(fse.getLocalizedMessage(), fse);
            }
        }

    }
}
