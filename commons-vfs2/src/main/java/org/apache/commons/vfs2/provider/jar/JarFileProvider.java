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
package org.apache.commons.vfs2.provider.jar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.LayeredFileName;
import org.apache.commons.vfs2.provider.zip.ZipFileProvider;

/**
 * A file system provider for Jar files. Provides read-only file systems. This provides access to Jar specific features
 * like Signing and Manifest Attributes.
 */
public class JarFileProvider extends ZipFileProvider {
    static final Collection<Capability> capabilities;

    static {
        final Collection<Capability> combined = new ArrayList<>();
        combined.addAll(ZipFileProvider.capabilities);
        combined.addAll(Arrays.asList(new Capability[] { Capability.ATTRIBUTES, Capability.FS_ATTRIBUTES,
                Capability.SIGNING, Capability.MANIFEST_ATTRIBUTES, Capability.VIRTUAL }));
        capabilities = Collections.unmodifiableCollection(combined);
    }

    public JarFileProvider() {
        super();
    }

    /**
     * Creates a layered file system. This method is called if the file system is not cached.
     *
     * @param scheme The URI scheme.
     * @param file The file to create the file system on top of.
     * @return The file system.
     */
    @Override
    protected FileSystem doCreateFileSystem(final String scheme, final FileObject file,
            final FileSystemOptions fileSystemOptions) throws FileSystemException {
        final AbstractFileName name = new LayeredFileName(scheme, file.getName(), FileName.ROOT_PATH, FileType.FOLDER);
        return new JarFileSystem(name, file, fileSystemOptions);
    }

    @Override
    public Collection<Capability> getCapabilities() {
        return capabilities;
    }
}
