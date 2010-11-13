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
package org.apache.commons.vfs2.provider.ftp.test;

import junit.framework.Test;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.ftp.FtpFileProvider;
import org.apache.commons.vfs2.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs2.test.AbstractProviderTestConfig;
import org.apache.commons.vfs2.test.ProviderTestConfig;
import org.apache.commons.vfs2.test.ProviderTestSuite;

/**
 * Tests for FTP file systems.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 */
public class FtpProviderTestCase
    extends AbstractProviderTestConfig
    implements ProviderTestConfig
{
    private static final String TEST_URI = "test.ftp.uri";
    /**
     * Creates the test suite for the ftp file system.
     */
    public static Test suite() throws Exception
    {
        if (System.getProperty(TEST_URI) != null)
        {
            return new ProviderTestSuite(new FtpProviderTestCase());
        }
        else
        {
            return notConfigured(FtpProviderTestCase.class);
        }
    }

    /**
     * Prepares the file system manager.
     */
    @Override
    public void prepare(final DefaultFileSystemManager manager) throws Exception
    {
        manager.addProvider("ftp", new FtpFileProvider());
    }

    /**
     * Returns the base folder for tests.
     */
    @Override
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception
    {
        final String uri = System.getProperty(TEST_URI);
        FileSystemOptions opts = new FileSystemOptions();
        FtpFileSystemConfigBuilder.getInstance().setPassiveMode(opts, true);
        return manager.resolveFile(uri, opts);
    }
}
