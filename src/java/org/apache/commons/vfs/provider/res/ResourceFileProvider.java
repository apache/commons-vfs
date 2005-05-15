/*
 * Copyright 2002-2005 The Apache Software Foundation.
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
package org.apache.commons.vfs.provider.res;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemConfigBuilder;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.AbstractFileProvider;
import org.apache.commons.vfs.provider.UriParser;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Description
 *
 * @author <a href="mailto:imario@apache.org">Mario Ivankovits</a>
 * @version $Revision$ $Date$
 */
public class ResourceFileProvider extends AbstractFileProvider
{
    protected final static Collection capabilities = Collections.unmodifiableCollection(Arrays.asList(new Capability[]
    {
        Capability.DISPATCHER
    }));

    public ResourceFileProvider()
    {
        super();
    }

    /**
     * Locates a file object, by absolute URI.
     */
    public FileObject findFile(final FileObject baseFile,
                               final String uri,
                               final FileSystemOptions fileSystemOptions)
        throws FileSystemException
    {
        StringBuffer buf = new StringBuffer(80);
        UriParser.extractScheme(uri, buf);
        String resourceName = buf.toString();

        ClassLoader cl = ResourceFileSystemConfigBuilder.getInstance().getClassLoader(fileSystemOptions);
        if (cl == null)
        {
            cl = getClass().getClassLoader();
        }
        final URL url = cl.getResource(resourceName);

        if (url == null)
        {
            throw new FileSystemException("vfs.provider.url/badly-formed-uri.error", uri);
        }

        FileObject fo = getContext().getFileSystemManager().resolveFile(url.toExternalForm());
        return fo;
    }

    public FileSystemConfigBuilder getConfigBuilder()
    {
        return org.apache.commons.vfs.provider.res.ResourceFileSystemConfigBuilder.getInstance();
    }

    public void closeFileSystem(FileSystem filesystem)
    {
        // no filesystem created here - so nothing to do
    }

    public Collection getCapabilities()
    {
        return capabilities;
    }
}
