package org.apache.commons.vfs2.provider.zip.test;

import org.apache.commons.io.IOUtils;

import org.apache.commons.vfs2.FileContent;

import org.apache.commons.vfs2.FileObject;

import org.apache.commons.vfs2.FileSystemManager;

import org.apache.commons.vfs2.VFS;

import java.io.ByteArrayOutputStream;

import java.io.InputStream;

/**
 * 
 * Demonstrate a bug in commons vfs2:
 * 
 * Closing a zip file object doesn't release the underlying file lock.
 * 
 * 
 * 
 * Discovered on:
 * 
 * Windows 7 pro SP1
 * 
 * Java 1.6.0_20-b02 64-bit
 * 
 * VFS maven version: org.apache.commons:commons-vfs2:2.0
 */

public class BugReport
{

    public static void main(String args[]) throws Exception
    {

        String zipFile = "zip:C:\\some-file.zip!some-zip-entry.xml";

        FileSystemManager vfs = VFS.getManager();

        FileObject file = vfs.resolveFile(zipFile);

        FileContent content = file.getContent();

        InputStream is = content.getInputStream(); // actually locks the file

        // Optionally consume the input stream.

        // This implicitly call close() on the input stream when finish reading.

        // See DefaultFileContent.FileContentInputStream class and MonitorInputStream.read()

        // But either way, the zip file is still locked.

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        IOUtils.copy(is, buffer);

        // The following expected to unlock the file but they don't

        IOUtils.closeQuietly(is);

        content.close();

        file.close(); // doesn't close the embedded ZipFileSystem which takes a file lock.

        // Only this releases file lock.

        vfs.closeFileSystem(file.getFileSystem());

    }

}
