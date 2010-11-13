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
package org.apache.commons.vfs2.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VfsLog;
import org.apache.commons.vfs2.provider.AbstractVfsComponent;
import org.apache.commons.vfs2.provider.FileReplicator;
import org.apache.commons.vfs2.provider.TemporaryFileStore;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.util.Messages;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

/**
 * A simple file replicator and temporary file store.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class DefaultFileReplicator
    extends AbstractVfsComponent
    implements FileReplicator, TemporaryFileStore
{
    private static final char[] TMP_RESERVED_CHARS = new char[]
        {
            '?', '/', '\\', ' ', '&', '"', '\'', '*', '#', ';', ':', '<', '>', '|'
        };
    private static final Log log = LogFactory.getLog(DefaultFileReplicator.class);

    private static final int MASK = 0xffff;

    private final ArrayList<Object> copies = new ArrayList<Object>();
    private File tempDir;
    private long filecount;
    private boolean tempDirMessageLogged;

    /**
     * constructor to set the location of the temporary directory.
     *
     * @param tempDir The temporary directory.
     */
    public DefaultFileReplicator(final File tempDir)
    {
        this.tempDir = tempDir;
    }

    public DefaultFileReplicator()
    {
    }

    /**
     * Initialises this component.
     * @throws FileSystemException if an error occurs.
     */
    @Override
    public void init() throws FileSystemException
    {
        if (tempDir == null)
        {
            String baseTmpDir = System.getProperty("java.io.tmpdir");

            tempDir = new File(baseTmpDir, "vfs_cache").getAbsoluteFile();
        }

        filecount = new Random().nextInt() & MASK;

        if (!tempDirMessageLogged)
        {
            final String message = Messages.getString("vfs.impl/temp-dir.info", tempDir);
            VfsLog.info(getLogger(), log, message);

            tempDirMessageLogged = true;
        }
    }

    /**
     * Closes the replicator, deleting all temporary files.
     */
    @Override
    public void close()
    {
        // Delete the temporary files
        synchronized (copies)
        {
            while (copies.size() > 0)
            {
                final File file = (File) removeFile();
                deleteFile(file);
            }
        }

        // Clean up the temp directory, if it is empty
        if (tempDir != null && tempDir.exists() && tempDir.list().length == 0)
        {
            tempDir.delete();
            tempDir = null;
        }
    }

    /**
     * physically deletes the file from the filesystem
     * @param file The File to delete.
     */
    protected void deleteFile(File file)
    {
        try
        {
            final FileObject fileObject = getContext().toFileObject(file);
            fileObject.delete(Selectors.SELECT_ALL);
        }
        catch (final FileSystemException e)
        {
            final String message = Messages.getString("vfs.impl/delete-temp.warn", file.getName());
            VfsLog.warn(getLogger(), log, message, e);
        }
    }

    /**
     * removes a file from the copies list. Will be used for cleanup. <br/>
     * Notice: The system awaits that the returning object can be cast to a java.io.File
     * @return the File that was removed.
     */
    protected Object removeFile()
    {
        synchronized (copies)
        {
            return copies.remove(0);
        }
    }

    /**
     * removes a instance from the list of copies
     * @param file The File to remove.
     */
    protected void removeFile(Object file)
    {
        synchronized (copies)
        {
            copies.remove(file);
        }
    }

    /**
     * Allocates a new temporary file.
     * @param baseName the base file name.
     * @return The created File.
     * @throws FileSystemException if an error occurs.
     */
    public File allocateFile(final String baseName) throws FileSystemException
    {
        // Create a unique-ish file name
        final String basename = createFilename(baseName);
        synchronized (this)
        {
            filecount++;
        }

        return createAndAddFile(tempDir, basename);
    }

    protected File createAndAddFile(final File parent, final String basename) throws FileSystemException
    {
        final File file = createFile(tempDir, basename);

        // Keep track to delete later
        addFile(file);

        return file;
    }

    protected void addFile(Object file)
    {
        synchronized (copies)
        {
            copies.add(file);
        }
    }

    protected long getFilecount()
    {
        return filecount;
    }

    /**
     * create the temporary file name
     * @param baseName The base to prepend to the file name being created.
     * @return the name of the File.
     */
    protected String createFilename(final String baseName)
    {
        // BUG29007
        // return baseName + "_" + getFilecount() + ".tmp";

        // imario@apache.org: BUG34976 get rid of maybe reserved and dangerous characters
        // e.g. to allow replication of http://hostname.org/fileservlet?file=abc.txt
        String safeBasename = UriParser.encode(baseName, TMP_RESERVED_CHARS).replace('%', '_');
        return "tmp_" + getFilecount() + "_" + safeBasename;
    }

    /**
     * create the temporary file
     * @param parent The file to use as the parent of the file being created.
     * @param name The name of the file to create.
     * @return The File that was created.
     * @throws FileSystemException if an error occurs creating the file.
     */
    protected File createFile(final File parent, final String name) throws FileSystemException
    {
        return new File(parent, UriParser.decode(name));
    }

    /**
     * Creates a local copy of the file, and all its descendents.
     * @param srcFile The file to copy.
     * @param selector The FileSelector.
     * @return the created File.
     * @throws FileSystemException if an error occurs copying the file.
     */
    public File replicateFile(final FileObject srcFile,
                              final FileSelector selector)
        throws FileSystemException
    {
        final String basename = srcFile.getName().getBaseName();
        final File file = allocateFile(basename);

        // Copy from the source file
        final FileObject destFile = getContext().toFileObject(file);
        destFile.copyFrom(srcFile, selector);

        return file;
    }
}
