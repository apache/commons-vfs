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
package org.apache.commons.vfs2.provider.gzip;

import java.util.Collection;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.compressed.CompressedFileFileSystem;

/**
 * A compressed file system using the Gzip method.
 */
public class GzipFileSystem extends CompressedFileFileSystem {

    /**
     * Constructs a new instance.
     *
     * @param rootName The root file name of this file system.
     * @param parentLayer The parent layer of this file system.
     * @param fileSystemOptions Options to build this file system.
     */
    protected GzipFileSystem(final FileName rootName, final FileObject parentLayer, final FileSystemOptions fileSystemOptions) {
        super(rootName, parentLayer, fileSystemOptions);
    }

    @Override
    protected void addCapabilities(final Collection<Capability> caps) {
        caps.addAll(GzipFileProvider.capabilities);
    }

    @Override
    protected FileObject createFile(final AbstractFileName name) throws FileSystemException {
        return new GzipFileObject(name, getParentLayer(), this);
    }
}
