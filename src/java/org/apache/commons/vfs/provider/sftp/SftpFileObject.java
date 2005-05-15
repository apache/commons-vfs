/*
 * Copyright 2002-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs.provider.sftp;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.RandomAccessContent;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.provider.UriParser;
import org.apache.commons.vfs.util.MonitorOutputStream;
import org.apache.commons.vfs.util.RandomAccessMode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * An SFTP file.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class SftpFileObject
    extends AbstractFileObject
    implements FileObject
{
    private final SftpFileSystem fileSystem;
    private SftpATTRS attrs;

    protected SftpFileObject(final FileName name,
                             final SftpFileSystem fileSystem)
    {
        super(name, fileSystem);
        this.fileSystem = fileSystem;
    }

    /**
     * Determines the type of this file, returns null if the file does not
     * exist.
     */
    protected FileType doGetType()
        throws Exception
    {
        statSelf();

        if (attrs == null)
        {
            return FileType.IMAGINARY;
        }

        if ((attrs.getFlags() & SftpATTRS.SSH_FILEXFER_ATTR_PERMISSIONS) == 0)
        {
            throw new FileSystemException("vfs.provider.sftp/unknown-permissions.error");
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
    protected void onChange()
        throws Exception
    {
        statSelf();
    }

    /**
     * Fetches file attrs from server.
     */
    private void statSelf()
        throws Exception
    {
        final ChannelSftp channel = fileSystem.getChannel();
        try
        {
            attrs = channel.stat(getName().getPathDecoded());
        }
        catch (final SftpException e)
        {
            // TODO - not strictly true, but jsch 0.1.2 does not give us
            // enough info in the exception.  Should be using:
            // if ( e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE )
            // However, sometimes the exception has the correct id, and sometimes
            // it does not.  Need to look into why.

            // Does not exist
            attrs = null;
        }
        finally
        {
            fileSystem.putChannel(channel);
        }
    }

    /**
     * Creates this file as a folder.
     */
    protected void doCreateFolder()
        throws Exception
    {
        final ChannelSftp channel = fileSystem.getChannel();
        try
        {
            channel.mkdir(getName().getPathDecoded());
        }
        finally
        {
            fileSystem.putChannel(channel);
        }
    }

    protected long doGetLastModifiedTime() throws Exception
    {
        if (attrs == null || (attrs.getFlags() & SftpATTRS.SSH_FILEXFER_ATTR_ACMODTIME) == 0)
        {
            throw new FileSystemException("vfs.provider.sftp/unknown-modtime.error");
        }
        return attrs.getMTime() * 1000L;
    }

    /**
     * Sets the last modified time of this file.  Is only called if
     * {@link #doGetType} does not return {@link FileType#IMAGINARY}.
     * <p/>
     *
     * @param modtime is modification time in milliseconds. SFTP protocol can
     *                send times with nanosecond precision but at the moment jsch send them
     *                with second precision.
     */
    protected void doSetLastModifiedTime(final long modtime)
        throws Exception
    {
        final ChannelSftp channel = fileSystem.getChannel();
        try
        {
            int newMTime = (int) (modtime / 1000L);

            attrs.setACMODTIME(attrs.getATime(), newMTime);
            channel.setStat(getName().getPathDecoded(), attrs);
        }
        finally
        {
            fileSystem.putChannel(channel);
        }
    }

    /**
     * Deletes the file.
     */
    protected void doDelete()
        throws Exception
    {
        final ChannelSftp channel = fileSystem.getChannel();
        try
        {
            if (getType() == FileType.FILE)
            {
                channel.rm(getName().getPathDecoded());
            }
            else
            {
                channel.rmdir(getName().getPathDecoded());
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
    protected void doRename(FileObject newfile) throws Exception
    {
        final ChannelSftp channel = fileSystem.getChannel();
        try
        {
            channel.rename(getName().getPathDecoded(), newfile.getName().getPathDecoded());
        }
        finally
        {
            fileSystem.putChannel(channel);
        }
    }

    /**
     * Lists the children of this file.
     */
    protected String[] doListChildren()
        throws Exception
    {
        // List the contents of the folder
        final Vector vector;
        final ChannelSftp channel = fileSystem.getChannel();
        try
        {
            vector = channel.ls(getName().getPathDecoded());
        }
        finally
        {
            fileSystem.putChannel(channel);
        }
        if (vector == null)
        {
            throw new FileSystemException("vfs.provider.sftp/list-children.error");
        }

        // Extract the child names
        final ArrayList children = new ArrayList();
        for (Iterator iterator = vector.iterator(); iterator.hasNext();)
        {
            // Each entry is in unix ls format <perms> <?> <user> <group> <size> <date> <name>
            final String stat = (String) iterator.next();
            final StringTokenizer tokens = new StringTokenizer(stat);
            // TODO - check there are the correct number of tokens
            // TODO - handle names with spaces in 'em
            for (int i = 0; i < 8; tokens.nextToken(), i++)
            {
            }
            final String name = tokens.nextToken();
            if (name.equals(".") || name.equals(".."))
            {
                continue;
            }
            children.add(name);
        }
        return UriParser.encode((String[]) children.toArray(new String[children.size()]));
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    protected long doGetContentSize()
        throws Exception
    {
        if (attrs == null || (attrs.getFlags() & SftpATTRS.SSH_FILEXFER_ATTR_SIZE) == 0)
        {
            throw new FileSystemException("vfs.provider.sftp/unknown-size.error");
        }
        return attrs.getSize();
    }

    protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception
    {
        return new SftpRandomAccessContent(this, mode);
    }
    
    /**
     * Creates an input stream to read the file content from.
     */
    InputStream getInputStream(long filePointer) throws IOException
    {
        throw new UnsupportedOperationException("Implemented. Yes. But have to wait for jsch release :-)");
        /*
        final ChannelSftp channel = fileSystem.getChannel();
        try
        {
            // return channel.get(getName().getPath());
            // hmmm - using the in memory method is soooo much faster ...

            // TODO - Don't read the entire file into memory.  Use the
            // stream-based methods on ChannelSftp once they work properly
            final ByteArrayOutputStream outstr = new ByteArrayOutputStream();
            try
            {
                channel.get(getName().getPathDecoded(), outstr, null, ChannelSftp.RESUME, filePointer);
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
        */
    }

    /**
     * Creates an input stream to read the file content from.
     */
    protected InputStream doGetInputStream()
        throws Exception
    {
        final ChannelSftp channel = fileSystem.getChannel();
        try
        {
            // return channel.get(getName().getPath());
            // hmmm - using the in memory method is soooo much faster ...

            // TODO - Don't read the entire file into memory.  Use the
            // stream-based methods on ChannelSftp once they work properly
            final ByteArrayOutputStream outstr = new ByteArrayOutputStream();
            channel.get(getName().getPathDecoded(), outstr);
            outstr.close();
            return new ByteArrayInputStream(outstr.toByteArray());

        }
        finally
        {
            fileSystem.putChannel(channel);
        }
    }

    /**
     * Creates an output stream to write the file content to.
     */
    protected OutputStream doGetOutputStream(boolean bAppend)
        throws Exception
    {
        // TODO - Don't write the entire file into memory.  Use the stream-based
        // methods on ChannelSftp once the work properly
        final ChannelSftp channel = fileSystem.getChannel();
        return new SftpOutputStream(channel);
    }

    /**
     * An OutputStream that wraps an sftp OutputStream, and closes the channel
     * when the stream is closed.
     */
    private class SftpOutputStream
        extends MonitorOutputStream
    {
        private final ChannelSftp channel;

        public SftpOutputStream(final ChannelSftp channel)
        {
            super(new ByteArrayOutputStream());
            this.channel = channel;
        }

        /**
         * Called after this stream is closed.
         */
        protected void onClose()
            throws IOException
        {
            try
            {
                final ByteArrayOutputStream outstr = (ByteArrayOutputStream) out;
                channel.put(new ByteArrayInputStream(outstr.toByteArray()),
                    getName().getPathDecoded());
            }
            catch (final SftpException e)
            {
                throw new FileSystemException(e);
            }
            finally
            {
                fileSystem.putChannel(channel);
            }
        }
    }
}
