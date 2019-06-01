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
package org.apache.commons.vfs2.provider.webdav.test;

import java.io.OutputStream;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.provider.URLFileName;
import org.apache.commons.vfs2.provider.webdav.WebdavFileSystemConfigBuilder;
import org.apache.commons.vfs2.test.AbstractProviderTestCase;
import org.apache.jackrabbit.webdav.version.DeltaVConstants;
import org.apache.jackrabbit.webdav.version.VersionControlledResource;

/**
 * Test to verify Webdav Versioning support
 */
public class WebdavVersioningTests extends AbstractProviderTestCase {
    /**
     */
    public void testVersioning() throws Exception {
        final FileObject scratchFolder = createScratchFolder();
        final FileSystemOptions opts = scratchFolder.getFileSystem().getFileSystemOptions();
        final WebdavFileSystemConfigBuilder builder = (WebdavFileSystemConfigBuilder) getManager()
                .getFileSystemConfigBuilder("webdav");
        builder.setVersioning(opts, true);
        final FileObject file = getManager().resolveFile(scratchFolder, "file1.txt", opts);
        final FileSystemOptions newOpts = file.getFileSystem().getFileSystemOptions();
        assertTrue(opts == newOpts);
        assertTrue(builder.isVersioning(newOpts));
        assertTrue(!file.exists());
        file.createFile();
        assertTrue(file.exists());
        assertSame(FileType.FILE, file.getType());
        assertTrue(file.isFile());
        assertEquals(0, file.getContent().getSize());
        assertFalse(file.isExecutable());
        assertFalse(file.isHidden());
        assertTrue(file.isReadable());
        assertTrue(file.isWriteable());
        Map<?, ?> map = file.getContent().getAttributes();
        final String name = ((URLFileName) file.getName()).getUserName();
        assertTrue(map.containsKey(DeltaVConstants.CREATOR_DISPLAYNAME.toString()));
        if (name != null) {
            assertEquals(name, map.get(DeltaVConstants.CREATOR_DISPLAYNAME.toString()));
        }
        assertTrue(map.containsKey(VersionControlledResource.CHECKED_IN.toString()));

        // Create the source file
        final String content = "Here is some sample content for the file.  Blah Blah Blah.";

        final OutputStream os = file.getContent().getOutputStream();
        try {
            os.write(content.getBytes("utf-8"));
        } finally {
            os.close();
        }
        assertSameContent(content, file);
        map = file.getContent().getAttributes();
        assertTrue(map.containsKey(DeltaVConstants.CREATOR_DISPLAYNAME.toString()));
        if (name != null) {
            assertEquals(name, map.get(DeltaVConstants.CREATOR_DISPLAYNAME.toString()));
        }
        assertTrue(map.containsKey(VersionControlledResource.CHECKED_IN.toString()));
        builder.setVersioning(opts, false);
    }

    /**
     */
    public void testVersioningWithCreator() throws Exception {
        final FileObject scratchFolder = createScratchFolder();
        final FileSystemOptions opts = scratchFolder.getFileSystem().getFileSystemOptions();
        final WebdavFileSystemConfigBuilder builder = (WebdavFileSystemConfigBuilder) getManager()
                .getFileSystemConfigBuilder("webdav");
        builder.setVersioning(opts, true);
        builder.setCreatorName(opts, "testUser");
        final FileObject file = getManager().resolveFile(scratchFolder, "file1.txt", opts);
        final FileSystemOptions newOpts = file.getFileSystem().getFileSystemOptions();
        assertTrue(opts == newOpts);
        assertTrue(builder.isVersioning(newOpts));
        assertTrue(!file.exists());
        file.createFile();
        assertTrue(file.exists());
        assertSame(FileType.FILE, file.getType());
        assertTrue(file.isFile());
        assertEquals(0, file.getContent().getSize());
        assertFalse(file.isExecutable());
        assertFalse(file.isHidden());
        assertTrue(file.isReadable());
        assertTrue(file.isWriteable());
        Map<?, ?> map = file.getContent().getAttributes();
        final String name = ((URLFileName) file.getName()).getUserName();
        assertTrue(map.containsKey(DeltaVConstants.CREATOR_DISPLAYNAME.toString()));
        assertEquals(map.get(DeltaVConstants.CREATOR_DISPLAYNAME.toString()), "testUser");
        if (name != null) {
            assertTrue(map.containsKey(DeltaVConstants.COMMENT.toString()));
            assertEquals("Modified by user " + name, map.get(DeltaVConstants.COMMENT.toString()));
        }
        assertTrue(map.containsKey(VersionControlledResource.CHECKED_IN.toString()));

        // Create the source file
        final String content = "Here is some sample content for the file.  Blah Blah Blah.";

        final OutputStream os = file.getContent().getOutputStream();
        try {
            os.write(content.getBytes("utf-8"));
        } finally {
            os.close();
        }
        assertSameContent(content, file);
        map = file.getContent().getAttributes();
        assertTrue(map.containsKey(DeltaVConstants.CREATOR_DISPLAYNAME.toString()));
        assertEquals(map.get(DeltaVConstants.CREATOR_DISPLAYNAME.toString()), "testUser");
        if (name != null) {
            assertTrue(map.containsKey(DeltaVConstants.COMMENT.toString()));
            assertEquals("Modified by user " + name, map.get(DeltaVConstants.COMMENT.toString()));
        }
        assertTrue(map.containsKey(VersionControlledResource.CHECKED_IN.toString()));
        builder.setVersioning(opts, false);
        builder.setCreatorName(opts, null);
    }

    /**
     * Sets up a scratch folder for the test to use.
     */
    protected FileObject createScratchFolder() throws Exception {
        final FileObject scratchFolder = getWriteFolder();

        // Make sure the test folder is empty
        scratchFolder.delete(Selectors.EXCLUDE_SELF);
        scratchFolder.createFolder();

        return scratchFolder;
    }

}
