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

import java.net.URL;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.provider.FileProvider;
import org.apache.commons.vfs.util.Messages;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A {@link org.apache.commons.vfs.FileSystemManager} that configures itself
 * from an XML configuration file.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.13 $ $Date: 2003/02/24 07:31:08 $
 */
public class StandardFileSystemManager
    extends DefaultFileSystemManager
{
    private static final String CONFIG_RESOURCE = "providers.xml";

    private String configUri;
    private ClassLoader classLoader;

    /**
     * Sets the configuration file for this manager.
     */
    public void setConfiguration( final String configUri )
    {
        this.configUri = configUri;
    }

    /**
     * Sets the ClassLoader to use to load the providers.  Default is to
     * use the ClassLoader that loaded this class.
     */
    public void setClassLoader( final ClassLoader classLoader )
    {
        this.classLoader = classLoader;
    }

    /**
     * Initializes this manager.  Adds the providers and replicator.
     */
    public void init() throws FileSystemException
    {
        // Set the replicator and temporary file store (use the same component)
        final DefaultFileReplicator replicator = new DefaultFileReplicator();
        setReplicator( new PrivilegedFileReplicator( replicator ) );
        setTemporaryFileStore( replicator );

        if ( classLoader == null )
        {
            // Use default classloader
            classLoader = getClass().getClassLoader();
        }
        if ( configUri == null )
        {
            // Use default config
            final URL url = getClass().getResource( CONFIG_RESOURCE );
            if ( url == null )
            {
                throw new FileSystemException( "vfs.impl/find-config-file.error", CONFIG_RESOURCE );
            }
            configUri = url.toExternalForm();
        }

        // Configure
        configure( configUri );

        // Initialise super-class
        super.init();
    }

    /**
     * Configures this manager from an XML configuration file.
     */
    private void configure( final String configUri ) throws FileSystemException
    {
        try
        {
            // Load up the config
            // TODO - validate
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringElementContentWhitespace( true );
            factory.setIgnoringComments( true );
            factory.setExpandEntityReferences( true );
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Element config = builder.parse( configUri ).getDocumentElement();

            // Add the providers
            final NodeList providers = config.getElementsByTagName( "provider" );
            final int count = providers.getLength();
            for ( int i = 0; i < count; i++ )
            {
                final Element provider = (Element)providers.item( i );
                addProvider( provider, false );
            }

            // Add the default provider
            final NodeList defProviders = config.getElementsByTagName( "default-provider" );
            if ( defProviders.getLength() > 0 )
            {
                final Element provider = (Element)defProviders.item( 0 );
                addProvider( provider, true );
            }

            // Add the mime-type maps
            final NodeList mimeTypes = config.getElementsByTagName( "mime-type-map" );
            for ( int i = 0; i < mimeTypes.getLength(); i++ )
            {
                final Element map = (Element)mimeTypes.item( i );
                addMimeTypeMap( map );
            }

            // Add the extension maps
            final NodeList extensions = config.getElementsByTagName( "extension-map" );
            for ( int i = 0; i < extensions.getLength(); i++ )
            {
                final Element map = (Element)extensions.item( i );
                addExtensionMap( map );
            }
        }
        catch ( final Exception e )
        {
            throw new FileSystemException( "vfs.impl/load-config.error", configUri, e );
        }
    }

    /**
     * Adds an extension map.
     */
    private void addExtensionMap( final Element map )
    {
        final String extension = map.getAttribute( "extension" );
        final String scheme = map.getAttribute( "scheme" );
        addExtensionMap( extension, scheme );
    }

    /**
     * Adds a mime-type map.
     */
    private void addMimeTypeMap( final Element map )
    {
        final String mimeType = map.getAttribute( "mime-type" );
        final String scheme = map.getAttribute( "scheme" );
        addMimeTypeMap( mimeType, scheme );
    }

    /**
     * Adds a provider from a provider definition.
     */
    private void addProvider( final Element providerDef, final boolean isDefault )
        throws FileSystemException
    {
        final String classname = providerDef.getAttribute( "class-name" );

        // Make sure all required classes are in classpath
        final String[] requiredClasses = getRequiredClasses( providerDef );
        for ( int i = 0; i < requiredClasses.length; i++ )
        {
            final String requiredClass = requiredClasses[ i ];
            if ( !findClass( requiredClass ) )
            {
                final String msg = Messages.getString( "vfs.impl/skipping-provider.warn",
                                                       new String[] { classname, requiredClass } );
                getLog().warn( msg );
                return;
            }
        }

        // Create and register the provider
        final FileProvider provider = createProvider( classname );
        final String[] schemas = getSchemas( providerDef );
        if ( schemas.length > 0 )
        {
            addProvider( schemas, provider );
        }

        // Set as default, if required
        if ( isDefault )
        {
            setDefaultProvider( provider );
        }
    }

    /**
     * Tests if a class is available.
     */
    private boolean findClass( final String className )
    {
        try
        {
            classLoader.loadClass( className );
            return true;
        }
        catch ( final ClassNotFoundException e )
        {
            return false;
        }
    }

    /**
     * Extracts the required classes from a provider definition.
     */
    private String[] getRequiredClasses( final Element providerDef )
    {
        final ArrayList classes = new ArrayList();
        final NodeList deps = providerDef.getElementsByTagName( "if-available" );
        final int count = deps.getLength();
        for ( int i = 0; i < count; i++ )
        {
            final Element dep = (Element)deps.item( i );
            classes.add( dep.getAttribute( "class-name" ) );
        }
        return (String[])classes.toArray( new String[ classes.size() ] );
    }

    /**
     * Extracts the schema names from a provider definition.
     */
    private String[] getSchemas( final Element provider )
    {
        final ArrayList schemas = new ArrayList();
        final NodeList schemaElements = provider.getElementsByTagName( "scheme" );
        final int count = schemaElements.getLength();
        for ( int i = 0; i < count; i++ )
        {
            final Element scheme = (Element)schemaElements.item( i );
            schemas.add( scheme.getAttribute( "name" ) );
        }
        return (String[])schemas.toArray( new String[ schemas.size() ] );
    }

    /**
     * Creates a provider.
     */
    private FileProvider createProvider( final String providerClassName )
        throws FileSystemException
    {
        try
        {
            final Class providerClass = classLoader.loadClass( providerClassName );
            return (FileProvider)providerClass.newInstance();
        }
        catch ( final Exception e )
        {
            throw new FileSystemException( "vfs.impl/create-provider.error", providerClassName, e );
        }
    }
}