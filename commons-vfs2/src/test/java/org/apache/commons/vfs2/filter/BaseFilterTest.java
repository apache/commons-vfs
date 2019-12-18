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
package org.apache.commons.vfs2.filter;

import static org.junit.Assert.fail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;

/**
 * Base class for test cases.
 */
public abstract class BaseFilterTest {

    /**
     * Creates a file select info object for the given file.
     *
     * @param file File to create an info for.
     *
     * @return File selct info.
     */
    protected static FileSelectInfo createFileSelectInfo(final File file) {
        try {
            final FileSystemManager fsManager = VFS.getManager();
            final FileObject fileObject = fsManager.toFileObject(file);
            return new FileSelectInfo() {
                @Override
                public FileObject getFile() {
                    return fileObject;
                }

                @Override
                public int getDepth() {
                    return 0;
                }

                @Override
                public FileObject getBaseFolder() {
                    try {
                        return fileObject.getParent();
                    } catch (final FileSystemException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public String toString() {
                    return Objects.toString(fileObject);
                }
            };
        } catch (final FileSystemException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Returns a ZIP file object.
     *
     * @param file File to resolve.
     *
     * @return File object.
     *
     * @throws FileSystemException Error resolving the file.
     */
    protected static FileObject getZipFileObject(final File file) throws FileSystemException {
        final FileSystemManager fsManager = VFS.getManager();
        return fsManager.resolveFile("zip:" + file.toURI());
    }

    /**
     * Asserts that the array contains the given file names.
     *
     * @param files     Array to check.
     * @param file names File names to find.
     */
    protected void assertContains(final FileObject[] files, final String... fileNames) {
        for (final String fileName : fileNames) {
            if (!find(files, fileName)) {
                fail("File '" + fileName + "' not found in: " + Arrays.asList(files));
            }
        }
    }

    private boolean find(final FileObject[] files, final String fileName) {
        for (final FileObject file : files) {
            final String name = file.getName().getBaseName();
            if (name.equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the temporary directory.
     *
     * @return java.io.tmpdir
     */
    protected static File getTempDir() {
        return new File(System.getProperty("java.io.tmpdir"));
    }

    /**
     * Returns a sub directory of the temporary directory.
     *
     * @param name Name of the sub directory.
     *
     * @return Sub directory of java.io.tmpdir.
     */
    protected static File getTestDir(final String name) {
        return new File(getTempDir(), name);
    }

    /**
     * Verifies at least all given objects are in the list.
     *
     * @param list    List to use.
     * @param objects Objects to find.
     */
    protected static void assertContains(final List<?> list, final Object... objects) {
        for (final Object obj : objects) {
            Assert.assertTrue("Couldn't find " + obj + " in " + Arrays.asList(objects), list.indexOf(obj) > -1);
        }
    }

    /**
     * Verifies only the given objects are in the list.
     *
     * @param list    List to scan.
     * @param objects Objects to find.
     */
    protected static void assertContainsOnly(final List<?> list, final Object... objects) {
        for (final Object obj : objects) {
            Assert.assertTrue("Couldn't find " + obj + " in " + Arrays.asList(objects), list.indexOf(obj) > -1);
        }
        Assert.assertEquals(objects.length, list.size());
    }

    /**
     * Adds a file to a ZIP output stream.
     *
     * @param srcFile  File to add - Cannot be {@code null}.
     * @param destPath Path to use for the file - May be {@code null} or empty.
     * @param out      Destination stream - Cannot be {@code null}.
     *
     * @throws IOException Error writing to the output stream.
     */
    private static void zipFile(final File srcFile, final String destPath, final ZipOutputStream out)
            throws IOException {

        final byte[] buf = new byte[1024];
        try (final InputStream in = new BufferedInputStream(new FileInputStream(srcFile))) {
            final ZipEntry zipEntry = new ZipEntry(concatPathAndFilename(destPath, srcFile.getName(), File.separator));
            zipEntry.setTime(srcFile.lastModified());
            out.putNextEntry(zipEntry);
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.closeEntry();
        }
    }

    /**
     * Add a directory to a ZIP output stream.
     *
     * @param srcDir   Directory to add - Cannot be {@code null} and must be a
     *                 valid directory.
     * @param filter   Filter or {@code null} for all files.
     * @param destPath Path to use for the ZIP archive - May be {@code null} or
     *                 an empyt string.
     * @param out      Destination stream - Cannot be {@code null}.
     *
     * @throws IOException Error writing to the output stream.
     */
    private static void zipDir(final File srcDir, final FileFilter filter, final String destPath,
            final ZipOutputStream out) throws IOException {

        final File[] files = listFiles(srcDir, filter);
        for (final File file : files) {
            if (file.isDirectory()) {
                zipDir(file, filter, concatPathAndFilename(destPath, file.getName(), File.separator), out);
            } else {
                zipFile(file, destPath, out);
            }
        }

    }

    /**
     * Creates a ZIP file and adds all files in a directory and all it's sub
     * directories to the archive. Only entries are added that comply to the file
     * filter.
     *
     * @param srcDir   Directory to add - Cannot be {@code null} and must be a
     *                 valid directory.
     * @param filter   Filter or {@code null} for all files/directories.
     * @param destPath Path to use for the ZIP archive - May be {@code null} or
     *                 an empyt string.
     * @param destFile Target ZIP file - Cannot be {@code null}.
     *
     * @throws IOException Error writing to the output stream.
     */
    public static void zipDir(final File srcDir, final FileFilter filter, final String destPath, final File destFile)
            throws IOException {

        if (srcDir == null) {
            throw new IllegalArgumentException("srcDir cannot be null");
        }
        if (!srcDir.exists()) {
            throw new IllegalArgumentException("srcDir does not exist");
        }
        if (!srcDir.isDirectory()) {
            throw new IllegalArgumentException("srcDir is not a directory");
        }
        if (destFile == null) {
            throw new IllegalArgumentException("destFile cannot be null");
        }

        try (final ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destFile)));) {
            zipDir(srcDir, filter, destPath, out);
        }

    }

    /**
     * Creates a ZIP file and adds all files in a directory and all it's sub
     * directories to the archive.
     *
     * @param srcDir   Directory to add - Cannot be {@code null} and must be a
     *                 valid directory.
     * @param destPath Path to use for the ZIP archive - May be {@code null} or
     *                 an empyt string.
     * @param destFile Target ZIP file - Cannot be {@code null}.
     *
     * @throws IOException Error writing to the output stream.
     */
    public static void zipDir(final File srcDir, final String destPath, final File destFile) throws IOException {

        zipDir(srcDir, null, destPath, destFile);

    }

    /**
     * Concatenate a path and a file name taking {@code null} and empty string
     * values into account.
     *
     * @param path      Path - Can be {@code null} or an empty string.
     * @param fileName  Filename - Cannot be {@code null}.
     * @param separator Separator for directories - Can be {@code null} or an
     *                  empty string.
     *
     * @return Path and file name divided by the separator.
     */
    public static String concatPathAndFilename(final String path, final String fileName, final String separator) {

        if (fileName == null) {
            throw new IllegalArgumentException("file name cannot be null");
        }
        if (fileName.trim().length() == 0) {
            throw new IllegalArgumentException("file name cannot be empty");
        }
        if (separator == null) {
            throw new IllegalArgumentException("separator cannot be null");
        }
        if (separator.trim().length() == 0) {
            throw new IllegalArgumentException("separator cannot be empty");
        }

        if (path == null) {
            return fileName;
        }
        final String trimmedPath = path.trim();
        if (trimmedPath.length() == 0) {
            return fileName;
        }
        final String trimmedFilename = fileName.trim();
        if (trimmedPath.endsWith(separator)) {
            return trimmedPath + trimmedFilename;
        }
        return trimmedPath + separator + trimmedFilename;

    }

    /**
     * List all files for a directory.
     *
     * @param srcDir Directory to list the files for - Cannot be {@code null}
     *               and must be a valid directory.
     * @param filter Filter or {@code null} for all files.
     *
     * @return List of child entries of the directory.
     */
    private static File[] listFiles(final File srcDir, final FileFilter filter) {

        final File[] files;
        if (filter == null) {
            files = srcDir.listFiles();
        } else {
            files = srcDir.listFiles(filter);
        }
        return files;

    }

}
