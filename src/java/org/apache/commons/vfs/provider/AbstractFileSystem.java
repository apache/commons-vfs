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
 *    any, must include the following acknowledgement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Software Foundation.
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileChangeEvent;
import org.apache.commons.vfs.FileListener;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.util.Messages;

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
    private final Collection caps = new HashSet();

    /** Map from FileName to FileObject. */
    private final Map files = new HashMap();

    /** Map from FileName to an ArrayList of listeners for that file. */
    private final Map listenerMap = new HashMap();

    protected AbstractFileSystem( final FileName rootName,
                                  final FileObject parentLayer )
    {
        this.parentLayer = parentLayer;
        this.rootName = rootName;
    }

    /**
     * Initialises this component.
     */
    public void init() throws FileSystemException
    {
        addCapabilities( caps );
    }

    /**
     * Closes this component.
     */
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
        throws Exception;

    /**
     * Adds the capabilities of this file system.
     */
    protected abstract void addCapabilities( Collection caps );

    /** Returns the name of the root of this file system. */
    protected FileName getRootName()
    {
        return rootName;
    }

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
     * Determines if this file system has a particular capability.
     */
    public boolean hasCapability( final Capability capability )
    {
        return caps.contains( capability );
    }

    /**
     * Retrieves the attribute with the specified name. The default
     * implementation simply throws an exception.
     */
    public Object getAttribute( final String attrName ) throws FileSystemException
    {
        throw new FileSystemException( "vfs.provider/get-attribute-not-supported.error" );
    }

    /**
     * Sets the attribute with the specified name. The default
     * implementation simply throws an exception.
     */
    public void setAttribute( final String attrName, final Object value )
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
        if ( !rootName.getRootURI().equals( name.getRootURI() ) )
        {
            throw new FileSystemException( "vfs.provider/mismatched-fs-for-name.error", new Object[]{name, rootName} );
        }

        FileObject file = (FileObject)files.get( name );
        if ( file == null )
        {
            try
            {
                file = createFile( name );
            }
            catch ( Exception e )
            {
                throw new FileSystemException( "vfs.provider/create-file.error", name );
            }
            files.put( name, file );
        }
        return file;
    }

    /**
     * Creates a temporary local copy of a file and its descendents.
     */
    public File replicateFile( final FileObject file,
                               final FileSelector selector )
        throws FileSystemException
    {
        if ( !file.exists() )
        {
            throw new FileSystemException( "vfs.provider/replicate-missing-file.error", file.getName() );
        }

        try
        {
            return doReplicateFile( file, selector );
        }
        catch ( final Exception e )
        {
            throw new FileSystemException( "vfs.provider/replicate-file.error", file.getName(), e );
        }
    }

    /**
     * Creates a temporary local copy of a file and its descendents.
     */
    protected File doReplicateFile( final FileObject file,
                                    final FileSelector selector )
        throws Exception
    {
        return getContext().getReplicator().replicateFile( file, selector );
    }

    /**
     * Adds a junction to this file system.
     */
    public void addJunction( final String junctionPoint,
                             final FileObject targetFile )
        throws FileSystemException
    {
        throw new FileSystemException( "vfs.provider/junctions-not-supported.error", rootName );
    }

    /**
     * Removes a junction from this file system.
     */
    public void removeJunction( final String junctionPoint ) throws FileSystemException
    {
        throw new FileSystemException( "vfs.provider/junctions-not-supported.error", rootName );
    }

    /**
     * Adds a listener on a file in this file system.
     */
    public void addListener( final FileObject file,
                             final FileListener listener )
    {
        ArrayList listeners = (ArrayList)listenerMap.get( file.getName() );
        if ( listeners == null )
        {
            listeners = new ArrayList();
            listenerMap.put( file.getName(), listeners );
        }
        listeners.add( listener );
    }

    /**
     * Removes a listener from a file in this file system.
     */
    public void removeListener( final FileObject file,
                                final FileListener listener )
    {
        final ArrayList listeners = (ArrayList)listenerMap.get( file.getName() );
        if ( listeners != null )
        {
            listeners.remove( listener );
        }
    }

    /**
     * Fires a file create event.
     */
    protected void fireFileCreated( final FileObject file )
    {
        fireEvent( new CreateEvent( file ) );
    }

    /**
     * Fires a file delete event.
     */
    protected void fireFileDeleted( final FileObject file )
    {
        fireEvent( new DeleteEvent( file ) );
    }

    /**
     * Fires an event.
     */
    private void fireEvent( final ChangeEvent event )
    {
        final FileObject file = event.getFile();
        final ArrayList listeners = (ArrayList)listenerMap.get( file.getName() );
        if ( listeners != null )
        {
            final int count = listeners.size();
            for ( int i = 0; i < count; i++ )
            {
                final FileListener listener = (FileListener)listeners.get( i );
                try
                {
                    event.notify( listener );
                }
                catch ( final Exception e )
                {
                    final String message = Messages.getString( "vfs.provider/notify-listener.warn", file );
                    getLogger().warn( message, e );
                }
            }
        }
    }

    /** A change event that knows how to notify a listener. */
    private abstract static class ChangeEvent extends FileChangeEvent
    {
        public ChangeEvent( final FileObject file )
        {
            super( file );
        }

        public abstract void notify( final FileListener listener ) throws Exception;
    }

    /** File creation event. */
    private static class CreateEvent extends ChangeEvent
    {
        public CreateEvent( final FileObject file )
        {
            super( file );
        }

        public void notify( final FileListener listener ) throws Exception
        {
            listener.fileCreated( this );
        }
    }

    /** File deletion event. */
    private static class DeleteEvent extends ChangeEvent
    {
        public DeleteEvent( final FileObject file )
        {
            super( file );
        }

        public void notify( final FileListener listener ) throws Exception
        {
            listener.fileDeleted( this );
        }
    }
}
