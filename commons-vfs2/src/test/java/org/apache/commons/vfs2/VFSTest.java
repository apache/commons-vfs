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

package org.apache.commons.vfs2;

import java.nio.file.Paths;

import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.junit.Assert;
import org.junit.Test;

public class VFSTest {

    /**
     * Tests {@link FileSystemManager#close()}.
     *
     * @throws FileSystemException
     * @since 2.5.0
     */
    @Test
    public void test_close() throws FileSystemException {
        try (FileSystemManager fileSystemManager = new StandardFileSystemManager()) {
            VFS.setManager(fileSystemManager);
            VFS.setManager(null);
        }
        Assert.assertNotNull(VFS.getManager());
        Assert.assertFalse(VFS.getManager().resolveFile(Paths.get("DoesNotExist.not").toUri()).exists());
    }

    @Test
    public void test_setManager() throws FileSystemException {
        final StandardFileSystemManager fileSystemManager = new StandardFileSystemManager();
        VFS.setManager(fileSystemManager);
        Assert.assertEquals(fileSystemManager, VFS.getManager());
        // Reset global for other tests
        VFS.setManager(null);
        Assert.assertNotNull(VFS.getManager());
        Assert.assertNotEquals(fileSystemManager, VFS.getManager());
    }
}
