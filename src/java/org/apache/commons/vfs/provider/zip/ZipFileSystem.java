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
package org.apache.commons.vfs.provider.zip;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.vfs.Selectors;
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
    private final File file;
    protected final ZipFile zipFile;

    public ZipFileSystem( final DefaultFileName rootName,
                          final FileObject parentLayer )
        throws FileSystemException
    {
        super( rootName, parentLayer );

        // Make a local copy of the file
        final File file = parentLayer.replicateFile( Selectors.SELECT_SELF );

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
            if ( entry.isDirectory() && getFile( name ) != null )
            {
                fileObj = (ZipFileObject)getFile( name );
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
                parent = (ZipFileObject)getFile( parentName );
                if ( parent == null )
                {
                    parent = createZipFileObject( parentName, null, zipFile );
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
            throw new FileSystemException( "vfs.provider.zip/open-zip-file.error", file, ioe );
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
            getLogger().warn( "vfs.provider.zip/close-zip-file.error :" + file, e );
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
