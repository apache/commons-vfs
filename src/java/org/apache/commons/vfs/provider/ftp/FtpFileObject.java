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
package org.apache.commons.vfs.provider.ftp;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.RandomAccessContent;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.provider.UriParser;
import org.apache.commons.vfs.util.MonitorInputStream;
import org.apache.commons.vfs.util.MonitorOutputStream;
import org.apache.commons.vfs.util.RandomAccessMode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * An FTP file.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class FtpFileObject
    extends AbstractFileObject
{
    private static final FTPFile[] EMPTY_FTP_FILE_ARRAY = {};

    private final FtpFileSystem ftpFs;
    private final String relPath;

    // Cached info
    private FTPFile fileInfo;
    private FTPFile[] children;
    private FileObject linkDestination;

    protected FtpFileObject(final FileName name,
                            final FtpFileSystem fileSystem,
                            final FileName rootName)
        throws FileSystemException
    {
        super(name, fileSystem);
        ftpFs = fileSystem;
        String relPath = UriParser.decode(rootName.getRelativeName(name));
        if (".".equals(relPath))
        {
            // do not use the "." as path against the ftp-server
            // e.g. the uu.net ftp-server do a recursive listing then
            // this.relPath = UriParser.decode(rootName.getPath());
            // this.relPath = ".";
            this.relPath = null;
        }
        else
        {
            this.relPath = relPath;
        }
    }

    /**
     * Called by child file objects, to locate their ftp file info.
     *
     * @param name  the filename in its native form ie. without uri stuff (%nn)
     * @param flush recreate children cache
     */
    private FTPFile getChildFile(final String name, final boolean flush) throws IOException
    {
        if (flush)
        {
            children = null;
        }

        // List the children of this file
        doGetChildren();

        // Look for the requested child
        // TODO - use hash table
        for (int i = 0; i < children.length; i++)
        {
            final FTPFile child = children[i];
            if (child.getName().equals(name))
            {
                // TODO - should be using something else to compare names
                return child;
            }
        }

        return null;
    }

    /**
     * Fetches the children of this file, if not already cached.
     */
    private void doGetChildren() throws IOException
    {
        if (children != null)
        {
            return;
        }

        final FtpClient client = ftpFs.getClient();
        try
        {
            String key = FtpFileSystemConfigBuilder.getInstance().getEntryParser(getFileSystem().getFileSystemOptions());
            final FTPFile[] tmpChildren = client.listFiles(key, relPath);
            if (tmpChildren == null || tmpChildren.length == 0)
            {
                children = EMPTY_FTP_FILE_ARRAY;
            }
            else
            {
                // Remove '.' and '..' elements
                final ArrayList childList = new ArrayList();
                for (int i = 0; i < tmpChildren.length; i++)
                {
                    final FTPFile child = tmpChildren[i];
                    if (!".".equals(child.getName())
                        && !"..".equals(child.getName()))
                    {
                        childList.add(child);
                    }
                }
                children = (FTPFile[]) childList.toArray(new FTPFile[childList.size()]);
            }
        }
        finally
        {
            ftpFs.putClient(client);
        }
    }

    /**
     * Attaches this file object to its file resource.
     */
    protected void doAttach()
        throws IOException
    {
        // Get the parent folder to find the info for this file
        getInfo(false);
    }

    /**
     * Fetches the info for this file.
     */
    private void getInfo(boolean flush) throws IOException
    {
        final FtpFileObject parent = (FtpFileObject) getParent();
        if (parent != null)
        {
            fileInfo = parent.getChildFile(UriParser.decode(getName().getBaseName()), flush);
        }
        else
        {
            // Assume the root is a directory and exists
            fileInfo = new FTPFile();
            fileInfo.setType(FTPFile.DIRECTORY_TYPE);
        }
    }

    /**
     * Detaches this file object from its file resource.
     */
    protected void doDetach()
    {
        fileInfo = null;
        children = null;
    }

    /**
     * Called when the children of this file change.
     */
    protected void onChildrenChanged()
    {
        children = null;
    }

    /**
     * Called when the type or content of this file changes.
     */
    protected void onChange() throws IOException
    {
        children = null;
        getInfo(true);
    }

    /**
     * Determines the type of the file, returns null if the file does not
     * exist.
     */
    protected FileType doGetType()
        throws Exception
    {
        if (fileInfo == null)
        {
            return FileType.IMAGINARY;
        }
        else if (fileInfo.isDirectory())
        {
            return FileType.FOLDER;
        }
        else if (fileInfo.isFile())
        {
            return FileType.FILE;
        }
        else if (fileInfo.isSymbolicLink())
        {
            return getLinkDestination().getType();
        }

        throw new FileSystemException("vfs.provider.ftp/get-type.error", getName());
    }

    private FileObject getLinkDestination() throws FileSystemException
    {
        if (linkDestination == null)
        {
            final String path = fileInfo.getLink();
            FileName relativeTo = getName().getParent();
            if (relativeTo == null)
            {
                relativeTo = getName();
            }
            FileName linkDestinationName = getFileSystem().getFileSystemManager().resolveName(relativeTo, path);
            linkDestination = getFileSystem().resolveFile(linkDestinationName);
        }

        return linkDestination;
    }

    protected FileObject[] doListChildrenResolved() throws Exception
    {
        if (fileInfo.isSymbolicLink())
        {
            return getLinkDestination().getChildren();
        }

        return null;
    }

    /**
     * Lists the children of the file.
     */
    protected String[] doListChildren()
        throws Exception
    {
        // List the children of this file
        doGetChildren();

        final String[] childNames = new String[children.length];
        for (int i = 0; i < children.length; i++)
        {
            final FTPFile child = children[i];
            childNames[i] = child.getName();
        }

        return UriParser.encode(childNames);
    }

    /**
     * Deletes the file.
     */
    protected void doDelete() throws Exception
    {
        final boolean ok;
        final FtpClient ftpClient = ftpFs.getClient();
        try
        {
            if (fileInfo.isDirectory())
            {
                ok = ftpClient.removeDirectory(relPath);
            }
            else
            {
                ok = ftpClient.deleteFile(relPath);
            }
        }
        finally
        {
            ftpFs.putClient(ftpClient);
        }

        if (!ok)
        {
            throw new FileSystemException("vfs.provider.ftp/delete-file.error", getName());
        }
        fileInfo = null;
        children = EMPTY_FTP_FILE_ARRAY;
    }

    /**
     * Renames the file
     */
    protected void doRename(FileObject newfile) throws Exception
    {
        final boolean ok;
        final FtpClient ftpClient = ftpFs.getClient();
        try
        {
            String oldName = getName().getPath();
            String newName = newfile.getName().getPath();
            ok = ftpClient.rename(oldName, newName);
        }
        finally
        {
            ftpFs.putClient(ftpClient);
        }

        if (!ok)
        {
            throw new FileSystemException("vfs.provider.ftp/rename-file.error", new Object[]{getName().toString(), newfile});
        }
        fileInfo = null;
        children = EMPTY_FTP_FILE_ARRAY;
    }

    /**
     * Creates this file as a folder.
     */
    protected void doCreateFolder()
        throws Exception
    {
        final boolean ok;
        final FtpClient client = ftpFs.getClient();
        try
        {
            ok = client.makeDirectory(relPath);
        }
        finally
        {
            ftpFs.putClient(client);
        }

        if (!ok)
        {
            throw new FileSystemException("vfs.provider.ftp/create-folder.error", getName());
        }
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    protected long doGetContentSize() throws Exception
    {
        if (fileInfo.isSymbolicLink())
        {
            return getLinkDestination().getContent().getSize();
        }
        else
        {
            return fileInfo.getSize();
        }
    }

    /**
     * get the last modified time on an ftp file
     *
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doGetLastModifiedTime()
     */
    protected long doGetLastModifiedTime() throws Exception
    {
        if (fileInfo.isSymbolicLink())
        {
            return getLinkDestination().getContent().getLastModifiedTime();
        }
        else
        {
            Calendar timestamp = fileInfo.getTimestamp();
            if (timestamp == null)
            {
                return 0L;
            }
            else
            {
                return (timestamp.getTime().getTime());
            }
        }
    }

    /**
     * get the last modified time on an ftp file
     *
     * @param modtime the time to set on the ftp file
     * @see org.apache.commons.vfs.provider.AbstractFileObject#doSetLastModifiedTime(long)
     */
    protected void doSetLastModifiedTime(final long modtime) throws Exception
    {
        if (fileInfo.isSymbolicLink())
        {
            getLinkDestination().getContent().setLastModifiedTime(modtime);
        }
        else
        {
            final Date d = new Date(modtime);
            final Calendar c = new GregorianCalendar();
            c.setTime(d);
            fileInfo.setTimestamp(c);
        }
    }

    /**
     * Creates an input stream to read the file content from.
     */
    protected InputStream doGetInputStream() throws Exception
    {
        final FtpClient client = ftpFs.getClient();
        final InputStream instr = client.retrieveFileStream(relPath);
        return new FtpInputStream(client, instr);
    }

    protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception
    {
        return new FtpRandomAccessContent(this, mode);
    }

    /**
     * Creates an output stream to write the file content to.
     */
    protected OutputStream doGetOutputStream(boolean bAppend)
        throws Exception
    {
        final FtpClient client = ftpFs.getClient();
        if (bAppend)
        {
            return new FtpOutputStream(client, client.appendFileStream(relPath));
        }
        else
        {
            return new FtpOutputStream(client, client.storeFileStream(relPath));
        }
    }

    String getRelPath()
    {
        return relPath;
    }

    FtpInputStream getInputStream(long filePointer) throws IOException
    {
        final FtpClient client = ftpFs.getClient();
        final InputStream instr = client.retrieveFileStream(relPath, filePointer);
        return new FtpInputStream(client, instr);
    }

    /**
     * An InputStream that monitors for end-of-file.
     */
    class FtpInputStream
        extends MonitorInputStream
    {
        private final FtpClient client;

        public FtpInputStream(final FtpClient client, final InputStream in)
        {
            super(in);
            this.client = client;
        }

        void abort() throws IOException
        {
            client.abort();
            close();
        }

        /**
         * Called after the stream has been closed.
         */
        protected void onClose() throws IOException
        {
            final boolean ok;
            try
            {
                ok = client.completePendingCommand();
            }
            finally
            {
                ftpFs.putClient(client);
            }

            if (!ok)
            {
                throw new FileSystemException("vfs.provider.ftp/finish-get.error", getName());
            }
        }
    }

    /**
     * An OutputStream that monitors for end-of-file.
     */
    private class FtpOutputStream
        extends MonitorOutputStream
    {
        private final FtpClient client;

        public FtpOutputStream(final FtpClient client, final OutputStream outstr)
        {
            super(outstr);
            this.client = client;
        }

        /**
         * Called after this stream is closed.
         */
        protected void onClose() throws IOException
        {
            final boolean ok;
            try
            {
                ok = client.completePendingCommand();
            }
            finally
            {
                ftpFs.putClient(client);
            }

            if (!ok)
            {
                throw new FileSystemException("vfs.provider.ftp/finish-put.error", getName());
            }
        }
    }
}
