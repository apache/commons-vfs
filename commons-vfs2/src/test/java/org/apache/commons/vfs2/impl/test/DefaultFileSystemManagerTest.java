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
package org.apache.commons.vfs2.impl.test;

import java.io.File;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.junit.Test;

public class DefaultFileSystemManagerTest {
    @Test(expected = NullPointerException.class)
    public void testResolveFileRelativeThrows() throws FileSystemException {
        VFS.getManager().resolveFile((File) null, "relativePath");
    }

    /**
     * Even if the file name is absolute, the base file must be given. This is an inconsistency in the API, but it is
     * documented as such.
     *
     * @see "VFS-519"
     */
    @Test(expected = NullPointerException.class)
    public void testResolveFileAbsoluteThrows() throws FileSystemException {
        final String absolute = new File("/").getAbsoluteFile().toURI().toString();
        VFS.getManager().resolveFile((File) null, absolute);
    }

    @Test(expected = FileSystemException.class)
    public void testResolveFileObjectRelativeThrows() throws FileSystemException {
        VFS.getManager().resolveFile((FileObject) null, "relativePath");
    }

    @Test
    public void testResolveFileObjectNullAbsolute() throws FileSystemException {
        final String absolute = new File("/").getAbsoluteFile().toURI().toString();
        VFS.getManager().resolveFile((FileObject) null, absolute);
    }

    /**
     * If the base name is {@code null}, the file system manager should fail throwing a FileSystemException.
     *
     * @see VFS-189
     */
    @Test(expected = FileSystemException.class)
    public void testResolveFileNameNull() throws FileSystemException {
        VFS.getManager().resolveName((FileName) null, "../");
    }
}
