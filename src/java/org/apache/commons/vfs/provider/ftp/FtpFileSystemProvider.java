/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.ftp;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystemProvider;
import org.apache.commons.vfs.provider.DefaultFileName;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.provider.ParsedUri;

/**
 * A provider for FTP file systems.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.6 $ $Date: 2002/07/05 04:08:19 $
 *
 * @ant.type type="file-provider" name="ftp"
 */
public final class FtpFileSystemProvider
    extends AbstractFileSystemProvider
{
    private final FtpFileNameParser parser = new FtpFileNameParser();

    /**
     * Parses a URI into its components.
     */
    protected ParsedUri parseUri( final FileObject baseFile,
                                  final String uri )
        throws FileSystemException
    {
        return parser.parseFtpUri( uri );
    }

    /**
     * Creates the filesystem.
     */
    protected FileSystem createFileSystem( final ParsedUri uri )
        throws FileSystemException
    {
        final ParsedFtpUri ftpUri = (ParsedFtpUri)uri;

        // Build the root name
        final FileName rootName = new DefaultFileName( parser, ftpUri.getRootUri(), "/" );

        // Determine the username and password to use
        String username = ftpUri.getUserName();
        if ( username == null )
        {
            username = "anonymous";
        }
        String password = ftpUri.getPassword();
        if ( password == null )
        {
            password = "anonymous";
        }

        // Create the file system
        return new FtpFileSystem( rootName, ftpUri.getHostName(), username, password );
    }
}
