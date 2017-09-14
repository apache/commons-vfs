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
package org.apache.commons.vfs2.provider.url;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileProvider;

/**
 * A file provider backed by Java's URL API.
 */
public class UrlFileProvider extends AbstractFileProvider {
    /** The provider's capabilities */
    protected static final Collection<Capability> capabilities = Collections.unmodifiableCollection(
            Arrays.asList(new Capability[] { Capability.READ_CONTENT, Capability.URI, Capability.GET_LAST_MODIFIED }));

    public UrlFileProvider() {
        super();
        setFileNameParser(new UrlFileNameParser());
    }

    /**
     * Locates a file object, by absolute URI.
     *
     * @param baseFile The base FileObject.
     * @param uri The uri of the file to locate.
     * @param fileSystemOptions The FileSystemOptions
     * @return The FileObject
     * @throws FileSystemException if an error occurs.
     */
    @Override
    public synchronized FileObject findFile(final FileObject baseFile, final String uri,
            final FileSystemOptions fileSystemOptions) throws FileSystemException {
        try {
            final URL url = new URL(uri);

            final URL rootUrl = new URL(url, "/");
            final String key = this.getClass().getName() + rootUrl.toString();
            FileSystem fs = findFileSystem(key, fileSystemOptions);
            if (fs == null) {
                final String extForm = rootUrl.toExternalForm();
                final FileName rootName = getContext().parseURI(extForm);
                // final FileName rootName =
                // new BasicFileName(rootUrl, FileName.ROOT_PATH);
                fs = new UrlFileSystem(rootName, fileSystemOptions);
                addFileSystem(key, fs);
            }
            return fs.resolveFile(url.getPath());
        } catch (final MalformedURLException e) {
            throw new FileSystemException("vfs.provider.url/badly-formed-uri.error", uri, e);
        }
    }

    @Override
    public FileSystemConfigBuilder getConfigBuilder() {
        return org.apache.commons.vfs2.provider.res.ResourceFileSystemConfigBuilder.getInstance();
    }

    @Override
    public Collection<Capability> getCapabilities() {
        return capabilities;
    }
}
