/* ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
package org.apache.commons.vfs.provider;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystem;

/**
 * A partial {@link org.apache.commons.vfs.FileSystem} implementation.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.2 $ $Date: 2002/04/07 02:27:56 $
 */
public abstract class AbstractFileSystem
    extends AbstractVfsComponent
    implements FileSystem
{
    private final FileName rootName;
    private FileObject parentLayer;
    private FileObject root;

    /** Map from FileName to FileObject. */
    private final Map files = new HashMap();

    protected AbstractFileSystem( final FileName rootName,
                                  final FileObject parentLayer )
    {
        this.parentLayer = parentLayer;
        this.rootName = rootName;
    }

    public void close()
    {
        // Clean-up
        files.clear();
    }

    /**
     * Creates a file object.  This method is called only if the requested
     * file is not cached.
     */
    protected abstract FileObject createFile( final FileName name )
        throws FileSystemException;

    /**
     * Adds a file object to the cache.
     */
    protected void putFile( final FileObject file )
    {
        files.put( file.getName(), file );
    }

    /**
     * Returns a cached file.
     */
    protected FileObject getFile( final FileName name )
    {
        return (FileObject)files.get( name );
    }

    /**
     * Retrieves the attribute with the specified name. The default
     * implementation simply throws an exception.
     */
    public Object getAttribute( String attrName ) throws FileSystemException
    {
        throw new FileSystemException( "vfs.provider/get-attribute-not-supported.error" );
    }

    /**
     * Sets the attribute with the specified name. The default
     * implementation simply throws an exception.
     */
    public void setAttribute( String attrName, Object value )
        throws FileSystemException
    {
        throw new FileSystemException( "vfs.provider/set-attribute-not-supported.error" );
    }

    /**
     * Returns the parent layer if this is a layered file system.
     */
    public FileObject getParentLayer() throws FileSystemException
    {
        return parentLayer;
    }

    /**
     * Returns the root file of this file system.
     */
    public FileObject getRoot() throws FileSystemException
    {
        if ( root == null )
        {
            root = resolveFile( rootName );
        }
        return root;
    }

    /**
     * Finds a file in this file system.
     */
    public FileObject resolveFile( final String nameStr ) throws FileSystemException
    {
        // Resolve the name, and create the file
        final FileName name = rootName.resolveName( nameStr );
        return resolveFile( name );
    }

    /**
     * Finds a file in this file system.
     */
    public FileObject resolveFile( final FileName name ) throws FileSystemException
    {
        // TODO - assert that name is from this file system
        FileObject file = (FileObject)files.get( name );
        if ( file == null )
        {
            file = createFile( name );
            files.put( name, file );
        }
        return file;
    }
}
