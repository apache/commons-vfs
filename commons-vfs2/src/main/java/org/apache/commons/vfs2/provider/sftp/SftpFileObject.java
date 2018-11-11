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
package org.apache.commons.vfs2.provider.sftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.vfs2.FileNotFoundException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.util.FileObjectUtils;
import org.apache.commons.vfs2.util.MonitorInputStream;
import org.apache.commons.vfs2.util.MonitorOutputStream;
import org.apache.commons.vfs2.util.PosixPermissions;
import org.apache.commons.vfs2.util.RandomAccessMode;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

/**
 * An SFTP file.
 */
public class SftpFileObject extends AbstractFileObject<SftpFileSystem> {
    private static final long MOD_TIME_FACTOR = 1000L;

    private SftpATTRS attrs;
    private final String relPath;

    private boolean inRefresh;

    protected SftpFileObject(final AbstractFileName name, final SftpFileSystem fileSystem) throws FileSystemException {
        super(name, fileSystem);
        relPath = UriParser.decode(fileSystem.getRootName().getRelativeName(name));
    }

    /** @since 2.0 */
    @Override
    protected void doDetach() throws Exception {
        attrs = null;
    }

    /**
     * @throws FileSystemException if error occurs.
     * @since 2.0
     */
    @Override
    public void refresh() throws FileSystemException {
        if (!inRefresh) {
            try {
                inRefresh = true;
                super.refresh();
                try {
                    attrs = null;
                    getType();
                } catch (final IOException e) {
                    throw new FileSystemException(e);
                }
            } finally {
                inRefresh = false;
            }
        }
    }

    /**
     * Determines the type of this file, returns null if the file does not exist.
     */
    @Override
    protected FileType doGetType() throws Exception {
        if (attrs == null) {
            statSelf();
        }

        if (attrs == null) {
            return FileType.IMAGINARY;
        }

        if ((attrs.getFlags() & SftpATTRS.SSH_FILEXFER_ATTR_PERMISSIONS) == 0) {
            throw new FileSystemException("vfs.provider.sftp/unknown-permissions.error");
        }
        if (attrs.isDir()) {
            return FileType.FOLDER;
        }
        return FileType.FILE;
    }

    /**
     * Called when the type or content of this file changes.
     */
    @Override
    protected void onChange() throws Exception {
        statSelf();
    }

    /**
     * Fetches file attributes from server.
     *
     * @throws IOException
     */
    private void statSelf() throws IOException {
        ChannelSftp channel = getAbstractFileSystem().getChannel();
        try {
            setStat(channel.stat(relPath));
        } catch (final SftpException e) {
            try {
                // maybe the channel has some problems, so recreate the channel and retry
                if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    channel.disconnect();
                    channel = getAbstractFileSystem().getChannel();
                    setStat(channel.stat(relPath));
                } else {
                    // Really does not exist
                    attrs = null;
                }
            } catch (final SftpException innerEx) {
                // TODO - not strictly true, but jsch 0.1.2 does not give us
                // enough info in the exception. Should be using:
                // if ( e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE )
                // However, sometimes the exception has the correct id, and
                // sometimes
                // it does not. Need to look into why.

                // Does not exist
                attrs = null;
            }
        } finally {
            getAbstractFileSystem().putChannel(channel);
        }
    }

    /**
     * Set attrs from listChildrenResolved
     */
    private void setStat(final SftpATTRS attrs) {
        this.attrs = attrs;
    }

    /**
     * Creates this file as a folder.
     */
    @Override
    protected void doCreateFolder() throws Exception {
        final ChannelSftp channel = getAbstractFileSystem().getChannel();
        try {
            channel.mkdir(relPath);
        } finally {
            getAbstractFileSystem().putChannel(channel);
        }
    }

    @Override
    protected long doGetLastModifiedTime() throws Exception {
        if (attrs == null || (attrs.getFlags() & SftpATTRS.SSH_FILEXFER_ATTR_ACMODTIME) == 0) {
            throw new FileSystemException("vfs.provider.sftp/unknown-modtime.error");
        }
        return attrs.getMTime() * MOD_TIME_FACTOR;
    }

    /**
     * Sets the last modified time of this file. Is only called if {@link #doGetType} does not return
     * {@link FileType#IMAGINARY}.
     *
     * @param modtime is modification time in milliseconds. SFTP protocol can send times with nanosecond precision but
     *            at the moment jsch send them with second precision.
     */
    @Override
    protected boolean doSetLastModifiedTime(final long modtime) throws Exception {
        final int newMTime = (int) (modtime / MOD_TIME_FACTOR);
        attrs.setACMODTIME(attrs.getATime(), newMTime);
        flushStat();
        return true;
    }

    private void flushStat() throws IOException, SftpException {
        final ChannelSftp channel = getAbstractFileSystem().getChannel();
        try {
            channel.setStat(relPath, attrs);
        } finally {
            getAbstractFileSystem().putChannel(channel);
        }
    }

    /**
     * Deletes the file.
     */
    @Override
    protected void doDelete() throws Exception {
        final ChannelSftp channel = getAbstractFileSystem().getChannel();
        try {
            if (isFile()) {
                channel.rm(relPath);
            } else {
                channel.rmdir(relPath);
            }
        } finally {
            getAbstractFileSystem().putChannel(channel);
        }
    }

    /**
     * Rename the file.
     */
    @Override
    protected void doRename(final FileObject newFile) throws Exception {
        final ChannelSftp channel = getAbstractFileSystem().getChannel();
        try {
            final SftpFileObject newSftpFileObject = (SftpFileObject) FileObjectUtils.getAbstractFileObject(newFile);
            channel.rename(relPath, newSftpFileObject.relPath);
        } finally {
            getAbstractFileSystem().putChannel(channel);
        }
    }

    /**
     * Returns the POSIX type permissions of the file.
     *
     * @param checkIds {@code true} if user and group ID should be checked (needed for some access rights checks)
     * @return A PosixPermission object
     * @throws Exception If an error occurs
     * @since 2.1
     */
    protected PosixPermissions getPermissions(final boolean checkIds) throws Exception {
        statSelf();
        boolean isInGroup = false;
        if (checkIds) {
            for (final int groupId : getAbstractFileSystem().getGroupsIds()) {
                if (groupId == attrs.getGId()) {
                    isInGroup = true;
                    break;
                }
            }
        }
        final boolean isOwner = checkIds ? attrs.getUId() == getAbstractFileSystem().getUId() : false;
        return new PosixPermissions(attrs.getPermissions(), isOwner, isInGroup);
    }

    @Override
    protected boolean doIsReadable() throws Exception {
        return getPermissions(true).isReadable();
    }

    @Override
    protected boolean doSetReadable(final boolean readable, final boolean ownerOnly) throws Exception {
        final PosixPermissions permissions = getPermissions(false);
        final int newPermissions = permissions.makeReadable(readable, ownerOnly);
        if (newPermissions == permissions.getPermissions()) {
            return true;
        }

        attrs.setPERMISSIONS(newPermissions);
        flushStat();

        return true;
    }

    @Override
    protected boolean doIsWriteable() throws Exception {
        return getPermissions(true).isWritable();
    }

    @Override
    protected boolean doSetWritable(final boolean writable, final boolean ownerOnly) throws Exception {
        final PosixPermissions permissions = getPermissions(false);
        final int newPermissions = permissions.makeWritable(writable, ownerOnly);
        if (newPermissions == permissions.getPermissions()) {
            return true;
        }

        attrs.setPERMISSIONS(newPermissions);
        flushStat();

        return true;
    }

    @Override
    protected boolean doIsExecutable() throws Exception {
        return getPermissions(true).isExecutable();
    }

    @Override
    protected boolean doSetExecutable(final boolean executable, final boolean ownerOnly) throws Exception {
        final PosixPermissions permissions = getPermissions(false);
        final int newPermissions = permissions.makeExecutable(executable, ownerOnly);
        if (newPermissions == permissions.getPermissions()) {
            return true;
        }

        attrs.setPERMISSIONS(newPermissions);
        flushStat();

        return true;
    }

    /**
     * Lists the children of this file.
     */
    @Override
    protected FileObject[] doListChildrenResolved() throws Exception {
        // should not require a round-trip because type is already set.
        if (this.isFile()) {
            return null;
        }
        // List the contents of the folder
        Vector<?> vector = null;
        final ChannelSftp channel = getAbstractFileSystem().getChannel();

        try {
            // try the direct way to list the directory on the server to avoid too many roundtrips
            vector = channel.ls(relPath);
        } catch (final SftpException e) {
            String workingDirectory = null;
            try {
                if (relPath != null) {
                    workingDirectory = channel.pwd();
                    channel.cd(relPath);
                }
            } catch (final SftpException ex) {
                // VFS-210: seems not to be a directory
                return null;
            }

            SftpException lsEx = null;
            try {
                vector = channel.ls(".");
            } catch (final SftpException ex) {
                lsEx = ex;
            } finally {
                try {
                    if (relPath != null) {
                        channel.cd(workingDirectory);
                    }
                } catch (final SftpException xe) {
                    throw new FileSystemException("vfs.provider.sftp/change-work-directory-back.error",
                            workingDirectory, lsEx);
                }
            }

            if (lsEx != null) {
                throw lsEx;
            }
        } finally {
            getAbstractFileSystem().putChannel(channel);
        }
        if (vector == null) {
            throw new FileSystemException("vfs.provider.sftp/list-children.error");
        }

        // Extract the child names
        final ArrayList<FileObject> children = new ArrayList<>();
        for (@SuppressWarnings("unchecked") // OK because ChannelSftp.ls() is documented to return Vector<LsEntry>
        final Iterator<LsEntry> iterator = (Iterator<LsEntry>) vector.iterator(); iterator.hasNext();) {
            final LsEntry stat = iterator.next();

            String name = stat.getFilename();
            if (VFS.isUriStyle() && stat.getAttrs().isDir() && name.charAt(name.length() - 1) != '/') {
                name = name + "/";
            }

            if (name.equals(".") || name.equals("..") || name.equals("./") || name.equals("../")) {
                continue;
            }

            final FileObject fo = getFileSystem().resolveFile(getFileSystem().getFileSystemManager()
                    .resolveName(getName(), UriParser.encode(name), NameScope.CHILD));

            ((SftpFileObject) FileObjectUtils.getAbstractFileObject(fo)).setStat(stat.getAttrs());

            children.add(fo);
        }

        return children.toArray(new FileObject[children.size()]);
    }

    /**
     * Lists the children of this file.
     */
    @Override
    protected String[] doListChildren() throws Exception {
        // use doListChildrenResolved for performance
        return null;
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    @Override
    protected long doGetContentSize() throws Exception {
        if (attrs == null || (attrs.getFlags() & SftpATTRS.SSH_FILEXFER_ATTR_SIZE) == 0) {
            throw new FileSystemException("vfs.provider.sftp/unknown-size.error");
        }
        return attrs.getSize();
    }

    @Override
    protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception {
        return new SftpRandomAccessContent(this, mode);
    }

    /**
     * Creates an input stream to read the file content from. The input stream is starting at the given position in the
     * file.
     */
    InputStream getInputStream(final long filePointer) throws IOException {
        final ChannelSftp channel = getAbstractFileSystem().getChannel();
        // Using InputStream directly from the channel
        // is much faster than the memory method.
        try {
            final InputStream is = channel.get(getName().getPathDecoded(), null, filePointer);
            return new SftpInputStream(channel, is);
        } catch (final SftpException e) {
            getAbstractFileSystem().putChannel(channel);
            throw new FileSystemException(e);
        }
    }

    /**
     * Creates an input stream to read the file content from.
     */
    @Override
    protected InputStream doGetInputStream() throws Exception {
        // VFS-113: avoid npe
        synchronized (getAbstractFileSystem()) {
            final ChannelSftp channel = getAbstractFileSystem().getChannel();
            try {
                // return channel.get(getName().getPath());
                // hmmm - using the in memory method is soooo much faster ...

                // TODO - Don't read the entire file into memory. Use the
                // stream-based methods on ChannelSftp once they work properly

                /*
                 * final ByteArrayOutputStream outstr = new ByteArrayOutputStream(); channel.get(relPath, outstr);
                 * outstr.close(); return new ByteArrayInputStream(outstr.toByteArray());
                 */

                InputStream is;
                try {
                    // VFS-210: sftp allows to gather an input stream even from a directory and will
                    // fail on first read. So we need to check the type anyway
                    if (!getType().hasContent()) {
                        throw new FileSystemException("vfs.provider/read-not-file.error", getName());
                    }

                    is = channel.get(relPath);
                } catch (final SftpException e) {
                    if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                        throw new FileNotFoundException(getName());
                    }

                    throw new FileSystemException(e);
                }

                return new SftpInputStream(channel, is);

            } finally {
                // getAbstractFileSystem().putChannel(channel);
            }
        }
    }

    /**
     * Creates an output stream to write the file content to.
     */
    @Override
    protected OutputStream doGetOutputStream(final boolean bAppend) throws Exception {
        // TODO - Don't write the entire file into memory. Use the stream-based
        // methods on ChannelSftp once the work properly
        /*
         * final ChannelSftp channel = getAbstractFileSystem().getChannel(); return new SftpOutputStream(channel);
         */

        final ChannelSftp channel = getAbstractFileSystem().getChannel();
        return new SftpOutputStream(channel, channel.put(relPath));
    }

    /**
     * An InputStream that monitors for end-of-file.
     */
    private class SftpInputStream extends MonitorInputStream {
        private final ChannelSftp channel;

        public SftpInputStream(final ChannelSftp channel, final InputStream in) {
            super(in);
            this.channel = channel;
        }

        /**
         * Called after the stream has been closed.
         */
        @Override
        protected void onClose() throws IOException {
            getAbstractFileSystem().putChannel(channel);
        }
    }

    /**
     * An OutputStream that wraps an sftp OutputStream, and closes the channel when the stream is closed.
     */
    private class SftpOutputStream extends MonitorOutputStream {
        private final ChannelSftp channel;

        public SftpOutputStream(final ChannelSftp channel, final OutputStream out) {
            super(out);
            this.channel = channel;
        }

        /**
         * Called after this stream is closed.
         */
        @Override
        protected void onClose() throws IOException {
            getAbstractFileSystem().putChannel(channel);
        }
    }

}
