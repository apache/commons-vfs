/*
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
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
package org.apache.commons.vfs.provider.local;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.RandomAccessContent;
import org.apache.commons.vfs.provider.AbstractFileObject;
import org.apache.commons.vfs.util.RandomAccessMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A file object implementation which uses direct file access.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @author Gary D. Gregory
 * @version $Revision$ $Date$
 */
public class LocalFile
    extends AbstractFileObject
    implements FileObject
{
    private File file;
    private final String fileName;

    /**
     * Creates a non-root file.
     */
    protected LocalFile(final LocalFileSystem fileSystem,
                        final String fileName,
                        final FileName name)
    {
        super(name, fileSystem);
        this.fileName = fileName;
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
    protected void doAttach()
        throws Exception
    {
        if (file == null)
        {
            file = new File(fileName);
        }
    }

    /**
     * Returns the file's type.
     */
    protected FileType doGetType()
        throws Exception
    {
        if (!file.exists())
        {
            return FileType.IMAGINARY;
        }
        else if (file.isDirectory())
        {
            return FileType.FOLDER;
        }
        else if (file.isFile())
        {
            return FileType.FILE;
        }

        throw new FileSystemException("vfs.provider.local/get-type.error", file);
    }

    /**
     * Returns the children of the file.
     */
    protected String[] doListChildren()
        throws Exception
    {
        return file.list();
    }

    /**
     * Deletes this file, and all children.
     */
    protected void doDelete()
        throws Exception
    {
        if (!file.delete())
        {
            throw new FileSystemException("vfs.provider.local/delete-file.error", file);
        }
    }

    /**
     * rename this file
     */
    protected void doRename(FileObject newfile) throws Exception
    {
        if (!file.renameTo(((LocalFile) newfile).getLocalFile()))
        {
            throw new FileSystemException("vfs.provider.local/rename-file.error",
                new String[]{file.toString(), newfile.toString()});
        }
    }

    /**
     * Creates this folder.
     */
    protected void doCreateFolder()
        throws Exception
    {
        if (!file.mkdirs())
        {
            throw new FileSystemException("vfs.provider.local/create-folder.error", file);
        }
    }

    /**
     * Determines if this file can be written to.
     */
    protected boolean doIsWriteable() throws FileSystemException
    {
        return file.canWrite();
    }

    /**
     * Determines if this file is hidden.
     */
    protected boolean doIsHidden()
    {
        return file.isHidden();
    }

    /**
     * Determines if this file can be read.
     */
    protected boolean doIsReadable() throws FileSystemException
    {
        return file.canRead();
    }

    /**
     * Gets the last modified time of this file.
     */
    protected long doGetLastModifiedTime() throws FileSystemException
    {
        return file.lastModified();
    }

    /**
     * Sets the last modified time of this file.
     */
    protected void doSetLastModifiedTime(final long modtime)
        throws FileSystemException
    {
        file.setLastModified(modtime);
    }

    /**
     * Creates an input stream to read the content from.
     */
    protected InputStream doGetInputStream()
        throws Exception
    {
        return new FileInputStream(file);
    }

    /**
     * Creates an output stream to write the file content to.
     */
    protected OutputStream doGetOutputStream(boolean bAppend)
        throws Exception
    {
        return new FileOutputStream(file.getPath(), bAppend);
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    protected long doGetContentSize()
        throws Exception
    {
        return file.length();
    }

    protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception
    {
        return new LocalFileRandomAccessContent(file, mode);
    }
}
