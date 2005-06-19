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
package org.apache.commons.vfs.provider;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.local.GenericFileNameParser;

import java.util.Iterator;
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
     * The cached file systems.  This is a mapping from root URI to
     * FileSystem object.
     */
    // private final Map fileSystems = new HashMap();
    private final Map fileSystems = new TreeMap();

    /**
     * Closes the file systems created by this provider.
     */
    public void close()
    {
        synchronized (fileSystems)
        {
            fileSystems.clear();
            super.close();
        }
    }

    /**
     * Creates a layered file system.  This method throws a 'not supported' exception.
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
        synchronized (fileSystems)
        {
            // Add to the cache
            addComponent(fs);

            FileSystemKey treeKey = new FileSystemKey(key, fs.getFileSystemOptions());
            ((AbstractFileSystem) fs).setCacheKey(treeKey);
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
        synchronized (fileSystems)
        {
            FileSystemKey treeKey = new FileSystemKey(key, fileSystemProps);

            return (FileSystem) fileSystems.get(treeKey);
        }
    }

    public FileSystemConfigBuilder getConfigBuilder()
    {
        return null;
    }

    public void freeUnusedResources()
    {
        synchronized (fileSystems)
        {
            Iterator iterFileSystems = fileSystems.values().iterator();
            while (iterFileSystems.hasNext())
            {
                AbstractFileSystem fs = (AbstractFileSystem) iterFileSystems.next();
                if (fs.isReleaseable())
                {
                    fs.closeCommunicationLink();
                }
            }
        }
    }

    public void closeFileSystem(final FileSystem filesystem)
    {
        synchronized (fileSystems)
        {
            AbstractFileSystem fs = (AbstractFileSystem) filesystem;

            fileSystems.remove(fs.getCacheKey());
            removeComponent(fileSystems);
            fs.close();
        }
    }

    /**
     * Parses an absolute URI.
     *
     * @param base The base file - if null the <code>uri</code> needs to be absolute
     * @param uri The URI to parse.
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
