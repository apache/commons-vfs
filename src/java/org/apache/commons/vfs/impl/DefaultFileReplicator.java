/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import org.apache.commons.vfs.FileConstants;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractVfsComponent;
import org.apache.commons.vfs.provider.FileReplicator;

/**
 * A simple file replicator.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.2 $ $Date: 2002/07/05 04:08:18 $
 */
public final class DefaultFileReplicator
    extends AbstractVfsComponent
    implements FileReplicator
{

    private final ArrayList copies = new ArrayList();
    private File tempDir;
    private long filecount;

    /**
     * Initialises this component.
     */
    public void init() throws FileSystemException
    {
        tempDir = new File( "vfs_cache" ).getAbsoluteFile();
        filecount = new Random().nextInt() & 0xffff;
    }

    /**
     * Closes the replicator, deleting all temporary files.
     */
    public void close()
    {
        // Delete the temporary files
        while ( copies.size() > 0 )
        {
            final FileObject file = (FileObject)copies.remove( 0 );
            try
            {
                file.delete( FileConstants.SELECT_ALL );
            }
            catch ( final FileSystemException e )
            {
                //TODO - fix this
                //final String message = REZ.getString( "delete-temp.warn", file.getName() );
                getLogger().warn( "vfs.impl/delete-temp.warn", e );
            }
        }

        // Clean up the temp directory, if it is empty
        if ( tempDir != null && tempDir.exists() && tempDir.list().length == 0 )
        {
            tempDir.delete();
            tempDir = null;
        }
    }

    /**
     * Generates a new temp file name.
     */
    private File generateTempFile( String prefix )
    {
        // Create a unique-ish file name
        final String basename = prefix + "_" + filecount + ".tmp";
        filecount++;
        return new File( tempDir, basename );
    }

    /**
     * Creates a local copy of the file, and all its descendents.
     */
    public File replicateFile( final FileObject srcFile,
                               final FileSelector selector )
        throws FileSystemException
    {
        final String basename = srcFile.getName().getBaseName();
        final File file = generateTempFile( basename );

        // Copy from the source file
        final FileObject destFile = getContext().getFile( file );
        destFile.copyFrom( srcFile, selector );

        // Keep track of the copy
        copies.add( destFile );

        return file;
    }

}
