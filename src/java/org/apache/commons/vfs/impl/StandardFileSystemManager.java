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
package org.apache.commons.vfs.impl;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.util.Messages;
import org.apache.commons.vfs.provider.FileReplicator;
import org.apache.commons.vfs.provider.FileProvider;

/**
 * A {@link org.apache.commons.vfs.FileSystemManager} that configures itself
 * to use the standard providers and other components.  To use this
 * manager:
 *
 * <ul>
 * <li>Create an instance of this class.
 * <li>Set the logger using {@link #setLogger}.
 * <li>Configure the manager using {@link #init}.
 * <li>Add additional providers, or replace the default services.
 * </ul>
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.3 $ $Date: 2002/10/28 02:05:06 $
 */
public class StandardFileSystemManager
    extends DefaultFileSystemManager
{
    /**
     * Initializes this manager.  Adds the providers and replicator.
     */
    public void init() throws FileSystemException
    {
        // Set the replicator and temporary file store (use the same component)
        DefaultFileReplicator replicator = new DefaultFileReplicator();
        setReplicator( new PrivilegedFileReplicator( replicator ) );
        setTemporaryFileStore( replicator );

        // Add the standard providers
        addProvider( "file", "org.apache.commons.vfs.provider.local.DefaultLocalFileSystemProvider" );
        addProvider( "zip", "org.apache.commons.vfs.provider.zip.ZipFileSystemProvider" );
        addProvider( "jar", "org.apache.commons.vfs.provider.jar.JarFileSystemProvider" );
        addProvider( "ftp", "org.apache.commons.vfs.provider.ftp.FtpFileSystemProvider" );
        addProvider( "smb", "org.apache.commons.vfs.provider.smb.SmbFileSystemProvider" );
        addProvider( "tmp", "org.apache.commons.vfs.provider.temp.TemporaryFileProvider" );

        // Add a default provider
        final FileProvider provider = createProvider( "org.apache.commons.vfs.provider.url.UrlFileProvider" );
        if ( provider != null )
        {
            setDefaultProvider( provider );
        }
    }

    /**
     * Adds a provider.
     */
    private void addProvider( final String scheme,
                              final String providerClassName )
        throws FileSystemException
    {
        final FileProvider provider = createProvider( providerClassName );
        if ( provider != null )
        {
            addProvider( scheme, provider );
        }
    }

    /**
     * Creates a provider.
     */
    private FileProvider createProvider( final String providerClassName )
        throws FileSystemException
    {
        try
        {
            final Class providerClass = Class.forName( providerClassName );
            return (FileProvider)providerClass.newInstance();
        }
        catch ( final ClassNotFoundException e )
        {
            // Ignore
            final String message = Messages.getString( "vfs.impl/create-provider.warn", providerClassName );
            getLog().warn( message, e );
            return null;
        }
        catch ( final Exception e )
        {
            throw new FileSystemException("vfs.impl/create-provider.error", providerClassName, e );
        }
    }

}
