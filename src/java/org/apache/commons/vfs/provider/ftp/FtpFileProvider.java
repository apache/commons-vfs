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
package org.apache.commons.vfs.provider.ftp;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs.provider.GenericFileName;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * A provider for FTP file systems.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public class FtpFileProvider
    extends AbstractOriginatingFileProvider
{
    public final static String ATTR_FILE_ENTRY_PARSER = "FEP";
    private static final int DEFAULT_PORT = 21;

    final static Collection capabilities = Collections.unmodifiableCollection(Arrays.asList(new Capability[]
    {
        Capability.CREATE,
        Capability.DELETE,
        Capability.RENAME,
        Capability.GET_TYPE,
        Capability.LIST_CHILDREN,
        Capability.READ_CONTENT,
        Capability.SET_LAST_MODIFIED_FILE,
        Capability.SET_LAST_MODIFIED_FOLDER,
        Capability.GET_LAST_MODIFIED,
        Capability.URI,
        Capability.WRITE_CONTENT,
        Capability.APPEND_CONTENT,
        Capability.RANDOM_ACCESS_READ,
    }));

    public FtpFileProvider()
    {
        super();
    }

    /**
     * Parses a URI.
     */
    protected FileName parseUri(final String uri)
        throws FileSystemException
    {
        return GenericFileName.parseUri(uri, DEFAULT_PORT);
    }

    /**
     * Creates the filesystem.
     */
    protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions)
        throws FileSystemException
    {
        // Create the file system
        final GenericFileName rootName = (GenericFileName) name;

        FTPClientWrapper ftpClient = new FTPClientWrapper(rootName, fileSystemOptions);
        /*
        FTPClient ftpClient = FtpClientFactory.createConnection(rootName.getHostName(),
            rootName.getPort(),
            rootName.getUserName(),
            rootName.getPassword(),
            rootName.getPath(),
            fileSystemOptions);
        */

        return new FtpFileSystem(rootName, ftpClient, fileSystemOptions);
    }

    public FileSystemConfigBuilder getConfigBuilder()
    {
        return FtpFileSystemConfigBuilder.getInstance();
    }

    public Collection getCapabilities()
    {
        return capabilities;
    }
}
