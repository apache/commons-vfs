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
package org.apache.commons.vfs2.provider.tar.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.cache.SoftRefFilesCache;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs2.provider.tar.TarFileProvider;
import org.junit.Before;
import org.junit.Test;

//@SuppressWarnings("nls")
public class LargeTarTestCase {
    private final static String baseDir = "target/test-classes/test-data/";

    private DefaultFileSystemManager manager;
    private final static String largeFilePath = baseDir;
    private final static String largeFileName = "largefile";

    @Before
    public void setUp() throws Exception {
        manager = new DefaultFileSystemManager();

        manager.setFilesCache(new SoftRefFilesCache());
        manager.setCacheStrategy(CacheStrategy.ON_RESOLVE);

        manager.addProvider("file", new DefaultLocalFileProvider());
        manager.addProvider("tgz", new TarFileProvider());
        manager.addProvider("tar", new TarFileProvider());

        new File(baseDir).mkdir(); // if test is run standalone
        createLargeFile(largeFilePath, largeFileName);
    }

    @Test
    public void testLargeFile() throws Exception {
        final File realFile = new File(largeFilePath + largeFileName + ".tar.gz");

        final FileObject file = manager.resolveFile("tgz:file://" + realFile.getCanonicalPath() + "!/");

        assertNotNull(file);
        final List<FileObject> files = Arrays.asList(file.getChildren());

        assertNotNull(files);
        assertEquals(1, files.size());
        final FileObject f = files.get(0);

        assertTrue("Expected file not found: " + largeFileName + ".txt",
                f.getName().getBaseName().equals(largeFileName + ".txt"));
    }

    /*
     * public void testFileCheck() throws Exception { String[] expectedFiles = { "plugins.tsv", "languages.tsv",
     * "browser_type.tsv", "timezones.tsv", "color_depth.tsv", "resolution.tsv", "connection_type.tsv",
     * "search_engines.tsv", "javascript_version.tsv", "operating_systems.tsv", "country.tsv", "browser.tsv" };
     *
     * fileCheck(expectedFiles, "tar:file://c:/temp/data/data/data-small.tar"); }
     */

    protected void fileCheck(final String[] expectedFiles, final String tarFile) throws Exception {
        assertNotNull(manager);
        final FileObject file = manager.resolveFile(tarFile);

        assertNotNull(file);
        final List<FileObject> files = Arrays.asList(file.getChildren());

        assertNotNull(files);
        for (final String expectedFile : expectedFiles) {
            assertTrue("Expected file not found: " + expectedFile, fileExists(expectedFile, files));
        }
    }

    /**
     * Search for the expected file in a given list, without using the full path.
     *
     * @param expectedFile the expected file.
     * @param files a list of files to search.
     * @return {@code true} if {@code expectedFile} is in {@code files}.
     */
    protected boolean fileExists(final String expectedFile, final List<FileObject> files) {
        for (final FileObject file : files) {
            if (file.getName().getBaseName().equals(expectedFile)) {
                return true;
            }
        }
        return false;
    }

    protected boolean endsWith(final String testString, final String[] testList) {
        for (final String string : testList) {
            if (testString.endsWith(string)) {
                return true;
            }
        }
        return false;
    }

    // @SuppressWarnings("unused")
    protected void createLargeFile(final String path, final String name) throws Exception {
        final long _1K = 1024;
        final long _1M = 1024 * _1K;
        // long _256M = 256 * _1M;
        // long _512M = 512 * _1M;
        final long _1G = 1024 * _1M;

        // File size of 3 GB
        final long fileSize = 3 * _1G;

        final File tarGzFile = new File(path + name + ".tar.gz");

        if (!tarGzFile.exists()) {
            System.out.println(
                    "This test is a bit slow. It needs to write 3GB of data as a compressed file (approx. 3MB) to your hard drive");

            final PipedOutputStream outTarFileStream = new PipedOutputStream();
            final PipedInputStream inTarFileStream = new PipedInputStream(outTarFileStream);

            final Thread source = new Thread() {

                @Override
                public void run() {
                    final byte ba_1k[] = new byte[(int) _1K];
                    for (int i = 0; i < ba_1k.length; i++) {
                        ba_1k[i] = 'a';
                    }
                    try {
                        final TarArchiveOutputStream outTarStream = (TarArchiveOutputStream) new ArchiveStreamFactory()
                                .createArchiveOutputStream(ArchiveStreamFactory.TAR, outTarFileStream);
                        // Create archive contents
                        final TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(name + ".txt");
                        tarArchiveEntry.setSize(fileSize);

                        outTarStream.putArchiveEntry(tarArchiveEntry);
                        for (long i = 0; i < fileSize; i += ba_1k.length) {
                            outTarStream.write(ba_1k);
                        }
                        outTarStream.closeArchiveEntry();
                        outTarStream.close();
                        outTarFileStream.close();
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }

            };
            source.start();

            // Create compressed archive
            final OutputStream outGzipFileStream = new FileOutputStream(path + name + ".tar.gz");

            final GzipCompressorOutputStream outGzipStream = (GzipCompressorOutputStream) new CompressorStreamFactory()
                    .createCompressorOutputStream(CompressorStreamFactory.GZIP, outGzipFileStream);

            IOUtils.copy(inTarFileStream, outGzipStream);
            inTarFileStream.close();

            outGzipStream.close();
            outGzipFileStream.close();

        }
    }
}
