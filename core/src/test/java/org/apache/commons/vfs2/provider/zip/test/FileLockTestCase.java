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

package org.apache.commons.vfs2.provider.zip.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests https://issues.apache.org/jira/browse/VFS-291
 */
public class FileLockTestCase {

    /**
     * This test checks whether we can modify an underlying zip file after we have performed IO operations on files
     * within it, but although we no longer have any FileObjects explicitely open.
     * 
     * @throws IOException
     */
    @Test
    @Ignore
    public void testResourcesReleasedByZipFileProvider() throws Exception {
        FileSystemManager manager = VFS.getManager();
        //
        // We copy the normal test zip to a second, nominally temporary, file so that we can try to delete it with
        // impunity. Since the test fails, the file will be left behind, so it should probably be created in a temporary
        // directory somewhere.
        //
        final File zipFile = new File("src/test/resources/test-data/test.zip");
        final File newZipFile = new File(AbstractVfsTestCase.getTestDirectory(), "test2.zip");
        FileUtils.copyFile(zipFile, newZipFile);

        final String uri = "zip:file:" + newZipFile.getAbsolutePath() + "!/read-tests/file1.txt";
        try (final FileObject fileOne = manager.resolveFile(uri)) {
            fileOne.getContent().getInputStream().close();
        }

        Assert.assertTrue("Could not delete file", newZipFile.delete());
    }
}
