package org.apache.commons.vfs2;

import java.util.Iterator;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests {@link FileObject}s with iterators its implementation the {@link Iterable} interface to allow a FileObject to be the target of the
 * Java 5 "foreach" statement.
 */
public class FileIteratorTest
{

    private static FileObject BaseFolder;

    private final static int FileCount = 14;

    /**
     * Creates a RAM FS.
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception
    {
        BaseFolder = VFS.getManager().resolveFile("ram://" + FileIteratorTest.class.getName());
        BaseFolder.deleteAll();
        BaseFolder.resolveFile("a.htm").createFile();
        BaseFolder.resolveFile("a.html").createFile();
        BaseFolder.resolveFile("a.xhtml").createFile();
        BaseFolder.resolveFile("b.htm").createFile();
        BaseFolder.resolveFile("b.html").createFile();
        BaseFolder.resolveFile("b.xhtml").createFile();
        BaseFolder.resolveFile("c.htm").createFile();
        BaseFolder.resolveFile("c.html").createFile();
        BaseFolder.resolveFile("c.xhtml").createFile();
        BaseFolder.resolveFile("subdir1").createFolder();
        BaseFolder.resolveFile("subdir1/subfile1.txt").createFile();
        BaseFolder.resolveFile("subdir2").createFolder();
        BaseFolder.resolveFile("subdir2/subfile1.txt").createFile();
    }

    /**
     * Deletes RAM FS files.
     * 
     * @throws Exception
     */
    @AfterClass
    public static void tearDownClass() throws Exception
    {
        if (BaseFolder != null)
        {
            BaseFolder.deleteAll();
        }
    }

    @Test
    public void testIterator() throws FileSystemException
    {
        final FileObject[] findFiles = BaseFolder.findFiles(Selectors.SELECT_ALL);
        Assert.assertEquals(FileCount, findFiles.length);
        final FileObject[] listFiles = BaseFolder.getChildren();
        Assert.assertTrue(FileCount > listFiles.length);
        int i = 0;
        for (FileObject actualFile : BaseFolder)
        {
            FileObject expectedFile = findFiles[i];
            Assert.assertEquals(expectedFile, actualFile);
            i++;
        }
        Iterator<FileObject> iter = BaseFolder.iterator();
        i = 0;
        while (iter.hasNext())
        {
            FileObject expectedFile = findFiles[i];
            Assert.assertEquals(expectedFile, iter.next());
            i++;
        }
    }
}
