/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.commons.vfs.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.util.Messages;
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
                file.delete( Selectors.SELECT_ALL );
            }
            catch ( final FileSystemException e )
            {
                final String message = Messages.getString( "vfs.impl/delete-temp.warn", file.getName() );
                getLogger().warn( message, e );
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
