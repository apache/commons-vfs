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
package org.apache.commons.vfs2.provider.ftp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.junit.jupiter.api.Test;

/**
 * Tests https://issues.apache.org/jira/browse/VFS-793.
 *
 * See also https://issues.apache.org/jira/browse/VFS-810.
 */
public class FtpFileNameParserTest {

    @Test
    public void testGenericFileName1() throws Exception {
        final String uri = "ftp://blanks:blanks@localhost/path/file_b%20lanks";
        final FileName n = FtpFileNameParser.getInstance().parseUri(null, null, uri);
        assertInstanceOf(GenericFileName.class, n);
        final String genericUri = n.getURI();
        assertEquals(genericUri, uri.toString());
    }

    @Test
    public void testGenericFileName2() throws Exception {
        final String uri = "ftp://b%20lanks:b%20lanks@localhost/path/file";
        final FileName n = FtpFileNameParser.getInstance().parseUri(null, null, uri);
        assertInstanceOf(GenericFileName.class, n);
        final String genericUri = n.getURI();
        assertEquals(genericUri, uri.toString());
    }

}
