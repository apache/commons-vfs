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
package org.apache.commons.vfs.impl;

import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.SecureClassLoader;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.NameScope;


/**
 * A class loader that can load classes and resources from a search path
 * VFS FileObjects refering both to folders and JAR files. Any FileObject
 * of type {@link FileType#FILE} is asumed to be a JAR and is opened
 * by creating a layered file system with the "jar" scheme.
 * </p><p>
 * TODO - Test this with signed Jars and a SecurityManager.
 *
 * @see FileSystemManager#createFileSystem
 * @author <a href="mailto:brian@mmmanager.org">Brian Olsen</a>
 * @version $Revision: 1.15 $ $Date: 2003/02/12 07:56:10 $
 */
public class VFSClassLoader
    extends SecureClassLoader
{
    private final ArrayList resources = new ArrayList();

    /**
     * Constructors a new VFSClassLoader for the given file.
     *
     * @param file the file to load the classes and resources from.
     *
     * @param manager
     *      the FileManager to use when trying create a layered Jar file
     *      system.
     */
    public VFSClassLoader( final FileObject file,
                           final FileSystemManager manager )
        throws FileSystemException
    {
        this( new FileObject[]{file}, manager, null );
    }

    /**
     * Constructors a new VFSClassLoader for the given file.
     *
     * @param file the file to load the classes and resources from.
     *
     * @param manager
     *      the FileManager to use when trying create a layered Jar file
     *      system.
     *
     * @param parent the parent class loader for delegation.
     */
    public VFSClassLoader( final FileObject file,
                           final FileSystemManager manager,
                           final ClassLoader parent )
        throws FileSystemException
    {
        this( new FileObject[]{file}, manager, parent );
    }

    /**
     * Constructors a new VFSClassLoader for the given files.  The files will
     * be searched in the order specified.
     *
     * @param files the files to load the classes and resources from.
     *
     * @param manager
     *      the FileManager to use when trying create a layered Jar file
     *      system.
     */
    public VFSClassLoader( final FileObject[] files,
                           final FileSystemManager manager )
        throws FileSystemException
    {
        this( files, manager, null );
    }

    /**
     * Constructors a new VFSClassLoader for the given FileObjects.
     * The FileObjects will be searched in the order specified.
     *
     * @param files the FileObjects to load the classes and resources from.
     *
     * @param manager
     *      the FileManager to use when trying create a layered Jar file
     *      system.
     *
     * @param parent the parent class loader for delegation.
     */
    public VFSClassLoader( final FileObject[] files,
                           final FileSystemManager manager,
                           final ClassLoader parent ) throws FileSystemException
    {
        super( parent );
        addFileObjects( manager, files );
    }

    /**
     * Appends the specified FileObjects to the list of FileObjects to search
     * for classes and resources.
     *
     * @param files the FileObjects to append to the search path.
     */
    private void addFileObjects( final FileSystemManager manager,
                                 final FileObject[] files ) throws FileSystemException
    {
        for ( int i = 0; i < files.length; i++ )
        {
            FileObject file = files[ i ];
            if ( !file.exists() )
            {
                // Does not exist - skip
                continue;
            }

            if ( file.getType() == FileType.FILE )
            {
                // TODO - use federation instead
                final String extension = file.getName().getExtension();
                if ( extension.equalsIgnoreCase( "jar" ) )
                {
                    // Open as Jar file
                    file = manager.createFileSystem( "jar", file );
                }
                else if ( extension.equalsIgnoreCase( "zip" ) )
                {
                    // Open as a Zip file
                    file = manager.createFileSystem( "zip", file );
                }
            }

            resources.add( file );
        }
    }

    /**
     * Finds and loads the class with the specified name from the search
     * path.
     * @throws ClassNotFoundException if the class is not found.
     */
    protected Class findClass( final String name ) throws ClassNotFoundException
    {
        try
        {
            final String path = name.replace( '.', '/' ).concat( ".class" );
            final Resource res = loadResource( path );
            if ( res == null )
            {
                throw new ClassNotFoundException( name );
            }
            return defineClass( name, res );
        }
        catch ( final IOException ioe )
        {
            throw new ClassNotFoundException( name, ioe );
        }
    }

    /**
     * Loads and verifies the class with name and located with res.
     */
    private Class defineClass( final String name, final Resource res )
        throws IOException
    {
        final URL url = res.getCodeSourceURL();
        final String pkgName = res.getPackageName();
        if ( pkgName != null )
        {
            final Package pkg = getPackage( pkgName );
            if ( pkg != null )
            {
                if ( pkg.isSealed() )
                {
                    if ( !pkg.isSealed( url ) )
                    {
                        throw new FileSystemException( "vfs.impl/pkg-sealed-other-url", pkgName );
                    }
                }
                else
                {
                    if ( isSealed( res ) )
                    {
                        throw new FileSystemException( "vfs.impl/pkg-sealing-unsealed", pkgName );
                    }
                }
            }
            else
            {
                definePackage( pkgName, res );
            }
        }

        final byte[] bytes = res.getBytes();
        final Certificate[] certs =
            res.getFileObject().getContent().getCertificates();
        final CodeSource cs = new CodeSource( url, certs );
        return defineClass( name, bytes, 0, bytes.length, cs );
    }

    /**
     * Returns true if the we should seal the package where res resides.
     */
    private boolean isSealed( final Resource res )
        throws FileSystemException
    {
        final String sealed = res.getPackageAttribute( Attributes.Name.SEALED );
        return "true".equalsIgnoreCase( sealed );
    }

    /**
     * Reads attributes for the package and defines it.
     */
    private Package definePackage( final String name,
                                   final Resource res )
        throws FileSystemException
    {
        // TODO - check for MANIFEST_ATTRIBUTES capability first
        final String specTitle = res.getPackageAttribute( Name.SPECIFICATION_TITLE );
        final String specVendor = res.getPackageAttribute( Attributes.Name.SPECIFICATION_VENDOR );
        final String specVersion = res.getPackageAttribute( Name.SPECIFICATION_VERSION );
        final String implTitle = res.getPackageAttribute( Name.IMPLEMENTATION_TITLE );
        final String implVendor = res.getPackageAttribute( Name.IMPLEMENTATION_VENDOR );
        final String implVersion = res.getPackageAttribute( Name.IMPLEMENTATION_VERSION );

        final URL sealBase;
        if ( isSealed( res ) )
        {
            sealBase = res.getCodeSourceURL();
        }
        else
        {
            sealBase = null;
        }

        return definePackage( name, specTitle, specVersion, specVendor,
                              implTitle, implVersion, implVendor, sealBase );
    }

    /**
     * Calls super.getPermissions both for the code source and also
     * adds the permissions granted to the parent layers.
     */
    protected PermissionCollection getPermissions( final CodeSource cs )
    {
        try
        {
            final String url = cs.getLocation().toString();
            FileObject file = lookupFileObject( url );
            if ( file == null )
            {
                return super.getPermissions( cs );
            }

            FileObject parentLayer = file.getFileSystem().getParentLayer();
            if ( parentLayer == null )
            {
                return super.getPermissions( cs );
            }

            Permissions combi = new Permissions();
            PermissionCollection permCollect = super.getPermissions( cs );
            copyPermissions( permCollect, combi );

            for ( FileObject parent = parentLayer;
                  parent != null;
                  parent = parent.getFileSystem().getParentLayer() )
            {
                final CodeSource parentcs =
                    new CodeSource( parent.getURL(),
                                    parent.getContent().getCertificates() );
                permCollect = super.getPermissions( parentcs );
                copyPermissions( permCollect, combi );
            }

            return combi;
        }
        catch ( final FileSystemException fse )
        {
            throw new SecurityException( fse.getMessage() );
        }
    }

    /**
     * Copies the permissions from src to dest.
     */
    protected void copyPermissions( final PermissionCollection src,
                                    final PermissionCollection dest )
    {
        for ( Enumeration elem = src.elements(); elem.hasMoreElements(); )
        {
            final Permission permission = (Permission)elem.nextElement();
            dest.add( permission );
        }
    }

    /**
     * Does a reverse lookup to find the FileObject when we only have the
     * URL.
     */
    private FileObject lookupFileObject( final String name )
    {
        final Iterator it = resources.iterator();
        while ( it.hasNext() )
        {
            final FileObject object = (FileObject)it.next();
            if ( name.equals( object.getName().getURI() ) )
            {
                return object;
            }
        }
        return null;
    }

    /**
     * Finds the resource with the specified name from the search path.
     * This returns null if the resource is not found.
     */
    protected URL findResource( final String name )
    {
        try
        {
            final Resource res = loadResource( name );
            if ( res != null )
            {
                return res.getURL();
            }
        }
        catch ( final Exception mue )
        {
            // Ignore
            // TODO - report?
        }

        return null;
    }

    /**
     * Returns an Enumeration of all the resources in the search path
     * with the specified name.
     * TODO - Implement this.
     */
    protected Enumeration findResources( final String name )
    {
        return new Enumeration()
        {
            public boolean hasMoreElements()
            {
                return false;
            }

            public Object nextElement()
            {
                return null;
            }
        };
    }

    /**
     * Searches through the search path of for the first class or resource
     * with specified name.
     */
    private Resource loadResource( final String name ) throws FileSystemException
    {
        final Iterator it = resources.iterator();
        while ( it.hasNext() )
        {
            final FileObject baseFile = (FileObject)it.next();
            final FileObject file =
                baseFile.resolveFile( name, NameScope.DESCENDENT_OR_SELF );
            if ( file.exists() )
            {
                return new Resource( name, baseFile, file );
            }
        }

        return null;
    }
}
