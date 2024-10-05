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
package org.apache.commons.vfs2.provider.tar;

import java.io.InputStream;
import java.util.HashSet;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.io.function.Uncheck;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;

/**
 * A file in a Tar file system.
 */
public class TarFileObject extends AbstractFileObject<TarFileSystem> {

    /** The TarArchiveEntry */
    private TarArchiveEntry entry;
    private final HashSet<String> children = new HashSet<>();
    private FileType type;

    /**
     * Constructs a new instance.
     *
     * @param fileName the file name.
     * @param entry the archive entry.
     * @param fileSystem the file system.
     * @param tarExists whether the tar file exists.
     */
    protected TarFileObject(final AbstractFileName fileName, final TarArchiveEntry entry, final TarFileSystem fileSystem,
            final boolean tarExists) {
        super(fileName, fileSystem);
        setTarEntry(entry);
        if (!tarExists) {
            type = FileType.IMAGINARY;
        }
    }

    /**
     * Attaches a child.
     *
     * @param childName Name of child to remember.
     */
    protected void attachChild(final FileName childName) {
        children.add(childName.getBaseName());
    }

    /**
     * Returns the size of the file content (in bytes). Is only called if {@link #doGetType} returns
     * {@link FileType#FILE}.
     */
    @Override
    protected long doGetContentSize() {
        if (entry == null) {
            return 0;
        }

        return entry.getSize();
    }

    /**
     * Creates an input stream to read the file content from. Is only called if {@link #doGetType} returns
     * {@link FileType#FILE}. The input stream returned by this method is guaranteed to be closed before this method is
     * called again.
     */
    @Override
    protected InputStream doGetInputStream(final int bufferSize) throws Exception {
        // VFS-210: zip allows to gather an input stream even from a directory and will
        // return -1 on the first read. getType should not be expensive and keeps the tests
        // running
        if (!getType().hasContent()) {
            throw new FileSystemException("vfs.provider/read-not-file.error", getName());
        }

        return getAbstractFileSystem().getInputStream(entry);
    }

    /**
     * Returns the last modified time of this file.
     */
    @Override
    protected long doGetLastModifiedTime() throws Exception {
        if (entry == null) {
            return 0;
        }

        return entry.getModTime().getTime();
    }

    /**
     * Returns the file's type.
     */
    @Override
    protected FileType doGetType() {
        return type;
    }

    /**
     * Lists the children of the file.
     */
    @Override
    protected String[] doListChildren() {
        if (!Uncheck.get(this::getType).hasChildren()) {
            return null;
        }
        return children.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    /**
     * Determines if this file can be written to.
     *
     * @return {@code true} if this file is writable, {@code false} if not.
     * @throws FileSystemException if an error occurs.
     */
    @Override
    public boolean isWriteable() throws FileSystemException {
        return false;
    }

    /**
     * Sets the details for this file object.
     *
     * Consider this method package private. TODO Might be made package private in the next major version.
     *
     * @param entry Tar archive entry.
     */
    protected void setTarEntry(final TarArchiveEntry entry) {
        if (this.entry != null) {
            return;
        }

        if (entry == null || entry.isDirectory()) {
            type = FileType.FOLDER;
        } else {
            type = FileType.FILE;
        }

        this.entry = entry;
    }
}
