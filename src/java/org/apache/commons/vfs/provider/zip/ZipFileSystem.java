/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.zip;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.avalon.excalibur.i18n.ResourceManager;
import org.apache.avalon.excalibur.i18n.Resources;
import org.apache.commons.vfs.FileConstants;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.DefaultFileName;
import org.apache.commons.vfs.FileSystem;

/**
 * A read-only file system for Zip/Jar files.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.8 $ $Date: 2002/07/05 06:52:16 $
 */
public class ZipFileSystem
    extends AbstractFileSystem
    implements FileSystem
{
    private static final Resources REZ =
        ResourceManager.getPackageResources( ZipFileSystem.class );

    private final File file;
    protected final ZipFile zipFile;

    public ZipFileSystem( final DefaultFileName rootName,
                          final FileObject parentLayer )
        throws FileSystemException
    {
        super( rootName, parentLayer );

        // Make a local copy of the file
        final File file = parentLayer.replicateFile( FileConstants.SELECT_SELF );

        this.file = file;

        // Open the Zip file
        if ( !file.exists() )
        {
            // Don't need to do anything
            zipFile = null;
            return;
        }

        zipFile = createZipFile( this.file );

        // Build the index
        Enumeration entries = zipFile.entries();
        while ( entries.hasMoreElements() )
        {
            ZipEntry entry = (ZipEntry)entries.nextElement();
            FileName name = rootName.resolveName( entry.getName() );

            // Create the file
            ZipFileObject fileObj;
            if ( entry.isDirectory() &&
                 getFile( name ) != null )
            {
                fileObj = (ZipFileObject) getFile( name );
                fileObj.setZipEntry( entry );
                continue;
            }
            
            fileObj = createZipFileObject( name, entry, zipFile );
            putFile( fileObj );

            // Make sure all ancestors exist
            // TODO - create these on demand
            ZipFileObject parent = null;
            for ( FileName parentName = name.getParent();
                  parentName != null;
                  fileObj = parent, parentName = parentName.getParent() )
            {
                // Locate the parent
                parent = (ZipFileObject) getFile( parentName );
                if ( parent == null )
                {
                    parent = createZipFileObject( parentName, null, null );
                    putFile( parent );
                }

                // Attach child to parent
                parent.attachChild( fileObj.getName() );
            }
        }
    }

    protected ZipFileObject createZipFileObject( FileName name, 
                                                 ZipEntry entry,
                                                 ZipFile file )
        throws FileSystemException
    {
        return new ZipFileObject( name, entry, file, this );
    }

    protected ZipFile createZipFile( File file ) throws FileSystemException
    {
        try
        {
            return new ZipFile( file );
        }
        catch ( IOException ioe )
        {
            final String message = REZ.getString( "open-zip-file.error", file );
            throw new FileSystemException( message, ioe );
        }
    }

    public void close()
    {
        // Release the zip file
        try
        {
            if ( zipFile != null )
            {
                zipFile.close();
            }
        }
        catch ( final IOException e )
        {
            final String message = REZ.getString( "close-zip-file.error", file );
            getLogger().warn( message, e );
        }

        super.close();
    }

    /**
     * Creates a file object.
     */
    protected FileObject createFile( FileName name ) throws FileSystemException
    {
        // This is only called for files which do not exist in the Zip file
        return new ZipFileObject( name, null, null, this );
    }
}
