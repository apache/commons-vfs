/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.jar;

import java.io.IOException;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.zip.ZipFileObject;

/**
 * A file in a Jar file system.
 *
 * @author <a href="mailto:brian@mmmanager.org">Brian Olsen</a>
 * @version $Revision: 1.1 $ $Date: 2002/08/22 01:32:49 $
 */
class JarFileObject extends ZipFileObject
{
    private Attributes attributes;

    public JarFileObject( FileName name,
                          ZipEntry entry,
                          ZipFile zipFile,
                          JarFileSystem fs )
    {
        super( name, entry, zipFile, fs );
    }

    Manifest getManifest() throws IOException
    {
        if( file == null )
            return null;
            
        return ((JarFile) file).getManifest();
    }

    Attributes getAttributes() throws IOException
    {
        if( attributes == null )
        {
            if( entry == null )
            {
                attributes = new Attributes( 1 );
            }
            else
            {
                attributes = ( (JarEntry) entry).getAttributes();
                if( attributes == null )
                {
                    attributes = new Attributes( 1 );
                }
            }
        }

        return attributes;
    }

    /**
     *
     */
    protected Object doGetAttribute( String attrName )
        throws FileSystemException
    {
        try
        {
            final JarFileSystem fs = (JarFileSystem) getFileSystem();
            final Attributes attr = getAttributes();
            final Name name = fs.lookupName( attrName );
            String value = attr.getValue( name );
            if ( value != null )
            {
                return value;
            }

            return fs.getAttribute( name );
        }
        catch ( IOException ioe )
        {
            throw new FileSystemException( attrName, ioe );
        }
    }
    
    /**
     * Return the certificates of this JarEntry.
     */
    protected Certificate[] doGetCertificates()
    {
        if( entry == null )
            return null;

        return ((JarEntry) entry).getCertificates();
    }
}
