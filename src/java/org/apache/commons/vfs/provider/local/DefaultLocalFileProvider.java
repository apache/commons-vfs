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

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs.provider.LocalFileProvider;
import org.apache.commons.vfs.util.Os;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * A file system provider, which uses direct file access.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class DefaultLocalFileProvider
    extends AbstractOriginatingFileProvider
    implements LocalFileProvider
{
    public final static Collection capabilities = Collections.unmodifiableCollection(Arrays.asList(new Capability[]
    {
        Capability.CREATE,
        Capability.DELETE,
        Capability.RENAME,
        Capability.GET_TYPE,
        Capability.GET_LAST_MODIFIED,
        Capability.SET_LAST_MODIFIED_FILE,
        Capability.SET_LAST_MODIFIED_FOLDER,
        Capability.LIST_CHILDREN,
        Capability.READ_CONTENT,
        Capability.URI,
        Capability.WRITE_CONTENT,
        Capability.APPEND_CONTENT,
        Capability.RANDOM_ACCESS_READ,
        Capability.RANDOM_ACCESS_WRITE
    }));

    private final LocalFileNameParser parser;

    public DefaultLocalFileProvider()
    {
        super();

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
        StringBuffer uri = new StringBuffer(name.length() + 5);
        uri.append("file:");
        uri.append(name);
        FileName filename = parseUri(uri.toString(), false);
        return findFile(filename, null);
    }

    /**
     * Finds a local file.
     */
    public FileObject findLocalFile(final File file)
        throws FileSystemException
    {
        return findLocalFile(file.getAbsolutePath());
    }

    /**
     * Parses a URI.
     */
    protected FileName parseUri(final String uri, final boolean uriEncoded)
        throws FileSystemException
    {
        return LocalFileName.parseUri(uri, uriEncoded, parser);
    }

    /**
     * Parses a URI.
     */
    protected FileName parseUri(final String uri)
        throws FileSystemException
    {
        return parseUri(uri, true);
    }

    /**
     * Creates the filesystem.
     */
    protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions)
        throws FileSystemException
    {
        // Create the file system
        final LocalFileName rootName = (LocalFileName) name;
        return new LocalFileSystem(rootName, rootName.getRootFile());
    }

    public Collection getCapabilities()
    {
        return capabilities;
    }
}
