package org.apache.commons.vfs2.provider.https.test;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests VFS-427 NPE on HttpFileObject.getContent().getContentInfo()
 * 
 * @since 2.1
 */
public class GetContentInfoFunctionalTest
{

    /**
     * Tests VFS-427 NPE on HttpFileObject.getContent().getContentInfo().
     * 
     * @throws FileSystemException
     *             thrown when the getContentInfo API fails.
     */
    @Test
    public void testGoogle() throws FileSystemException
    {
        final FileSystemManager fsManager = VFS.getManager();
        final FileObject fo = fsManager.resolveFile("https://www.google.com/images/logos/ps_logo2.png");
        final FileContent content = fo.getContent();
        Assert.assertNotNull(content);
        // Used to NPE before fix:
        content.getContentInfo();
    }
}
