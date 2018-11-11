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
package org.apache.commons.vfs2.provider.jar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileSystemException;

/**
 * A default URL connection that will work for most file systems.
 */
public class JarURLConnectionImpl extends JarURLConnection {
    // This is because JarURLConnection SUCKS
    private static final String HACK_URL = "jar:http://somehost/somejar.jar!/";

    private final FileContent content;
    private final URL parentURL;
    private final JarFileObject file;
    private final String entryName;

    public JarURLConnectionImpl(final JarFileObject file, final FileContent content)
            throws MalformedURLException, FileSystemException {
        // This is because JarURLConnection SUCKS!!
        super(new URL(HACK_URL));

        this.url = file.getURL();
        this.content = content;
        this.parentURL = file.getURL();
        this.entryName = file.getName().getPath();
        this.file = file;
    }

    @Override
    public URL getJarFileURL() {
        return parentURL;
    }

    @Override
    public String getEntryName() {
        return entryName;
    }

    @Override
    public JarFile getJarFile() throws IOException {
        throw new FileSystemException("vfs.provider.jar/jar-file-no-access.error");
    }

    @Override
    public Manifest getManifest() throws IOException {
        return file.getManifest();
    }

    @Override
    public JarEntry getJarEntry() throws IOException {
        throw new FileSystemException("vfs.provider.jar/jar-entry-no-access.error");
    }

    @Override
    public Attributes getAttributes() throws IOException {
        return file.getAttributes();
    }

    @Override
    public Certificate[] getCertificates() {
        return file.doGetCertificates();
    }

    @Override
    public void connect() {
        connected = true;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return content.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return content.getOutputStream();
    }

    @Override
    public int getContentLength() {
        try {
            return (int) content.getSize();
        } catch (final FileSystemException ignored) {
            return -1;
        }
    }

}
