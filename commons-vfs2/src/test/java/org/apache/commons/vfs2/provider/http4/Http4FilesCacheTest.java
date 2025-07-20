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
package org.apache.commons.vfs2.provider.http4;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.jupiter.api.Test;

/**
 * Tests https://issues.apache.org/jira/browse/VFS-426 and https://issues.apache.org/jira/browse/VFS-810.
 */
public class Http4FilesCacheTest {

    @Test
    public void testQueryStringUrl0() throws FileSystemException {
        @SuppressWarnings("resource")
        final FileSystemManager fileSystemManager = VFS.getManager();

        final String noQueryStringUrl = "http4://commons.apache.org/";
        try (FileObject noQueryFile = fileSystemManager.resolveFile(noQueryStringUrl)) {
            assertEquals(noQueryStringUrl, noQueryFile.getURL().toExternalForm());
        }
    }

    @Test
    public void testQueryStringUrl1() throws FileSystemException {
        @SuppressWarnings("resource")
        final FileSystemManager fileSystemManager = VFS.getManager();

        final String noQueryStringUrl = "http4://commons.apache.org/vfs";
        try (FileObject noQueryFile = fileSystemManager.resolveFile(noQueryStringUrl)) {
            assertEquals(noQueryStringUrl, noQueryFile.getURL().toExternalForm());
        }
    }

    @Test
    public void testQueryStringUrl2() throws FileSystemException {
        @SuppressWarnings("resource")
        final FileSystemManager fileSystemManager = VFS.getManager();

        final String queryStringUrl = "http4://commons.apache.org/vfs?query=string";
        try (FileObject queryFile = fileSystemManager.resolveFile(queryStringUrl)) {
            assertEquals(queryStringUrl, queryFile.getURL().toExternalForm()); // failed for VFS-426
        }
    }

    @Test
    public void testQueryStringUrl3() throws FileSystemException {
        @SuppressWarnings("resource")
        final FileSystemManager fileSystemManager = VFS.getManager();

        final String queryStringUrl2 = "http4://commons.apache.org/vfs?query=string&more=stuff";
        try (FileObject queryFile2 = fileSystemManager.resolveFile(queryStringUrl2)) {
            assertEquals(queryStringUrl2, queryFile2.getURL().toExternalForm()); // failed for VFS-426
        }
    }

    @Test
    public void testQueryStringUrl4() throws FileSystemException {
        @SuppressWarnings("resource")
        final FileSystemManager fileSystemManager = VFS.getManager();

        // TODO All lowercase input except the percent encoded '\' (%5C);
        // We end up converting back to lowercase, but OK per RFC.
        final String queryStringUrl3 = "http4://alice%5C1234:secret@localhost:8080/";
        try (FileObject queryFile3 = fileSystemManager.resolveFile(queryStringUrl3)) {
            assertEquals(StringUtils.toRootLowerCase(queryStringUrl3), queryFile3.getURL().toExternalForm());
        }
    }

    @Test
    public void testQueryStringUrl5() throws FileSystemException {
        @SuppressWarnings("resource")
        final FileSystemManager fileSystemManager = VFS.getManager();

        // Like testQueryStringUrl4() but with all LC input.
        final String queryStringUrl4 = "http4://alice%5c1234:secret@localhost:8080/";
        try (FileObject queryFile4 = fileSystemManager.resolveFile(queryStringUrl4)) {
            assertEquals(queryStringUrl4, queryFile4.getURL().toExternalForm());
        }
    }

    @Test
    public void testQueryStringUrl6() throws FileSystemException {
        @SuppressWarnings("resource")
        final FileSystemManager fileSystemManager = VFS.getManager();

        // Like testQueryStringUrl4() but with all LC input and NO percent encoding.
        final String queryStringUrl4 = "http4://alice:secret@localhost:8080/";
        try (FileObject queryFile4 = fileSystemManager.resolveFile(queryStringUrl4)) {
            assertEquals(queryStringUrl4, queryFile4.getURL().toExternalForm());
        }
    }

    @Test
    public void testQueryStringUrl7() throws FileSystemException {
        @SuppressWarnings("resource")
        final FileSystemManager fileSystemManager = VFS.getManager();

        // Like testQueryStringUrl4() but with all LC input and NO percent encoding.
        final String queryStringUrl4 = "http4://localhost:8080/";
        try (FileObject queryFile4 = fileSystemManager.resolveFile(queryStringUrl4)) {
            assertEquals(queryStringUrl4, queryFile4.getURL().toExternalForm());
        }
    }

}
