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
package org.apache.commons.vfs.test;

import java.io.File;
import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.impl.DefaultFileReplicator;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.impl.PrivilegedFileReplicator;
import org.apache.commons.vfs.provider.local.DefaultLocalFileSystemProvider;

/**
 * File system test cases, which verifies the structure and naming
 * functionality.
 *
 * Works from a base folder, and assumes a particular structure under
 * that base folder.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.9 $ $Date: 2002/07/05 03:21:55 $
 */
public class AbstractFileSystemTestCase
    extends AbstractVfsTestCase
{
    private FileObject readFolder;
    private FileObject writeFolder;
    private DefaultFileSystemManager manager;
    private ProviderTestConfig providerConfig;
    private File tempDir;

    /** Sets the provider test config, if any. */
    public void setConfig( final ProviderTestConfig providerConfig )
    {
        this.providerConfig = providerConfig;
    }

    /**
     * Returns the file system manager used by this test.
     */
    protected DefaultFileSystemManager getManager()
    {
        return manager;
    }

    /**
     * Returns the read test folder.
     */
    protected FileObject getReadFolder()
    {
        return readFolder;
    }

    /**
     * Returns the write test folder.
     */
    protected FileObject getWriteFolder()
    {
        return writeFolder;
    }

    /**
     * Sets up the test
     */
    protected void setUp() throws Exception
    {
        // Create the file system manager
        manager = new DefaultFileSystemManager();
        manager.addProvider( "file", new DefaultLocalFileSystemProvider() );

        tempDir = getTestDirectory( "temp" );
        final DefaultFileReplicator replicator = new DefaultFileReplicator( tempDir );
        manager.setReplicator( new PrivilegedFileReplicator( replicator ) );
        manager.setTemporaryFileStore( replicator );

        if ( providerConfig != null )
        {
            providerConfig.prepare( manager );
        }

        manager.init();

        if ( providerConfig != null )
        {
            // Locate the base folder
            readFolder = providerConfig.getReadTestFolder( manager );
            writeFolder = providerConfig.getWriteTestFolder( manager );

            // Make some assumptions absout the name
            assertTrue( !readFolder.getName().getPath().equals( "/" ) );
        }
    }

    /**
     * Cleans-up test.
     */
    protected void tearDown() throws Exception
    {
        manager.close();

        // Make sure temp directory is empty or gone
        assertTrue( ( ! tempDir.exists() ) || ( tempDir.isDirectory() && tempDir.list().length == 0 ) );
    }
}
