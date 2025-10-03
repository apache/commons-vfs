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
package org.apache.commons.vfs2.provider.local;

import org.apache.commons.vfs2.AbstractProviderTestCase;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Additional naming tests for local file system.
 * <p>
 * Only executed on Windows.
 */
public class WindowsFileNameTests extends AbstractProviderTestCase {

    @Test
    public void testWindowsFileNameParserError() {
        // check VFS-338 with 2+4 slashes we want a dedicated error
        try {
            final String file = "file://////";
            final DefaultFileSystemManager manager = getManager();
            Assertions.assertNotNull(manager, "Unexpected null manager for test " + this);
            final FileObject fo = manager.resolveFile(file);
            fail("Windows File Parser should not allow " + file + " " + fo);
        } catch (FileSystemException ex) {
            assertEquals("Exception code", "vfs.provider/invalid-absolute-uri.error", ex.getCode());
            ex = (FileSystemException) ex.getCause();
            assertEquals("Exception code", "vfs.provider.local/not-absolute-file-name.error", ex.getCode());
        }
    }

    @Test
    public void testWindowsFileNameUNCStartError() {
        try {
            final String file = "file://///";
            final DefaultFileSystemManager manager = getManager();
            Assertions.assertNotNull(manager, "Unexpected null manager for test " + this);
            final FileObject fo = manager.resolveFile(file);
            fail("Windows File Parser should not allow " + file + " " + fo);
        } catch (FileSystemException ex) {
            assertEquals("Exception code", "vfs.provider/invalid-absolute-uri.error", ex.getCode());
            ex = (FileSystemException) ex.getCause();
            assertEquals("Exception code", "vfs.provider.local/missing-share-name.error", ex.getCode());
        }
    }

    @Test
    public void testWindowsRoots() throws Exception {
        // valid URI forms of the filesystem root
        final String[] tests = { "file:///C:/", "file://C:/", "file:/C:/", "file:C:/" };

        for (final String name : tests) {
            final DefaultFileSystemManager manager = getManager();
            Assertions.assertNotNull(manager, "Unexpected null manager for test " + this);
            final FileName fn = manager.resolveFile(name).getName();

            // the following tests work for Windows file names only
            assertSame(WindowsFileName.class, fn.getClass());

            // all should result in the same FileName
            assertEquals("file:///C:/", fn.toString());
            assertEquals("/", fn.getPath());
            assertEquals("/", fn.getPathDecoded());
            assertEquals("file:///C:/", fn.getRootURI());
            assertEquals("file:///C:/", fn.getFriendlyURI());

            assertEquals("file:///C:/", fn.getRoot().toString());

            assertEquals("", fn.getExtension());
            assertEquals("", fn.getBaseName());
        }
    }

    @Test
    public void testWindowsWrongRoots() {
        final String[] tests = { "file:///C:", "file://C:", "file:/C:", "file:C:" };

        for (final String name : tests) {
            try {
                final DefaultFileSystemManager manager = getManager();
                Assertions.assertNotNull(manager, "Unexpected null manager for test " + this);
                final FileName fn = manager.resolveFile(name).getName();
                fail("should not accept root " + name);
            } catch (final FileSystemException ex) {
                assertEquals("vfs.provider/invalid-absolute-uri.error", ex.getCode());
                assertTrue(ex.toString().contains(name));
            }
        }
    }

}
