/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002, 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.commons.vfs.provider.jar;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.zip.ZipFileObject;
import org.apache.commons.vfs.provider.zip.ZipFileSystem;

/**
 * A read-only file system for Jar files.
 *
 * @author <a href="mailto:brian@mmmanager.org">Brian Olsen</a>
 * @version $Revision: 1.11 $ $Date: 2003/02/12 07:56:15 $
 */
class JarFileSystem
    extends ZipFileSystem
{
    private Attributes attributes;

    public JarFileSystem( final FileName rootName,
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
        catch ( IOException ioe )
        {
            throw new FileSystemException( "vfs.provider.jar/open-jar-file.error", file, ioe );
        }
    }

    protected ZipFileObject createZipFileObject( FileName name,
                                                 ZipEntry entry,
                                                 ZipFile file )
        throws FileSystemException
    {
        return new JarFileObject( name, entry, file, this );
    }

    /**
     * Returns the capabilities of this file system.
     */
    protected void addCapabilities( final Collection caps )
    {
        super.addCapabilities( caps );
        caps.add( Capability.ATTRIBUTES );
        caps.add( Capability.FS_ATTRIBUTES );
        caps.add( Capability.SIGNING );
        caps.add( Capability.MANIFEST_ATTRIBUTES );
    }

    Attributes getAttributes() throws IOException
    {
        if ( attributes == null )
        {
            final Manifest man = ( (JarFile)zipFile ).getManifest();
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
