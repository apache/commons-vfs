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
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs.provider.LocalFileProvider;
import org.apache.commons.vfs.util.Os;

import java.io.File;

/**
 * A file system provider, which uses direct file access.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.5 $ $Date: 2004/05/03 19:48:47 $
 */
public final class DefaultLocalFileProvider
    extends AbstractOriginatingFileProvider
    implements LocalFileProvider
{
    private final LocalFileNameParser parser;

    public DefaultLocalFileProvider(FileSystemManager manager)
    {
        super(manager);

        if (Os.isFamily(Os.OS_FAMILY_WINDOWS))
        {
            parser = new WindowsFileNameParser();
        }
        else
        {
            parser = new GenericFileNameParser();
        }
    }

    /**
     * Determines if a name is an absolute file name.
     */
    public boolean isAbsoluteLocalName(final String name)
    {
        return parser.isAbsoluteName(name);
    }

    /**
     * Finds a local file, from its local name.
     */
    public FileObject findLocalFile(final String name)
        throws FileSystemException
    {
        // TODO - tidy this up, no need to turn the name into an absolute URI,
        // and then straight back again
        return findFile(null, "file:" + name, null);
    }

    /**
     * Finds a local file.
     */
    public FileObject findLocalFile(final File file)
        throws FileSystemException
    {
        // TODO - tidy this up, should build file object straight from the file
        return findFile(null, "file:" + file.getAbsolutePath(), null);
    }

    /**
     * Parses a URI.
     */
    protected FileName parseUri(final String uri)
        throws FileSystemException
    {
        return LocalFileName.parseUri(uri, parser);
    }

    /**
     * Creates the filesystem.
     */
    protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions)
        throws FileSystemException
    {
        // Create the file system
        final LocalFileName rootName = (LocalFileName) name;
        return new LocalFileSystem(getFileSystemManager(), rootName, rootName.getRootFile());
    }
}
