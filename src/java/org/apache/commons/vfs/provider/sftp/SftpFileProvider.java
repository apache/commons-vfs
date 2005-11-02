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
package org.apache.commons.vfs.provider.sftp;

import com.jcraft.jsch.Session;
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
 * A provider for accessing files over SFTP.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @author Gary D. Gregory
 * @version $Id$
 */
public class SftpFileProvider extends AbstractOriginatingFileProvider
{
    protected final static Collection capabilities = Collections.unmodifiableCollection(Arrays.asList(new Capability[]
    {
        Capability.CREATE,
        Capability.DELETE,
        Capability.RENAME,
        Capability.GET_TYPE,
        Capability.LIST_CHILDREN,
        Capability.READ_CONTENT,
        Capability.URI,
        Capability.WRITE_CONTENT,
        Capability.GET_LAST_MODIFIED,
        Capability.SET_LAST_MODIFIED_FILE,
        Capability.RANDOM_ACCESS_READ
    }));

    public final static String ATTR_USER_INFO = "UI";

    // private JSch jSch = new JSch();

    public SftpFileProvider()
    {
        super();
        setFileNameParser(SftpFileNameParser.getInstance());
    }

    /**
     * Creates a {@link FileSystem}.
     */
    protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions) throws FileSystemException
    {
        // JSch jsch = createJSch(fileSystemOptions);

        // Create the file system
        final GenericFileName rootName = (GenericFileName) name;

        Session session;
        try
        {
            session = SftpClientFactory.createConnection(rootName.getHostName(),
                rootName.getPort(),
                rootName.getUserName(),
                rootName.getPassword(),
                fileSystemOptions);
        }
        catch (final Exception e)
        {
            throw new FileSystemException("vfs.provider.sftp/connect.error",
                name,
                e);
        }

        return new SftpFileSystem(rootName, session, fileSystemOptions);
    }


    /**
     * Returns the JSch.
     *
     * @return Returns the jSch.
     */
    /*
    private JSch getJSch()
    {
        return this.jSch;
    }
    */

    /**
     * Initialises the component.
     */
    public void init() throws FileSystemException
    {
    }

    public FileSystemConfigBuilder getConfigBuilder()
    {
        return SftpFileSystemConfigBuilder.getInstance();
    }

    public Collection getCapabilities()
    {
        return capabilities;
    }
}