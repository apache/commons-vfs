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
package org.apache.commons.vfs2.provider.compressed;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;

/**
 * A compressed file.
 *
 * <p>
 * Such a file only has one child (the compressed filename with stripped last extension)
 * </p>
 *
 * @param <FS> A CompressedFileFileSystem
 */
public abstract class CompressedFileFileObject<FS extends CompressedFileFileSystem> extends AbstractFileObject<FS> {
    private final FileObject container;
    private final String[] children;

    protected CompressedFileFileObject(final AbstractFileName name, final FileObject container, final FS fs) {
        super(name, fs);
        this.container = container;

        // todo, add getBaseName(String) to FileName
        String basename = container.getName().getBaseName();
        final int pos = basename.lastIndexOf('.');
        if (pos > 0) {
            basename = basename.substring(0, pos);
        }
        children = new String[] { basename };
    }

    /**
     * Determines if this file can be written to.
     *
     * @return {@code true} if this file is writeable, {@code false} if not.
     * @throws FileSystemException if an error occurs.
     */
    @Override
    public boolean isWriteable() throws FileSystemException {
        return getFileSystem().hasCapability(Capability.WRITE_CONTENT);
    }

    /**
     * Returns the file's type.
     */
    @Override
    protected FileType doGetType() throws FileSystemException {
        if (getName().getPath().endsWith("/")) {
            return FileType.FOLDER;
        }
        return FileType.FILE;
    }

    /**
     * Lists the children of the file.
     */
    @Override
    protected String[] doListChildren() {
        return children;
    }

    /**
     * Returns the size of the file content (in bytes). Is only called if {@link #doGetType} returns
     * {@link FileType#FILE}.
     */
    @Override
    protected long doGetContentSize() {
        return -1;
    }

    /**
     * Returns the last modified time of this file.
     */
    @Override
    protected long doGetLastModifiedTime() throws Exception {
        return container.getContent().getLastModifiedTime();
    }

    protected FileObject getContainer() {
        return container;
    }

    @Override
    public void createFile() throws FileSystemException {
        container.createFile();
        injectType(FileType.FILE);
    }
}
