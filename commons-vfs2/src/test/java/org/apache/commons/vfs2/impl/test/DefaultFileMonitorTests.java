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
package org.apache.commons.vfs2.impl.test;

import java.io.File;
import java.io.FileWriter;

import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;

/**
 * Test to verify DefaultFileMonitor
 */
public class DefaultFileMonitorTests extends AbstractVfsTestCase {
    private FileSystemManager fsManager;
    private File testDir;
    private int changeStatus = 0;
    private File testFile;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        fsManager = VFS.getManager();
        testDir = AbstractVfsTestCase.getTestDirectoryFile();
        changeStatus = 0;
        testFile = new File(testDir, "testReload.properties");

        if (testFile.exists()) {
            testFile.delete();
        }
    }

    @Override
    public void tearDown() throws Exception {
        if (testFile != null && testFile.exists()) {
            testFile.delete();
        }
        super.tearDown();
    }

    public void testFileCreated() throws Exception {
        final FileObject fileObj = fsManager.resolveFile(testFile.toURI().toURL().toString());
        final DefaultFileMonitor monitor = new DefaultFileMonitor(new TestFileListener());
        // TestFileListener manipulates changeStatus
        monitor.setDelay(100);
        monitor.addFile(fileObj);
        monitor.start();
        try {
            writeToFile(testFile);
            Thread.sleep(300);
            assertTrue("No event occurred", changeStatus != 0);
            assertTrue("Incorrect event", changeStatus == 3);
        } finally {
            monitor.stop();
        }
    }

    public void testFileDeleted() throws Exception {
        writeToFile(testFile);
        final FileObject fileObj = fsManager.resolveFile(testFile.toURI().toString());
        final DefaultFileMonitor monitor = new DefaultFileMonitor(new TestFileListener());
        // TestFileListener manipulates changeStatus
        monitor.setDelay(100);
        monitor.addFile(fileObj);
        monitor.start();
        try {
            testFile.delete();
            Thread.sleep(300);
            assertTrue("No event occurred", changeStatus != 0);
            assertTrue("Incorrect event", changeStatus == 2);
        } finally {
            monitor.stop();
        }
    }

    public void testFileModified() throws Exception {
        writeToFile(testFile);
        final FileObject fileObj = fsManager.resolveFile(testFile.toURI().toURL().toString());
        final DefaultFileMonitor monitor = new DefaultFileMonitor(new TestFileListener());
        // TestFileListener manipulates changeStatus
        monitor.setDelay(100);
        monitor.addFile(fileObj);
        monitor.start();
        try {
            // Need a long delay to insure the new timestamp doesn't truncate to be the same as
            // the current timestammp. Java only guarantees the timestamp will be to 1 second.
            Thread.sleep(1000);
            final long value = System.currentTimeMillis();
            final boolean rc = testFile.setLastModified(value);
            assertTrue("setLastModified succeeded", rc);
            Thread.sleep(300);
            assertTrue("No event occurred", changeStatus != 0);
            assertTrue("Incorrect event", changeStatus == 1);
        } finally {
            monitor.stop();
        }
    }

    public void testFileRecreated() throws Exception {
        final FileObject fileObj = fsManager.resolveFile(testFile.toURI().toURL().toString());
        final DefaultFileMonitor monitor = new DefaultFileMonitor(new TestFileListener());
        // TestFileListener manipulates changeStatus
        monitor.setDelay(100);
        monitor.addFile(fileObj);
        monitor.start();
        try {
            writeToFile(testFile);
            Thread.sleep(300);
            assertTrue("No event occurred", changeStatus != 0);
            assertTrue("Incorrect event " + changeStatus, changeStatus == 3);
            changeStatus = 0;
            testFile.delete();
            Thread.sleep(300);
            assertTrue("No event occurred", changeStatus != 0);
            assertTrue("Incorrect event " + changeStatus, changeStatus == 2);
            changeStatus = 0;
            Thread.sleep(500);
            monitor.addFile(fileObj);
            writeToFile(testFile);
            Thread.sleep(300);
            assertTrue("No event occurred", changeStatus != 0);
            assertTrue("Incorrect event " + changeStatus, changeStatus == 3);
        } finally {
            monitor.stop();
        }
    }

    public void testChildFileRecreated() throws Exception {
        writeToFile(testFile);
        final FileObject fileObj = fsManager.resolveFile(testDir.toURI().toURL().toString());
        final DefaultFileMonitor monitor = new DefaultFileMonitor(new TestFileListener());
        monitor.setDelay(2000);
        monitor.addFile(fileObj);
        monitor.start();
        try {
            changeStatus = 0;
            Thread.sleep(300);
            testFile.delete();
            Thread.sleep(3000);
            assertTrue("No event occurred", changeStatus != 0);
            assertTrue("Incorrect event " + changeStatus, changeStatus == 2);
            changeStatus = 0;
            Thread.sleep(300);
            writeToFile(testFile);
            Thread.sleep(3000);
            assertTrue("No event occurred", changeStatus != 0);
            assertTrue("Incorrect event " + changeStatus, changeStatus == 3);
        } finally {
            monitor.stop();
        }
    }

    private void writeToFile(final File file) throws Exception {
        final FileWriter out = new FileWriter(file);
        out.write("string=value1");
        out.close();
    }

    public class TestFileListener implements FileListener {
        @Override
        public void fileChanged(final FileChangeEvent event) throws Exception {
            changeStatus = 1;
        }

        @Override
        public void fileDeleted(final FileChangeEvent event) throws Exception {
            changeStatus = 2;
        }

        @Override
        public void fileCreated(final FileChangeEvent event) throws Exception {
            changeStatus = 3;
        }
    }

}
