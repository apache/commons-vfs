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
package org.apache.commons.vfs2.provider.webdav;

import java.net.URLStreamHandler;
import java.util.Collection;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.DefaultURLStreamHandler;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.apache.commons.vfs2.provider.http.HttpFileSystem;

/**
 * A WebDAV file system.
 *
 * @since 2.0
 */
public class WebdavFileSystem extends HttpFileSystem {
    protected WebdavFileSystem(final GenericFileName rootName, final HttpClient client,
            final FileSystemOptions fileSystemOptions) {
        super(rootName, client, fileSystemOptions);
    }

    @Override
    protected HttpClient getClient() {
        // make accessible
        return super.getClient();
    }

    /**
     * Returns the capabilities of this file system.
     *
     * @param caps The Capabilities to add.
     */
    @Override
    protected void addCapabilities(final Collection<Capability> caps) {
        caps.addAll(WebdavFileProvider.capabilities);
    }

    /**
     * Creates a file object. This method is called only if the requested file is not cached.
     *
     * @param name the FileName.
     * @return The created FileObject.
     */
    @Override
    protected FileObject createFile(final AbstractFileName name) {

        return new WebdavFileObject(name, this);
    }

    /**
     * Return a URLStreamHandler.
     *
     * @return The URLStreamHandler.
     */
    public URLStreamHandler getURLStreamHandler() {
        return new DefaultURLStreamHandler(getContext(), getFileSystemOptions());
    }
}
