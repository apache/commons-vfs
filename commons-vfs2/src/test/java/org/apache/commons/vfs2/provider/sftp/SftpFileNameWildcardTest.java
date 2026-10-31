/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.provider.sftp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VfsTestUtils;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.UriParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

/**
 * Tests SFTP file names that contain the characters JSch treats as glob wildcards ({@code *} and {@code ?}).
 * <p>
 * Disabled on Windows because the embedded server stores files on the local disk, and Windows file names cannot contain those characters.
 * </p>
 */
@DisabledOnOs(OS.WINDOWS)
public class SftpFileNameWildcardTest {

    private static final String DIR_NAME = "sftp-wildcard-test";

    private File localDir;
    private DefaultFileSystemManager manager;
    private FileObject remoteDir;

    private static File createLocalFile(final File dir, final String name) throws IOException {
        final File file = new File(dir, name);
        Files.write(file.toPath(), name.getBytes(StandardCharsets.UTF_8));
        return file;
    }

    private static String baseName(final FileObject file) throws IOException {
        return UriParser.decode(file.getName().getBaseName());
    }

    private FileObject child(final String baseName) throws IOException {
        for (final FileObject child : remoteDir.getChildren()) {
            if (baseName(child).equals(baseName)) {
                return child;
            }
        }
        return null;
    }

    private String[] childNames(final FileObject folder) throws IOException {
        final FileObject[] children = folder.getChildren();
        final String[] names = new String[children.length];
        for (int i = 0; i < children.length; i++) {
            names[i] = baseName(children[i]);
        }
        Arrays.sort(names);
        return names;
    }

    @BeforeEach
    public void setUp() throws Exception {
        SftpTestServerHelper.startServer();
        localDir = new File(VfsTestUtils.getTestDirectoryFile(), DIR_NAME);
        FileUtils.deleteDirectory(localDir);
        assertTrue(localDir.mkdirs());
        manager = new DefaultFileSystemManager();
        manager.addProvider("sftp", new SftpFileProvider());
        manager.init();
        final FileSystemOptions options = new FileSystemOptions();
        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(options, "no");
        remoteDir = manager.resolveFile(SftpTestServerHelper.getConnectionUri() + "/" + DIR_NAME, options);
    }

    @AfterEach
    public void tearDown() throws Exception {
        try {
            manager.close();
            FileUtils.deleteDirectory(localDir);
        } finally {
            SftpTestServerHelper.stopServer();
        }
    }

    @Test
    public void testEscapeGlob() {
        assertEquals("/dir/plain.txt", SftpFileObject.escapeGlob("/dir/plain.txt"));
        assertEquals("/dir/a\\*b\\?c\\\\d.txt", SftpFileObject.escapeGlob("/dir/a*b?c\\d.txt"));
    }

    /**
     * Deleting a listed file named {@code a*.txt} must not delete its sibling {@code abc.txt}.
     */
    @Test
    public void testDeleteListedChildDoesNotDeleteSiblings() throws Exception {
        final File literal = createLocalFile(localDir, "a*.txt");
        final File sibling = createLocalFile(localDir, "abc.txt");
        final FileObject file = child("a*.txt");
        assertNotNull(file, "a*.txt should be listed");
        assertTrue(file.delete());
        assertFalse(literal.exists(), "a*.txt should be deleted");
        assertTrue(sibling.exists(), "abc.txt must not be deleted");
    }

    /**
     * Deleting an empty folder named {@code d*} must delete it and leave its empty sibling {@code dX}.
     */
    @Test
    public void testDeleteFolderWithWildcardName() throws Exception {
        final File folder = new File(localDir, "d*");
        final File sibling = new File(localDir, "dX");
        assertTrue(folder.mkdir());
        assertTrue(sibling.mkdir());
        final FileObject remoteFolder = child("d*");
        assertNotNull(remoteFolder);
        final boolean deleted = remoteFolder.delete();
        assertTrue(sibling.exists(), "dX must not be deleted");
        assertFalse(folder.exists(), "d* should be deleted");
        assertTrue(deleted);
    }

    /**
     * A file resolved by name must not match a different file through a wildcard.
     */
    @Test
    public void testResolvedFileDoesNotMatchOtherFile() throws Exception {
        final File other = createLocalFile(localDir, "delA.txt");
        final FileObject file = remoteDir.resolveFile("del%3F.txt");
        assertEquals("del?.txt", baseName(file));
        assertFalse(file.exists(), "del?.txt does not exist");
        assertFalse(file.delete());
        assertTrue(other.exists(), "delA.txt must not be deleted");
    }

    /**
     * Listing must keep the whole name of a child that contains {@code ?}.
     */
    @Test
    public void testListChildWithQuestionMark() throws Exception {
        createLocalFile(localDir, "v2?final.txt");
        assertArrayEquals(new String[] {"v2?final.txt"}, childNames(remoteDir));
        final FileObject file = child("v2?final.txt");
        assertNotNull(file);
        assertTrue(file.isFile());
    }

    /**
     * Reading a file named {@code a*.txt} must read that file, even when other names match the wildcard.
     */
    @Test
    public void testReadFileWithWildcardName() throws Exception {
        createLocalFile(localDir, "a*.txt");
        createLocalFile(localDir, "abc.txt");
        final FileObject file = child("a*.txt");
        assertNotNull(file);
        try (InputStream in = file.getContent().getInputStream()) {
            assertEquals("a*.txt", IOUtils.toString(in, StandardCharsets.UTF_8));
        }
    }

    /**
     * Writing a new file named {@code new?.txt} must create that file.
     */
    @Test
    public void testWriteFileWithWildcardName() throws Exception {
        final FileObject file = remoteDir.resolveFile("new%3F.txt");
        try (OutputStream out = file.getContent().getOutputStream()) {
            out.write("content".getBytes(StandardCharsets.UTF_8));
        }
        final File local = new File(localDir, "new?.txt");
        assertTrue(local.exists(), "new?.txt should be created");
        assertEquals("content", new String(Files.readAllBytes(local.toPath()), StandardCharsets.UTF_8));
    }

    /**
     * Renaming {@code a*.txt} must rename that file, even when other names match the wildcard.
     */
    @Test
    public void testRenameFileWithWildcardName() throws Exception {
        createLocalFile(localDir, "a*.txt");
        final File sibling = createLocalFile(localDir, "abc.txt");
        final FileObject file = child("a*.txt");
        assertNotNull(file);
        file.moveTo(remoteDir.resolveFile("b*.txt"));
        assertTrue(new File(localDir, "b*.txt").exists(), "b*.txt should exist");
        assertFalse(new File(localDir, "a*.txt").exists(), "a*.txt should be gone");
        assertTrue(sibling.exists(), "abc.txt must not be touched");
    }

    /**
     * Listing a folder named {@code d*} must list its content, not the siblings that match the wildcard.
     */
    @Test
    public void testListFolderWithWildcardName() throws Exception {
        final File folder = new File(localDir, "d*");
        assertTrue(folder.mkdir());
        assertTrue(new File(localDir, "dX").mkdir());
        createLocalFile(folder, "inside.txt");
        final FileObject remoteFolder = child("d*");
        assertNotNull(remoteFolder);
        assertArrayEquals(new String[] {"inside.txt"}, childNames(remoteFolder));
    }
}
