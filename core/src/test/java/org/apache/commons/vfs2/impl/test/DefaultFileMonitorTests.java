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
public class DefaultFileMonitorTests extends AbstractVfsTestCase
{
    private FileSystemManager fsManager;
    private File testDir;
    private int changeStatus = 0;
    private File testFile;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        fsManager = VFS.getManager();
        testDir = AbstractVfsTestCase.getTestDirectoryFile();
        changeStatus = 0;
        testFile = new File(testDir, "testReload.properties");

        if (testFile.exists())
        {
            testFile.delete();
        }
    }

    @Override
    public void tearDown() throws Exception
    {
        if (testFile != null && testFile.exists())
        {
            testFile.delete();
        }
        super.tearDown();
    }

    public void testFileCreated() throws Exception
    {
        FileObject fileObj = fsManager.resolveFile(testFile.toURL().toString());
        DefaultFileMonitor monitor = new DefaultFileMonitor(new TestFileListener());
        monitor.setDelay(100);
        monitor.addFile(fileObj);
        monitor.start();
        writeToFile(testFile);
        Thread.sleep(300);
        assertTrue("No event occurred", changeStatus != 0);
        assertTrue("Incorrect event", changeStatus == 3);
        monitor.stop();
    }

    public void testFileDeleted() throws Exception
    {
        writeToFile(testFile);
        FileObject fileObj = fsManager.resolveFile(testFile.toURL().toString());
        DefaultFileMonitor monitor = new DefaultFileMonitor(new TestFileListener());
        monitor.setDelay(100);
        monitor.addFile(fileObj);
        monitor.start();
        testFile.delete();
        Thread.sleep(300);
        assertTrue("No event occurred", changeStatus != 0);
        assertTrue("Incorrect event", changeStatus == 2);
        monitor.stop();
    }

    public void testFileModified() throws Exception
    {
        writeToFile(testFile);
        FileObject fileObj = fsManager.resolveFile(testFile.toURL().toString());
        DefaultFileMonitor monitor = new DefaultFileMonitor(new TestFileListener());
        monitor.setDelay(100);
        monitor.addFile(fileObj);
        monitor.start();
        // Need a long delay to insure the new timestamp doesn't truncate to be the same as
        // the current timestammp. Java only guarantees the timestamp will be to 1 second.
        Thread.sleep(1000);
        long value = System.currentTimeMillis();
        boolean rc = testFile.setLastModified(value);
        assertTrue("setLastModified succeeded",rc);
        Thread.sleep(300);
        assertTrue("No event occurred", changeStatus != 0);
        assertTrue("Incorrect event", changeStatus == 1);
        monitor.stop();
    }


    public void testFileRecreated() throws Exception
    {
        FileObject fileObj = fsManager.resolveFile(testFile.toURL().toString());
        DefaultFileMonitor monitor = new DefaultFileMonitor(new TestFileListener());
        monitor.setDelay(100);
        monitor.addFile(fileObj);
        monitor.start();
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
        monitor.stop();
    }

    private void writeToFile(File file) throws Exception
    {
        FileWriter out = new FileWriter(file);
        out.write("string=value1");
        out.flush();
        out.close();
    }


    public class TestFileListener implements FileListener
    {
        public void fileChanged(FileChangeEvent event) throws Exception
        {
            changeStatus = 1;
        }

        public void fileDeleted(FileChangeEvent event) throws Exception
        {
            changeStatus = 2;
        }

        public void fileCreated(FileChangeEvent event) throws Exception
        {
            changeStatus = 3;
        }
    }

}
