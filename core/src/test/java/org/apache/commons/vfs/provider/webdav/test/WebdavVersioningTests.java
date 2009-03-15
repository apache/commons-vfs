package org.apache.commons.vfs.provider.webdav.test;

import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FilesCache;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.webdav.WebdavFileSystemConfigBuilder;
import org.apache.commons.vfs.test.AbstractProviderTestCase;
import org.apache.jackrabbit.webdav.version.DeltaVConstants;
import org.apache.jackrabbit.webdav.version.VersionControlledResource;

import java.util.Map;
import java.io.OutputStream;

/**
 * Test to verify Webdav Versioning support
 */
public class WebdavVersioningTests extends AbstractProviderTestCase
{
    /**
     *
     */
    public void testVersioning() throws Exception
    {
        FileObject scratchFolder = createScratchFolder();
        FileSystemOptions opts = scratchFolder.getFileSystem().getFileSystemOptions();
        WebdavFileSystemConfigBuilder builder =
            (WebdavFileSystemConfigBuilder)getManager().getFileSystemConfigBuilder("webdav");
        builder.setVersioning(opts, true);
        FileObject file = getManager().resolveFile(scratchFolder, "file1.txt", opts);
        FileSystemOptions newOpts = file.getFileSystem().getFileSystemOptions();
        assertTrue(opts == newOpts);
        assertTrue(builder.isVersioning(newOpts));
        assertTrue(!file.exists());
        file.createFile();
        assertTrue(file.exists());
        assertSame(FileType.FILE, file.getType());
        assertEquals(0, file.getContent().getSize());
        assertFalse(file.isHidden());
        assertTrue(file.isReadable());
        assertTrue(file.isWriteable());
        Map map = file.getContent().getAttributes();
        assertTrue(map.containsKey(DeltaVConstants.CREATOR_DISPLAYNAME.toString()));
        assertEquals(map.get(DeltaVConstants.CREATOR_DISPLAYNAME.toString()),"admin");
        assertTrue(map.containsKey(VersionControlledResource.CHECKED_OUT.toString()));

        // Create the source file
        final String content = "Here is some sample content for the file.  Blah Blah Blah.";
        final String contentAppend = content + content;

        final OutputStream os = file.getContent().getOutputStream();
        try
        {
            os.write(content.getBytes("utf-8"));
        }
        finally
        {
            os.close();
        }
        assertSameContent(content, file);
        map = file.getContent().getAttributes();
        assertTrue(map.containsKey(DeltaVConstants.CREATOR_DISPLAYNAME.toString()));
        assertEquals(map.get(DeltaVConstants.CREATOR_DISPLAYNAME.toString()),"admin");
        assertTrue(map.containsKey(VersionControlledResource.CHECKED_IN.toString()));
        builder.setVersioning(opts, false);
    }
    /**
     *
     */
    public void testVersioningWithCreator() throws Exception
    {
        FileObject scratchFolder = createScratchFolder();
        FileSystemOptions opts = scratchFolder.getFileSystem().getFileSystemOptions();
        WebdavFileSystemConfigBuilder builder =
            (WebdavFileSystemConfigBuilder)getManager().getFileSystemConfigBuilder("webdav");
        builder.setVersioning(opts, true);
        builder.setCreatorName(opts, "testUser");
        FileObject file = getManager().resolveFile(scratchFolder, "file1.txt", opts);
        FileSystemOptions newOpts = file.getFileSystem().getFileSystemOptions();
        assertTrue(opts == newOpts);
        assertTrue(builder.isVersioning(newOpts));
        assertTrue(!file.exists());
        file.createFile();
        assertTrue(file.exists());
        assertSame(FileType.FILE, file.getType());
        assertEquals(0, file.getContent().getSize());
        assertFalse(file.isHidden());
        assertTrue(file.isReadable());
        assertTrue(file.isWriteable());
        Map map = file.getContent().getAttributes();
        assertTrue(map.containsKey(DeltaVConstants.CREATOR_DISPLAYNAME.toString()));
        assertEquals(map.get(DeltaVConstants.CREATOR_DISPLAYNAME.toString()),"testUser");
        assertTrue(map.containsKey(DeltaVConstants.COMMENT.toString()));
        assertEquals(map.get(DeltaVConstants.COMMENT.toString()),"Modified by user admin");
        assertTrue(map.containsKey(VersionControlledResource.CHECKED_OUT.toString()));

        // Create the source file
        final String content = "Here is some sample content for the file.  Blah Blah Blah.";
        final String contentAppend = content + content;

        final OutputStream os = file.getContent().getOutputStream();
        try
        {
            os.write(content.getBytes("utf-8"));
        }
        finally
        {
            os.close();
        }
        assertSameContent(content, file);
        map = file.getContent().getAttributes();
        assertTrue(map.containsKey(DeltaVConstants.CREATOR_DISPLAYNAME.toString()));
        assertEquals(map.get(DeltaVConstants.CREATOR_DISPLAYNAME.toString()),"testUser");
        assertTrue(map.containsKey(DeltaVConstants.COMMENT.toString()));
        assertEquals(map.get(DeltaVConstants.COMMENT.toString()),"Modified by user admin");
        assertTrue(map.containsKey(VersionControlledResource.CHECKED_IN.toString()));
        builder.setVersioning(opts, false);
        builder.setCreatorName(opts, null);
    }  
        /**
     * Sets up a scratch folder for the test to use.
     */
    protected FileObject createScratchFolder() throws Exception
    {
        FileObject scratchFolder = getWriteFolder();

        // Make sure the test folder is empty
        scratchFolder.delete(Selectors.EXCLUDE_SELF);
        scratchFolder.createFolder();

        return scratchFolder;
    }

}
