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
package org.apache.commons.vfs;

import org.apache.commons.vfs.impl.DefaultFileReplicator;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.impl.PrivilegedFileReplicator;
import org.apache.commons.vfs.provider.FileProvider;
import org.apache.commons.vfs.provider.FileReplicator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A static factory for {@link FileSystemManager} instances.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.8 $ $Date: 2002/10/23 11:59:39 $
 */
public class FileSystemManagerFactory
{
    private static FileSystemManager instance;

    private FileSystemManagerFactory()
    {
    }

    /**
     * Returns the default {@link FileSystemManager} instance.
     */
    public static synchronized FileSystemManager getManager()
        throws FileSystemException
    {
        if ( instance == null )
        {
            instance = doCreateManager();
        }
        return instance;
    }

    /**
     * Creates a file system manager instance.
     *
     * @todo Load manager config from a file.
     * @todo Ignore missing providers.
     */
    private static FileSystemManager doCreateManager()
        throws FileSystemException
    {
        final DefaultFileSystemManager mgr = new DefaultFileSystemManager();

        // Set the logger
        final Log logger = LogFactory.getLog( FileSystemManagerFactory.class );
        mgr.setLogger( logger );

        // Set the replicator
        FileReplicator replicator = new DefaultFileReplicator();
        replicator = new PrivilegedFileReplicator( replicator );
        mgr.setReplicator( replicator );

        // Add the default providers
        FileProvider provider = createProvider( "org.apache.commons.vfs.provider.local.DefaultLocalFileSystemProvider" );
        mgr.addProvider( "file", provider );
        provider = createProvider( "org.apache.commons.vfs.provider.zip.ZipFileSystemProvider" );
        mgr.addProvider( "zip", provider );
        provider = createProvider( "org.apache.commons.vfs.provider.jar.JarFileSystemProvider" );
        mgr.addProvider( "jar", provider );
        provider = createProvider( "org.apache.commons.vfs.provider.ftp.FtpFileSystemProvider" );
        mgr.addProvider( "ftp", provider );
        provider = createProvider( "org.apache.commons.vfs.provider.smb.SmbFileSystemProvider" );
        mgr.addProvider( "smb", provider );
        provider = createProvider( "org.apache.commons.vfs.provider.url.UrlFileProvider" );
        mgr.setDefaultProvider( provider );

        return mgr;
    }

    /**
     * Creates a provider.
     */
    private static FileProvider createProvider( final String providerClassName )
        throws FileSystemException
    {
        try
        {
            final Class providerClass = Class.forName( providerClassName );
            return (FileProvider)providerClass.newInstance();
        }
        catch ( final Exception e )
        {
            throw new FileSystemException("vfs/create-provider.error", new Object[]{providerClassName}, e );
        }
    }
}
