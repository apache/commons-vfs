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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.compressed.CompressedFileFileProvider;

/**
 * Provides access to the content of gzip compressed files.
 */
public class GzipFileProvider extends CompressedFileFileProvider {
    /**
     * Capabilities.
     */
    protected static final Collection<Capability> capabilities = Collections.unmodifiableCollection(Arrays
            .asList(new Capability[] { Capability.GET_LAST_MODIFIED, Capability.GET_TYPE, Capability.LIST_CHILDREN,
                    Capability.WRITE_CONTENT, Capability.READ_CONTENT, Capability.URI, Capability.COMPRESS }));

    public GzipFileProvider() {
        super();
    }

    @Override
    protected FileSystem createFileSystem(final FileName name, final FileObject file,
            final FileSystemOptions fileSystemOptions) throws FileSystemException {
        return new GzipFileSystem(name, file, fileSystemOptions);
    }

    @Override
    public Collection<Capability> getCapabilities() {
        return capabilities;
    }
}
