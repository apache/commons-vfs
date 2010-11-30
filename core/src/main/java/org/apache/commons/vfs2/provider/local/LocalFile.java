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
package org.apache.commons.vfs2.provider.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.util.FileObjectUtils;
import org.apache.commons.vfs2.util.RandomAccessMode;

/**
 * A file object implementation which uses direct file access.
 *
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 * @version $Revision$ $Date$
 */
public class LocalFile extends AbstractFileObject implements FileObject
{
    private final String rootFile;

    private File file;

    /**
     * Creates a non-root file.
     */
    protected LocalFile(final LocalFileSystem fileSystem,
                        final String rootFile,
                        final AbstractFileName name) throws FileSystemException
    {
        super(name, fileSystem);
        this.rootFile = rootFile;
    }

    /**
     * Returns the local file that this file object represents.
     */
    protected File getLocalFile()
    {
        return file;
    }

    /**
     * Attaches this file object to its file resource.
     */
    @Override
    protected void doAttach() throws Exception
    {
        if (file == null)
        {
            // Remove the "file:///"
            // LocalFileName localFileName = (LocalFileName) getName();
            String fileName = rootFile + getName().getPathDecoded();
            // fileName = UriParser.decode(fileName);
            file = new File(fileName);
        }
    }

    /**
     * Returns the file's type.
     */
    @Override
    protected FileType doGetType() throws Exception
    {
        // JDK BUG: 6192331
        // if (!file.exists())
        if (!file.exists() && file.length() < 1)
        {
            return FileType.IMAGINARY;
        }

        if (file.isDirectory())
        {
            return FileType.FOLDER;
        }

        // In doubt, treat an existing file as file
        // if (file.isFile())
        // {
            return FileType.FILE;
        // }

        // throw new FileSystemException("vfs.provider.local/get-type.error", file);
    }

    /**
     * Returns the children of the file.
     */
    @Override
    protected String[] doListChildren() throws Exception
    {
        return UriParser.encode(file.list());
    }

    /**
     * Deletes this file, and all children.
     */
    @Override
    protected void doDelete() throws Exception
    {
        if (!file.delete())
        {
            throw new FileSystemException("vfs.provider.local/delete-file.error", file);
        }
    }

    /**
     * rename this file
     */
    @Override
    protected void doRename(final FileObject newfile) throws Exception
    {
        LocalFile newLocalFile = (LocalFile) FileObjectUtils.getAbstractFileObject(newfile);

        if (!file.renameTo(newLocalFile.getLocalFile()))
        {
            throw new FileSystemException("vfs.provider.local/rename-file.error",
                new String[]{file.toString(), newfile.toString()});
        }
    }

    /**
     * Creates this folder.
     */
    @Override
    protected void doCreateFolder() throws Exception
    {
        if (!file.mkdirs())
        {
            throw new FileSystemException("vfs.provider.local/create-folder.error", file);
        }
    }

    /**
     * Determines if this file can be written to.
     */
    @Override
    protected boolean doIsWriteable() throws FileSystemException
    {
        return file.canWrite();
    }

    /**
     * Determines if this file is hidden.
     */
    @Override
    protected boolean doIsHidden()
    {
        return file.isHidden();
    }

    /**
     * Determines if this file can be read.
     */
    @Override
    protected boolean doIsReadable() throws FileSystemException
    {
        return file.canRead();
    }

    /**
     * Gets the last modified time of this file.
     */
    @Override
    protected long doGetLastModifiedTime() throws FileSystemException
    {
        return file.lastModified();
    }

    /**
     * Sets the last modified time of this file.
     * @since 2.0
     */
    @Override
    protected boolean doSetLastModifiedTime(final long modtime) throws FileSystemException
    {
        return file.setLastModified(modtime);
    }

    /**
     * Creates an input stream to read the content from.
     */
    @Override
    protected InputStream doGetInputStream() throws Exception
    {
        return new FileInputStream(file);
    }

    /**
     * Creates an output stream to write the file content to.
     */
    @Override
    protected OutputStream doGetOutputStream(boolean bAppend)
        throws Exception
    {
        return new FileOutputStream(file.getPath(), bAppend);
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    @Override
    protected long doGetContentSize() throws Exception
    {
        return file.length();
    }

    @Override
    protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception
    {
        return new LocalFileRandomAccessContent(file, mode);
    }

    @Override
    protected boolean doIsSameFile(FileObject destFile) throws FileSystemException
    {
        if (!FileObjectUtils.isInstanceOf(destFile, LocalFile.class))
        {
            return false;
        }

        LocalFile destLocalFile = (LocalFile) FileObjectUtils.getAbstractFileObject(destFile);
        if (!exists() || !destLocalFile.exists())
        {
            return false;
        }

        try
        {
            return file.getCanonicalPath().equals(destLocalFile.file.getCanonicalPath());
        }
        catch (IOException e)
        {
            throw new FileSystemException(e);
        }

    }
}
