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
package org.apache.commons.vfs.test;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.impl.DefaultFileReplicator;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.impl.PrivilegedFileReplicator;
import org.apache.commons.vfs.impl.test.VfsClassLoaderTests;
import org.apache.commons.vfs.provider.local.DefaultLocalFileProvider;

/**
 * The suite of tests for a file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.13 $ $Date: 2003/10/13 08:42:27 $
 */
public class ProviderTestSuite
    extends TestSetup
{
    private final ProviderTestConfig providerConfig;
    private final String prefix;
    private final TestSuite testSuite;

    private FileObject baseFolder;
    private FileObject readFolder;
    private FileObject writeFolder;
    private DefaultFileSystemManager manager;
    private File tempDir;

    /**
     * Adds the tests for a file system to this suite.
     */
    public ProviderTestSuite( final ProviderTestConfig providerConfig ) throws Exception
    {
        this( providerConfig, "", false );
    }

    private ProviderTestSuite( final ProviderTestConfig providerConfig,
                               final String prefix,
                               final boolean nested )
        throws Exception
    {
        super( new TestSuite() );
        testSuite = (TestSuite)fTest;
        this.providerConfig = providerConfig;
        this.prefix = prefix;
        addBaseTests();
        if ( !nested )
        {
            // Add nested tests
            // TODO - move nested jar and zip tests here
            // TODO - enable this again
            //testSuite.addTest( new ProviderTestSuite( new JunctionProviderConfig( providerConfig ), "junction.", true ));
        }
    }

    /**
     * Adds base tests - excludes the nested test cases.
     */
    private void addBaseTests() throws Exception
    {
        addTests( UriTests.class );
        addTests( NamingTests.class );
        addTests( ContentTests.class );
        addTests( ProviderReadTests.class );
        addTests( ProviderWriteTests.class );
        addTests( LastModifiedTests.class );
        addTests( UrlTests.class );
        addTests( UrlStructureTests.class );
        addTests( VfsClassLoaderTests.class );
    }

    /**
     * Adds the tests from a class to this suite.  The supplied class must be
     * a subclass of {@link AbstractProviderTestCase} and have a public a
     * no-args constructor.  This method creates an instance of the supplied
     * class for each public 'testNnnn' method provided by the class.
     */
    public void addTests( final Class testClass ) throws Exception
    {
        // Verify the class
        if ( !AbstractProviderTestCase.class.isAssignableFrom( testClass ) )
        {
            throw new Exception( "Test class " + testClass.getName() + " is not assignable to " + AbstractProviderTestCase.class.getName() );
        }

        // Locate the test methods
        final Method[] methods = testClass.getMethods();
        for ( int i = 0; i < methods.length; i++ )
        {
            final Method method = methods[ i ];
            if ( !method.getName().startsWith( "test" )
                || Modifier.isStatic( method.getModifiers() )
                || method.getReturnType() != Void.TYPE
                || method.getParameterTypes().length != 0 )
            {
                continue;
            }

            // Create instance
            final AbstractProviderTestCase testCase = (AbstractProviderTestCase)testClass.newInstance();
            testCase.setMethod( method );
            testCase.setName( prefix + method.getName() );
            testSuite.addTest( testCase );
        }
    }

    protected void setUp() throws Exception
    {
        // Locate the temp directory, and clean it up
        tempDir = AbstractVfsTestCase.getTestDirectory( "temp" );
        checkTempDir( "Temp dir not empty before test" );

        // Create the file system manager
        manager = new DefaultFileSystemManager();

        final DefaultFileReplicator replicator = new DefaultFileReplicator( tempDir );
        manager.setReplicator( new PrivilegedFileReplicator( replicator ) );
        manager.setTemporaryFileStore( replicator );

        providerConfig.prepare( manager );

        if ( !manager.hasProvider( "file" ) )
        {
            manager.addProvider( "file", new DefaultLocalFileProvider() );
        }

        manager.init();

        // Locate the base folders
        baseFolder = providerConfig.getBaseTestFolder( manager );
        readFolder = baseFolder.resolveFile( "read-tests" );
        writeFolder = baseFolder.resolveFile( "write-tests" );

        // Make some assumptions about the read folder
        assertTrue( readFolder.exists() );
        assertFalse( readFolder.getName().getPath().equals( FileName.ROOT_PATH ) );

        // Configure the tests
        final Enumeration tests = testSuite.tests();
        while ( tests.hasMoreElements() )
        {
            final Test test = (Test)tests.nextElement();
            if ( test instanceof AbstractProviderTestCase )
            {
                final AbstractProviderTestCase providerTestCase = (AbstractProviderTestCase)test;
                providerTestCase.setConfig( manager, baseFolder, readFolder, writeFolder );
            }
        }
    }

    protected void tearDown() throws Exception
    {
        manager.close();

        // Make sure temp directory is empty or gone
        checkTempDir( "Temp dir not empty after test" );
    }

    /** Asserts that the temp dir is empty or gone. */
    private void checkTempDir( final String assertMsg )
    {
        if ( tempDir.exists() )
        {
            assertTrue( assertMsg, tempDir.isDirectory() && tempDir.list().length == 0 );
        }
    }

}
