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
package org.apache.commons.vfs2.provider.zip;

import java.io.InputStream;
import java.util.HashSet;
import java.util.zip.ZipEntry;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;

/**
 * A file in a Zip file system.
 */
public class ZipFileObject extends AbstractFileObject<ZipFileSystem>
{
    /** The ZipEntry. */
    protected ZipEntry entry;
    // causes lots of duplication, create on demand
    private volatile HashSet<String> children = null;

    private FileType type;

    protected ZipFileObject(final AbstractFileName name,
                            final ZipEntry entry,
                            final ZipFileSystem fs,
                            final boolean zipExists) throws FileSystemException
    {
        super(name, fs);
        setZipEntry(entry);
        if (!zipExists)
        {
            type = FileType.IMAGINARY;
        }
    }

    /**
     * Sets the details for this file object.
     * @param entry ZIP information related to this file.
     */
    protected void setZipEntry(final ZipEntry entry)
    {
        if (this.entry != null)
        {
            return;
        }

        if (entry == null || entry.isDirectory())
        {
            type = FileType.FOLDER;
        }
        else
        {
            type = FileType.FILE;
        }

        this.entry = entry;
    }

    /**
     * Attaches a child.
     * <p>
     * TODO: Shouldn't this method have package-only visibility?
     * Cannot change this without breaking binary compatibility.
     *
     * @param childName The name of the child.
     */
    public void attachChild(final FileName childName)
    {
        if (children == null)
            children = new HashSet<String>();
        children.add(childName.getBaseName());
    }

    /**
     * Determines if this file can be written to.
     *
     * @return {@code true} if this file is writeable, {@code false} if not.
     * @throws FileSystemException if an error occurs.
     */
    @Override
    public boolean isWriteable() throws FileSystemException
    {
        return false;
    }

    /**
     * Returns the file's type.
     */
    @Override
    protected FileType doGetType()
    {
        return type;
    }

    /**
     * Lists the children of the file.
     */
    @Override
    protected String[] doListChildren()
    {
        try
        {
            if (!getType().hasChildren())
            {
                return null;
            }
        }
        catch (final FileSystemException e)
        {
            // should not happen as the type has already been cached.
            throw new RuntimeException(e);
        }

        if (children == null)
            return new String[0];
        else
            return children.toArray(new String[children.size()]);
    }

    /**
     * Returns the size of the file content (in bytes).  Is only called if
     * {@link #doGetType} returns {@link FileType#FILE}.
     */
    @Override
    protected long doGetContentSize()
    {
        return entry.getSize();
    }

    /**
     * Returns the last modified time of this file.
     */
    @Override
    protected long doGetLastModifiedTime() throws Exception
    {
        return entry.getTime();
    }

    /**
     * Creates an input stream to read the file content from.  Is only called
     * if  {@link #doGetType} returns {@link FileType#FILE}.  The input stream
     * returned by this method is guaranteed to be closed before this
     * method is called again.
     */
    @Override
    protected InputStream doGetInputStream() throws Exception
    {
        // VFS-210: zip allows to gather an input stream even from a directory and will
        // return -1 on the first read. getType should not be expensive and keeps the tests
        // running
        if (!getType().hasContent())
        {
            throw new FileSystemException("vfs.provider/read-not-file.error", getName());
        }

        return getAbstractFileSystem().getZipFile().getInputStream(entry);
    }
}
