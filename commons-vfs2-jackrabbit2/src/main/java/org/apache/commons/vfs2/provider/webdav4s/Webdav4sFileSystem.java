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
package org.apache.commons.vfs2.provider.webdav4s;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.DefaultURLStreamHandler;
import org.apache.commons.vfs2.provider.http4.Http4FileSystem;
import org.apache.commons.vfs2.provider.webdav4.Webdav4FileObject;
import org.apache.commons.vfs2.provider.webdav4.Webdav4FileProvider;
import org.apache.commons.vfs2.provider.webdav4.Webdav4FileSystem;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.HttpClientContext;

import java.net.URLStreamHandler;
import java.util.Collection;

/**
 * A WebDAV file system based on HTTP4.
 *
 * @since 2.7.0
 */
public class Webdav4sFileSystem extends Webdav4FileSystem {

    protected Webdav4sFileSystem(final FileName rootName, final FileSystemOptions fileSystemOptions,
            final HttpClient httpClient, final HttpClientContext httpClientContext) {
        super(rootName, fileSystemOptions, httpClient, httpClientContext);
    }

    /**
     * Creates a file object. This method is called only if the requested file is not cached.
     *
     * @param name the FileName.
     * @return The created FileObject.
     */
    @Override
    protected FileObject createFile(final AbstractFileName name) throws Exception {
        return new Webdav4FileObject(name, this, true){};
    }
}
