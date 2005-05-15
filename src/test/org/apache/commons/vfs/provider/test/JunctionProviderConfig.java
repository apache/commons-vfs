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
package org.apache.commons.vfs.provider.test;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FilesCache;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.test.ProviderTestConfig;

/**
 * A provider config that wraps another provider, to run the tests via
 * junctions.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class JunctionProviderConfig
    implements ProviderTestConfig
{
    private final ProviderTestConfig config;

    public JunctionProviderConfig(final ProviderTestConfig config)
    {
        this.config = config;
    }

    public FilesCache getFilesCache()
    {
        return config.getFilesCache();
    }

    /**
     * Prepares the file system manager.
     */
    public void prepare(final DefaultFileSystemManager manager) throws Exception
    {
        config.prepare(manager);
    }

    /**
     * Returns the base folder for tests.
     */
    public FileObject getBaseTestFolder(final FileSystemManager manager) throws Exception
    {
        final FileObject baseFolder = config.getBaseTestFolder(manager);

        // Create an empty file system, then link in the base folder
        final FileSystem newFs = manager.createVirtualFileSystem("vfs:").getFileSystem();
        final String junctionPoint = "/some/dir";
        newFs.addJunction(junctionPoint, baseFolder);

        return newFs.resolveFile(junctionPoint);
    }
}
