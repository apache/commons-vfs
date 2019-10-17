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

package org.apache.commons.vfs2.provider.res.test;

import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.res.ResourceFileProvider;
import org.apache.commons.vfs2.provider.zip.ZipFileProvider;
import org.apache.commons.vfs2.test.AbstractProviderTestCase;
import org.apache.commons.vfs2.test.AbstractProviderTestConfig;
import org.apache.commons.vfs2.test.ProviderTestSuite;
import org.junit.Assert;

import junit.framework.Test;

/**
 * Test cases for VFS-444.
 */
public class Vfs444TestCase extends AbstractProviderTestConfig {
    public static Test suite() throws Exception {
        final ProviderTestSuite suite = new ProviderTestSuite(new Vfs444TestCase(), true);
        suite.addTests(Vfs444Tests.class);
        return suite;
    }

    /**
     * Prepares the file system manager. This implementation does nothing.
     */
    @Override
    public void prepare(final DefaultFileSystemManager manager) throws Exception {
        manager.addProvider("res", new ResourceFileProvider());
        manager.addProvider("zip", new ZipFileProvider());
    }

    /**
     * Returns the base folder for tests.
     */
    @Override
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
        final String baseDir = AbstractVfsTestCase.getResourceTestDirectory();
        return manager.resolveFile("zip:res:" + baseDir + "/test.zip");
    }

    public static class Vfs444Tests extends AbstractProviderTestCase {

        public void testResolveFullPathURI0() throws FileSystemException {
            final FileName result = getManager().resolveURI("res:test-data/test.zip");
            Assert.assertTrue(result.isFile());
        }

        public void testResolveFullPathFile0() throws FileSystemException {
            final FileObject result = getManager().resolveFile("res:test-data/test.zip");
            Assert.assertTrue(result.exists());
        }

        public void testResolveFullPathURI1() throws FileSystemException {
            final FileName result = getManager().resolveURI("res:/test-data/test.zip");
            Assert.assertTrue(result.isFile());
        }

        public void testResolveFullPathFile1() throws FileSystemException {
            final FileObject result = getManager().resolveFile("res:/test-data/test.zip");
            Assert.assertTrue(result.exists());
        }

        public void testResolveFullPathURI2() throws FileSystemException {
            final FileName result = getManager().resolveURI("res://test-data/test.zip");
            Assert.assertTrue(result.isFile());
        }

        public void testResolveFullPathFile2() throws FileSystemException {
        	final FileObject result = getManager().resolveFile("res://test-data/test.zip");
            Assert.assertTrue(result.exists());
        }

        public void testResolvePartialPath1() throws FileSystemException {
            final FileName result = getManager().resolveURI("res:test-data");
            Assert.assertTrue(result.isFile());
        }

        public void testResolvePartialPath2() throws FileSystemException {
            final FileName root = getManager().resolveURI("res:test-data");
            final FileName file = getManager().resolveName(root, "test.zip");
            final String uri = file.getURI();
            final FileObject result = getManager().resolveFile(uri);
            Assert.assertNotNull(result);
            Assert.assertTrue(result.exists());
        }
    }
}
