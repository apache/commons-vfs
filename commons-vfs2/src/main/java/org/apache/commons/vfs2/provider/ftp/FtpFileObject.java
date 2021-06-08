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
package org.apache.commons.vfs2.provider.ftp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileNotFolderException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.util.FileObjectUtils;
import org.apache.commons.vfs2.util.Messages;
import org.apache.commons.vfs2.util.MonitorInputStream;
import org.apache.commons.vfs2.util.MonitorOutputStream;
import org.apache.commons.vfs2.util.RandomAccessMode;

/**
 * An FTP file.
 */
public class FtpFileObject extends AbstractFileObject<FtpFileSystem> {

    /**
     * An InputStream that monitors for end-of-file.
     */
    class FtpInputStream extends MonitorInputStream {
        private final FtpClient client;

        public FtpInputStream(final FtpClient client, final InputStream in) {
            super(in);
            this.client = client;
        }

        public FtpInputStream(final FtpClient client, final InputStream in, final int bufferSize) {
            super(in, bufferSize);
            this.client = client;
        }

        void abort() throws IOException {
            client.abort();
            close();
        }

        private boolean isTransferAbortedOkReplyCode() throws IOException {
            final List<Integer> transferAbortedOkReplyCodes = FtpFileSystemConfigBuilder
                .getInstance()
                .getTransferAbortedOkReplyCodes(getAbstractFileSystem().getFileSystemOptions());
            return transferAbortedOkReplyCodes != null && transferAbortedOkReplyCodes.contains(client.getReplyCode());
        }

        /**
         * Called after the stream has been closed.
         */
        @Override
        protected void onClose() throws IOException {
            final boolean ok;
            try {
                ok = client.completePendingCommand() || isTransferAbortedOkReplyCode();
            } finally {
                getAbstractFileSystem().putClient(client);
            }

            if (!ok) {
                throw new FileSystemException("vfs.provider.ftp/finish-get.error", getName());
            }
        }
    }
    /**
     * An OutputStream that monitors for end-of-file.
     */
    private class FtpOutputStream extends MonitorOutputStream {
        private final FtpClient client;

        public FtpOutputStream(final FtpClient client, final OutputStream outstr) {
            super(outstr);
            this.client = client;
        }

        /**
         * Called after this stream is closed.
         */
        @Override
        protected void onClose() throws IOException {
            final boolean ok;
            try {
                ok = client.completePendingCommand();
            } finally {
                getAbstractFileSystem().putClient(client);
            }

            if (!ok) {
                throw new FileSystemException("vfs.provider.ftp/finish-put.error", getName());
            }
        }
    }
    private static final long DEFAULT_TIMESTAMP = 0L;
    private static final Map<String, FTPFile> EMPTY_FTP_FILE_MAP = Collections
            .unmodifiableMap(new TreeMap<>());

    private static final FTPFile UNKNOWN = new FTPFile();

    private static final Log log = LogFactory.getLog(FtpFileObject.class);
    private volatile boolean mdtmSet;
    private final String relPath;
    // Cached info
    private volatile FTPFile ftpFile;
    private volatile Map<String, FTPFile> childMap;

    private volatile FileObject linkDestination;

    private final AtomicBoolean inRefresh = new AtomicBoolean();

    protected FtpFileObject(final AbstractFileName name, final FtpFileSystem fileSystem, final FileName rootName)
            throws FileSystemException {
        super(name, fileSystem);
        final String relPath = UriParser.decode(rootName.getRelativeName(name));
        if (".".equals(relPath)) {
            // do not use the "." as path against the ftp-server
            // e.g. the uu.net ftp-server do a recursive listing then
            // this.relPath = UriParser.decode(rootName.getPath());
            // this.relPath = ".";
            this.relPath = null;
        } else {
            this.relPath = relPath;
        }
    }

    /**
     * Attaches this file object to its file resource.
     */
    @Override
    protected void doAttach() throws IOException {
        // Get the parent folder to find the info for this file
        // VFS-210 getInfo(false);
    }

    /**
     * Creates this file as a folder.
     */
    @Override
    protected void doCreateFolder() throws Exception {
        final boolean ok;
        final FtpClient client = getAbstractFileSystem().getClient();
        try {
            ok = client.makeDirectory(relPath);
        } finally {
            getAbstractFileSystem().putClient(client);
        }

        if (!ok) {
            throw new FileSystemException("vfs.provider.ftp/create-folder.error", getName());
        }
    }

    /**
     * Deletes the file.
     */
    @Override
    protected void doDelete() throws Exception {
        synchronized (getFileSystem()) {
            if (this.ftpFile != null) {
                final boolean ok;
                final FtpClient ftpClient = getAbstractFileSystem().getClient();
                try {
                    if (this.ftpFile.isDirectory()) {
                        ok = ftpClient.removeDirectory(relPath);
                    } else {
                        ok = ftpClient.deleteFile(relPath);
                    }
                } finally {
                    getAbstractFileSystem().putClient(ftpClient);
                }

                if (!ok) {
                    throw new FileSystemException("vfs.provider.ftp/delete-file.error", getName());
                }
                this.ftpFile = null;
            }
            this.childMap = EMPTY_FTP_FILE_MAP;
        }
    }

    /**
     * Detaches this file object from its file resource.
     */
    @Override
    protected void doDetach() {
        synchronized (getFileSystem()) {
            this.ftpFile = null;
            this.childMap = null;
        }
    }

    /**
     * Fetches the children of this file, if not already cached.
     */
    private void doGetChildren() throws IOException {
        if (childMap != null) {
            return;
        }

        final FtpClient client = getAbstractFileSystem().getClient();
        try {
            final String path = ftpFile != null && ftpFile.isSymbolicLink()
                    ? getFileSystem().getFileSystemManager().resolveName(getParent().getName(), ftpFile.getLink())
                            .getPath()
                    : relPath;
            final FTPFile[] tmpChildren = client.listFiles(path);
            if (ArrayUtils.isEmpty(tmpChildren)) {
                childMap = EMPTY_FTP_FILE_MAP;
            } else {
                childMap = new TreeMap<>();

                // Remove '.' and '..' elements
                for (int i = 0; i < tmpChildren.length; i++) {
                    final FTPFile child = tmpChildren[i];
                    if (child == null) {
                        if (log.isDebugEnabled()) {
                            log.debug(Messages.getString("vfs.provider.ftp/invalid-directory-entry.debug",
                                    Integer.valueOf(i), relPath));
                        }
                        continue;
                    }
                    if (!".".equals(child.getName()) && !"..".equals(child.getName())) {
                        childMap.put(child.getName(), child);
                    }
                }
            }
        } finally {
            getAbstractFileSystem().putClient(client);
        }
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    @Override
    protected long doGetContentSize() throws Exception {
        synchronized (getFileSystem()) {
            if (this.ftpFile == null) {
                return 0;
            }
            if (this.ftpFile.isSymbolicLink()) {
                final FileObject linkDest = getLinkDestination();
                // VFS-437: Try to avoid a recursion loop.
                if (this.isCircular(linkDest)) {
                    return this.ftpFile.getSize();
                }
                return linkDest.getContent().getSize();
            }
            return this.ftpFile.getSize();
        }
    }

    /**
     * Creates an input stream to read the file content from.
     */
    @Override
    protected InputStream doGetInputStream(final int bufferSize) throws Exception {
        final FtpClient client = getAbstractFileSystem().getClient();
        try {
            final InputStream inputStream = client.retrieveFileStream(relPath, 0);
            // VFS-210
            if (inputStream == null) {
                throw new FileNotFoundException(getName().toString());
            }
            return new FtpInputStream(client, inputStream, bufferSize);
        } catch (final Exception e) {
            getAbstractFileSystem().putClient(client);
            throw e;
        }
    }

    /**
     * Get the last modified time on an FTP file
     *
     * @see org.apache.commons.vfs2.provider.AbstractFileObject#doGetLastModifiedTime()
     */
    @Override
    protected long doGetLastModifiedTime() throws Exception {
        synchronized (getFileSystem()) {
            if (this.ftpFile == null) {
                return DEFAULT_TIMESTAMP;
            }
            if (this.ftpFile.isSymbolicLink()) {
                final FileObject linkDest = getLinkDestination();
                // VFS-437: Try to avoid a recursion loop.
                if (this.isCircular(linkDest)) {
                    return getTimestampMillis();
                }
                return linkDest.getContent().getLastModifiedTime();
            }
            return getTimestampMillis();
        }
    }

    /**
     * Creates an output stream to write the file content to.
     */
    @Override
    protected OutputStream doGetOutputStream(final boolean bAppend) throws Exception {
        final FtpClient client = getAbstractFileSystem().getClient();
        try {
            final OutputStream out;
            if (bAppend) {
                out = client.appendFileStream(relPath);
            } else {
                out = client.storeFileStream(relPath);
            }

            FileSystemException.requireNonNull(out, "vfs.provider.ftp/output-error.debug", this.getName(),
                    client.getReplyString());

            return new FtpOutputStream(client, out);
        } catch (final Exception e) {
            getAbstractFileSystem().putClient(client);
            throw e;
        }
    }

    @Override
    protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception {
        return new FtpRandomAccessContent(this, mode);
    }

    /**
     * Determines the type of the file, returns null if the file does not exist.
     */
    @Override
    protected FileType doGetType() throws Exception {
        // VFS-210
        synchronized (getFileSystem()) {
            if (this.ftpFile == null) {
                setFTPFile(false);
            }

            if (this.ftpFile == UNKNOWN) {
                return FileType.IMAGINARY;
            }
            if (this.ftpFile.isDirectory()) {
                return FileType.FOLDER;
            }
            if (this.ftpFile.isFile()) {
                return FileType.FILE;
            }
            if (this.ftpFile.isSymbolicLink()) {
                final FileObject linkDest = getLinkDestination();
                // VFS-437: We need to check if the symbolic link links back to the symbolic link itself
                if (this.isCircular(linkDest)) {
                    // If the symbolic link links back to itself, treat it as an imaginary file to prevent following
                    // this link. If the user tries to access the link as a file or directory, the user will end up with
                    // a FileSystemException warning that the file cannot be accessed. This is to prevent the infinite
                    // call back to doGetType() to prevent the StackOverFlow
                    return FileType.IMAGINARY;
                }
                return linkDest.getType();

            }
        }
        throw new FileSystemException("vfs.provider.ftp/get-type.error", getName());
    }

    /**
     * Lists the children of the file.
     */
    @Override
    protected String[] doListChildren() throws Exception {
        // List the children of this file
        doGetChildren();

        // VFS-210
        if (childMap == null) {
            return null;
        }

        // TODO - get rid of this children stuff
        final String[] childNames = childMap.values().stream().map(FTPFile::getName).toArray(String[]::new);

        return UriParser.encode(childNames);
    }

    @Override
    protected FileObject[] doListChildrenResolved() throws Exception {
        synchronized (getFileSystem()) {
            if (this.ftpFile != null && this.ftpFile.isSymbolicLink()) {
                final FileObject linkDest = getLinkDestination();
                // VFS-437: Try to avoid a recursion loop.
                if (this.isCircular(linkDest)) {
                    return null;
                }
                return linkDest.getChildren();
            }
        }
        return null;
    }

    /**
     * Renames the file
     */
    @Override
    protected void doRename(final FileObject newFile) throws Exception {
        synchronized (getFileSystem()) {
            final boolean ok;
            final FtpClient ftpClient = getAbstractFileSystem().getClient();
            try {
                final String newName = ((FtpFileObject) FileObjectUtils.getAbstractFileObject(newFile)).getRelPath();
                ok = ftpClient.rename(relPath, newName);
            } finally {
                getAbstractFileSystem().putClient(ftpClient);
            }

            if (!ok) {
                throw new FileSystemException("vfs.provider.ftp/rename-file.error", getName().toString(), newFile);
            }
            this.ftpFile = null;
            this.childMap = EMPTY_FTP_FILE_MAP;
        }
    }

    /**
     * Called by child file objects, to locate their FTP file info.
     *
     * @param name the file name in its native form ie. without URI stuff (%nn)
     * @param flush recreate children cache
     */
    private FTPFile getChildFile(final String name, final boolean flush) throws IOException {
        /*
         * If we should flush cached children, clear our children map unless we're in the middle of a refresh in which
         * case we've just recently refreshed our children. No need to do it again when our children are refresh()ed,
         * calling getChildFile() for themselves from within getInfo(). See getChildren().
         */
        if (flush && !inRefresh.get()) {
            childMap = null;
        }

        // List the children of this file
        doGetChildren();

        // Look for the requested child
        // VFS-210 adds the null check.
        return childMap != null ? childMap.get(name) : null;
    }

    /**
     * Returns the file's list of children.
     *
     * @return The list of children
     * @throws FileSystemException If there was a problem listing children
     * @see AbstractFileObject#getChildren()
     * @since 2.0
     */
    @Override
    public FileObject[] getChildren() throws FileSystemException {
        try {
            if (doGetType() != FileType.FOLDER) {
                throw new FileNotFolderException(getName());
            }
        } catch (final Exception ex) {
            throw new FileNotFolderException(getName(), ex);
        }

        try {
            /*
             * Wrap our parent implementation, noting that we're refreshing so that we don't refresh() ourselves and
             * each of our parents for each children. Note that refresh() will list children. Meaning, if if this file
             * has C children, P parents, there will be (C * P) listings made with (C * (P + 1)) refreshes, when there
             * should really only be 1 listing and C refreshes.
             */
            this.inRefresh.set(true);
            return super.getChildren();
        } finally {
            this.inRefresh.set(false);
        }
    }

    FtpInputStream getInputStream(final long filePointer) throws IOException {
        final FtpClient client = getAbstractFileSystem().getClient();
        try {
            final InputStream instr = client.retrieveFileStream(relPath, filePointer);
            FileSystemException.requireNonNull(instr, "vfs.provider.ftp/input-error.debug", this.getName(),
                    client.getReplyString());
            return new FtpInputStream(client, instr);
        } catch (final IOException e) {
            getAbstractFileSystem().putClient(client);
            throw e;
        }
    }

    private FileObject getLinkDestination() throws FileSystemException {
        if (linkDestination == null) {
            final String path;
            synchronized (getFileSystem()) {
                path = this.ftpFile == null ? null : this.ftpFile.getLink();
            }
            final FileName parent = getName().getParent();
            final FileName relativeTo = parent == null ? getName() : parent;
            final FileName linkDestinationName = getFileSystem().getFileSystemManager().resolveName(relativeTo, path);
            linkDestination = getFileSystem().resolveFile(linkDestinationName);
        }
        return linkDestination;
    }

    String getRelPath() {
        return relPath;
    }

    /**
     * ftpFile is not null.
     */
    @SuppressWarnings("resource") // abstractFileSystem is managed in the superclass.
    private long getTimestampMillis() throws IOException {
        final FtpFileSystem abstractFileSystem = getAbstractFileSystem();
        final Boolean mdtmLastModifiedTime = FtpFileSystemConfigBuilder.getInstance()
            .getMdtmLastModifiedTime(abstractFileSystem.getFileSystemOptions());
        if (mdtmLastModifiedTime != null && mdtmLastModifiedTime.booleanValue()) {
            final FtpClient client = abstractFileSystem.getClient();
            if (!mdtmSet && client.hasFeature("MDTM")) {
                final Instant mdtmInstant = client.mdtmInstant(relPath);
                final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                final long epochMilli = mdtmInstant.toEpochMilli();
                calendar.setTimeInMillis(epochMilli);
                ftpFile.setTimestamp(calendar);
                mdtmSet = true;
            }
        }
        return ftpFile.getTimestamp().getTime().getTime();
    }

    /**
     * This is an over simplistic implementation for VFS-437.
     */
    private boolean isCircular(final FileObject linkDest) throws FileSystemException {
        return linkDest.getName().getPathDecoded().equals(this.getName().getPathDecoded());
    }

    /**
     * Called when the type or content of this file changes.
     */
    @Override
    protected void onChange() throws IOException {
        childMap = null;

        if (getType().equals(FileType.IMAGINARY)) {
            // file is deleted, avoid server lookup
            synchronized (getFileSystem()) {
                this.ftpFile = UNKNOWN;
            }
            return;
        }

        setFTPFile(true);
    }

    /**
     * Called when the children of this file change.
     */
    @Override
    protected void onChildrenChanged(final FileName child, final FileType newType) {
        if (childMap != null && newType.equals(FileType.IMAGINARY)) {
            try {
                childMap.remove(UriParser.decode(child.getBaseName()));
            } catch (final FileSystemException e) {
                throw new RuntimeException(e.getMessage());
            }
        } else {
            // if child was added we have to rescan the children
            // TODO - get rid of this
            childMap = null;
        }
    }

    /**
     * @throws FileSystemException if an error occurs.
     */
    @Override
    public void refresh() throws FileSystemException {
        if (inRefresh.compareAndSet(false, true)) {
            try {
                super.refresh();
                synchronized (getFileSystem()) {
                    this.ftpFile = null;
                }
                /*
                 * VFS-210 try { // this will tell the parent to recreate its children collection getInfo(true); } catch
                 * (IOException e) { throw new FileSystemException(e); }
                 */
            } finally {
                inRefresh.set(false);
            }
        }
    }

    /**
     * Sets the internal FTPFile for this instance.
     */
    private void setFTPFile(final boolean flush) throws IOException {
        synchronized (getFileSystem()) {
            final FtpFileObject parent = (FtpFileObject) FileObjectUtils.getAbstractFileObject(getParent());
            final FTPFile newFileInfo;
            if (parent != null) {
                newFileInfo = parent.getChildFile(UriParser.decode(getName().getBaseName()), flush);
            } else {
                // Assume the root is a directory and exists
                newFileInfo = new FTPFile();
                newFileInfo.setType(FTPFile.DIRECTORY_TYPE);
            }
            this.ftpFile = newFileInfo == null ? UNKNOWN : newFileInfo;
        }}
}
