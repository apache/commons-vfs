/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.provider.res;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileProvider;
import org.apache.commons.vfs2.provider.UriParser;

/**
 * The Resource provider.
 *
 * @author <a href="http://commons.apache.org/vfs/team-list.html">Commons VFS team</a>
 */
public class ResourceFileProvider extends AbstractFileProvider
{
    /** The provider's capabilities */
    protected static final Collection<Capability> capabilities =
        Collections.unmodifiableCollection(Arrays.asList(new Capability[]
    {
        Capability.DISPATCHER
    }));

    public ResourceFileProvider()
    {
        super();
    }

    /**
     * Locates a file object, by absolute URI.
     * @param baseFile The base file.
     * @param uri The URI of the file to locate.
     * @param fileSystemOptions The FileSystem options.
     * @return the FileObject.
     * @throws FileSystemException if an error occurs.
     */
    public FileObject findFile(final FileObject baseFile,
                               final String uri,
                               final FileSystemOptions fileSystemOptions)
        throws FileSystemException
    {
        StringBuilder buf = new StringBuilder(80);
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

    @Override
    public FileSystemConfigBuilder getConfigBuilder()
    {
        return org.apache.commons.vfs2.provider.res.ResourceFileSystemConfigBuilder.getInstance();
    }

    @Override
    public void closeFileSystem(FileSystem filesystem)
    {
        // no filesystem created here - so nothing to do
    }

    public Collection<Capability> getCapabilities()
    {
        return capabilities;
    }
}
