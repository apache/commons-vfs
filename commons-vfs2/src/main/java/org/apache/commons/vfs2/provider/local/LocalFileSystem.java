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
package org.apache.commons.vfs2.provider.local;

import java.io.File;
import java.io.FilePermission;
import java.util.Collection;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.util.FileObjectUtils;

/**
 * A local file system.
 */
public class LocalFileSystem extends AbstractFileSystem {
    private final String rootFile;

    public LocalFileSystem(final FileName rootName, final String rootFile, final FileSystemOptions opts) {
        super(rootName, null, opts);
        this.rootFile = rootFile;
    }

    /**
     * Creates a file object.
     */
    @Override
    protected FileObject createFile(final AbstractFileName name) throws FileSystemException {
        // Create the file
        return new LocalFile(this, rootFile, name);
    }

    /**
     * Returns the capabilities of this file system.
     */
    @Override
    protected void addCapabilities(final Collection<Capability> caps) {
        caps.addAll(DefaultLocalFileProvider.capabilities);
    }

    /**
     * Creates a temporary local copy of a file and its descendants.
     */
    @Override
    protected File doReplicateFile(final FileObject fileObject, final FileSelector selector) throws Exception {
        final LocalFile localFile = (LocalFile) FileObjectUtils.getAbstractFileObject(fileObject);
        final File file = localFile.getLocalFile();
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            final FilePermission requiredPerm = new FilePermission(file.getAbsolutePath(), "read");
            sm.checkPermission(requiredPerm);
        }
        return file;
    }

}
