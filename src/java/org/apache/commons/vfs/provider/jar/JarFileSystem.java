/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.jar;

import java.io.File;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.DefaultFileName;
import org.apache.commons.vfs.provider.zip.ZipFileObject;
import org.apache.commons.vfs.provider.zip.ZipFileSystem;

/**
 * A read-only file system for Jar files.
 *
 * @author <a href="mailto:brian@mmmanager.org">Brian Olsen</a>
 * @version $Revision: 1.2 $ $Date: 2002/10/21 01:40:38 $
 */
public class JarFileSystem
    extends ZipFileSystem
{
    private Attributes attributes;

    public JarFileSystem( final DefaultFileName rootName,
                          final FileObject file ) throws FileSystemException
    {
        super( rootName, file );
    }

    protected ZipFile createZipFile( File file ) throws FileSystemException
    {
        try
        {
            return new JarFile( file );
        }
        catch( IOException ioe )
        {
            throw new FileSystemException( "vfs.provider.jar/open-jar-file.error", new Object[]{file}, ioe );
        }
    }

    protected ZipFileObject createZipFileObject( FileName name,
                                                 ZipEntry entry,
                                                 ZipFile file )
        throws FileSystemException
    {
        return new JarFileObject( name, entry, file, this );
    }


    Attributes getAttributes() throws IOException
    {
        if ( attributes == null )
        {
            final Manifest man = ( (JarFile) zipFile).getManifest();
            if ( man == null )
            {
                attributes = new Attributes( 1 );
            }
            else
            {
                attributes = man.getMainAttributes();
                if ( attributes == null )
                {
                    attributes = new Attributes( 1 );
                }
            }
        }

        return attributes;
    }

    Object getAttribute( Name attrName )
        throws FileSystemException
    {
        try
        {
            final Attributes attr = getAttributes();
            final String value = attr.getValue( attrName );
            return value;
        }
        catch ( IOException ioe )
        {
            throw new FileSystemException( attrName.toString(), ioe );
        }
    }

    Name lookupName( String attrName )
    {
        if ( Name.CLASS_PATH.equals( attrName ) )
        {
            return Name.CLASS_PATH;
        }
        else if ( Name.CONTENT_TYPE.equals( attrName ) )
        {
            return Name.CONTENT_TYPE;
        }
        else if ( Name.EXTENSION_INSTALLATION.equals( attrName ) )
        {
            return Name.EXTENSION_INSTALLATION;
        }
        else if ( Name.EXTENSION_LIST.equals( attrName ) )
        {
            return Name.EXTENSION_LIST;
        }
        else if ( Name.EXTENSION_NAME.equals( attrName ) )
        {
            return Name.EXTENSION_NAME;
        }
        else if ( Name.IMPLEMENTATION_TITLE.equals( attrName ) )
        {
            return Name.IMPLEMENTATION_TITLE;
        }
        else if ( Name.IMPLEMENTATION_URL.equals( attrName ) )
        {
            return Name.IMPLEMENTATION_URL;
        }
        else if ( Name.IMPLEMENTATION_VENDOR.equals( attrName ) )
        {
            return Name.IMPLEMENTATION_VENDOR;
        }
        else if ( Name.IMPLEMENTATION_VENDOR_ID.equals( attrName ) )
        {
            return Name.IMPLEMENTATION_VENDOR_ID;
        }
        else if ( Name.IMPLEMENTATION_VERSION.equals( attrName ) )
        {
            return Name.IMPLEMENTATION_VENDOR;
        }
        else if ( Name.MAIN_CLASS.equals( attrName ) )
        {
            return Name.MAIN_CLASS;
        }
        else if ( Name.MANIFEST_VERSION.equals( attrName ) )
        {
            return Name.MANIFEST_VERSION;
        }
        else if ( Name.SEALED.equals( attrName ) )
        {
            return Name.SEALED;
        }
        else if ( Name.SIGNATURE_VERSION.equals( attrName ) )
        {
            return Name.SIGNATURE_VERSION;
        }
        else if ( Name.SPECIFICATION_TITLE.equals( attrName ) )
        {
            return Name.SPECIFICATION_TITLE;
        }
        else if ( Name.SPECIFICATION_VENDOR.equals( attrName ) )
        {
            return Name.SPECIFICATION_VENDOR;
        }
        else if ( Name.SPECIFICATION_VERSION.equals( attrName ) )
        {
            return Name.SPECIFICATION_VERSION;
        }
        else
        {
            return new Name( attrName );
        }
    }

    /**
     * Retrives the attribute with the specified name. The default
     * implementation simply throws an exception.
     */
    public Object getAttribute( String attrName ) throws FileSystemException
    {
        final Name name = lookupName( attrName );
        return getAttribute( name );
    }
}
