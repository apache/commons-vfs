/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.jar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.apache.avalon.excalibur.i18n.ResourceManager;
import org.apache.avalon.excalibur.i18n.Resources;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileSystemException;

/**
 * A default URL connection that will work for most file systems.
 *
 * @author <a href="mailto:brian@mmmanager.org">Brian Olsen</a>
 * @version $Revision: 1.2 $ $Date: 2002/08/22 02:42:46 $
 */
public class JarURLConnectionImpl
    extends JarURLConnection
{
    private static final Resources REZ =
        ResourceManager.getPackageResources( JarURLConnectionImpl.class );
        
    // This is because JarURLConnection SUCKS
    private static final String HACK_URL = "jar:http://somehost/somejar.jar!/";

    FileContent             content;
    protected URL           parentURL;
    protected JarFileObject file;
    protected String        entryName;

    public JarURLConnectionImpl( JarFileObject file, FileContent content )
        throws MalformedURLException, FileSystemException
    {
        //This is because JarURLConnection SUCKS!!
        super( new URL( HACK_URL ) );

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
        final String message = REZ.getString( "jar-file-no-access.error" );
        throw new UnsupportedOperationException( message );
    }


    public Manifest getManifest() throws IOException
    {
        return file.getManifest();
    }


    public JarEntry getJarEntry() throws IOException
    {
        final String message = REZ.getString( "jar-entry-no-access.error" );
        throw new UnsupportedOperationException( message );
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
        try{
            return content.getInputStream();
        }
        catch ( FileSystemException fse )
        {
            throw new ProtocolException( fse.getMessage() );
        }
    }

    public OutputStream getOutputStream()
        throws IOException
    {
        try{
            return content.getOutputStream();
        }
        catch ( FileSystemException fse )
        {
            throw new ProtocolException( fse.getMessage() );
        }
    }

    public int getContentLength()
    {
        try{
            return (int) content.getSize();
        }
        catch ( FileSystemException fse ) {}

        return -1;
    }

}
