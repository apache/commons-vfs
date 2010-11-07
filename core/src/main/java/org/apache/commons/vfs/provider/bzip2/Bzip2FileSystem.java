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
package org.apache.commons.vfs.provider.bzip2;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.compressed.CompressedFileFileSystem;

import java.util.Collection;

/**
 * Filesytem to handle compressed files using the bzip2 method.
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date$
 */
public class Bzip2FileSystem extends CompressedFileFileSystem
{
    protected Bzip2FileSystem(FileName rootName, FileObject parentLayer, FileSystemOptions fileSystemOptions)
            throws FileSystemException
    {
        super(rootName, parentLayer, fileSystemOptions);
    }

    @Override
    protected FileObject createFile(FileName name) throws FileSystemException
    {
        return new Bzip2FileObject(name, getParentLayer(), this);
    }

    @Override
    protected void addCapabilities(final Collection<Capability> caps)
    {
        caps.addAll(Bzip2FileProvider.capabilities);
    }
}
