/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider;

import java.util.HashMap;
import java.util.Map;
import org.apache.avalon.excalibur.i18n.ResourceManager;
import org.apache.avalon.excalibur.i18n.Resources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;

/**
 * A partial file system implementation.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.2 $ $Date: 2002/04/07 02:27:56 $
 */
public abstract class AbstractFileSystem
    implements FileSystem
{
    private static final Resources REZ =
        ResourceManager.getPackageResources( AbstractFileSystem.class );
        
    private FileObject parentLayer;
    private FileObject root;
    private final FileName rootName;
    private final FileSystemProviderContext context;

    /** Map from FileName to FileObject. */
    private final Map files = new HashMap();

    protected AbstractFileSystem( final FileSystemProviderContext context,
                                  final FileName rootName,
                                  final FileObject parentLayer )
    {
        this.parentLayer = parentLayer;
        this.rootName = rootName;
        this.context = context;
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
    protected abstract FileObject createFile( final FileName name ) throws FileSystemException;

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
     * Returns the context fir this file system.
     */
    public FileSystemProviderContext getContext()
    {
        return context;
    }
    
    /**
     * Retrives the attribute with the specified name. The default
     * implementation simply throws an exception.
     */
    public Object getAttribute( String attrName ) throws FileSystemException
    {
        final String message = REZ.getString( "get-attribute-not-supported.error" );
        throw new FileSystemException( message ); 
    }

    /**
     * Sets the attribute with the specified name. The default
     * implementation simply throws an exception.
     */
    public void setAttribute( String attrName, Object value )
        throws FileSystemException
    {
        final String message = REZ.getString( "set-attribute-not-supported.error" );
        throw new FileSystemException( message ); 
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
            root = findFile( rootName );
        }
        return root;
    }

    /**
     * Finds a file in this file system.
     */
    public FileObject findFile( final String nameStr ) throws FileSystemException
    {
        // Resolve the name, and create the file
        final FileName name = rootName.resolveName( nameStr );
        return findFile( name );
    }

    /**
     * Finds a file in this file system.
     */
    public FileObject findFile( final FileName name ) throws FileSystemException
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

    /**
     * Returns the logger for this file system to use.
     */
    protected Log getLogger()
    {
        return LogFactory.getLog( getClass() );
    }
}
