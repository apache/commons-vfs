/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.impl;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Random;
import org.apache.avalon.excalibur.i18n.ResourceManager;
import org.apache.avalon.excalibur.i18n.Resources;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.FileReplicator;

/**
 * A simple file replicator.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.2 $ $Date: 2002/07/05 04:08:18 $
 */
public final class DefaultFileReplicator
    implements FileReplicator
{
    private static final Resources REZ =
        ResourceManager.getPackageResources( DefaultFileReplicator.class );

    private static long filecount = -1;

    private final DefaultFileSystemManager m_manager;
    private final File m_tempDir;
    private final ArrayList m_copies = new ArrayList();

    public DefaultFileReplicator( final DefaultFileSystemManager manager )
    {
        m_manager = manager;
        m_tempDir = (File) AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    return new File( "vfs_cache" ).getAbsoluteFile();
                }
            } );
    }

    /**
     * Closes the replicator, deleting the temporary files.
     */
    public void close()
    {
    /*
        AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run()
                {
                    while( m_copies.size() > 0 )
                    {
                        final FileObject file = (FileObject)m_copies.remove( 0 );
                        try
                        {
                            file.delete( FileConstants.SELECT_ALL );
                        }
                        catch( final FileSystemException e )
                        {
                            final String message = REZ.getString( "delete-temp.warn", file.getName() );
                            getLogger().warn( message, e );
                        }
                    }
                    return null;
                }
            } );
      */      
    }

    protected static File generateTempFile( String prefix, File tempDir )
    {
        if( filecount == -1 )
        {
            filecount = new Random().nextInt() & 0xffff;
        }
        // Create a unique-ish file name
        final String basename = prefix + "_"+ filecount + ".tmp";
        filecount++;
        return  new File( tempDir, basename );
    }

    /**
     * Creates a local copy of the file, and all its descendents.
     */
    public File replicateFile( final FileObject srcFile,
                               final FileSelector selector )
        throws FileSystemException
    {
        final String basename = srcFile.getName().getBaseName();
        final File file = generateTempFile( basename, m_tempDir );
        try
        {
            // Copy from the source file
            final FileObject destFile = m_manager.convert( file );
            AccessController.doPrivileged(
                new PrivilegedExceptionAction() {
                    public Object run() throws FileSystemException {
                        destFile.copyFrom( srcFile, selector );
                        return null;
                }
            } );

            // Keep track of the copy
            m_copies.add( destFile );
        }
        catch( final PrivilegedActionException e )
        {
            final String message = REZ.getString( "replicate-file.error", srcFile.getName(), file );
            throw new FileSystemException( message, e );
        }

        return file;
    }

}
