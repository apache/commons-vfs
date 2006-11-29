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
package org.apache.commons.vfs.test;

import org.apache.commons.vfs.FilesCache;
import org.apache.commons.vfs.cache.SoftRefFilesCache;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;

/**
 * A partial {@link org.apache.commons.vfs.test.ProviderTestConfig} implementation.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public abstract class AbstractProviderTestConfig
    implements ProviderTestConfig
{
    private FilesCache cache = null;

    /**
     * Prepares the file system manager.  This implementation does nothing.
     */
    public void prepare(final DefaultFileSystemManager manager)
        throws Exception
    {
    }

    public FilesCache getFilesCache()
    {
        if (cache == null)
        {
            // cache = new DefaultFilesCache();
            cache = new SoftRefFilesCache();
        }

        return cache;
    }
}
