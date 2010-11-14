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

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileNotFoundException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.util.FileObjectUtils;
import org.apache.commons.vfs2.util.MonitorInputStream;
import org.apache.commons.vfs2.util.MonitorOutputStream;
import org.apache.commons.vfs2.util.RandomAccessMode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

/**
 * An SFTP file.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date: 2005-10-14 19:59:47 +0200 (Fr, 14 Okt
 *          2005) $
 */
public class SftpFileObject extends AbstractFileObject implements FileObject
{
    private final SftpFileSystem fileSystem;
    private SftpATTRS attrs;
    private final String relPath;

    private boolean inRefresh;

    protected SftpFileObject(final FileName name,
            final SftpFileSystem fileSystem) throws FileSystemException
    {
        super(name, fileSystem);
        this.fileSystem = fileSystem;
        relPath = UriParser.decode(fileSystem.getRootName().getRelativeName(
                name));
    }

    @Override
    protected void doDetach() throws Exception
    {
        attrs = null;
    }

    @Override
    public void refresh() throws FileSystemException
    {
        if (!inRefresh)
        {
            try
            {
                inRefresh = true;
                super.refresh();
                try
                {
                    attrs = null;
                    getType();
                }
                catch (IOException e)
                {
                    throw new FileSystemException(e);
                }
            }
            finally
            {
                inRefresh = false;
            }
        }
    }

    /**
     * Determines the type of this file, returns null if the file does not
     * exist.
     */
    @Override
    protected FileType doGetType() throws Exception
    {
        if (attrs == null)
        {
            statSelf();
        }

        if (attrs == null)
        {
            return FileType.IMAGINARY;
        }

        if ((attrs.getFlags() & SftpATTRS.SSH_FILEXFER_ATTR_PERMISSIONS) == 0)
        {
            throw new FileSystemException(
                    "vfs.provider.sftp/unknown-permissions.error");
        }
        if (attrs.isDir())
        {
            return FileType.FOLDER;
        }
        else
        {
            return FileType.FILE;
        }
    }

    /**
     * Called when the type or content of this file changes.
     */
    @Override
    protected void onChange() throws Exception
    {
        statSelf();
    }

    /**
     * Fetches file attrs from server.
     */
    private void statSelf() throws Exception
    {
        ChannelSftp channel = fileSystem.getChannel();
        try
        {
            setStat(channel.stat(relPath));
        }
        catch (final SftpException e)
        {
            try
            {
                // maybe the channel has some problems, so recreate the channel and retry
                if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE)
                {
                    channel.disconnect();
                    channel = fileSystem.getChannel();
                    setStat(channel.stat(relPath));
                }
                else
                {
                    // Really does not exist
                    attrs = null;
                }
            }
            catch (final SftpException e2)
            {
                // TODO - not strictly true, but jsch 0.1.2 does not give us
                // enough info in the exception. Should be using:
                // if ( e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE )
                // However, sometimes the exception has the correct id, and
                // sometimes
                // it does not. Need to look into why.

                // Does not exist
                attrs = null;
            }
        }
        finally
        {
            fileSystem.putChannel(channel);
        }
    }

    /**
     * Set attrs from listChildrenResolved
     */
    private void setStat(SftpATTRS attrs)
    {
        this.attrs = attrs;
    }

    /**
     * Creates this file as a folder.
     */
    @Override
    protected void doCreateFolder() throws Exception
    {
        final ChannelSftp channel = fileSystem.getChannel();
        try
        {
            channel.mkdir(relPath);
        }
        finally
        {
            fileSystem.putChannel(channel);
        }
    }

    @Override
    protected long doGetLastModifiedTime() throws Exception
    {
        if (attrs == null
                || (attrs.getFlags() & SftpATTRS.SSH_FILEXFER_ATTR_ACMODTIME) == 0)
        {
            throw new FileSystemException(
                    "vfs.provider.sftp/unknown-modtime.error");
        }
        return attrs.getMTime() * 1000L;
    }

    /**
     * Sets the last modified time of this file. Is only called if
     * {@link #doGetType} does not return {@link FileType#IMAGINARY}. <p/>
     *
     * @param modtime
     *            is modification time in milliseconds. SFTP protocol can send
     *            times with nanosecond precision but at the moment jsch send
     *            them with second precision.
     */
    @Override
    protected void doSetLastModifiedTime(final long modtime) throws Exception
    {
        final ChannelSftp channel = fileSystem.getChannel();
        try
        {
            int newMTime = (int) (modtime / 1000L);

            attrs.setACMODTIME(attrs.getATime(), newMTime);
            channel.setStat(relPath, attrs);
        }
        finally
        {
            fileSystem.putChannel(channel);
        }
    }

    /**
     * Deletes the file.
     */
    @Override
    protected void doDelete() throws Exception
    {
        final ChannelSftp channel = fileSystem.getChannel();
        try
        {
            if (getType() == FileType.FILE)
            {
                channel.rm(relPath);
            }
            else
            {
                channel.rmdir(relPath);
            }
        }
        finally
        {
            fileSystem.putChannel(channel);
        }
    }

    /**
     * Rename the file.
     */
    @Override
    protected void doRename(FileObject newfile) throws Exception
    {
        final ChannelSftp channel = fileSystem.getChannel();
        try
        {
            channel.rename(relPath, ((SftpFileObject) newfile).relPath);
        }
        finally
        {
            fileSystem.putChannel(channel);
        }
    }

    /**
     * Lists the children of this file.
     */
    @Override
    protected FileObject[] doListChildrenResolved() throws Exception
    {
        // List the contents of the folder
        Vector<?> vector = null;
        final ChannelSftp channel = fileSystem.getChannel();

        try
        {
            // try the direct way to list the directory on the server to avoid too many roundtrips
            vector = channel.ls(relPath);
        }
        catch (SftpException e)
        {
            String workingDirectory = null;
            try
            {
                if (relPath != null)
                {
                    workingDirectory = channel.pwd();
                    channel.cd(relPath);
                }
            }
            catch (SftpException ex)
            {
                // VFS-210: seems not to be a directory
                return null;
            }

            SftpException lsEx = null;
            try
            {
                vector = channel.ls(".");
            }
            catch (SftpException ex)
            {
                lsEx = ex;
            }
            finally
            {
                try
                {
                    if (relPath != null)
                    {
                        channel.cd(workingDirectory);
                    }
                }
                catch (SftpException xe)
                {
                    throw new FileSystemException("vfs.provider.sftp/change-work-directory-back.error",
                                                  workingDirectory, lsEx);
                }
            }

            if (lsEx != null)
            {
                throw lsEx;
            }
        }
        finally
        {
            fileSystem.putChannel(channel);
        }
        if (vector == null)
        {
            throw new FileSystemException(
                    "vfs.provider.sftp/list-children.error");
        }

        // Extract the child names
        final ArrayList<FileObject> children = new ArrayList<FileObject>();
        for (@SuppressWarnings("unchecked") // OK because ChannelSftp.ls() is documented to return Vector<LsEntry>
            Iterator<LsEntry> iterator = (Iterator<LsEntry>) vector.iterator(); iterator.hasNext();)
        {
            final LsEntry stat = iterator.next();

            String name = stat.getFilename();
            if (VFS.isUriStyle())
            {
                if (stat.getAttrs().isDir()
                        && name.charAt(name.length() - 1) != '/')
                {
                    name = name + "/";
                }
            }

            if (name.equals(".") || name.equals("..") || name.equals("./")
                    || name.equals("../"))
            {
                continue;
            }

            FileObject fo =
                getFileSystem()
                    .resolveFile(
                            getFileSystem().getFileSystemManager().resolveName(
                                    getName(), UriParser.encode(name),
                                    NameScope.CHILD));

            ((SftpFileObject) FileObjectUtils.getAbstractFileObject(fo)).setStat(stat.getAttrs());

            children.add(fo);
        }

        return children.toArray(new FileObject[children.size()]);
    }

    /**
     * Lists the children of this file.
     */
    @Override
    protected String[] doListChildren() throws Exception
    {
        // use doListChildrenResolved for performance
        return null;
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    @Override
    protected long doGetContentSize() throws Exception
    {
        if (attrs == null
                || (attrs.getFlags() & SftpATTRS.SSH_FILEXFER_ATTR_SIZE) == 0)
        {
            throw new FileSystemException(
                    "vfs.provider.sftp/unknown-size.error");
        }
        return attrs.getSize();
    }

    @Override
    protected RandomAccessContent doGetRandomAccessContent(
            final RandomAccessMode mode) throws Exception
    {
        return new SftpRandomAccessContent(this, mode);
    }

    /**
     * Creates an input stream to read the file content from.
     */
    InputStream getInputStream(long filePointer) throws IOException
    {
        final ChannelSftp channel = fileSystem.getChannel();
        try
        {
            // hmmm - using the in memory method is soooo much faster ...
            // TODO - Don't read the entire file into memory. Use the
            // stream-based methods on ChannelSftp once they work properly final
            // .... no stream based method with resume???
            ByteArrayOutputStream outstr = new ByteArrayOutputStream();
            try
            {
                channel.get(getName().getPathDecoded(), outstr, null,
                        ChannelSftp.RESUME, filePointer);
            }
            catch (SftpException e)
            {
                throw new FileSystemException(e);
            }
            outstr.close();
            return new ByteArrayInputStream(outstr.toByteArray());
        }
        finally
        {
            fileSystem.putChannel(channel);
        }
    }

    /**
     * Creates an input stream to read the file content from.
     */
    @Override
    protected InputStream doGetInputStream() throws Exception
    {
        // VFS-113: avoid npe
        synchronized (fileSystem)
        {
            final ChannelSftp channel = fileSystem.getChannel();
            try
            {
                // return channel.get(getName().getPath());
                // hmmm - using the in memory method is soooo much faster ...

                // TODO - Don't read the entire file into memory. Use the
                // stream-based methods on ChannelSftp once they work properly

                /*
                final ByteArrayOutputStream outstr = new ByteArrayOutputStream();
                channel.get(relPath, outstr);
                outstr.close();
                return new ByteArrayInputStream(outstr.toByteArray());
                */

                InputStream is;
                try
                {
                    // VFS-210: sftp allows to gather an input stream even from a directory and will
                    // fail on first read. So we need to check the type anyway
                    if (!getType().hasContent())
                    {
                        throw new FileSystemException("vfs.provider/read-not-file.error", getName());
                    }

                    is = channel.get(relPath);
                }
                catch (SftpException e)
                {
                    if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE)
                    {
                        throw new FileNotFoundException(getName());
                    }

                    throw new FileSystemException(e);
                }

                return new SftpInputStream(channel, is);

            }
            finally
            {
//              fileSystem.putChannel(channel);
            }
        }
    }

    /**
     * Creates an output stream to write the file content to.
     */
    @Override
    protected OutputStream doGetOutputStream(boolean bAppend) throws Exception
    {
        // TODO - Don't write the entire file into memory. Use the stream-based
        // methods on ChannelSftp once the work properly
        /*
        final ChannelSftp channel = fileSystem.getChannel();
        return new SftpOutputStream(channel);
        */

        final ChannelSftp channel = fileSystem.getChannel();
        return new SftpOutputStream(channel, channel.put(relPath));
    }

    /**
     * An InputStream that monitors for end-of-file.
     */
    private class SftpInputStream extends MonitorInputStream
    {
        private final ChannelSftp channel;

        public SftpInputStream(final ChannelSftp channel, final InputStream in)
        {
            super(in);
            this.channel = channel;
        }

        /**
         * Called after the stream has been closed.
         */
        @Override
        protected void onClose() throws IOException
        {
            fileSystem.putChannel(channel);
        }
    }

    /**
     * An OutputStream that wraps an sftp OutputStream, and closes the channel
     * when the stream is closed.
     */
    private class SftpOutputStream extends MonitorOutputStream
    {
        private final ChannelSftp channel;

        public SftpOutputStream(final ChannelSftp channel, OutputStream out)
        {
            super(out);
            this.channel = channel;
        }

        /**
         * Called after this stream is closed.
         */
        @Override
        protected void onClose() throws IOException
        {
            fileSystem.putChannel(channel);
        }
    }

}
