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
package org.apache.commons.vfs2.provider.smb.test;

import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.smb.SmbFileName;
import org.apache.commons.vfs2.provider.smb.SmbFileNameParser;

/**
 * Some additional SMB file name test cases.
 */
public class FileNameTestCase extends AbstractVfsTestCase {
    /**
     * Tests parsing a URI into its parts.
     *
     * @throws Exception in case of error
     */
    public void testParseUri() throws Exception {
        // Simple name
        SmbFileName name = (SmbFileName) SmbFileNameParser.getInstance().parseUri(null, null,
                "smb://hostname/share/file");
        assertEquals("smb", name.getScheme());
        assertNull(name.getUserName());
        assertNull(name.getPassword());
        assertEquals("hostname", name.getHostName());
        assertEquals(139, name.getPort());
        assertEquals(name.getDefaultPort(), name.getPort());
        assertEquals("share", name.getShare());
        assertEquals("/file", name.getPath());
        assertEquals("smb://hostname/share/", name.getRootURI());
        assertEquals("smb://hostname/share/file", name.getURI());

        // Name with port
        name = (SmbFileName) SmbFileNameParser.getInstance().parseUri(null, null, "smb://hostname:9090/share/file");
        assertEquals("smb", name.getScheme());
        assertNull(name.getUserName());
        assertNull(name.getPassword());
        assertEquals("hostname", name.getHostName());
        assertEquals(9090, name.getPort());
        assertEquals("share", name.getShare());
        assertEquals("/file", name.getPath());
        assertEquals("smb://hostname:9090/share/", name.getRootURI());
        assertEquals("smb://hostname:9090/share/file", name.getURI());

        // Name with no path
        name = (SmbFileName) SmbFileNameParser.getInstance().parseUri(null, null, "smb://hostname/share");
        assertEquals("smb", name.getScheme());
        assertNull(name.getUserName());
        assertNull(name.getPassword());
        assertEquals("hostname", name.getHostName());
        assertEquals(139, name.getPort());
        assertEquals("share", name.getShare());
        assertEquals("/", name.getPath());
        assertEquals("smb://hostname/share/", name.getRootURI());
        assertEquals("smb://hostname/share/", name.getURI());

        // Name with username
        name = (SmbFileName) SmbFileNameParser.getInstance().parseUri(null, null, "smb://user@hostname/share/file");
        assertEquals("smb", name.getScheme());
        assertEquals("user", name.getUserName());
        assertNull(name.getPassword());
        assertEquals("hostname", name.getHostName());
        assertEquals(139, name.getPort());
        assertEquals("share", name.getShare());
        assertEquals("/file", name.getPath());
        assertEquals("smb://user@hostname/share/", name.getRootURI());
        assertEquals("smb://user@hostname/share/file", name.getURI());

        // Name with extension
        name = (SmbFileName) SmbFileNameParser.getInstance().parseUri(null, null, "smb://user@hostname/share/file.txt");
        assertEquals("smb", name.getScheme());
        assertEquals("user", name.getUserName());
        assertNull(name.getPassword());
        assertEquals("hostname", name.getHostName());
        assertEquals(139, name.getPort());
        assertEquals("share", name.getShare());
        assertEquals("/file.txt", name.getPath());
        assertEquals("file.txt", name.getBaseName());
        assertEquals("txt", name.getExtension());
        assertEquals("smb://user@hostname/share/", name.getRootURI());
        assertEquals("smb://user@hostname/share/file.txt", name.getURI());

        // Name look likes extension, but isnt
        name = (SmbFileName) SmbFileNameParser.getInstance().parseUri(null, null, "smb://user@hostname/share/.bashrc");
        assertEquals("smb", name.getScheme());
        assertEquals("user", name.getUserName());
        assertNull(name.getPassword());
        assertEquals("hostname", name.getHostName());
        assertEquals(139, name.getPort());
        assertEquals("share", name.getShare());
        assertEquals("/.bashrc", name.getPath());
        assertEquals(".bashrc", name.getBaseName());
        assertEquals("", name.getExtension());
        assertEquals("smb://user@hostname/share/", name.getRootURI());
        assertEquals("smb://user@hostname/share/.bashrc", name.getURI());
    }

    /**
     * Tests error handling in URI parser.
     *
     * @throws Exception in case of error
     */
    public void testBadlyFormedUri() throws Exception {
        // Does not start with smb://
        testBadlyFormedUri("smb:", "vfs.provider/missing-double-slashes.error");
        testBadlyFormedUri("smb:/", "vfs.provider/missing-double-slashes.error");
        testBadlyFormedUri("smb:a", "vfs.provider/missing-double-slashes.error");

        // Missing hostname
        testBadlyFormedUri("smb://", "vfs.provider/missing-hostname.error");
        testBadlyFormedUri("smb://:21/share", "vfs.provider/missing-hostname.error");
        testBadlyFormedUri("smb:///share", "vfs.provider/missing-hostname.error");

        // Empty port
        testBadlyFormedUri("smb://host:", "vfs.provider/missing-port.error");
        testBadlyFormedUri("smb://host:/share", "vfs.provider/missing-port.error");
        testBadlyFormedUri("smb://host:port/share/file", "vfs.provider/missing-port.error");

        // Missing absolute path
        testBadlyFormedUri("smb://host:90a", "vfs.provider/missing-hostname-path-sep.error");
        testBadlyFormedUri("smb://host?a", "vfs.provider/missing-hostname-path-sep.error");

        // Missing share name
        testBadlyFormedUri("smb://host", "vfs.provider.smb/missing-share-name.error");
        testBadlyFormedUri("smb://host/", "vfs.provider.smb/missing-share-name.error");
        testBadlyFormedUri("smb://host:9090/", "vfs.provider.smb/missing-share-name.error");
    }

    /**
     * Assert that parsing a URI fails with the expected error.
     */
    private void testBadlyFormedUri(final String uri, final String errorMsg) {
        try {
            SmbFileNameParser.getInstance().parseUri(null, null, uri);
            fail();
        } catch (final FileSystemException e) {
            assertSameMessage(errorMsg, uri, e);
        }
    }
}
