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
package org.apache.commons.vfs2.provider.local.test;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.test.AbstractProviderTestCase;

/**
 * Additional naming tests for local file system.
 */
public class FileNameTests extends AbstractProviderTestCase {
    /**
     * Tests resolution of an absolute file name.
     */
    public void testAbsoluteFileName() throws Exception {
        // Locate file by absolute file name
        final String fileName = new File("testdir").getAbsolutePath();
        final FileObject absFile = getManager().resolveFile(fileName);

        // Locate file by URI
        final String uri = "file://" + fileName.replace(File.separatorChar, '/');
        final FileObject uriFile = getManager().resolveFile(uri);

        assertSame("file object", absFile, uriFile);
    }
}
