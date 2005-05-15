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
package org.apache.commons.vfs.provider.jar.test;

import junit.framework.Test;
import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.jar.JarFileProvider;
import org.apache.commons.vfs.test.AbstractProviderTestConfig;
import org.apache.commons.vfs.test.ProviderTestConfig;
import org.apache.commons.vfs.test.ProviderTestSuite;

import java.io.File;

/**
 * Tests for the Zip file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 */
public class NestedJarTestCase
    extends AbstractProviderTestConfig
    implements ProviderTestConfig
{
    /**
     * Creates the test suite for nested jar files.
     */
    public static Test suite() throws Exception
    {
        return new ProviderTestSuite(new NestedJarTestCase());
    }

    /**
     * Prepares the file system manager.
     */
    public void prepare(final DefaultFileSystemManager manager)
        throws Exception
    {
        manager.addProvider("jar", new JarFileProvider());
        manager.addExtensionMap("jar", "jar");
    }

    /**
     * Returns the base folder for tests.
     */
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception
    {
        // Locate the Jar file
        final File outerFile = AbstractVfsTestCase.getTestResource("nested.jar");
        final String uri = "jar:" + outerFile.getAbsolutePath() + "!/test.jar";
        final FileObject jarFile = manager.resolveFile(uri);

        // Now build the nested file system
        final FileObject nestedFS = manager.createFileSystem(jarFile);
        return nestedFS.resolveFile("/");
    }
}
