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
package org.apache.commons.vfs.provider;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.local.GenericFileNameParser;

import java.util.Map;
import java.util.TreeMap;

/**
 * A partial {@link FileProvider} implementation.  Takes care of managing the
 * file systems created by the provider.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public abstract class AbstractFileProvider
    extends AbstractVfsContainer
    implements FileProvider
{
    /**
     * The cached file systems.  This is a mapping from root URI to
     * FileSystem object.
     */
    // private final Map fileSystems = new HashMap();
    private final Map<FileSystemKey, FileSystem> fileSystems = new TreeMap<FileSystemKey, FileSystem>();

    private FileNameParser parser;

    public AbstractFileProvider()
    {
        parser = GenericFileNameParser.getInstance();
    }

    protected FileNameParser getFileNameParser()
    {
        return parser;
    }

    protected void setFileNameParser(FileNameParser parser)
    {
        this.parser = parser;
    }

    /**
     * Closes the file systems created by this provider.
     */
    @Override
    public void close()
    {
        synchronized (this)
        {
            fileSystems.clear();
        }

        super.close();
    }

    /**
     * Creates a layered file system.  This method throws a 'not supported' exception.
     * @param scheme The protocol to use to access the file.
     * @param file a FileObject.
     * @param properties Options to the file system.
     * @return A FileObject associated with the new FileSystem.
     * @throws FileSystemException if an error occurs.
     */
    public FileObject createFileSystem(final String scheme, final FileObject file, final FileSystemOptions properties)
        throws FileSystemException
    {
        // Can't create a layered file system
        throw new FileSystemException("vfs.provider/not-layered-fs.error", scheme);
    }

    /**
     * Adds a file system to those cached by this provider.  The file system
     * may implement {@link VfsComponent}, in which case it is initialised.
     */
    protected void addFileSystem(final Comparable key, final FileSystem fs)
        throws FileSystemException
    {
        // Add to the cache
        addComponent(fs);

        FileSystemKey treeKey = new FileSystemKey(key, fs.getFileSystemOptions());
        ((AbstractFileSystem) fs).setCacheKey(treeKey);

        synchronized (this)
        {
            fileSystems.put(treeKey, fs);
        }
    }

    /**
     * Locates a cached file system
     *
     * @return The provider, or null if it is not cached.
     */
    protected FileSystem findFileSystem(final Comparable key, final FileSystemOptions fileSystemProps)
    {
        FileSystemKey treeKey = new FileSystemKey(key, fileSystemProps);

        synchronized (this)
        {
            return fileSystems.get(treeKey);
        }
    }

    /**
     * Returns the FileSystemConfigBuidler.
     * @return the FileSystemConfigBuilder.
     */
    public FileSystemConfigBuilder getConfigBuilder()
    {
        return null;
    }

    /**
     * Free unused resources.
     */
    public void freeUnusedResources()
    {
        Object[] item;
        synchronized (this)
        {
            item = fileSystems.values().toArray();
        }
        for (int i = 0; i < item.length; ++i)
        {
            AbstractFileSystem fs = (AbstractFileSystem) item[i];
            if (fs.isReleaseable())
            {
                fs.closeCommunicationLink();
            }
        }
    }

    /**
     * Close the FileSystem.
     * @param filesystem The FileSystem to close.
     */
    public void closeFileSystem(final FileSystem filesystem)
    {
        AbstractFileSystem fs = (AbstractFileSystem) filesystem;

        synchronized (this)
        {
            if (fs.getCacheKey() != null)
            {
                fileSystems.remove(fs.getCacheKey());
            }
        }

        removeComponent(fs);
        fs.close();
    }

    /**
     * Parses an absolute URI.
     *
     * @param base The base file - if null the <code>uri</code> needs to be absolute
     * @param uri The URI to parse.
     * @return The FileName.
     * @throws FileSystemException if an error occurs.
     */
    public FileName parseUri(FileName base, String uri) throws FileSystemException
    {
        if (getFileNameParser() != null)
        {
            return getFileNameParser().parseUri(getContext(), base, uri);
        }

        throw new FileSystemException("vfs.provider/filename-parser-missing.error");
        // return GenericFileName.parseUri(getFileNameParser(), uri, 0);
    }
}
