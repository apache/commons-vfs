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
package org.apache.commons.vfs2.provider.hdfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.vfs2.FileNotFolderException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * A VFS representation of an HDFS file.
 *
 * @since 2.1
 */
public class HdfsFileObject extends AbstractFileObject<HdfsFileSystem> {

    private final HdfsFileSystem fs;
    private final FileSystem hdfs;
    private final Path path;
    private FileStatus stat;

    /**
     * Constructs a new HDFS FileObject
     *
     * @param name FileName
     * @param fs HdfsFileSystem instance
     * @param hdfs Hadoop FileSystem instance
     * @param p Path to the file in HDFS
     */
    protected HdfsFileObject(final AbstractFileName name, final HdfsFileSystem fs, final FileSystem hdfs,
            final Path p) {
        super(name, fs);
        this.fs = fs;
        this.hdfs = hdfs;
        this.path = p;
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#canRenameTo(org.apache.commons.vfs2.FileObject)
     */
    @Override
    public boolean canRenameTo(final FileObject newfile) {
        if (!super.canRenameTo(newfile)) {
            return false;
        }
        try {
            return this.hdfs.getFileStatus(new Path(newfile.getName().getPath())) == null;
        } catch (final FileNotFoundException e) {
            return false;
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doAttach()
     */
    @Override
    protected void doAttach() throws Exception {
        try {
            this.stat = this.hdfs.getFileStatus(this.path);
        } catch (final FileNotFoundException e) {
            this.stat = null;
            return;
        }
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doCreateFolder()
     * @since 2.7.0
     */
    @Override
    protected void doCreateFolder() throws Exception {
        hdfs.mkdirs(this.path);
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doDelete()
     * @since 2.7.0
     */
    @Override
    protected void doDelete() throws Exception {
        hdfs.delete(this.path, true);
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doGetAttributes()
     */
    @Override
    protected Map<String, Object> doGetAttributes() throws Exception {
        if (null == this.stat) {
            return super.doGetAttributes();
        }
        final Map<String, Object> attrs = new HashMap<>();
        attrs.put(HdfsFileAttributes.LAST_ACCESS_TIME.toString(), this.stat.getAccessTime());
        attrs.put(HdfsFileAttributes.BLOCK_SIZE.toString(), this.stat.getBlockSize());
        attrs.put(HdfsFileAttributes.GROUP.toString(), this.stat.getGroup());
        attrs.put(HdfsFileAttributes.OWNER.toString(), this.stat.getOwner());
        attrs.put(HdfsFileAttributes.PERMISSIONS.toString(), this.stat.getPermission().toString());
        attrs.put(HdfsFileAttributes.LENGTH.toString(), this.stat.getLen());
        attrs.put(HdfsFileAttributes.MODIFICATION_TIME.toString(), this.stat.getModificationTime());
        return attrs;
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doGetContentSize()
     */
    @Override
    protected long doGetContentSize() throws Exception {
        return stat.getLen();
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doGetInputStream(int)
     */
    @Override
    protected InputStream doGetInputStream(final int bufferSize) throws Exception {
        return this.hdfs.open(this.path, bufferSize);
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doGetLastModifiedTime()
     */
    @Override
    protected long doGetLastModifiedTime() throws Exception {
        doAttach();
        if (null != this.stat) {
            return this.stat.getModificationTime();
        }
        return -1;
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doGetOutputStream(boolean)
     * @since 2.7.0
     */
    @Override
    protected OutputStream doGetOutputStream(final boolean append) throws Exception {
        if (append) {
            throw new FileSystemException("vfs.provider/write-append-not-supported.error", this.path.getName());
        }
        return hdfs.create(this.path);
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doGetRandomAccessContent
     *      (org.apache.commons.vfs2.util.RandomAccessMode)
     */
    @Override
    protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception {
        if (mode.equals(RandomAccessMode.READWRITE)) {
            throw new UnsupportedOperationException();
        }
        return new HdfsRandomAccessContent(this.path, this.hdfs);
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doGetType()
     */
    @Override
    protected FileType doGetType() throws Exception {
        try {
            doAttach();
            if (null == stat) {
                return FileType.IMAGINARY;
            }
            if (stat.isDirectory()) {
                return FileType.FOLDER;
            }
            return FileType.FILE;
        } catch (final FileNotFoundException fnfe) {
            return FileType.IMAGINARY;
        }
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doIsHidden()
     */
    @Override
    protected boolean doIsHidden() throws Exception {
        return false;
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doIsReadable()
     */
    @Override
    protected boolean doIsReadable() throws Exception {
        return true;
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doIsWriteable()
     */
    @Override
    protected boolean doIsWriteable() throws Exception {
        return true;
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doListChildren()
     */
    @Override
    protected String[] doListChildren() throws Exception {
        if (this.doGetType() != FileType.FOLDER) {
            throw new FileNotFolderException(this);
        }

        final FileStatus[] fileStatuses = this.hdfs.listStatus(this.path);
        return Stream.of(fileStatuses).map(status -> status.getPath().getName()).toArray(String[]::new);
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doListChildrenResolved()
     */
    @Override
    protected FileObject[] doListChildrenResolved() throws Exception {
        if (this.doGetType() != FileType.FOLDER) {
            return null;
        }
        final String[] children = doListChildren();
        final FileObject[] fo = new FileObject[children.length];
        for (int i = 0; i < children.length; i++) {
            final Path p = new Path(this.path, children[i]);
            fo[i] = this.fs.resolveFile(p.toUri().toString());
        }
        return fo;
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doRemoveAttribute(java.lang.String)
     */
    @Override
    protected void doRemoveAttribute(final String attrName) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doRename(FileObject)
     * @since 2.7.0
     */
    @Override
    protected void doRename(final FileObject newfile) throws Exception {
        hdfs.rename(this.path, new Path(newfile.getName().getPath()));
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doSetAttribute(java.lang.String, java.lang.Object)
     */
    @Override
    protected void doSetAttribute(final String attrName, final Object value) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doSetLastModifiedTime(long)
     */
    @Override
    protected boolean doSetLastModifiedTime(final long modtime) throws Exception {
        try {
            hdfs.setTimes(this.path, modtime, System.currentTimeMillis());
        } catch (final IOException ioe) {
            throw new FileSystemException(ioe);
        }
        return true;
    }

    /**
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#exists()
     * @return boolean true if file exists, false if not
     */
    @Override
    public boolean exists() throws FileSystemException {
        try {
            doAttach();
            return this.stat != null;
        } catch (final FileNotFoundException fne) {
            return false;
        } catch (final Exception e) {
            throw new FileSystemException("Unable to check existance ", e);
        }
    }

}
