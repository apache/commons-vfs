package org.apache.commons.vfs2.provider.local.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests conversion from VFS to File.
 * <p/>
 * VFS-443 Need an easy way to convert from a FileObject to a File.
 * 
 * @version $Id$
 */
public class ConversionTestCase
{

    @Test
    @Ignore
    public void testFileNameWithSpaces() throws URISyntaxException, IOException
    {
        final File file = new File("target", "a name.txt");
        final String fileURL = file.toURI().toURL().toExternalForm();
        assertEquals(file.getAbsoluteFile(), new File(file.toURI().getPath()));
        assertEquals(file.getAbsoluteFile(), new File(new URL(fileURL).toURI().getPath()));

        final FileSystemManager manager = VFS.getManager();
        final FileObject fo = manager.resolveFile(fileURL);
        assertEquals(file.getAbsoluteFile(), new File(new URL(fo.getURL().toExternalForm()).toURI().getPath()));
    }

    @Test
    @Ignore
    public void testFileNameWithCharacters() throws URISyntaxException, IOException
    {
        final File file = new File("target", "+# %&.txt");
        final String fileURL = file.toURI().toURL().toExternalForm();
        assertEquals(file.getAbsoluteFile(), new File(file.toURI().getPath()));
        assertEquals(file.getAbsoluteFile(), new File(new URL(fileURL).toURI().getPath()));
        try
        {
            new FileOutputStream(file).close();
            assertTrue(file.exists());

            final FileSystemManager manager = VFS.getManager();
            final FileObject fo = manager.resolveFile(fileURL);
            assertTrue(fo.exists());
            assertEquals(file.getAbsoluteFile(), new File(new URL(fo.getURL().toExternalForm()).toURI().getPath()));
        }
        finally
        {
            file.delete();
        }
    }

}
