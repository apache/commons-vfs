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

import static org.apache.commons.vfs2.VfsTestUtils.getTestDirectoryFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link DefaultFileMonitor}.
 */
public class DefaultFileMonitorTest {

    private static class CountingListener implements FileListener {
        private final AtomicLong changed = new AtomicLong();
        private final AtomicLong created = new AtomicLong();
        private final AtomicLong deleted = new AtomicLong();

        @Override
        public void fileChanged(final FileChangeEvent event) {
            changed.incrementAndGet();
        }

        @Override
        public void fileCreated(final FileChangeEvent event) {
            created.incrementAndGet();
        }

        @Override
        public void fileDeleted(final FileChangeEvent event) {
            deleted.incrementAndGet();
        }
    }

    private enum PeekLocation {
        FIRST, LAST
    }

    private enum Status {
        CHANGED, CREATED, DELETED
    }

    private class TestFileListener implements FileListener {

        @Override
        public void fileChanged(final FileChangeEvent event) throws Exception {
            status.add(Status.CHANGED);
        }

        @Override
        public void fileCreated(final FileChangeEvent event) throws Exception {
            status.add(Status.CREATED);
        }

        @Override
        public void fileDeleted(final FileChangeEvent event) throws Exception {
            status.add(Status.DELETED);
        }
    }

    private static final int DELAY_MILLIS = 100;

    private FileSystemManager fileSystemManager;

    private final Deque<Status> status = new ArrayDeque<>();

    private File testDir;

    private File testFile;

    private void deleteTestFileIfPresent() {
        if (testFile != null && testFile.exists()) {
            final boolean deleted = testFile.delete();
            assertTrue(deleted, testFile.toString());
        }
    }

    private Status getStatus(final PeekLocation peekLocation) {
        switch (Objects.requireNonNull(peekLocation, "peekLocation")) {
        case FIRST:
            return status.peekFirst();
        case LAST:
            return status.peekLast();
        }
        throw new IllegalStateException();
    }

    private void resetStatus() {
        status.clear();
    }

    @BeforeEach
    public void setUp() throws Exception {
        fileSystemManager = VFS.getManager();
        testDir = getTestDirectoryFile();
        resetStatus();
        testFile = new File(testDir, "testReload.properties");
        deleteTestFileIfPresent();
    }

    @AfterEach
    public void tearDown() {
        deleteTestFileIfPresent();
    }

    @Test
    public void testChildFileDeletedWithoutRecursiveChecking() throws Exception {
        writeToFile(testFile);
        try (FileObject fileObject = fileSystemManager.resolveFile(testDir.toURI().toURL().toString())) {
            try (DefaultFileMonitor monitor = new DefaultFileMonitor(new TestFileListener())) {
                monitor.setDelay(2000);
                monitor.setRecursive(false);
                monitor.addFile(fileObject);
                monitor.start();
                resetStatus();
                Thread.sleep(DELAY_MILLIS * 5);
                testFile.delete();
                Thread.sleep(DELAY_MILLIS * 30);
                assertNull(getStatus(PeekLocation.LAST), "Event should not have occurred");
            }
        }
    }

    @Test
    public void testChildFileRecreated() throws Exception {
        writeToFile(testFile);
        try (FileObject fileObj = fileSystemManager.resolveFile(testDir.toURI().toURL().toString())) {
            try (DefaultFileMonitor monitor = new DefaultFileMonitor(new TestFileListener())) {
                monitor.setDelay(2000);
                monitor.setRecursive(true);
                monitor.addFile(fileObj);
                monitor.start();
                resetStatus();
                Thread.sleep(DELAY_MILLIS * 5);
                testFile.delete();
                waitFor(Status.DELETED, DELAY_MILLIS * 30, PeekLocation.LAST);
                resetStatus();
                Thread.sleep(DELAY_MILLIS * 5);
                writeToFile(testFile);
                waitFor(Status.CREATED, DELAY_MILLIS * 30, PeekLocation.LAST);
            }
        }
    }

    @Test
    public void testFileCreated() throws Exception {
        try (FileObject fileObject = fileSystemManager.resolveFile(testFile.toURI().toURL().toString())) {
            try (DefaultFileMonitor monitor = new DefaultFileMonitor(new TestFileListener())) {
                // TestFileListener manipulates status
                monitor.setDelay(DELAY_MILLIS);
                monitor.addFile(fileObject);
                monitor.start();
                writeToFile(testFile);
                Thread.sleep(DELAY_MILLIS * 5);
                waitFor(Status.CREATED, DELAY_MILLIS * 5, PeekLocation.FIRST);
            }
        }
    }

    @Test
    public void testFileDeleted() throws Exception {
        writeToFile(testFile);
        try (FileObject fileObject = fileSystemManager.resolveFile(testFile.toURI().toString())) {
            try (DefaultFileMonitor monitor = new DefaultFileMonitor(new TestFileListener())) {
                // TestFileListener manipulates status
                monitor.setDelay(DELAY_MILLIS);
                monitor.addFile(fileObject);
                monitor.start();
                testFile.delete();
                waitFor(Status.DELETED, DELAY_MILLIS * 5, PeekLocation.LAST);
            }
        }
    }

    @Test
    public void testFileModified() throws Exception {
        writeToFile(testFile);
        try (FileObject fileObject = fileSystemManager.resolveFile(testFile.toURI().toURL().toString())) {
            try (DefaultFileMonitor monitor = new DefaultFileMonitor(new TestFileListener())) {
                // TestFileListener manipulates status
                monitor.setDelay(DELAY_MILLIS);
                monitor.addFile(fileObject);
                monitor.start();
                // Need a long delay to insure the new timestamp doesn't truncate to be the same as
                // the current timestamp. Java only guarantees the timestamp will be to 1 second.
                Thread.sleep(DELAY_MILLIS * 10);
                final long valueMillis = System.currentTimeMillis();
                final boolean rcMillis = testFile.setLastModified(valueMillis);
                assertTrue(rcMillis, "setLastModified succeeded");
                waitFor(Status.CHANGED, DELAY_MILLIS * 5, PeekLocation.LAST);
            }
        }
    }

    @Test
    public void testFileMonitorRestarted() throws Exception {
        try (FileObject fileObject = fileSystemManager.resolveFile(testFile.toURI().toString());
                DefaultFileMonitor monitor = new DefaultFileMonitor(new TestFileListener())) {
            try {
                // TestFileListener manipulates status
                monitor.setDelay(DELAY_MILLIS);
                monitor.addFile(fileObject);
                monitor.start();
                writeToFile(testFile);
                Thread.sleep(DELAY_MILLIS * 5);
            } finally {
                monitor.stop();
            }
            monitor.start();
            try {
                testFile.delete();
                waitFor(Status.DELETED, DELAY_MILLIS * 5, PeekLocation.LAST);
            } finally {
                monitor.stop();
            }
        }
    }

    @Test
    public void testFileRecreated() throws Exception {
        try (FileObject fileObject = fileSystemManager.resolveFile(testFile.toURI());
                DefaultFileMonitor monitor = new DefaultFileMonitor(new TestFileListener())) {
            // TestFileListener manipulates status
            monitor.setDelay(DELAY_MILLIS);
            monitor.addFile(fileObject);
            monitor.start();
            writeToFile(testFile);
            waitFor(Status.CREATED, DELAY_MILLIS * 10, PeekLocation.LAST);
            resetStatus();
            testFile.delete();
            waitFor(Status.DELETED, DELAY_MILLIS * 10, PeekLocation.LAST);
            resetStatus();
            Thread.sleep(DELAY_MILLIS * 5);
            monitor.addFile(fileObject);
            writeToFile(testFile);
            waitFor(Status.CREATED, DELAY_MILLIS * 10, PeekLocation.LAST);
        }
    }

    /**
     * VFS-299: Handlers are not removed. One instance is {@link DefaultFileMonitor#removeFile(FileObject)}.
     *
     * As a result, the file monitor will fire two created events.
     */
    @Disabled("VFS-299")
    @Test
    public void testIgnoreTestAddRemove() throws Exception {
        try (FileObject fileObject = fileSystemManager.resolveFile(testFile.toURI().toString())) {
            final CountingListener listener = new CountingListener();
            try (DefaultFileMonitor monitor = new DefaultFileMonitor(listener)) {
                monitor.setDelay(DELAY_MILLIS);
                monitor.addFile(fileObject);
                monitor.removeFile(fileObject);
                monitor.addFile(fileObject);
                monitor.start();
                writeToFile(testFile);
                Thread.sleep(DELAY_MILLIS * 3);
                assertEquals(1, listener.created.get(), "Created event is only fired once");
            }
        }
    }

    /**
     * VFS-299: Handlers are not removed. There is no API for properly decommissioning a file monitor.
     *
     * As a result, listeners of stopped monitors still receive events.
     */
    @Disabled("VFS-299")
    @Test
    public void testIgnoreTestStartStop() throws Exception {
        try (FileObject fileObject = fileSystemManager.resolveFile(testFile.toURI().toString())) {
            final CountingListener stoppedListener = new CountingListener();
            try (DefaultFileMonitor stoppedMonitor = new DefaultFileMonitor(stoppedListener)) {
                stoppedMonitor.start();
                stoppedMonitor.addFile(fileObject);
            }

            // Variant 1: it becomes documented behavior to manually remove all files after stop() such that all
            // listeners
            // are removed
            // This currently does not work, see DefaultFileMonitorTests#testAddRemove above.
            // stoppedMonitor.removeFile(file);

            // Variant 2: change behavior of stop(), which then removes all handlers.
            // This would remove the possibility to pause watching files. Resuming watching for the same files via
            // start();
            // stop(); start(); would not work.

            // Variant 3: introduce new method DefaultFileMonitor#close which definitely removes all resources held by
            // DefaultFileMonitor.

            final CountingListener activeListener = new CountingListener();
            try (DefaultFileMonitor activeMonitor = new DefaultFileMonitor(activeListener)) {
                activeMonitor.setDelay(DELAY_MILLIS);
                activeMonitor.addFile(fileObject);
                activeMonitor.start();
                writeToFile(testFile);
                Thread.sleep(DELAY_MILLIS * 10);

                assertEquals(1, activeListener.created.get(), "The listener of the active monitor received one created event");
                assertEquals(0, stoppedListener.created.get(), "The listener of the stopped monitor received no events");
            }
        }
    }

    private void waitFor(final Status expected, final long timeoutMillis, final PeekLocation peekLocation) throws InterruptedException {
        if (expected == getStatus(peekLocation)) {
            return;
        }
        long remaining = timeoutMillis;
        final long interval = timeoutMillis / 10;
        while (remaining > 0) {
            Thread.sleep(interval);
            remaining -= interval;
            if (expected == getStatus(peekLocation)) {
                return;
            }
        }
        assertNotNull(getStatus(peekLocation), "No event occurred");
        assertEquals(expected, getStatus(peekLocation), "Incorrect event " + getStatus(peekLocation));
    }

    private void writeToFile(final File file) throws IOException {
        Files.write(file.toPath(), "string=value1".getBytes(StandardCharsets.UTF_8));
    }

}
