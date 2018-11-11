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
package org.apache.commons.vfs2.provider;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileContentInfo;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileNotFolderException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.apache.commons.vfs2.util.WeakRefFileListener;

/**
 * A file backed by another file.
 * <p>
 * TODO - Extract subclass that overlays the children.
 *
 * @param <AFS> A subclass of AbstractFileSystem.
 */
public class DelegateFileObject<AFS extends AbstractFileSystem> extends AbstractFileObject<AFS>
        implements FileListener {
    private FileObject file;
    private final Set<String> children = new HashSet<>();
    private boolean ignoreEvent;

    public DelegateFileObject(final AbstractFileName name, final AFS fileSystem, final FileObject file)
            throws FileSystemException {
        super(name, fileSystem);
        this.file = file;
        if (file != null) {
            WeakRefFileListener.installListener(file, this);
        }
    }

    /**
     * Get access to the delegated file.
     *
     * @return The FileObject.
     * @since 2.0
     */
    public FileObject getDelegateFile() {
        return file;
    }

    /**
     * Adds a child to this file.
     *
     * @param baseName The base FileName.
     * @param type The FileType.
     * @throws Exception if an error occurs.
     */
    public void attachChild(final FileName baseName, final FileType type) throws Exception {
        final FileType oldType = doGetType();
        if (children.add(baseName.getBaseName())) {
            childrenChanged(baseName, type);
        }
        maybeTypeChanged(oldType);
    }

    /**
     * Attaches or detaches the target file.
     *
     * @param file The FileObject.
     * @throws Exception if an error occurs.
     */
    public void setFile(final FileObject file) throws Exception {
        final FileType oldType = doGetType();

        if (file != null) {
            WeakRefFileListener.installListener(file, this);
        }
        this.file = file;
        maybeTypeChanged(oldType);
    }

    /**
     * Checks whether the file's type has changed, and fires the appropriate events.
     *
     * @param oldType The old FileType.
     * @throws Exception if an error occurs.
     */
    private void maybeTypeChanged(final FileType oldType) throws Exception {
        final FileType newType = doGetType();
        if (oldType == FileType.IMAGINARY && newType != FileType.IMAGINARY) {
            handleCreate(newType);
        } else if (oldType != FileType.IMAGINARY && newType == FileType.IMAGINARY) {
            handleDelete();
        }
    }

    /**
     * Determines the type of the file, returns null if the file does not exist.
     */
    @Override
    protected FileType doGetType() throws FileSystemException {
        if (file != null) {
            return file.getType();
        } else if (children.size() > 0) {
            return FileType.FOLDER;
        } else {
            return FileType.IMAGINARY;
        }
    }

    /**
     * Determines if this file can be read.
     */
    @Override
    protected boolean doIsReadable() throws FileSystemException {
        if (file != null) {
            return file.isReadable();
        }
        return true;
    }

    /**
     * Determines if this file can be written to.
     */
    @Override
    protected boolean doIsWriteable() throws FileSystemException {
        if (file != null) {
            return file.isWriteable();
        }
        return false;
    }

    /**
     * Determines if this file is executable.
     */
    @Override
    protected boolean doIsExecutable() throws FileSystemException {
        if (file != null) {
            return file.isExecutable();
        }
        return false;
    }

    /**
     * Determines if this file is hidden.
     */
    @Override
    protected boolean doIsHidden() throws FileSystemException {
        if (file != null) {
            return file.isHidden();
        }
        return false;
    }

    /**
     * Lists the children of the file.
     */
    @Override
    protected String[] doListChildren() throws Exception {
        if (file != null) {
            final FileObject[] children;

            try {
                children = file.getChildren();
            }
            // VFS-210
            catch (final FileNotFolderException e) {
                throw new FileNotFolderException(getName(), e);
            }

            final String[] childNames = new String[children.length];
            for (int i = 0; i < children.length; i++) {
                childNames[i] = children[i].getName().getBaseName();
            }
            return childNames;
        }
        return children.toArray(new String[children.size()]);
    }

    /**
     * Creates this file as a folder.
     */
    @Override
    protected void doCreateFolder() throws Exception {
        ignoreEvent = true;
        try {
            file.createFolder();
        } finally {
            ignoreEvent = false;
        }
    }

    /**
     * Deletes the file.
     */
    @Override
    protected void doDelete() throws Exception {
        ignoreEvent = true;
        try {
            file.delete();
        } finally {
            ignoreEvent = false;
        }
    }

    /**
     * Returns the size of the file content (in bytes). Is only called if {@link #doGetType} returns
     * {@link FileType#FILE}.
     */
    @Override
    protected long doGetContentSize() throws Exception {
        return file.getContent().getSize();
    }

    /**
     * Returns the attributes of this file.
     */
    @Override
    protected Map<String, Object> doGetAttributes() throws Exception {
        return file.getContent().getAttributes();
    }

    /**
     * Sets an attribute of this file.
     */
    @Override
    protected void doSetAttribute(final String atttrName, final Object value) throws Exception {
        file.getContent().setAttribute(atttrName, value);
    }

    /**
     * Returns the certificates of this file.
     */
    @Override
    protected Certificate[] doGetCertificates() throws Exception {
        return file.getContent().getCertificates();
    }

    /**
     * Returns the last-modified time of this file.
     */
    @Override
    protected long doGetLastModifiedTime() throws Exception {
        return file.getContent().getLastModifiedTime();
    }

    /**
     * Sets the last-modified time of this file.
     *
     * @since 2.0
     */
    @Override
    protected boolean doSetLastModifiedTime(final long modtime) throws Exception {
        file.getContent().setLastModifiedTime(modtime);
        return true;
    }

    /**
     * Creates an input stream to read the file content from.
     */
    @Override
    protected InputStream doGetInputStream() throws Exception {
        return file.getContent().getInputStream();
    }

    /**
     * Creates an output stream to write the file content to.
     */
    @Override
    protected OutputStream doGetOutputStream(final boolean bAppend) throws Exception {
        return file.getContent().getOutputStream(bAppend);
    }

    /**
     * Called when a file is created.
     *
     * @param event The FileChangeEvent.
     * @throws Exception if an error occurs.
     */
    @Override
    public void fileCreated(final FileChangeEvent event) throws Exception {
        if (event.getFile() != file) {
            return;
        }
        if (!ignoreEvent) {
            handleCreate(file.getType());
        }
    }

    /**
     * Called when a file is deleted.
     *
     * @param event The FileChangeEvent.
     * @throws Exception if an error occurs.
     */
    @Override
    public void fileDeleted(final FileChangeEvent event) throws Exception {
        if (event.getFile() != file) {
            return;
        }
        if (!ignoreEvent) {
            handleDelete();
        }
    }

    /**
     * Called when a file is changed.
     * <p>
     * This will only happen if you monitor the file using {@link org.apache.commons.vfs2.FileMonitor}.
     *
     * @param event The FileChangeEvent.
     * @throws Exception if an error occurs.
     */
    @Override
    public void fileChanged(final FileChangeEvent event) throws Exception {
        if (event.getFile() != file) {
            return;
        }
        if (!ignoreEvent) {
            handleChanged();
        }
    }

    /**
     * Close the delegated file.
     *
     * @throws FileSystemException if an error occurs.
     */
    @Override
    public void close() throws FileSystemException {
        super.close();

        if (file != null) {
            file.close();
        }
    }

    /**
     * Refresh file information.
     *
     * @throws FileSystemException if an error occurs.
     * @since 2.0
     */
    @Override
    public void refresh() throws FileSystemException {
        super.refresh();
        if (file != null) {
            file.refresh();
        }
    }

    /**
     * Return file content info.
     *
     * @return the file content info of the delegee.
     * @throws Exception Any thrown Exception is wrapped in FileSystemException.
     * @since 2.0
     */
    protected FileContentInfo doGetContentInfo() throws Exception {
        return file.getContent().getContentInfo();
    }

    /**
     * Renames the file.
     *
     * @param newFile the new location/name.
     * @throws Exception Any thrown Exception is wrapped in FileSystemException.
     * @since 2.0
     */
    @Override
    protected void doRename(final FileObject newFile) throws Exception {
        file.moveTo(((DelegateFileObject) newFile).file);
    }

    /**
     * Removes an attribute of this file.
     *
     * @since 2.0
     */
    @Override
    protected void doRemoveAttribute(final String atttrName) throws Exception {
        file.getContent().removeAttribute(atttrName);
    }

    /**
     * Creates access to the file for random i/o.
     *
     * @since 2.0
     */
    @Override
    protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception {
        return file.getContent().getRandomAccessContent(mode);
    }
}
