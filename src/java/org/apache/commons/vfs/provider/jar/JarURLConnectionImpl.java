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
package org.apache.commons.vfs.provider.jar;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileSystemException;

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

/**
 * A default URL connection that will work for most file systems.
 *
 * @author <a href="mailto:brian@mmmanager.org">Brian Olsen</a>
 * @version $Revision$ $Date$
 */
public class JarURLConnectionImpl
    extends JarURLConnection
{
    // This is because JarURLConnection SUCKS
    private static final String HACK_URL = "jar:http://somehost/somejar.jar!/";

    private FileContent content;
    private URL parentURL;
    private JarFileObject file;
    private String entryName;

    public JarURLConnectionImpl(JarFileObject file, FileContent content)
        throws MalformedURLException, FileSystemException
    {
        //This is because JarURLConnection SUCKS!!
        super(new URL(HACK_URL));

        this.url = file.getURL();
        this.content = content;
        this.parentURL = file.getURL();
        this.entryName = file.getName().getPath();
        this.file = file;
    }


    public URL getJarFileURL()
    {
        return parentURL;
    }


    public String getEntryName()
    {
        return entryName;
    }


    public JarFile getJarFile() throws IOException
    {
        throw new FileSystemException("vfs.provider.jar/jar-file-no-access.error");
    }


    public Manifest getManifest() throws IOException
    {
        return file.getManifest();
    }


    public JarEntry getJarEntry() throws IOException
    {
        throw new FileSystemException("vfs.provider.jar/jar-entry-no-access.error");
    }


    public Attributes getAttributes() throws IOException
    {
        return file.getAttributes();
    }


    public Certificate[] getCertificates()
    {
        return file.doGetCertificates();
    }


    public void connect()
    {
        connected = true;
    }

    public InputStream getInputStream()
        throws IOException
    {
        return content.getInputStream();
    }

    public OutputStream getOutputStream()
        throws IOException
    {
        return content.getOutputStream();
    }

    public int getContentLength()
    {
        try
        {
            return (int) content.getSize();
        }
        catch (FileSystemException fse)
        {
        }

        return -1;
    }

}
