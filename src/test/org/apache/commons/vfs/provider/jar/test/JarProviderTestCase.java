/*
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
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
 * Tests for the Jar file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 */
public class JarProviderTestCase
    extends AbstractProviderTestConfig
    implements ProviderTestConfig
{
    /**
     * Creates the test suite for the jar file system.
     */
    public static Test suite() throws Exception
    {
        return new ProviderTestSuite(new JarProviderTestCase());
    }

    /**
     * Prepares the file system manager.
     */
    public void prepare(final DefaultFileSystemManager manager)
        throws Exception
    {
        manager.addProvider("jar", new JarFileProvider());
    }

    /**
     * Returns the base folder for tests.
     */
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception
    {
        final File jarFile = AbstractVfsTestCase.getTestResource("test.jar");
        final String uri = "jar:" + jarFile.getAbsolutePath() + "!/";
        return manager.resolveFile(uri);
    }
}
