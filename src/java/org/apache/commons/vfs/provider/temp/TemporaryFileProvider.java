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
package org.apache.commons.vfs.provider.temp;

import java.io.File;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileProvider;
import org.apache.commons.vfs.provider.BasicFileName;
import org.apache.commons.vfs.provider.FileProvider;
import org.apache.commons.vfs.provider.UriParser;
import org.apache.commons.vfs.provider.local.LocalFileSystem;

/**
 * A provider for temporary files.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.10 $ $Date: 2004/02/28 03:35:52 $
 */
public class TemporaryFileProvider
    extends AbstractFileProvider
    implements FileProvider
{
    private File rootFile;

    public TemporaryFileProvider( final File rootFile )
    {
        this.rootFile = rootFile;
    }

    public TemporaryFileProvider()
    {
    }

    /**
     * Locates a file object, by absolute URI.
     */
    public FileObject findFile( final FileObject baseFile, final String uri )
        throws FileSystemException
    {
        // Parse the name
        final StringBuffer buffer = new StringBuffer( uri );
        final String scheme = UriParser.extractScheme( uri, buffer );
        UriParser.decode( buffer, 0, buffer.length() );
        UriParser.normalisePath( buffer );
        final String path = buffer.toString();

        // Create the temp file system if it does not exist
        FileSystem filesystem = findFileSystem( this );
        if ( filesystem == null )
        {
            if ( rootFile == null )
            {
                rootFile = getContext().getTemporaryFileStore().allocateFile( "tempfs" );
            }
            final FileName rootName =
                new BasicFileName( scheme, scheme + ":",  FileName.ROOT_PATH );
            filesystem = new LocalFileSystem( rootName, rootFile.getAbsolutePath() );
            addFileSystem( this, filesystem );
        }

        // Find the file
        return filesystem.resolveFile( path );
    }
}
