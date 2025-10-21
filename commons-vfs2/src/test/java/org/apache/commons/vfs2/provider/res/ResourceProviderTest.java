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
package org.apache.commons.vfs2.provider.res;

import static org.apache.commons.vfs2.VfsTestUtils.getResourceTestDirectory;

import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.jar.JarFileProvider;
import org.apache.commons.vfs2.provider.url.UrlFileProvider;

/**
 * JUnit 5 test cases for the resource provider.
 * <p>
 * This class replaces {@link ResourceProviderTestCase} with a pure JUnit 5 implementation.
 * </p>
 */
public class ResourceProviderTest extends ProviderTestSuiteJunit5 {

    public ResourceProviderTest() throws Exception {
        super(new ResourceProviderTestConfig(), "", false);
    }

    /**
     * Configuration for resource provider tests.
     */
    private static class ResourceProviderTestConfig extends AbstractProviderTestConfig {

        /**
         * Returns the base folder for tests.
         */
        @Override
        public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
            final String baseDir = getResourceTestDirectory();
            return manager.resolveFile("res:" + baseDir);
        }

        /**
         * Prepares the file system manager.
         */
        @Override
        public void prepare(final DefaultFileSystemManager manager) throws Exception {
            manager.addProvider("res", new ResourceFileProvider());
            manager.addProvider("file", new UrlFileProvider());
            manager.addProvider("jar", new JarFileProvider());
        }
    }
}

