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
package org.apache.commons.vfs2.provider.jar;

import static org.apache.commons.vfs2.VfsTestUtils.getTestResource;

import java.io.File;

import org.apache.commons.vfs2.AbstractProviderTestConfig;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.ProviderTestSuiteJunit5;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;

/**
 * JUnit 5 tests for the Jar file system.
 * <p>
 * This class replaces {@link JarProviderTestCase} with a pure JUnit 5 implementation.
 * </p>
 */
public class JarProviderTest extends ProviderTestSuiteJunit5 {

    public JarProviderTest() throws Exception {
        super(new JarProviderTestConfig(), "", true);
    }

    /**
     * Returns a JAR file from the test resources.
     */
    public static FileObject getTestJar(final FileSystemManager manager, final String name) throws Exception {
        final File jarFile = getTestResource(name);
        return manager.resolveFile("jar:file:" + jarFile.getAbsolutePath() + "!/");
    }

    /**
     * Configuration for JAR provider tests.
     */
    private static class JarProviderTestConfig extends AbstractProviderTestConfig {

        /**
         * Returns the base folder for tests.
         */
        @Override
        public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception {
            return JarProviderTest.getTestJar(manager, "test.jar");
        }

        /**
         * Prepares the file system manager.
         */
        @Override
        public void prepare(final DefaultFileSystemManager manager) throws Exception {
            manager.addProvider("jar", new JarFileProvider());
        }
    }
}

