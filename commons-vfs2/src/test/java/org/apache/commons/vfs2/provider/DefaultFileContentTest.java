package org.apache.commons.vfs2.provider;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * {@code DefaultFileContentTest} tests for bug-VFS-614. This bug involves the stream implementation closing the stream
 * after reading to the end of the buffer, which broke marking.
 */
public class DefaultFileContentTest {
    private static final String expected = "testing";

    @Test
    public void testMarkingWorks() throws Exception {
        File temp = File.createTempFile("temp-file-name", ".tmp");
        FileSystemManager fileSystemManager = VFS.getManager();

        try (FileObject file = fileSystemManager.resolveFile(temp.getAbsolutePath())) {
            try (OutputStream outputStream = file.getContent().getOutputStream()) {
                outputStream.write(expected.getBytes());
                outputStream.flush();
            }
            try (InputStream stream = file.getContent().getInputStream()) {
                if (stream.markSupported()) {
                    for (int i = 0; i < 10; i++) {
                        stream.mark(0);
                        byte[] data = new byte[100];
                        stream.read(data, 0, 7);
                        Assert.assertEquals(expected, new String(data).trim());
                        stream.reset();
                    }
                }
            }
        }
    }

    @Test
    public void testMarkingWhenReadingEOS() throws Exception {
        File temp = File.createTempFile("temp-file-name", ".tmp");
        FileSystemManager fileSystemManager = VFS.getManager();

        try (FileObject file = fileSystemManager.resolveFile(temp.getAbsolutePath())) {
            try (OutputStream outputStream = file.getContent().getOutputStream()) {
                outputStream.write(expected.getBytes());
                outputStream.flush();
            }
            try (InputStream stream = file.getContent().getInputStream()) {
                int readCount = 0;
                if (stream.markSupported()) {
                    for (int i = 0; i < 10; i++) {
                        stream.mark(0);
                        byte[] data = new byte[100];
                        readCount = stream.read(data, 0, 7);
                        Assert.assertEquals(readCount, 7);
                        Assert.assertEquals(expected, new String(data).trim());
                        readCount = stream.read(data, 8, 10);
                        Assert.assertEquals(readCount, -1);
                        stream.reset();
                    }
                }
            }
        }
    }
}
