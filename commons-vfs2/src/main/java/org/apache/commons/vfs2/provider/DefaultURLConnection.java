/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs2.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.function.Uncheck;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileSystemException;

/**
 * A default URL connection that will work for most file systems.
 */
public final class DefaultURLConnection extends URLConnection {

    private final FileContent fileContent;

    /**
     * Constructs a new instance.
     *
     * @param url The URL to connect.
     * @param fileContent The URL fileContent.
     */
    public DefaultURLConnection(final URL url, final FileContent fileContent) {
        super(url);
        this.fileContent = fileContent;
    }

    @Override
    public void connect() {
        connected = true;
    }

    @Override
    public String getContentEncoding() {
        return Uncheck.get(() -> fileContent.getContentInfo().getContentEncoding());
    }

    @Override
    public int getContentLength() {
        try {
            return (int) fileContent.getSize();
        } catch (final FileSystemException fse) {
            return -1; // TODO: report?
        }
    }

    @Override
    public String getContentType() {
        return Uncheck.get(() -> fileContent.getContentInfo().getContentType());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return fileContent.getInputStream();
    }

    @Override
    public long getLastModified() {
        try {
            return fileContent.getLastModifiedTime();
        } catch (final FileSystemException ignored) {
            return -1; // TODO: report?
        }
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return fileContent.getOutputStream();
    }

    /*
     * public String getHeaderField(String name) { try { if
     * (content.getFile().getFileSystem().hasCapability(Capability.ATTRIBUTES)) { String value = (String)
     * content.getAttribute(name); if (value != null) { return value; } }
     *
     * return null; } catch (FileSystemException e) { throw new UncheckedIOException(e); } }
     */
}
