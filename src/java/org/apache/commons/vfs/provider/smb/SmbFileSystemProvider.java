/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.smb;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs.provider.DefaultFileName;
import org.apache.commons.vfs.provider.FileProvider;
import org.apache.commons.vfs.provider.ParsedUri;

/**
 * A provider for SMB (Samba, Windows share) file systems.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.7 $ $Date: 2002/07/05 04:08:19 $
 */
public final class SmbFileSystemProvider
    extends AbstractOriginatingFileProvider
    implements FileProvider
{
    private final SmbFileNameParser parser = new SmbFileNameParser();

    /**
     * Parses a URI into its components.
     */
    protected ParsedUri parseUri( final FileObject baseFile,
                                  final String uri )
        throws FileSystemException
    {
        return parser.parseSmbUri( uri );
    }

    /**
     * Creates the filesystem.
     */
    protected FileSystem doCreateFileSystem( final ParsedUri uri )
        throws FileSystemException
    {
        final ParsedSmbUri smbUri = (ParsedSmbUri)uri;
        final FileName rootName = new DefaultFileName( parser, smbUri.getRootUri(), "/" );
        return new SmbFileSystem( rootName );
    }
}
