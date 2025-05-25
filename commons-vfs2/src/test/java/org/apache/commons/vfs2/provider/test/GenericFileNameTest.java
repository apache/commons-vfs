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
package org.apache.commons.vfs2.provider.test;

import static org.apache.commons.vfs2.VfsTestUtils.assertSameMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.apache.commons.vfs2.provider.GenericURLFileNameParser;
import org.apache.commons.vfs2.provider.URLFileNameParser;
import org.junit.jupiter.api.Test;

/**
 * Some GenericFileName test cases.
 */
public class GenericFileNameTest {

    /**
     * Tests error handling in URI parser.
     */
    @Test
    public void testBadlyFormedUri() throws Exception {
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

        // TODO Improperly accepted malformed uris
        // testBadlyFormedUri("ftp://host[a/file", "malformed uri");
        // testBadlyFormedUri("ftp://host]a/file", "malformed uri");
    }

    /**
     * Tests that parsing a URI fails with the expected error.
     */
    private void testBadlyFormedUri(final String uri, final String errorMsg) {
        try {
            new URLFileNameParser(80).parseUri(null, null, uri);
            fail();
        } catch (final FileSystemException e) {
            assertSameMessage(errorMsg, uri, e);
        }
    }

    @Test
    public void testIPv6BadlyFormedUri() {
        // address with opening bracket only
        testBadlyFormedUri("ftp://[", "vfs.provider/unterminated-ipv6-hostname.error");

        // address with closing bracket only (ftp://]) actually currently parses ok, but it's not considered as IPv6 case by parser

        // address with unterminated host name
        testBadlyFormedUri("ftp://[fe80::8b2:d61e:e5c:b333", "vfs.provider/unterminated-ipv6-hostname.error");

        // address without opening bracket (first ":" considered as port number separator in this case)
        testBadlyFormedUri("ftp://fe80::8b2:d61e:e5c:b333]", "vfs.provider/missing-port.error");

        // empty address in brackets
        testBadlyFormedUri("ftp://[]", "vfs.provider/missing-hostname.error");

        // double square brackets
        // (first "]" considered as terminating bracket, path separator is expected instead of the second "]")
        testBadlyFormedUri("ftp://[[fe80::8b2:d61e:e5c:b333]]", "vfs.provider/missing-hostname-path-sep.error");

        // two empty strings in brackets
        testBadlyFormedUri("ftp://[][]", "vfs.provider/missing-hostname.error");

        // two non-empty strings in brackets
        testBadlyFormedUri("ftp://[fe80::8b2:d61e:e5c:b333][fe80::8b2:d61e:e5c:b333]", "vfs.provider/missing-hostname-path-sep.error");
    }

    @Test
    public void testParseIPv6InvalidHostsTolerance() throws Exception {
        // We don't strictly validate IPv6 host name, if it can be parsed out from URI
        // Assuming, it'll just fail on connection stage

        final GenericURLFileNameParser urlParser = new GenericURLFileNameParser(21);

        // too few segments
        GenericFileName name = (GenericFileName) urlParser.parseUri(null, null, "ftp://[1:2e]:2222/test");
        assertEquals("[1:2e]", name.getHostName());
        assertEquals(2222, name.getPort());
        assertEquals("ftp://[1:2e]:2222/test", name.getURI());

        // IPv4 address in square brackets
        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://[192.168.1.1]:2222/test");
        assertEquals("[192.168.1.1]", name.getHostName());
        assertEquals(2222, name.getPort());
        assertEquals("ftp://[192.168.1.1]:2222/test", name.getURI());

        // too many segments
        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://[::7:6:5:4:3:2:1:0]:2222/test");
        assertEquals("[::7:6:5:4:3:2:1:0]", name.getHostName());
        assertEquals(2222, name.getPort());
        assertEquals("ftp://[::7:6:5:4:3:2:1:0]:2222/test", name.getURI());

        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://[3ffe:0:0:0:0:0:0:0:1]:2222/test");
        assertEquals("[3ffe:0:0:0:0:0:0:0:1]", name.getHostName());
        assertEquals(2222, name.getPort());
        assertEquals("ftp://[3ffe:0:0:0:0:0:0:0:1]:2222/test", name.getURI());

        // segment exceeds 16 bits
        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://[3ffe::10000]:2222/test");
        assertEquals("[3ffe::10000]", name.getHostName());
        assertEquals(2222, name.getPort());
        assertEquals("ftp://[3ffe::10000]:2222/test", name.getURI());

        // whitespace host
        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://[ ]:2222/test");
        assertEquals("[ ]", name.getHostName());
        assertEquals(2222, name.getPort());
        assertEquals("ftp://[ ]:2222/test", name.getURI());

        // just some invalid sequences
        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://[:]:2222/test");
        assertEquals("[:]", name.getHostName());
        assertEquals(2222, name.getPort());
        assertEquals("ftp://[:]:2222/test", name.getURI());

        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://[:::]:2222/test");
        assertEquals("[:::]", name.getHostName());
        assertEquals(2222, name.getPort());
        assertEquals("ftp://[:::]:2222/test", name.getURI());

        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://[xyz]:2222/test");
        assertEquals("[xyz]", name.getHostName());
        assertEquals(2222, name.getPort());
        assertEquals("ftp://[xyz]:2222/test", name.getURI());
    }

    @Test
    public void testParseIPv6Uri() throws Exception {
        final GenericURLFileNameParser urlParser = new GenericURLFileNameParser(21);

        // basic case
        GenericFileName name = (GenericFileName) urlParser.parseUri(
                null, null, "ftp://[fe80::3dd0:7f8e:57b7:34d5]:2222/test");
        assertEquals("[fe80::3dd0:7f8e:57b7:34d5]", name.getHostName());
        assertEquals(2222, name.getPort());
        assertEquals("/test", name.getPath());
        assertEquals("ftp://[fe80::3dd0:7f8e:57b7:34d5]:2222/", name.getRootURI());
        assertEquals("ftp://[fe80::3dd0:7f8e:57b7:34d5]:2222/test", name.getURI());

        // full uri case
        name = (GenericFileName) urlParser.parseUri(
                null, null, "http://user:password@[fe80::3dd0:7f8e:57b7:34d5]:2222/test?param1=value1&param2=value2#fragment");
        assertEquals("[fe80::3dd0:7f8e:57b7:34d5]", name.getHostName());
        assertEquals(2222, name.getPort());
        assertEquals("/test", name.getPath());
        assertEquals("http://user:password@[fe80::3dd0:7f8e:57b7:34d5]:2222/", name.getRootURI());
        assertEquals(
                "http://user:password@[fe80::3dd0:7f8e:57b7:34d5]:2222/test?param1=value1&param2=value2#fragment",
                name.getURI());

        // no trailing zeroes case
        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://[2001:658:22a:cafe::]:2222/test");
        assertEquals("[2001:658:22a:cafe::]", name.getHostName());
        assertEquals(2222, name.getPort());
        assertEquals("ftp://[2001:658:22a:cafe::]:2222/test", name.getURI());

        // the loopback address
        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://[::1]:2222/test");
        assertEquals("[::1]", name.getHostName());
        assertEquals(2222, name.getPort());
        assertEquals("ftp://[::1]:2222/test", name.getURI());

        // the unspecified address
        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://[::]:2222/test");
        assertEquals("[::]", name.getHostName());
        assertEquals(2222, name.getPort());
        assertEquals("ftp://[::]:2222/test", name.getURI());

        // form for a mixed environment of IPv4 and IPv6
        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://[0:0:0:0:0:0:13.1.68.3]:2222/test");
        assertEquals("[0:0:0:0:0:0:13.1.68.3]", name.getHostName());
        assertEquals(2222, name.getPort());
        assertEquals("ftp://[0:0:0:0:0:0:13.1.68.3]:2222/test", name.getURI());

        // compressed form for a mixed environment of IPv4 and IPv6
        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://[::13.1.68.3]:2222/test");
        assertEquals("[::13.1.68.3]", name.getHostName());
        assertEquals(2222, name.getPort());
        assertEquals("ftp://[::13.1.68.3]:2222/test", name.getURI());

        // compressed form for a mixed environment of IPv4 and IPv6
        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://[::FFFF:129.144.52.38]:2222/test");
        assertEquals("[::ffff:129.144.52.38]", name.getHostName());
        assertEquals(2222, name.getPort());
        assertEquals("ftp://[::ffff:129.144.52.38]:2222/test", name.getURI());

        // a multicast address
        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://[FF01::101]:2222/test");
        assertEquals("[ff01::101]", name.getHostName());
        assertEquals(2222, name.getPort());
        assertEquals("ftp://[ff01::101]:2222/test", name.getURI());

        // url without path
        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://[FF01::101]:2222");
        assertEquals("[ff01::101]", name.getHostName());
        assertEquals(2222, name.getPort());
        assertEquals("ftp://[ff01::101]:2222/", name.getURI());

        // url without path and port
        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://[FF01::101]");
        assertEquals("[ff01::101]", name.getHostName());
        assertEquals(21, name.getPort());
        assertEquals("ftp://[ff01::101]/", name.getURI());

        // address with scopeId
        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://[fe80::8b2:d61e:e5c:b333%15]");
        assertEquals("[fe80::8b2:d61e:e5c:b333%15]", name.getHostName());
        assertEquals(21, name.getPort());
        assertEquals("ftp://[fe80::8b2:d61e:e5c:b333%15]/", name.getURI());

        // address with scopeId and escaped characters in the path
        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://[fe80::8b2:d61e:e5c:b333%15]/tests%3A+test+1");
        assertEquals("[fe80::8b2:d61e:e5c:b333%15]", name.getHostName());
        assertEquals(21, name.getPort());
        assertEquals("ftp://[fe80::8b2:d61e:e5c:b333%15]/tests:+test+1", name.getURI());
    }

    /**
     * Tests parsing a URI into its parts.
     */
    @Test
    public void testParseUri() throws Exception {
        final URLFileNameParser urlParser = new URLFileNameParser(21);
        // Simple name
        GenericFileName name = (GenericFileName) urlParser.parseUri(null, null, "ftp://hostname/file");
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
        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://hostname:9090/file");
        assertEquals("ftp", name.getScheme());
        assertNull(name.getUserName());
        assertNull(name.getPassword());
        assertEquals("hostname", name.getHostName());
        assertEquals(9090, name.getPort());
        assertEquals("/file", name.getPath());
        assertEquals("ftp://hostname:9090/", name.getRootURI());
        assertEquals("ftp://hostname:9090/file", name.getURI());

        // Name with no path
        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://hostname");
        assertEquals("ftp", name.getScheme());
        assertNull(name.getUserName());
        assertNull(name.getPassword());
        assertEquals("hostname", name.getHostName());
        assertEquals(21, name.getPort());
        assertEquals("/", name.getPath());
        assertEquals("ftp://hostname/", name.getRootURI());
        assertEquals("ftp://hostname/", name.getURI());

        // Name with username
        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://user@hostname/file");
        assertEquals("ftp", name.getScheme());
        assertEquals("user", name.getUserName());
        assertNull(name.getPassword());
        assertEquals("hostname", name.getHostName());
        assertEquals(21, name.getPort());
        assertEquals("/file", name.getPath());
        assertEquals("ftp://user@hostname/", name.getRootURI());
        assertEquals("ftp://user@hostname/file", name.getURI());

        // Name with username and password
        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://user:password@hostname/file");
        assertEquals("ftp", name.getScheme());
        assertEquals("user", name.getUserName());
        assertEquals("password", name.getPassword());
        assertEquals("hostname", name.getHostName());
        assertEquals(21, name.getPort());
        assertEquals("/file", name.getPath());
        assertEquals("ftp://user:password@hostname/", name.getRootURI());
        assertEquals("ftp://user:password@hostname/file", name.getURI());

        // Encoded username and password: %75 -> 'u', %40 -> '@'
        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://%75ser%3A:%40@hostname");
        assertEquals("ftp", name.getScheme());
        assertEquals("user:", name.getUserName());
        assertEquals("@", name.getPassword());
        assertEquals("hostname", name.getHostName());
        assertEquals(21, name.getPort());
        assertEquals("/", name.getPath());
        // RFC 2396: The ':' character in a userinfo does not have to be escaped/percent-encoded, it is NOT RECOMMENDED for use.
        // RFC 3986: The ':' character in a userinfo is deprecated.
        // See also https://issues.apache.org/jira/browse/VFS-810
        assertEquals("ftp://user::%40@hostname/", name.getRootURI());
        assertEquals("ftp://user::%40@hostname/", name.getURI());

        // Hostname with unreserved uri symbols "-", ".", "_", "~"
        // https://datatracker.ietf.org/doc/html/rfc3986#page-49
        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://p0~p1_p2-p3.p4/file");
        assertEquals("ftp", name.getScheme());
        assertNull(name.getUserName());
        assertNull(name.getPassword());
        assertEquals("p0~p1_p2-p3.p4", name.getHostName());
        assertEquals(21, name.getPort());
        assertEquals("/file", name.getPath());
        assertEquals("ftp://p0~p1_p2-p3.p4/", name.getRootURI());
        assertEquals("ftp://p0~p1_p2-p3.p4/file", name.getURI());

        // Hostname with sub-delim uri symbols that are currently accepted with the hostname parser
        // https://datatracker.ietf.org/doc/html/rfc3986#page-49
        name = (GenericFileName) urlParser.parseUri(null, null, "ftp://p0!p1'p2(p3)*p4/file");
        assertEquals("ftp", name.getScheme());
        assertNull(name.getUserName());
        assertNull(name.getPassword());
        assertEquals("p0!p1'p2(p3)*p4", name.getHostName());
        assertEquals(21, name.getPort());
        assertEquals("/file", name.getPath());
        assertEquals("ftp://p0!p1'p2(p3)*p4/", name.getRootURI());
        assertEquals("ftp://p0!p1'p2(p3)*p4/file", name.getURI());

        // Hostnames with sub-delim uri symbols that are currently not accepted with the hostname parser
        // (which looks wrong)
        // https://datatracker.ietf.org/doc/html/rfc3986#page-49
        // name = (GenericFileName) urlParser.parseUri(null, null, "ftp://p0$p1/file");
        // name = (GenericFileName) urlParser.parseUri(null, null, "ftp://p0&p1/file");
        // name = (GenericFileName) urlParser.parseUri(null, null, "ftp://p0+p1/file");
        // name = (GenericFileName) urlParser.parseUri(null, null, "ftp://p0,p1/file");
        // name = (GenericFileName) urlParser.parseUri(null, null, "ftp://p0;p1/file");
        // name = (GenericFileName) urlParser.parseUri(null, null, "ftp://p0=p1/file");
    }
}
