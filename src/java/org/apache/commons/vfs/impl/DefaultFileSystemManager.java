/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.impl;

import java.io.File;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.avalon.excalibur.i18n.ResourceManager;
import org.apache.avalon.excalibur.i18n.Resources;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.provider.DefaultURLStreamHandler;
import org.apache.commons.vfs.provider.FileProvider;
import org.apache.commons.vfs.provider.FileReplicator;
import org.apache.commons.vfs.provider.LocalFileProvider;
import org.apache.commons.vfs.provider.UriParser;

/**
 * A default file system manager implementation.
 *
 * @todo - Extract an AbstractFileSystemManager super-class from this class.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.12 $ $Date: 2002/07/05 06:51:45 $
 */
public class DefaultFileSystemManager
    implements FileSystemManager
{
    private static final Resources REZ
        = ResourceManager.getPackageResources( DefaultFileSystemManager.class );

    /** The provider for local files. */
    private LocalFileProvider m_localFileProvider;

    /** The file replicator to use. */
    private final DefaultFileReplicator m_fileReplicator = new DefaultFileReplicator( this );

    /** Mapping from URI scheme to FileProvider. */
    private final Map m_providers = new HashMap();

    /** The base file to use for relative URI. */
    private FileObject m_baseFile;

    /** The context to send to providers. */
    private final DefaultProviderContext m_context = new DefaultProviderContext( this );

    /**
     * Registers a file system provider.
     */
    public void addProvider( final String urlScheme,
                             final FileProvider provider )
        throws FileSystemException
    {
        addProvider( new String[]{urlScheme}, provider );
    }

    /**
     * Registers a file system provider.
     */
    public void addProvider( final String[] urlSchemes,
                             final FileProvider provider )
        throws FileSystemException
    {
        // Check for duplicates
        for( int i = 0; i < urlSchemes.length; i++ )
        {
            final String scheme = urlSchemes[ i ];
            if( m_providers.containsKey( scheme ) )
            {
                final String message = REZ.getString( "multiple-providers-for-scheme.error", scheme );
                throw new FileSystemException( message );
            }
        }

        // Contextualise
        provider.setContext( m_context );

        // Add to map
        for( int i = 0; i < urlSchemes.length; i++ )
        {
            final String scheme = urlSchemes[ i ];
            m_providers.put( scheme, provider );
        }

        if( provider instanceof LocalFileProvider )
        {
            m_localFileProvider = (LocalFileProvider)provider;
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
        return m_fileReplicator;
    }

    /**
     * Closes all files created by this manager, and cleans up any temporary
     * files.
     */
    public void close()
    {
        // Dispose the providers (making sure we only dispose each provider
        // once
        final Set providers = new HashSet();
        providers.addAll( m_providers.values() );
        for( Iterator iterator = providers.iterator(); iterator.hasNext(); )
        {
            FileProvider provider = (FileProvider)iterator.next();
            provider.close();
        }
        m_providers.clear();

        m_fileReplicator.close();
    }

    /**
     * Sets the base file to use when resolving relative URI.
     */
    public void setBaseFile( final FileObject baseFile ) throws FileSystemException
    {
        m_baseFile = baseFile;
    }

    /**
     * Sets the base file to use when resolving relative URI.
     */
    public void setBaseFile( final File baseFile ) throws FileSystemException
    {
        m_baseFile = getLocalFileProvider().findLocalFile( baseFile );
    }

    /**
     * Returns the base file used to resolve relative URI.
     */
    public FileObject getBaseFile()
    {
        return m_baseFile;
    }

    /**
     * Locates a file by URI.
     */
    public FileObject resolveFile( final String uri ) throws FileSystemException
    {
        return resolveFile( m_baseFile, uri );
    }

    /**
     * Locates a file by URI.
     */
    public FileObject resolveFile( final File baseFile, final String uri )
        throws FileSystemException
    {
        final FileObject baseFileObj = getLocalFileProvider().findLocalFile( baseFile );
        return resolveFile( baseFileObj, uri );
    }

    /**
     * Resolves a URI, relative to a base file.
     */
    public FileObject resolveFile( final FileObject baseFile, final String uri )
        throws FileSystemException
    {
        // Extract the scheme
        final String scheme = UriParser.extractScheme( uri );
        if( scheme != null )
        {
            // An absolute URI - locate the provider
            final FileProvider provider = (FileProvider)m_providers.get( scheme );
            if( provider != null )
            {
                return provider.findFile( baseFile, uri );
            }
        }

        // Decode the URI (remove %nn encodings)
        final String decodedUri = UriParser.decode( uri );

        // Handle absolute file names
        if( m_localFileProvider != null
            && m_localFileProvider.isAbsoluteLocalName( decodedUri ) )
        {
            return m_localFileProvider.findLocalFile( decodedUri );
        }

        if( scheme != null )
        {
            // Assume a bad scheme
            final String message = REZ.getString( "unknown-scheme.error", scheme, uri );
            throw new FileSystemException( message );
        }

        // Assume a relative name - use the supplied base file
        if( baseFile == null )
        {
            final String message = REZ.getString( "find-rel-file.error", uri );
            throw new FileSystemException( message );
        }
        return baseFile.resolveFile( decodedUri );
    }

    /**
     * Converts a local file into a {@link FileObject}.
     */
    public FileObject convert( final File file )
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
        FileProvider provider = (FileProvider)m_providers.get( scheme );
        if( provider == null )
        {
            final String message = REZ.getString( "unknown-provider.error", scheme );
            throw new FileSystemException( message );
        }
        return provider.createFileSystem( scheme, file );
    }

    /**
     * Locates the local file provider.
     */
    private LocalFileProvider getLocalFileProvider()
        throws FileSystemException
    {
        if( m_localFileProvider == null )
        {
            final String message = REZ.getString( "no-local-file-provider.error" );
            throw new FileSystemException( message );
        }
        return m_localFileProvider;
    }

    /**
     * Get the URLStreamHandlerFactory.
     */
    public URLStreamHandlerFactory getURLStreamHandlerFactory()
    {
        return new VfsStreamHandlerFactory();
    }

    // This is an internal class because it needs access to the private member m_providers.
    final class VfsStreamHandlerFactory implements URLStreamHandlerFactory
    {
        public URLStreamHandler createURLStreamHandler( final String protocol )
        {
            final FileProvider provider = (FileProvider)m_providers.get( protocol );
            if( provider != null )
            {
                return new DefaultURLStreamHandler( m_context );
            }

            //Route all other calls to the default URLStreamHandlerFactory
            return new URLStreamHandlerProxy();
        }
    }
}
