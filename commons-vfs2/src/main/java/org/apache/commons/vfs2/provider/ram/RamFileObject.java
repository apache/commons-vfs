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
package org.apache.commons.vfs2.provider.ram;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.util.FileObjectUtils;
import org.apache.commons.vfs2.util.RandomAccessMode;

/**
 * A RAM File contains a single RAM FileData instance, it provides methods to access the data by implementing FileObject
 * interface.
 */
public class RamFileObject extends AbstractFileObject<RamFileSystem> {

    /**
     * RAM File Object Data.
     */
    private RamFileData data;

    /**
     * @param name The name of the file.
     * @param fs The FileSystem.
     */
    protected RamFileObject(final AbstractFileName name, final RamFileSystem fs) {
        super(name, fs);
        getAbstractFileSystem().attach(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doAttach()
     */
    @Override
    protected void doAttach() throws Exception {
        getAbstractFileSystem().attach(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doCreateFolder()
     */
    @Override
    protected void doCreateFolder() throws Exception {
        injectType(FileType.FOLDER);
        save();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doDelete()
     */
    @Override
    protected void doDelete() throws Exception {

        if (isContentOpen()) {
            throw new FileSystemException(getName() + " cannot be deleted while the file is open");
        }
        getAbstractFileSystem().delete(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doGetContentSize()
     */
    @Override
    protected long doGetContentSize() throws Exception {
        return size();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doGetInputStream()
     */
    @Override
    protected InputStream doGetInputStream(final int bufferSize) throws Exception {
        // VFS-210: ram allows to gather an input stream even from a directory. So we need to check the type anyway.
        if (!getType().hasContent()) {
            throw new FileSystemException("vfs.provider/read-not-file.error", getName());
        }

        return new ByteArrayInputStream(data.getContent());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doGetLastModifiedTime()
     */
    @Override
    protected long doGetLastModifiedTime() throws Exception {
        return data.getLastModified();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doGetOutputStream(boolean)
     */
    @Override
    protected OutputStream doGetOutputStream(final boolean bAppend) throws Exception {
        if (!bAppend) {
            data.setContent(ArrayUtils.EMPTY_BYTE_ARRAY);
        }
        return new RamFileOutputStream(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doGetRandomAccessContent(
     * org.apache.commons.vfs2.util.RandomAccessMode)
     */
    @Override
    protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception {
        return new RamFileRandomAccessContent(this, mode);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doGetType()
     */
    @Override
    protected FileType doGetType() throws Exception {
        return data.getType();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doListChildren()
     */
    @Override
    protected String[] doListChildren() throws Exception {
        return getAbstractFileSystem().listChildren(getName());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doRename(org.apache.commons.vfs2.FileObject)
     */
    @Override
    protected void doRename(final FileObject newFile) throws Exception {
        final RamFileObject newRamFileObject = (RamFileObject) FileObjectUtils.getAbstractFileObject(newFile);
        getAbstractFileSystem().rename(this, newRamFileObject);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doSetLastModifiedTime(long)
     */
    /** @since 2.0 */
    @Override
    protected boolean doSetLastModifiedTime(final long modtime) throws Exception {
        data.setLastModified(modtime);
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#endOutput()
     */
    @Override
    protected void endOutput() throws Exception {
        super.endOutput();
        save();
    }

    /**
     * @return Returns the data.
     */
    RamFileData getData() {
        return data;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#injectType(org.apache.commons.vfs2.FileType)
     */
    @Override
    protected void injectType(final FileType fileType) {
        data.setType(fileType);
        super.injectType(fileType);
    }

    /**
     * @param newSize The new buffer size.
     * @throws IOException if the new size exceeds the limit
     */
    synchronized void resize(final long newSize) throws IOException {
        final RamFileSystem afs = getAbstractFileSystem();
        final FileSystemOptions afsOptions = afs.getFileSystemOptions();
        if (afsOptions != null) {
            final long maxSize = RamFileSystemConfigBuilder.getInstance().getLongMaxSize(afsOptions);
            if (afs.size() + newSize - size() > maxSize) {
                throw new IOException("FileSystem capacity (" + maxSize + ") exceeded.");
            }
        }
        data.resize(newSize);
    }

    private void save() throws FileSystemException {
        getAbstractFileSystem().save(this);
    }

    /**
     * @param data The data to set.
     */
    void setData(final RamFileData data) {
        this.data = data;
    }

    /**
     * @return Returns the size of the {@link RamFileData}.
     */
    int size() {
        return data == null ? 0 : data.size();
    }

}
