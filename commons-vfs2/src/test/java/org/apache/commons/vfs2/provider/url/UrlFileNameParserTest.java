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
package org.apache.commons.vfs2.provider.url;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;

import org.apache.commons.vfs2.FileName;
import org.junit.jupiter.api.Test;

public class UrlFileNameParserTest {

    private void testJira739(final String uriStr) throws Exception {
        // Check that we have a valid URI
        final URI uri = new URI(uriStr);
        // VFS-739 shows that parseUri throws an NPE:
        final FileName fileName = new UrlFileNameParser().parseUri(null, null, uriStr);
        assertEquals(uriStr, fileName.getURI());
        assertEquals(uri.getScheme(), fileName.getScheme());
    }

    @Test
    public void testJira739_scheme_file() throws Exception {
        testJira739("file:///");
    }

    @Test
    public void testJira739_scheme_maprfs() throws Exception {
        testJira739("maprfs:///");
    }

    @Test
    public void testJira739_scheme_ram() throws Exception {
        testJira739("ram:///");
    }

}
