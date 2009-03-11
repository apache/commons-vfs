package org.apache.commons.vfs.impl.test;

import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileChangeEvent;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.FileListener;
import org.apache.commons.vfs.impl.DefaultFileMonitor;

import java.io.File;
import java.io.FileWriter;

/**
 * Test to verify DefaultFileMonitor
 */
public class DefaultFileMonitorTests extends AbstractVfsTestCase
{
    private FileSystemManager fsManager;
    private File testDir;
    private int changeStatus = 0;
    private File testFile;

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
        Thread.sleep(500);
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
        Thread.sleep(500);
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
        Thread.sleep(300);
        assertTrue("No event occurred", changeStatus != 0);
        assertTrue("Incorrect event", changeStatus == 1);
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
