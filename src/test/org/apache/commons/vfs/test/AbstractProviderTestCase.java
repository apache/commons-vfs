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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLConnection;
import java.util.Arrays;
import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;

/**
 * File system test cases, which verifies the structure and naming
 * functionality.
 *
 * Works from a base folder, and assumes a particular structure under
 * that base folder.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.11 $ $Date: 2003/10/13 08:42:27 $
 */
public abstract class AbstractProviderTestCase
    extends AbstractVfsTestCase
{
    private FileObject baseFolder;
    private FileObject readFolder;
    private FileObject writeFolder;
    private DefaultFileSystemManager manager;
    private Method method;

    // Expected contents of "file1.txt"
    public static final String FILE1_CONTENT = "This is a test file.";

    // Expected contents of test files
    public static final String TEST_FILE_CONTENT = "A test file.";

    /** Sets the test method. */
    public void setMethod( final Method method )
    {
        this.method = method;
    }

    /** Configures this test. */
    public void setConfig( final DefaultFileSystemManager manager,
                           final FileObject baseFolder,
                           final FileObject readFolder,
                           final FileObject writeFolder )
    {
        this.manager = manager;
        this.baseFolder = baseFolder;
        this.readFolder = readFolder;
        this.writeFolder = writeFolder;
    }

    /**
     * Returns the file system manager used by this test.
     */
    protected DefaultFileSystemManager getManager()
    {
        return manager;
    }

    /**
     * Returns the base test folder.  This is the parent of both the read
     * test and write test folders.
     */
    public FileObject getBaseFolder()
    {
        return baseFolder;
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
     * Returns the capabilities required by the tests of this test case.  The
     * tests are not run if the provider being tested does not support all
     * the required capabilities.  Return null or an empty array to always
     * run the tests.
     *
     * <p>This implementation returns null.
     */
    protected Capability[] getRequiredCaps()
    {
        return null;
    }

    /**
     * Runs the test.  This implementation short-circuits the test if the
     * provider being tested does not have the capabilities required by this
     * test.
     *
     * @todo Handle negative caps as well - ie, only run a test if the provider does not have certain caps.
     * @todo Figure out how to remove the test from the TestResult if the test is skipped.
     */
    protected void runTest() throws Throwable
    {
        // Check the capabilities
        final Capability[] caps = getRequiredCaps();
        if ( caps != null )
        {
            for ( int i = 0; i < caps.length; i++ )
            {
                final Capability cap = caps[ i ];
                if ( !readFolder.getFileSystem().hasCapability( cap ) )
                {
                    System.out.println( "skipping " + getName() + " because fs does not have cap " + cap );
                    return;
                }
            }
        }

        // Provider has all the capabilities - execute the test
        if ( method != null )
        {
            try
            {
                method.invoke( this, null );
            }
            catch ( final InvocationTargetException e )
            {
                throw e.getTargetException();
            }
        }
        else
        {
            super.runTest();
        }
    }

    /**
     * Asserts that the content of a file is the same as expected. Checks the
     * length reported by getContentLength() is correct, then reads the content
     * as a byte stream and compares the result with the expected content.
     * Assumes files are encoded using UTF-8.
     */
    protected void assertSameURLContent( final String expected,
                                         final URLConnection connection )
        throws Exception
    {
        // Get file content as a binary stream
        final byte[] expectedBin = expected.getBytes( "utf-8" );

        // Check lengths
        assertEquals( "same content length", expectedBin.length, connection.getContentLength() );

        // Read content into byte array
        final InputStream instr = connection.getInputStream();
        final ByteArrayOutputStream outstr;
        try
        {
            outstr = new ByteArrayOutputStream();
            final byte[] buffer = new byte[ 256 ];
            int nread = 0;
            while ( nread >= 0 )
            {
                outstr.write( buffer, 0, nread );
                nread = instr.read( buffer );
            }
        }
        finally
        {
            instr.close();
        }

        // Compare
        assertTrue( "same binary content", Arrays.equals( expectedBin, outstr.toByteArray() ) );
    }

    /**
     * Asserts that the content of a file is the same as expected. Checks the
     * length reported by getSize() is correct, then reads the content as
     * a byte stream and compares the result with the expected content.
     * Assumes files are encoded using UTF-8.
     */
    protected void assertSameContent( final String expected,
                                      final FileObject file )
        throws Exception
    {
        // Check the file exists, and is a file
        assertTrue( file.exists() );
        assertSame( FileType.FILE, file.getType() );

        // Get file content as a binary stream
        final byte[] expectedBin = expected.getBytes( "utf-8" );

        // Check lengths
        final FileContent content = file.getContent();
        assertEquals( "same content length", expectedBin.length, content.getSize() );

        // Read content into byte array
        final InputStream instr = content.getInputStream();
        final ByteArrayOutputStream outstr;
        try
        {
            outstr = new ByteArrayOutputStream();
            final byte[] buffer = new byte[ 256 ];
            int nread = 0;
            while ( nread >= 0 )
            {
                outstr.write( buffer, 0, nread );
                nread = instr.read( buffer );
            }
        }
        finally
        {
            instr.close();
        }

        // Compare
        assertTrue( "same binary content", Arrays.equals( expectedBin, outstr.toByteArray() ) );
    }

    /**
     * Builds the expected structure of the read tests folder.
     */
    protected FileInfo buildExpectedStructure()
    {
        // Build the expected structure
        final FileInfo base = new FileInfo( getReadFolder().getName().getBaseName(), FileType.FOLDER );
        base.addFile( "file1.txt", FILE1_CONTENT );
        base.addFile( "empty.txt", "" );
        base.addFolder( "emptydir" );

        final FileInfo dir = base.addFolder( "dir1" );
        dir.addFile( "file1.txt", TEST_FILE_CONTENT );
        dir.addFile( "file2.txt", TEST_FILE_CONTENT );
        dir.addFile( "file3.txt", TEST_FILE_CONTENT );

        return base;
    }
}
