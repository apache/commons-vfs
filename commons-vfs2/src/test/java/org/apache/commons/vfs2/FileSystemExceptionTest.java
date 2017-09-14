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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests the {@link FileSystemException}.
 */
public class FileSystemExceptionTest {
    /**
     * Tests a {@link FileSystemException} containing info with a URL containing a complete basic authentication.
     */
    @Test
    public void testMasksPasswordOfUrlsWithBasicAuthentication() {
        final FileSystemException fse = new FileSystemException("vfs.provider/rename.error", "file://test.bin",
                "http://foo:bar@junit.org/test.bin");

        assertEquals("file://test.bin", fse.getInfo()[0]);
        assertEquals("http://foo:***@junit.org/test.bin", fse.getInfo()[1]);
    }

    /**
     * Tests a {@link FileSystemException} containing info with a URL containing only the user information.
     */
    @Test
    public void testDoesNotModifyUrlsWithoutPassword() {
        final FileSystemException fse = new FileSystemException("vfs.provider/delete.error",
                "http://foo@junit.org/test.bin");
        assertEquals("http://foo@junit.org/test.bin", fse.getInfo()[0]);
    }

    /**
     * Tests a {@link FileSystemException} containing info with a nested URL containing a complete basic authentication.
     */
    @Test
    public void testProperDetectionOfUrl() {
        final FileSystemException fse = new FileSystemException("vfs.provider/delete.error",
                "zip:http://foo:bar@junit.org/test.bin");
        assertEquals("zip:http://foo:***@junit.org/test.bin", fse.getInfo()[0]);
    }

}
