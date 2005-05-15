/*
 * Copyright 2002-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs.provider.test;

import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.GenericFileName;
import org.apache.commons.vfs.provider.URLFileNameParser;
import org.apache.commons.vfs.provider.local.GenericFileNameParser;

/**
 * Some GenericFileName test cases.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class GenericFileNameTestCase
    extends AbstractVfsTestCase
{
    /**
     * Tests parsing a URI into its parts.
     */
    public void testParseUri() throws Exception
    {
        URLFileNameParser urlParser = new URLFileNameParser(21);
        // Simple name
        GenericFileName name = (GenericFileName) urlParser.parseUri(null, "ftp://hostname/file");
        assertEquals("ftp", name.getScheme());
        assertNull(name.getUserName());
        assertNull(name.getPassword());
        assertEquals("hostname", name.getHostName());
        assertEquals(21, name.getPort());
        assertEquals(name.getDefaultPort(), name.getPort());
        assertEquals("/file", name.getPath());
        assertEquals("ftp://hostname/", name.getRootURI());
        assertEquals("ftp://hostname/file", name.getURI());

        // Name with port
        name = (GenericFileName) urlParser.parseUri(null, "ftp://hostname:9090/file");
        assertEquals("ftp", name.getScheme());
        assertNull(name.getUserName());
        assertNull(name.getPassword());
        assertEquals("hostname", name.getHostName());
        assertEquals(9090, name.getPort());
        assertEquals("/file", name.getPath());
        assertEquals("ftp://hostname:9090/", name.getRootURI());
        assertEquals("ftp://hostname:9090/file", name.getURI());

        // Name with no path
        name = (GenericFileName) urlParser.parseUri(null, "ftp://hostname");
        assertEquals("ftp", name.getScheme());
        assertNull(name.getUserName());
        assertNull(name.getPassword());
        assertEquals("hostname", name.getHostName());
        assertEquals(21, name.getPort());
        assertEquals("/", name.getPath());
        assertEquals("ftp://hostname/", name.getRootURI());
        assertEquals("ftp://hostname/", name.getURI());

        // Name with username
        name = (GenericFileName) urlParser.parseUri(null, "ftp://user@hostname/file");
        assertEquals("ftp", name.getScheme());
        assertEquals("user", name.getUserName());
        assertNull(name.getPassword());
        assertEquals("hostname", name.getHostName());
        assertEquals(21, name.getPort());
        assertEquals("/file", name.getPath());
        assertEquals("ftp://user@hostname/", name.getRootURI());
        assertEquals("ftp://user@hostname/file", name.getURI());

        // Name with username and password
        name = (GenericFileName) urlParser.parseUri(null, "ftp://user:password@hostname/file");
        assertEquals("ftp", name.getScheme());
        assertEquals("user", name.getUserName());
        assertEquals("password", name.getPassword());
        assertEquals("hostname", name.getHostName());
        assertEquals(21, name.getPort());
        assertEquals("/file", name.getPath());
        assertEquals("ftp://user:password@hostname/", name.getRootURI());
        assertEquals("ftp://user:password@hostname/file", name.getURI());

        // Encoded username and password
        name = (GenericFileName) urlParser.parseUri(null, "ftp://%75ser%3A:%40@hostname");
        assertEquals("ftp", name.getScheme());
        assertEquals("user:", name.getUserName());
        assertEquals("@", name.getPassword());
        assertEquals("hostname", name.getHostName());
        assertEquals(21, name.getPort());
        assertEquals("/", name.getPath());
        assertEquals("ftp://user%3a:%40@hostname/", name.getRootURI());
        assertEquals("ftp://user%3a:%40@hostname/", name.getURI());
    }

    /**
     * Tests error handling in URI parser.
     */
    public void testBadlyFormedUri() throws Exception
    {
        // Does not start with ftp://
        testBadlyFormedUri("ftp:", "vfs.provider/missing-double-slashes.error");
        testBadlyFormedUri("ftp:/", "vfs.provider/missing-double-slashes.error");
        testBadlyFormedUri("ftp:a", "vfs.provider/missing-double-slashes.error");

        // Missing hostname
        testBadlyFormedUri("ftp://", "vfs.provider/missing-hostname.error");
        testBadlyFormedUri("ftp://:21/file", "vfs.provider/missing-hostname.error");
        testBadlyFormedUri("ftp:///file", "vfs.provider/missing-hostname.error");

        // Empty port
        testBadlyFormedUri("ftp://host:", "vfs.provider/missing-port.error");
        testBadlyFormedUri("ftp://host:/file", "vfs.provider/missing-port.error");
        testBadlyFormedUri("ftp://host:port/file", "vfs.provider/missing-port.error");

        // Missing absolute path
        testBadlyFormedUri("ftp://host:90a", "vfs.provider/missing-hostname-path-sep.error");
        testBadlyFormedUri("ftp://host?a", "vfs.provider/missing-hostname-path-sep.error");
    }

    /**
     * Tests that parsing a URI fails with the expected error.
     */
    private void testBadlyFormedUri(final String uri, final String errorMsg)
    {
        try
        {
            GenericFileNameParser.getInstance().parseUri(null, uri);
            fail();
        }
        catch (final FileSystemException e)
        {
            assertSameMessage(errorMsg, uri, e);
        }
    }
}
