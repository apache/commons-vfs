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
package org.apache.commons.vfs.provider.url;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.AbstractFileProvider;
import org.apache.commons.vfs.provider.BasicFileName;

/**
 * A file provider backed by Java's URL API.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.16 $ $Date: 2004/02/28 03:35:52 $
 */
public class UrlFileProvider
    extends AbstractFileProvider
{
    /**
     * Locates a file object, by absolute URI.
     */
    public FileObject findFile( final FileObject baseFile,
                                final String uri )
        throws FileSystemException
    {
        try
        {
            final URL url = new URL( uri );
            final URL rootUrl = new URL( url, "/" );
            FileSystem fs = findFileSystem( rootUrl );
            if ( fs == null )
            {
                final FileName rootName =
                    new BasicFileName( rootUrl, FileName.ROOT_PATH );
                fs = new UrlFileSystem( rootName );
                addFileSystem( rootUrl, fs );
            }
            return fs.resolveFile( url.getPath() );
        }
        catch ( final MalformedURLException e )
        {
            throw new FileSystemException( "vfs.provider.url/badly-formed-uri.error", uri, e );
        }
    }
}
