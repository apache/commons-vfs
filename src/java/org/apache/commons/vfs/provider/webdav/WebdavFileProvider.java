/*
 * Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.commons.vfs.provider.webdav;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs.provider.GenericFileName;

/**
 * A provider for WebDAV.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.6 $ $Date: 2004/05/10 20:09:54 $
 */
public class WebdavFileProvider
    extends AbstractOriginatingFileProvider
{
    public WebdavFileProvider()
    {
        super();
    }

    /**
     * Parses an abolute URI.
     */
    protected FileName parseUri(final String uri)
        throws FileSystemException
    {
        return GenericFileName.parseUri(uri, 80);
    }

    /**
     * Creates a filesystem.
     */
    protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions)
        throws FileSystemException
    {
        final GenericFileName rootName = (GenericFileName) name;
        return new WebDavFileSystem(rootName, fileSystemOptions);
    }
}
