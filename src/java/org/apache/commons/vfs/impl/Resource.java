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
import java.util.jar.Attributes;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileUtil;

/**
 * Helper class for VFSClassLoader. This represents a resource loaded with
 * the classloader.
 *
 * @see VFSClassLoader
 *
 * @author <a href="mailto:brian@mmmanager.org">Brian Olsen</a>
 * @version $Revision: 1.7 $ $Date: 2003/02/12 07:56:10 $
 */
class Resource
{
    private final FileObject root;
    private final FileObject resource;
    private final FileObject packageFolder;
    private final String packageName;

    /**
     * Creates a new instance.
     *
     * @param root The code source FileObject.
     * @param resource The resource of the FileObject.
     */
    public Resource( final String name,
                     final FileObject root,
                     final FileObject resource )
        throws FileSystemException
    {
        this.root = root;
        this.resource = resource;
        packageFolder = resource.getParent();
        final int pos = name.lastIndexOf( '/' );
        if ( pos == -1 )
        {
            packageName = null;
        }
        else
        {
            packageName = name.substring( 0, pos ).replace( '/', '.' );
        }
    }

    /**
     * Returns the URL of the resource.
     */
    public URL getURL() throws FileSystemException
    {
        return resource.getURL();
    }

    /**
     * Returns the name of the package containing the resource.
     */
    public String getPackageName()
    {
        return packageName;
    }

    /**
     * Returns an attribute of the package containing the resource.
     */
    public String getPackageAttribute( final Attributes.Name attrName ) throws FileSystemException
    {
        return (String)packageFolder.getContent().getAttribute( attrName.toString() );
    }

    /**
     * Returns the folder for the package containing the resource.
     */
    public FileObject getPackageFolder()
    {
        return packageFolder;
    }

    /**
     * Returns the FileObject of the resource.
     */
    public FileObject getFileObject()
    {
        return resource;
    }

    /**
     * Returns the code source as an URL.
     */
    public URL getCodeSourceURL() throws FileSystemException
    {
        return root.getURL();
    }

    /**
     * Returns the data for this resource as a byte array.
     */
    public byte[] getBytes() throws IOException
    {
        return FileUtil.getContent( resource );
    }
}
