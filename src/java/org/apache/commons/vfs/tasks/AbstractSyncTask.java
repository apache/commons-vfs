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
package org.apache.commons.vfs.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.NameScope;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.util.Messages;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * An abstract file synchronization task.  Scans a set of source files and
 * folders, and a destination folder, and performs actions on missing and
 * out-of-date files.  Specifically, performs actions on the following:
 * <ul>
 * <li>Missing destination file.
 * <li>Missing source file.
 * <li>Out-of-date destination file.
 * <li>Up-to-date destination file.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.10 $ $Date: 2004/02/28 03:35:52 $
 *
 * @todo Deal with case where dest file maps to a child of one of the source files
 * @todo Deal with case where dest file already exists and is incorrect type (not file, not a folder)
 * @todo Use visitors
 * @todo Add default excludes
 * @todo Allow selector, mapper, filters, etc to be specified.
 * @todo Handle source/dest directories as well
 * @todo Allow selector to be specified for choosing which dest files to sync
 */
public abstract class AbstractSyncTask
    extends VfsTask
{
    private final ArrayList srcFiles = new ArrayList();
    private String destFileUrl;
    private String destDirUrl;

    /**
     * Sets the destination file.
     */
    public void setDestFile( final String destFile )
    {
        this.destFileUrl = destFile;
    }

    /**
     * Sets the destination directory.
     */
    public void setDestDir( final String destDir )
    {
        this.destDirUrl = destDir;
    }

    /**
     * Sets the source file
     */
    public void setSrc( final String srcFile )
    {
        final SourceInfo src = new SourceInfo();
        src.setFile( srcFile );
        addConfiguredSrc( src );
    }

    /**
     * Adds a nested <src> element.
     */
    public void addConfiguredSrc( final SourceInfo srcInfo )
        throws BuildException
    {
        if ( srcInfo.file == null )
        {
            final String message = Messages.getString( "vfs.tasks/sync.no-source-file.error" );
            throw new BuildException( message );
        }
        srcFiles.add( srcInfo );
    }

    /**
     * Executes this task.
     */
    public void execute() throws BuildException
    {
        // Validate
        if ( destFileUrl == null && destDirUrl == null )
        {
            final String message =
                Messages.getString( "vfs.tasks/sync.no-destination.error" );
            throw new BuildException( message );
        }
        if ( destFileUrl != null && destDirUrl != null )
        {
            final String message =
                Messages.getString( "vfs.tasks/sync.too-many-destinations.error" );
            throw new BuildException( message );
        }

        if ( srcFiles.size() == 0 )
        {
            final String message = Messages.getString( "vfs.tasks/sync.no-source-files.warn" );
            log( message, Project.MSG_WARN );
            return;
        }

        // Perform the sync
        try
        {
            if ( destFileUrl != null )
            {
                handleSingleFile();
            }
            else
            {
                handleFiles();
            }
        }
        catch ( final BuildException e )
        {
            throw e;
        }
        catch ( final Exception e )
        {
            throw new BuildException( e.getMessage(), e );
        }
    }

    /**
     * Copies the source files to the destination.
     */
    private void handleFiles() throws Exception
    {
        // Locate the destination folder, and make sure it exists
        final FileObject destFolder = resolveFile( destDirUrl );
        destFolder.createFolder();

        // Locate the source files, and make sure they exist
        final ArrayList srcs = new ArrayList();
        for ( int i = 0; i < srcFiles.size(); i++ )
        {
            // Locate the source file, and make sure it exists
            final SourceInfo src = (SourceInfo)srcFiles.get( i );
            final FileObject srcFile = resolveFile( src.file );
            if ( !srcFile.exists() )
            {
                final String message =
                    Messages.getString( "vfs.tasks/sync.src-file-no-exist.warn", srcFile );
                log( message, Project.MSG_WARN );
            }
            else
            {
                srcs.add( srcFile );
            }
        }

        // Scan the source files
        final Set destFiles = new HashSet();
        for ( int i = 0; i < srcs.size(); i++ )
        {
            final FileObject rootFile = (FileObject)srcs.get( i );
            final FileName rootName = rootFile.getName();

            if ( rootFile.getType() == FileType.FILE )
            {
                // Build the destination file name
                final FileObject destFile = destFolder.resolveFile( rootName.getBaseName(), NameScope.DESCENDENT );

                // Do the copy
                handleFile( destFiles, rootFile, destFile );
            }
            else
            {
                // Find matching files
                final FileObject[] files = rootFile.findFiles( Selectors.SELECT_FILES );
                for ( int j = 0; j < files.length; j++ )
                {
                    final FileObject srcFile = files[ j ];

                    // Build the destination file name
                    final String relName =
                        rootName.getRelativeName( srcFile.getName() );
                    final FileObject destFile =
                        destFolder.resolveFile( relName, NameScope.DESCENDENT );

                    // Do the copy
                    handleFile( destFiles, srcFile, destFile );
                }
            }
        }

        // Scan the destination files for files with no source file
        if ( detectMissingSourceFiles() )
        {
            final FileObject[] allDestFiles = destFolder.findFiles( Selectors.SELECT_FILES );
            for ( int i = 0; i < allDestFiles.length; i++ )
            {
                final FileObject destFile = allDestFiles[ i ];
                if ( !destFiles.contains( destFile ) )
                {
                    handleMissingSourceFile( destFile );
                }
            }
        }
    }

    /**
     * Handles a single file, checking for collisions where more than one
     * source file maps to the same destination file.
     */
    private void handleFile( final Set destFiles,
                             final FileObject srcFile,
                             final FileObject destFile ) throws Exception

    {
        // Check for duplicate source files
        if ( destFiles.contains( destFile ) )
        {
            final String message = Messages.getString( "vfs.tasks/sync.duplicate-source-files.warn", destFile );
            log( message, Project.MSG_WARN );
        }
        else
        {
            destFiles.add( destFile );
        }

        // Handle the file
        handleFile( srcFile, destFile );
    }

    /**
     * Copies a single file.
     */
    private void handleSingleFile() throws Exception
    {
        // Make sure there is exactly one source file, and that it exists
        // and is a file.
        if ( srcFiles.size() > 1 )
        {
            final String message =
                Messages.getString( "vfs.tasks/sync.too-many-source-files.error" );
            throw new BuildException( message );
        }
        final SourceInfo src = (SourceInfo)srcFiles.get( 0 );
        final FileObject srcFile = resolveFile( src.file );
        if ( srcFile.getType() != FileType.FILE )
        {
            final String message =
                Messages.getString( "vfs.tasks/sync.source-not-file.error", srcFile );
            throw new BuildException( message );
        }

        // Locate the destination file
        final FileObject destFile = resolveFile( destFileUrl );

        // Do the copy
        handleFile( srcFile, destFile );
    }

    /**
     * Handles a single source file.
     */
    private void handleFile( final FileObject srcFile,
                             final FileObject destFile )
        throws Exception
    {
        if ( !destFile.exists()
            || srcFile.getContent().getLastModifiedTime() > destFile.getContent().getLastModifiedTime() )
        {
            // Destination file is out-of-date
            handleOutOfDateFile( srcFile, destFile );
        }
        else
        {
            // Destination file is up-to-date
            handleUpToDateFile( srcFile, destFile );
        }
    }

    /**
     * Handles an out-of-date file (a file where the destination file
     * either doesn't exist, or is older than the source file).
     * This implementation does nothing.
     */
    protected void handleOutOfDateFile( final FileObject srcFile,
                                        final FileObject destFile )
        throws Exception
    {
    }

    /**
     * Handles an up-to-date file (where the destination file exists and is
     * newer than the source file).  This implementation does nothing.
     */
    protected void handleUpToDateFile( final FileObject srcFile,
                                       final FileObject destFile )
        throws Exception
    {
    }

    /**
     * Handles a destination for which there is no corresponding source file.
     * This implementation does nothing.
     */
    protected void handleMissingSourceFile( final FileObject destFile )
        throws Exception
    {
    }

    /**
     * Check if this task cares about destination files with a missing source
     * file.  This implementation returns false.
     */
    protected boolean detectMissingSourceFiles()
    {
        return false;
    }

    /**
     * Information about a source file.
     */
    public static class SourceInfo
    {
        private String file;

        public void setFile( final String file )
        {
            this.file = file;
        }
    }

}
