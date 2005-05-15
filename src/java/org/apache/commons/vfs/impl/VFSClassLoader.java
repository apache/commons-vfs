/*
 * Copyright 2002-2005 The Apache Software Foundation.
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
package org.apache.commons.vfs.impl;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.NameScope;

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


/**
 * A class loader that can load classes and resources from a search path
 * VFS FileObjects refering both to folders and JAR files. Any FileObject
 * of type {@link FileType#FILE} is asumed to be a JAR and is opened
 * by creating a layered file system with the "jar" scheme.
 * </p><p>
 * TODO - Test this with signed Jars and a SecurityManager.
 *
 * @author <a href="mailto:brian@mmmanager.org">Brian Olsen</a>
 * @version $Revision$ $Date$
 * @see FileSystemManager#createFileSystem
 */
public class VFSClassLoader
    extends SecureClassLoader
{
    private final ArrayList resources = new ArrayList();

    /**
     * Constructors a new VFSClassLoader for the given file.
     *
     * @param file    the file to load the classes and resources from.
     * @param manager the FileManager to use when trying create a layered Jar file
     *                system.
     */
    public VFSClassLoader(final FileObject file,
                          final FileSystemManager manager)
        throws FileSystemException
    {
        this(new FileObject[]{file}, manager, null);
    }

    /**
     * Constructors a new VFSClassLoader for the given file.
     *
     * @param file    the file to load the classes and resources from.
     * @param manager the FileManager to use when trying create a layered Jar file
     *                system.
     * @param parent  the parent class loader for delegation.
     */
    public VFSClassLoader(final FileObject file,
                          final FileSystemManager manager,
                          final ClassLoader parent)
        throws FileSystemException
    {
        this(new FileObject[]{file}, manager, parent);
    }

    /**
     * Constructors a new VFSClassLoader for the given files.  The files will
     * be searched in the order specified.
     *
     * @param files   the files to load the classes and resources from.
     * @param manager the FileManager to use when trying create a layered Jar file
     *                system.
     */
    public VFSClassLoader(final FileObject[] files,
                          final FileSystemManager manager)
        throws FileSystemException
    {
        this(files, manager, null);
    }

    /**
     * Constructors a new VFSClassLoader for the given FileObjects.
     * The FileObjects will be searched in the order specified.
     *
     * @param files   the FileObjects to load the classes and resources from.
     * @param manager the FileManager to use when trying create a layered Jar file
     *                system.
     * @param parent  the parent class loader for delegation.
     */
    public VFSClassLoader(final FileObject[] files,
                          final FileSystemManager manager,
                          final ClassLoader parent) throws FileSystemException
    {
        super(parent);
        addFileObjects(manager, files);
    }

    /**
     * Appends the specified FileObjects to the list of FileObjects to search
     * for classes and resources.
     *
     * @param files the FileObjects to append to the search path.
     */
    private void addFileObjects(final FileSystemManager manager,
                                final FileObject[] files) throws FileSystemException
    {
        for (int i = 0; i < files.length; i++)
        {
            FileObject file = files[i];
            if (!file.exists())
            {
                // Does not exist - skip
                continue;
            }

            // TODO - use federation instead
            if (manager.canCreateFileSystem(file))
            {
                // Use contents of the file
                file = manager.createFileSystem(file);
            }

            resources.add(file);
        }
    }

    /**
     * Finds and loads the class with the specified name from the search
     * path.
     *
     * @throws ClassNotFoundException if the class is not found.
     */
    protected Class findClass(final String name) throws ClassNotFoundException
    {
        try
        {
            final String path = name.replace('.', '/').concat(".class");
            final Resource res = loadResource(path);
            if (res == null)
            {
                throw new ClassNotFoundException(name);
            }
            return defineClass(name, res);
        }
        catch (final IOException ioe)
        {
            throw new ClassNotFoundException(name, ioe);
        }
    }

    /**
     * Loads and verifies the class with name and located with res.
     */
    private Class defineClass(final String name, final Resource res)
        throws IOException
    {
        final URL url = res.getCodeSourceURL();
        final String pkgName = res.getPackageName();
        if (pkgName != null)
        {
            final Package pkg = getPackage(pkgName);
            if (pkg != null)
            {
                if (pkg.isSealed())
                {
                    if (!pkg.isSealed(url))
                    {
                        throw new FileSystemException("vfs.impl/pkg-sealed-other-url", pkgName);
                    }
                }
                else
                {
                    if (isSealed(res))
                    {
                        throw new FileSystemException("vfs.impl/pkg-sealing-unsealed", pkgName);
                    }
                }
            }
            else
            {
                definePackage(pkgName, res);
            }
        }

        final byte[] bytes = res.getBytes();
        final Certificate[] certs =
            res.getFileObject().getContent().getCertificates();
        final CodeSource cs = new CodeSource(url, certs);
        return defineClass(name, bytes, 0, bytes.length, cs);
    }

    /**
     * Returns true if the we should seal the package where res resides.
     */
    private boolean isSealed(final Resource res)
        throws FileSystemException
    {
        final String sealed = res.getPackageAttribute(Attributes.Name.SEALED);
        return "true".equalsIgnoreCase(sealed);
    }

    /**
     * Reads attributes for the package and defines it.
     */
    private Package definePackage(final String name,
                                  final Resource res)
        throws FileSystemException
    {
        // TODO - check for MANIFEST_ATTRIBUTES capability first
        final String specTitle = res.getPackageAttribute(Name.SPECIFICATION_TITLE);
        final String specVendor = res.getPackageAttribute(Attributes.Name.SPECIFICATION_VENDOR);
        final String specVersion = res.getPackageAttribute(Name.SPECIFICATION_VERSION);
        final String implTitle = res.getPackageAttribute(Name.IMPLEMENTATION_TITLE);
        final String implVendor = res.getPackageAttribute(Name.IMPLEMENTATION_VENDOR);
        final String implVersion = res.getPackageAttribute(Name.IMPLEMENTATION_VERSION);

        final URL sealBase;
        if (isSealed(res))
        {
            sealBase = res.getCodeSourceURL();
        }
        else
        {
            sealBase = null;
        }

        return definePackage(name, specTitle, specVersion, specVendor,
            implTitle, implVersion, implVendor, sealBase);
    }

    /**
     * Calls super.getPermissions both for the code source and also
     * adds the permissions granted to the parent layers.
     */
    protected PermissionCollection getPermissions(final CodeSource cs)
    {
        try
        {
            final String url = cs.getLocation().toString();
            FileObject file = lookupFileObject(url);
            if (file == null)
            {
                return super.getPermissions(cs);
            }

            FileObject parentLayer = file.getFileSystem().getParentLayer();
            if (parentLayer == null)
            {
                return super.getPermissions(cs);
            }

            Permissions combi = new Permissions();
            PermissionCollection permCollect = super.getPermissions(cs);
            copyPermissions(permCollect, combi);

            for (FileObject parent = parentLayer;
                 parent != null;
                 parent = parent.getFileSystem().getParentLayer())
            {
                final CodeSource parentcs =
                    new CodeSource(parent.getURL(),
                        parent.getContent().getCertificates());
                permCollect = super.getPermissions(parentcs);
                copyPermissions(permCollect, combi);
            }

            return combi;
        }
        catch (final FileSystemException fse)
        {
            throw new SecurityException(fse.getMessage());
        }
    }

    /**
     * Copies the permissions from src to dest.
     */
    protected void copyPermissions(final PermissionCollection src,
                                   final PermissionCollection dest)
    {
        for (Enumeration elem = src.elements(); elem.hasMoreElements();)
        {
            final Permission permission = (Permission) elem.nextElement();
            dest.add(permission);
        }
    }

    /**
     * Does a reverse lookup to find the FileObject when we only have the
     * URL.
     */
    private FileObject lookupFileObject(final String name)
    {
        final Iterator it = resources.iterator();
        while (it.hasNext())
        {
            final FileObject object = (FileObject) it.next();
            if (name.equals(object.getName().getURI()))
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
    protected URL findResource(final String name)
    {
        try
        {
            final Resource res = loadResource(name);
            if (res != null)
            {
                return res.getURL();
            }
        }
        catch (final Exception mue)
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
    protected Enumeration findResources(final String name)
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
    private Resource loadResource(final String name) throws FileSystemException
    {
        final Iterator it = resources.iterator();
        while (it.hasNext())
        {
            final FileObject baseFile = (FileObject) it.next();
            final FileObject file =
                baseFile.resolveFile(name, NameScope.DESCENDENT_OR_SELF);
            if (file.exists())
            {
                return new Resource(name, baseFile, file);
            }
        }

        return null;
    }
}
