/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs;

import org.apache.avalon.excalibur.i18n.ResourceManager;
import org.apache.avalon.excalibur.i18n.Resources;
import org.apache.commons.vfs.impl.DefaultFileReplicator;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.impl.PrivilegedFileReplicator;
import org.apache.commons.vfs.provider.FileProvider;
import org.apache.commons.vfs.provider.FileReplicator;

/**
 * A static factory for {@link FileSystemManager} instances.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.3 $ $Date: 2002/08/21 07:00:10 $
 */
public class FileSystemManagerFactory
{
    private static final Resources REZ =
        ResourceManager.getPackageResources( FileSystemManagerFactory.class );
    private static FileSystemManager instance;

    private FileSystemManagerFactory()
    {
    }

    /**
     * Returns the default {@link FileSystemManager} instance.
     */
    public synchronized static FileSystemManager getManager()
        throws FileSystemException
    {
        if ( instance == null )
        {
            instance = doCreateManager();
        }
        return instance;
    }

    /**
     * Creates a file system manager instance.
     *
     * @todo Load manager config from a file.
     * @todo Ignore missing providers.
     */
    private static FileSystemManager doCreateManager() throws FileSystemException
    {
        final DefaultFileSystemManager mgr = new DefaultFileSystemManager();

        // Set the replicator
        FileReplicator replicator = new DefaultFileReplicator();
        replicator = new PrivilegedFileReplicator( replicator );
        mgr.setReplicator( replicator );

        // Add the default providers
        FileProvider provider = createProvider( "org.apache.commons.vfs.provider.local.DefaultLocalFileSystemProvider" );
        mgr.addProvider( "file", provider );
        provider = createProvider( "org.apache.commons.vfs.provider.zip.ZipFileSystemProvider" );
        mgr.addProvider( "zip", provider );
        mgr.addProvider( "jar", provider );
        provider = createProvider( "org.apache.commons.vfs.provider.ftp.FtpFileSystemProvider" );
        mgr.addProvider( "ftp", provider );
        provider = createProvider( "org.apache.commons.vfs.provider.smb.SmbFileSystemProvider" );
        mgr.addProvider( "smb", provider );
        provider = createProvider( "org.apache.commons.vfs.provider.url.UrlFileProvider" );
        mgr.setDefaultProvider( provider );

        return mgr;
    }

    /**
     * Creates a provider.
     */
    private static FileProvider createProvider( final String providerClassName )
        throws FileSystemException
    {
        try
        {
            final Class providerClass = Class.forName( providerClassName );
            return (FileProvider)providerClass.newInstance();
        }
        catch ( final Exception e )
        {
            final String message = REZ.getString( "create-provider.error", providerClassName );
            throw new FileSystemException( message, e );
        }
    }
}
