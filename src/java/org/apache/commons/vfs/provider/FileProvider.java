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
package org.apache.commons.vfs.provider;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;

/**
 * A file provider.  Each file provider is responsible for handling files for
 * a particular URI scheme.
 *
 * <p>A file provider may also implement {@link VfsComponent}.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1 $ $Date: 2002/06/17 07:19:45 $
 */
public interface FileProvider
{
    /**
     * Locates a file object, by absolute URI.
     *
     * @param baseFile
     *          The base file to use for resolving the individual parts of
     *          a compound URI.
     * @param uri
     *          The absolute URI of the file to find.
     */
    FileObject findFile( FileObject baseFile, String uri )
        throws FileSystemException;

    /**
     * Creates a layered file system.
     *
     * @param scheme
     *          The URI scheme for the layered file system.
     * @param file
     *          The file to build the file system on.
     */
    FileObject createFileSystem( String scheme, FileObject file )
        throws FileSystemException;
}
