/*
 * Copyright 2002, 2003,2004 The Apache Software Foundation.
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

import java.io.File;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.provider.DefaultURLStreamHandler;
import org.apache.commons.vfs.provider.FileProvider;
import org.apache.commons.vfs.provider.FileReplicator;
import org.apache.commons.vfs.provider.LocalFileProvider;
import org.apache.commons.vfs.provider.TemporaryFileStore;
import org.apache.commons.vfs.provider.UriParser;
import org.apache.commons.vfs.provider.VfsComponent;

/**
 * A default file system manager implementation.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.12 $ $Date: 2002/07/05 06:51:45 $
 */
public class DefaultFileSystemManager
    implements FileSystemManager
{
    /** The provider for local files. */
    private LocalFileProvider localFileProvider;

    /** The default provider. */
    private FileProvider defaultProvider;

    /** The file replicator to use. */
    private FileReplicator fileReplicator;

    /** Mapping from URI scheme to FileProvider. */
    private final Map providers = new HashMap();

    /** All components used by this manager. */
    private final ArrayList components = new ArrayList();

    /** The base file to use for relative URI. */
    private FileObject baseFile;

    /** The logger to use. */
    private Log log = LogFactory.getLog( DefaultFileSystemManager.class );

    /** The context to pass to providers. */
    private final DefaultVfsComponentContext context =
        new DefaultVfsComponentContext( this );

    private TemporaryFileStore tempFileStore;
    private final FileTypeMap map = new FileTypeMap();
    private final VirtualFileProvider vfsProvider = new VirtualFileProvider();
    private boolean init;

    /**
     * Returns the logger used by this manager.
     */
    protected Log getLog()
    {
        if ( log == null )
        {
            log = LogFactory.getLog( this.getClass() );
        }
        return log;
    }

    /**
     * Registers a file system provider.  The manager takes care of all
     * lifecycle management.  A provider may be registered multiple times.
     *
     * @param urlScheme The scheme the provider will handle.
     * @param provider The provider.
     */
    public void addProvider( final String urlScheme,
                             final FileProvider provider )
        throws FileSystemException
    {
        addProvider( new String[]{urlScheme}, provider );
    }

    /**
     * Registers a file system provider.  The manager takes care of all
     * lifecycle management.  A provider may be registered multiple times.
     *
     * @param urlSchemes The schemes the provider will handle.
     * @param provider The provider.
     */
    public void addProvider( final String[] urlSchemes,
                             final FileProvider provider )
        throws FileSystemException
    {
        // Warn about duplicate providers
        for ( int i = 0; i < urlSchemes.length; i++ )
        {
            final String scheme = urlSchemes[ i ];
            if ( providers.containsKey( scheme ) )
            {
                throw new FileSystemException( "vfs.impl/multiple-providers-for-scheme.error", scheme );
            }
        }

        // Contextualise the component (if not already)
        setupComponent( provider );

        // Add to map
        for ( int i = 0; i < urlSchemes.length; i++ )
        {
            final String scheme = urlSchemes[ i ];
            providers.put( scheme, provider );
        }

        if ( provider instanceof LocalFileProvider )
        {
            localFileProvider = (LocalFileProvider)provider;
        }
    }

    /**
     * Returns true if this manager has a provider for a particular scheme.
     */
    public boolean hasProvider( final String scheme )
    {
        return providers.containsKey( scheme );
    }

    /**
     * Adds an filename extension mapping.
     * @param extension The file name extension.
     * @param scheme The scheme to use for files with this extension.
     */
    public void addExtensionMap( final String extension, final String scheme )
    {
        map.addExtension( extension, scheme );
    }

    /**
     * Adds a mime type mapping.
     * @param mimeType The mime type.
     * @param scheme The scheme to use for files with this mime type.
     */
    public void addMimeTypeMap( final String mimeType, final String scheme )
    {
        map.addMimeType( mimeType, scheme );
    }

    /**
     * Sets the default provider.  This is the provider that will handle URI
     * with unknown schemes.  The manager takes care of all lifecycle
     * management.
     */
    public void setDefaultProvider( final FileProvider provider )
        throws FileSystemException
    {
        setupComponent( provider );
        defaultProvider = provider;
    }

    /**
     * Sets the file replicator to use.  The manager takes care of all
     * lifecycle management.
     */
    public void setReplicator( final FileReplicator replicator )
        throws FileSystemException
    {
        setupComponent( replicator );
        fileReplicator = replicator;
    }

    /**
     * Sets the temporary file store to use.  The manager takes care of all
     * lifecycle management.
     */
    public void setTemporaryFileStore( final TemporaryFileStore tempFileStore )
        throws FileSystemException
    {
        setupComponent( tempFileStore );
        this.tempFileStore = tempFileStore;
    }

    /**
     * Sets the logger to use.
     */
    public void setLogger( final Log log )
    {
        this.log = log;
    }

    /**
     * Initialises a component, if it has not already been initialised.
     */
    private void setupComponent( final Object component )
        throws FileSystemException
    {
        if ( !components.contains( component ) )
        {
            if ( component instanceof VfsComponent )
            {
                final VfsComponent vfsComponent = (VfsComponent)component;
                vfsComponent.setLogger( getLog() );
                vfsComponent.setContext( context );
                vfsComponent.init();
            }
            components.add( component );
        }
    }

    /**
     * Closes a component, if it has not already been closed.
     */
    private void closeComponent( final Object component )
    {
        if ( component != null && components.contains( component ) )
        {
            if ( component instanceof VfsComponent )
            {
                final VfsComponent vfsComponent = (VfsComponent)component;
                vfsComponent.close();
            }
            components.remove( component );
        }
    }

    /**
     * Returns the file replicator.
     *
     * @return The file replicator.  Never returns null.
     */
    public FileReplicator getReplicator()
        throws FileSystemException
    {
        if ( fileReplicator == null )
        {
            throw new FileSystemException( "vfs.impl/no-replicator.error" );
        }
        return fileReplicator;
    }

    /**
     * Returns the temporary file store.
     * @return The file store.  Never returns null.
     */
    public TemporaryFileStore getTemporaryFileStore()
        throws FileSystemException
    {
        if ( tempFileStore == null )
        {
            throw new FileSystemException( "vfs.impl/no-temp-file-store.error" );
        }
        return tempFileStore;
    }

    /**
     * Initialises this manager.
     */
    public void init() throws FileSystemException
    {
        setupComponent( vfsProvider );
        init = true;
    }

    /**
     * Closes all files created by this manager, and cleans up any temporary
     * files.  Also closes all providers and the replicator.
     */
    public void close()
    {
        if ( !init )
        {
            return;
        }

        // Close the providers.
        for ( Iterator iterator = providers.values().iterator(); iterator.hasNext(); )
        {
            final Object provider = iterator.next();
            closeComponent( provider );
        }

        // Close the other components
        closeComponent( defaultProvider );
        closeComponent( fileReplicator );
        closeComponent( tempFileStore );

        components.clear();
        providers.clear();
        localFileProvider = null;
        defaultProvider = null;
        fileReplicator = null;
        tempFileStore = null;
        init = false;
    }

    /**
     * Sets the base file to use when resolving relative URI.
     */
    public void setBaseFile( final FileObject baseFile )
        throws FileSystemException
    {
        this.baseFile = baseFile;
    }

    /**
     * Sets the base file to use when resolving relative URI.
     */
    public void setBaseFile( final File baseFile ) throws FileSystemException
    {
        this.baseFile = getLocalFileProvider().findLocalFile( baseFile );
    }

    /**
     * Returns the base file used to resolve relative URI.
     */
    public FileObject getBaseFile()
    {
        return baseFile;
    }

    /**
     * Locates a file by URI.
     */
    public FileObject resolveFile( final String uri ) throws FileSystemException
    {
        return resolveFile( baseFile, uri );
    }

    /**
     * Locates a file by URI.
     */
    public FileObject resolveFile( final File baseFile, final String uri )
        throws FileSystemException
    {
        final FileObject baseFileObj =
            getLocalFileProvider().findLocalFile( baseFile );
        return resolveFile( baseFileObj, uri );
    }

    /**
     * Resolves a URI, relative to a base file.
     */
    public FileObject resolveFile( final FileObject baseFile, final String uri )
        throws FileSystemException
    {
        if ( uri == null )
        {
            throw new IllegalArgumentException();
        }

        // Extract the scheme
        final String scheme = UriParser.extractScheme( uri );
        if ( scheme != null )
        {
            // An absolute URI - locate the provider
            final FileProvider provider = (FileProvider)providers.get( scheme );
            if ( provider != null )
            {
                return provider.findFile( baseFile, uri );
            }

            // Otherwise, assume a local file
        }

        // Decode the URI (remove %nn encodings)
        final String decodedUri = UriParser.decode( uri );

        // Handle absolute file names
        if ( localFileProvider != null
            && localFileProvider.isAbsoluteLocalName( decodedUri ) )
        {
            return localFileProvider.findLocalFile( decodedUri );
        }

        if ( scheme != null )
        {
            // An unknown scheme - hand it to the default provider
            if ( defaultProvider == null )
            {
                throw new FileSystemException( "vfs.impl/unknown-scheme.error", new Object[]{scheme, uri} );
            }
            return defaultProvider.findFile( baseFile, uri );
        }

        // Assume a relative name - use the supplied base file
        if ( baseFile == null )
        {
            throw new FileSystemException( "vfs.impl/find-rel-file.error", uri );
        }
        return baseFile.resolveFile( decodedUri );
    }

    /**
     * Converts a local file into a {@link FileObject}.
     */
    public FileObject toFileObject( final File file )
        throws FileSystemException
    {
        return getLocalFileProvider().findLocalFile( file );
    }

    /**
     * Creates a layered file system.
     */
    public FileObject createFileSystem( final String scheme,
                                        final FileObject file )
        throws FileSystemException
    {
        final FileProvider provider = (FileProvider)providers.get( scheme );
        if ( provider == null )
        {
            throw new FileSystemException( "vfs.impl/unknown-provider.error", new Object[] { scheme, file } );
        }
        return provider.createFileSystem( scheme, file );
    }

    /**
     * Creates a layered file system.
     */
    public FileObject createFileSystem( final FileObject file )
        throws FileSystemException
    {
        final String scheme = map.getScheme( file );
        if ( scheme == null )
        {
            throw new FileSystemException( "vfs.impl/no-provider-for-file.error", file );
        }
        return createFileSystem( scheme, file );
    }

    /**
     * Determines if a layered file system can be created for a given file.
     *
     * @param file The file to check for.
     */
    public boolean canCreateFileSystem( final FileObject file ) throws FileSystemException
    {
        return ( map.getScheme( file ) != null );
    }

    /**
     * Creates a virtual file system.
     */
    public FileObject createVirtualFileSystem( final FileObject rootFile )
        throws FileSystemException
    {
        return vfsProvider.createFileSystem( rootFile );
    }

    /**
     * Creates an empty virtual file system.
     */
    public FileObject createVirtualFileSystem( final String rootUri )
        throws FileSystemException
    {
        return vfsProvider.createFileSystem( rootUri );
    }

    /**
     * Locates the local file provider.
     */
    private LocalFileProvider getLocalFileProvider()
        throws FileSystemException
    {
        if ( localFileProvider == null )
        {
            throw new FileSystemException( "vfs.impl/no-local-file-provider.error" );
        }
        return localFileProvider;
    }

    /**
     * Get the URLStreamHandlerFactory.
     */
    public URLStreamHandlerFactory getURLStreamHandlerFactory()
    {
        return new VfsStreamHandlerFactory();
    }

    /**
     * This is an internal class because it needs access to the private
     * member providers.
     */
    final class VfsStreamHandlerFactory implements URLStreamHandlerFactory
    {
        public URLStreamHandler createURLStreamHandler( final String protocol )
        {
            FileProvider provider = (FileProvider)providers.get( protocol );
            if ( provider != null )
            {
                return new DefaultURLStreamHandler( context );
            }

            //Route all other calls to the default URLStreamHandlerFactory
            return new URLStreamHandlerProxy();
        }
    }
}
