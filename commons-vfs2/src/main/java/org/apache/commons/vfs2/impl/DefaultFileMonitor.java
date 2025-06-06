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

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ThreadFactory;
import java.util.stream.Stream;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
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
 * The DefaultFileMonitor is a Thread based polling file system monitor with a 1 second delay.
 * </p>
 *
 * <h2>Design:</h2>
 * <p>
 * There is a Map of monitors known as FileMonitorAgents. With the thread running, each FileMonitorAgent object is asked
 * to "check" on the file it is responsible for. To do this check, the cache is cleared.
 * </p>
 * <ul>
 * <li>If the file existed before the refresh and it no longer exists, a delete event is fired.</li>
 * <li>If the file existed before the refresh and it still exists, check the last modified timestamp to see if that has
 * changed.</li>
 * <li>If it has, fire a change event.</li>
 * </ul>
 * <p>
 * With each file delete, the FileMonitorAgent of the parent is asked to re-build its list of children, so that they can
 * be accurately checked when there are new children.
 * </p>
 * <p>
 * New files are detected during each "check" as each file does a check for new children. If new children are found,
 * create events are fired recursively if recursive descent is enabled.
 * </p>
 * <p>
 * For performance reasons, added a delay that increases as the number of files monitored increases. The default is a
 * delay of 1 second for every 1000 files processed.
 * </p>
 * <h2>Example usage:</h2>
 *
 * <pre>
 * FileSystemManager fsManager = VFS.getManager();
 * FileObject listenDir = fsManager.resolveFile("/home/username/monitored/");
 *
 * DefaultFileMonitor fm = new DefaultFileMonitor(new CustomFileListener());
 * fm.setRecursive(true);
 * fm.addFile(listenDir);
 * fm.start();
 * </pre>
 *
 * <em>(where CustomFileListener is a class that implements the FileListener interface.)</em>
 */
// TODO Add a Builder so we can construct and start.
public class DefaultFileMonitor implements Runnable, FileMonitor, AutoCloseable {

    /**
     * File monitor agent.
     */
    private static final class FileMonitorAgent {

        private final FileObject fileObject;
        private final DefaultFileMonitor defaultFileMonitor;
        private boolean exists;
        private long timestamp;
        private Map<FileName, Object> children;

        private FileMonitorAgent(final DefaultFileMonitor defaultFileMonitor, final FileObject fileObject) {
            this.defaultFileMonitor = defaultFileMonitor;
            this.fileObject = fileObject;

            refresh();
            resetChildrenList();

            try {
                exists = fileObject.exists();
            } catch (final FileSystemException fse) {
                exists = false;
                timestamp = -1;
            }

            if (exists) {
                try {
                    timestamp = fileObject.getContent().getLastModifiedTime();
                } catch (final FileSystemException fse) {
                    timestamp = -1;
                }
            }
        }

        private void check() {
            refresh();

            try {
                // If the file existed and now doesn't
                if (exists && !fileObject.exists()) {
                    exists = fileObject.exists();
                    timestamp = -1;

                    // Fire delete event

                    ((AbstractFileSystem) fileObject.getFileSystem()).fireFileDeleted(fileObject);

                    // Remove listener in case file is re-created. Don't want to fire twice.
                    if (defaultFileMonitor.getFileListener() != null) {
                        fileObject.getFileSystem().removeListener(fileObject, defaultFileMonitor.getFileListener());
                    }

                    // Remove from map
                    defaultFileMonitor.queueRemoveFile(fileObject);
                } else if (exists && fileObject.exists()) {

                    // Check the timestamp to see if it has been modified
                    if (timestamp != fileObject.getContent().getLastModifiedTime()) {
                        timestamp = fileObject.getContent().getLastModifiedTime();
                        // Fire change event

                        // Don't fire if it's a folder because new file children
                        // and deleted files in a folder have their own event triggered.
                        if (!fileObject.getType().hasChildren()) {
                            ((AbstractFileSystem) fileObject.getFileSystem()).fireFileChanged(fileObject);
                        }
                    }

                } else if (!exists && fileObject.exists()) {
                    exists = fileObject.exists();
                    timestamp = fileObject.getContent().getLastModifiedTime();
                    // Don't fire if it's a folder because new file children
                    // and deleted files in a folder have their own event triggered.
                    if (!fileObject.getType().hasChildren()) {
                        ((AbstractFileSystem) fileObject.getFileSystem()).fireFileCreated(fileObject);
                    }
                }

                checkForNewChildren();

            } catch (final FileSystemException fse) {
                LOG.error(fse.getLocalizedMessage(), fse);
            }
        }

        /**
         * Only checks for new children. If children are removed, they'll eventually be checked.
         */
        private void checkForNewChildren() {
            try {
                if (fileObject.getType().hasChildren()) {
                    final FileObject[] newChildren = fileObject.getChildren();
                    if (children != null) {
                        // See which new children are not listed in the current children map.
                        final Map<FileName, Object> newChildrenMap = new HashMap<>();
                        final Stack<FileObject> missingChildren = new Stack<>();

                        for (final FileObject element : newChildren) {
                            newChildrenMap.put(element.getName(), new Object()); // null ?
                            // If the child's not there
                            if (!children.containsKey(element.getName())) {
                                missingChildren.push(element);
                            }
                        }

                        children = newChildrenMap;

                        // If there were missing children
                        if (!missingChildren.empty()) {

                            while (!missingChildren.empty()) {
                                fireAllCreate(missingChildren.pop());
                            }
                        }

                    } else if (newChildren.length > 0) {
                        // First set of children - Break out the cigars
                        children = new HashMap<>();
                        for (final FileObject element : newChildren) {
                            children.put(element.getName(), new Object()); // null?
                            fireAllCreate(element);
                        }
                    }
                }
            } catch (final FileSystemException fse) {
                LOG.error(fse.getLocalizedMessage(), fse);
            }
        }

        /**
         * Recursively fires create events for all children if recursive descent is enabled. Otherwise the create event is only
         * fired for the initial FileObject.
         *
         * @param child The child to add.
         */
        private void fireAllCreate(final FileObject child) {
            // Add listener so that it can be triggered
            if (defaultFileMonitor.getFileListener() != null) {
                child.getFileSystem().addListener(child, defaultFileMonitor.getFileListener());
            }

            ((AbstractFileSystem) child.getFileSystem()).fireFileCreated(child);

            // Remove it because a listener is added in the queueAddFile
            if (defaultFileMonitor.getFileListener() != null) {
                child.getFileSystem().removeListener(child, defaultFileMonitor.getFileListener());
            }

            defaultFileMonitor.queueAddFile(child); // Add

            try {
                if (defaultFileMonitor.isRecursive() && child.getType().hasChildren()) {
                    Stream.of(child.getChildren()).forEach(this::fireAllCreate);
                }
            } catch (final FileSystemException fse) {
                LOG.error(fse.getLocalizedMessage(), fse);
            }
        }

        /**
         * Clear the cache and re-request the file object.
         */
        private void refresh() {
            try {
                fileObject.refresh();
            } catch (final FileSystemException fse) {
                LOG.error(fse.getLocalizedMessage(), fse);
            }
        }

        private void resetChildrenList() {
            try {
                if (fileObject.getType().hasChildren()) {
                    children = new HashMap<>();
                    for (final FileObject element : fileObject.getChildren()) {
                        children.put(element.getName(), new Object()); // null?
                    }
                }
            } catch (final FileSystemException fse) {
                children = null;
            }
        }

    }

    private static final ThreadFactory THREAD_FACTORY = new BasicThreadFactory.Builder().daemon(true).priority(Thread.MIN_PRIORITY).build();

    private static final Log LOG = LogFactory.getLog(DefaultFileMonitor.class);

    private static final Duration DEFAULT_DELAY = Duration.ofSeconds(1);

    private static final int DEFAULT_MAX_FILES = 1000;

    /**
     * Map from FileName to FileObject being monitored.
     */
    private final Map<FileName, FileMonitorAgent> monitorMap = new HashMap<>();

    /**
     * The low priority thread used for checking the files being monitored.
     */
    private Thread monitorThread;

    /**
     * File objects to be removed from the monitor map.
     */
    private final Stack<FileObject> deleteStack = new Stack<>();

    /**
     * File objects to be added to the monitor map.
     */
    private final Stack<FileObject> addStack = new Stack<>();

    /**
     * A flag used to determine if the monitor thread should be running.
     */
    private volatile boolean runFlag = true; // used for inter-thread communication

    /**
     * A flag used to determine if adding files to be monitored should be recursive.
     */
    private boolean recursive;

    /**
     * Sets the delay between checks
     */
    private Duration delay = DEFAULT_DELAY;

    /**
     * Sets the number of files to check until a delay will be inserted
     */
    private int checksPerRun = DEFAULT_MAX_FILES;

    /**
     * A listener object that if set, is notified on file creation and deletion.
     */
    private final FileListener listener;

    /**
     * Constructs a new instance with the given listener.
     *
     * @param listener the listener.
     */
    public DefaultFileMonitor(final FileListener listener) {
        this.listener = listener;
    }

    /**
     * Adds a file to be monitored.
     *
     * @param file The FileObject to monitor.
     */
    @Override
    public void addFile(final FileObject file) {
        synchronized (monitorMap) {
            if (monitorMap.get(file.getName()) == null) {
                monitorMap.put(file.getName(), new FileMonitorAgent(this, file));

                try {
                    if (listener != null) {
                        file.getFileSystem().addListener(file, listener);
                    }

                    if (file.getType().hasChildren() && recursive) {
                        // Traverse the children
                        // Add depth first
                        Stream.of(file.getChildren()).forEach(this::addFile);
                    }

                } catch (final FileSystemException fse) {
                    LOG.error(fse.getLocalizedMessage(), fse);
                }

            }
        }
    }

    @Override
    public void close() {
        runFlag = false;
        if (monitorThread != null) {
            monitorThread.interrupt();
            try {
                monitorThread.join();
            } catch (final InterruptedException e) {
                // ignore
            }
            monitorThread = null;
        }
    }

    /**
     * Gets the number of files to check per run.
     *
     * @return The number of files to check per iteration.
     */
    public int getChecksPerRun() {
        return checksPerRun;
    }

    /**
     * Gets the delay between runs.
     *
     * @return The delay period in milliseconds.
     * @deprecated Use {@link #getDelayDuration()}.
     */
    @Deprecated
    public long getDelay() {
        return delay.toMillis();
    }

    /**
     * Gets the delay between runs.
     *
     * @return The delay period.
     */
    public Duration getDelayDuration() {
        return delay;
    }

    /**
     * Gets the current FileListener object notified when there are changes with the files added.
     *
     * @return The FileListener.
     */
    FileListener getFileListener() {
        return listener;
    }

    /**
     * Tests the recursive setting when adding files for monitoring.
     *
     * @return true if monitoring is enabled for children.
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * Queues a file for addition to be monitored.
     *
     * @param file The FileObject to add.
     */
    protected void queueAddFile(final FileObject file) {
        addStack.push(file);
    }

    /**
     * Queues a file for removal from being monitored.
     *
     * @param file The FileObject to be removed from being monitored.
     */
    protected void queueRemoveFile(final FileObject file) {
        deleteStack.push(file);
    }

    /**
     * Removes a file from being monitored.
     *
     * @param file The FileObject to remove from monitoring.
     */
    @Override
    public void removeFile(final FileObject file) {
        synchronized (monitorMap) {
            final FileName fn = file.getName();
            if (monitorMap.get(fn) != null) {
                FileObject parent;
                try {
                    parent = file.getParent();
                } catch (final FileSystemException fse) {
                    parent = null;
                }

                monitorMap.remove(fn);

                if (parent != null) { // Not the root
                    final FileMonitorAgent parentAgent = monitorMap.get(parent.getName());
                    if (parentAgent != null) {
                        parentAgent.resetChildrenList();
                    }
                }
            }
        }
    }

    /**
     * Asks the agent for each file being monitored to check its file for changes.
     */
    @Override
    public void run() {
        mainloop: while (!monitorThread.isInterrupted() && runFlag) {
            // For each entry in the map
            final Object[] fileNames;
            synchronized (monitorMap) {
                fileNames = monitorMap.keySet().toArray();
            }
            for (int iterFileNames = 0; iterFileNames < fileNames.length; iterFileNames++) {
                final FileName fileName = (FileName) fileNames[iterFileNames];
                final FileMonitorAgent agent;
                synchronized (monitorMap) {
                    agent = monitorMap.get(fileName);
                }
                if (agent != null) {
                    agent.check();
                }

                if (getChecksPerRun() > 0 && (iterFileNames + 1) % getChecksPerRun() == 0) {
                    try {
                        Thread.sleep(getDelayDuration().toMillis());
                    } catch (final InterruptedException e) {
                        // Woke up.
                    }
                }

                if (monitorThread.isInterrupted() || !runFlag) {
                    continue mainloop;
                }
            }

            while (!addStack.empty()) {
                addFile(addStack.pop());
            }

            while (!deleteStack.empty()) {
                removeFile(deleteStack.pop());
            }

            try {
                Thread.sleep(getDelayDuration().toMillis());
            } catch (final InterruptedException e) {
                continue;
            }
        }

        runFlag = true;
    }

    /**
     * Sets the number of files to check per run. An additional delay will be added if there are more files to check.
     *
     * @param checksPerRun a value less than 1 will disable this feature
     */
    public void setChecksPerRun(final int checksPerRun) {
        this.checksPerRun = checksPerRun;
    }

    /**
     * Sets the delay between runs.
     *
     * @param delay The delay period.
     * @since 2.10.0
     */
    public void setDelay(final Duration delay) {
        this.delay = delay == null || delay.isNegative() ? DEFAULT_DELAY : delay;
    }

    /**
     * Sets the delay between runs.
     *
     * @param delay The delay period in milliseconds.
     * @deprecated Use {@link #setDelay(Duration)}.
     */
    @Deprecated
    public void setDelay(final long delay) {
        setDelay(delay > 0 ? Duration.ofMillis(delay) : DEFAULT_DELAY);
    }

    /**
     * Sets the recursive setting when adding files for monitoring.
     *
     * @param newRecursive true if monitoring should be enabled for children.
     */
    public void setRecursive(final boolean newRecursive) {
        recursive = newRecursive;
    }

    /**
     * Starts monitoring the files that have been added.
     */
    public synchronized void start() {
        if (monitorThread == null) {
            monitorThread = THREAD_FACTORY.newThread(this);
        }
        monitorThread.start();
    }

    /**
     * Stops monitoring the files that have been added.
     */
    public synchronized void stop() {
        close();
    }
}
