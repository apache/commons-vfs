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
package org.apache.commons.vfs.provider;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;

/**
 * A {@link FileProvider} that handles physical files, such as the files in a
 * local fs, or on an FTP server.  An originating file system cannot be
 * layered on top of another file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public abstract class AbstractOriginatingFileProvider
    extends AbstractFileProvider
{
    public AbstractOriginatingFileProvider()
    {
        super();
    }

    /**
     * Locates a file object, by absolute URI.
     *
     * @param uri
     */
    public FileObject findFile(final FileObject baseFile,
                               final String uri,
                               final FileSystemOptions fileSystemOptions) throws FileSystemException
    {
        // Parse the URI
        final FileName name;
        try
        {
            name = parseUri(uri);
        }
        catch (FileSystemException exc)
        {
            throw new FileSystemException("vfs.provider/invalid-absolute-uri.error", uri, exc);
        }

        // Locate the file
        return findFile(name, fileSystemOptions);
    }

    /**
     * Locates a file from its parsed URI.
     */
    protected synchronized FileObject findFile(final FileName name, final FileSystemOptions fileSystemOptions)
        throws FileSystemException
    {
        // Check in the cache for the file system
        final FileName rootName = name.resolveName(FileName.ROOT_PATH);
        FileSystem fs = findFileSystem(rootName, fileSystemOptions);
        if (fs == null)
        {
            // Need to create the file system, and cache it
            fs = doCreateFileSystem(rootName, fileSystemOptions);
            addFileSystem(rootName, fs);
        }

        // Locate the file
        return fs.resolveFile(name.getPath());
    }

    /**
     * Parses an absolute URI.
     *
     * @return The name of the file.  This name is used to locate the file
     *         system in the cache, using the root URI.  This name is also
     *         passed to {@link #doCreateFileSystem} to create the file system.
     */
    protected abstract FileName parseUri(final String uri)
        throws FileSystemException;

    /**
     * Creates a {@link FileSystem}.  If the returned FileSystem implements
     * {@link VfsComponent}, it will be initialised.
     *
     * @param rootName The name of the root file of the file system to create.
     */
    protected abstract FileSystem doCreateFileSystem(final FileName rootName, final FileSystemOptions fileSystemOptions)
        throws FileSystemException;
}
