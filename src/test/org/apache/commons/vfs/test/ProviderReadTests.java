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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;

/**
 * Read-only test cases for file providers.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.11 $ $Date: 2003/10/13 08:42:27 $
 *
 * @todo Test getLastModified(), getAttribute()
 */
public class ProviderReadTests
    extends AbstractProviderTestCase
{
    /**
     * Returns the capabilities required by the tests of this test case.
     */
    protected Capability[] getRequiredCaps()
    {
        return new Capability[]
        {
            Capability.GET_TYPE,
            Capability.LIST_CHILDREN,
            Capability.READ_CONTENT
        };
    }

    /**
     * Walks the base folder structure, asserting it contains exactly the
     * expected files and folders.
     */
    public void testStructure() throws Exception
    {
        final FileInfo baseInfo = buildExpectedStructure();
        assertSameStructure( getReadFolder(), baseInfo );
    }

    /**
     * Walks a folder structure, asserting it contains exactly the
     * expected files and folders.
     */
    protected void assertSameStructure( final FileObject folder,
                                        final FileInfo expected )
        throws Exception
    {
        // Setup the structure
        final List queueExpected = new ArrayList();
        queueExpected.add( expected );

        final List queueActual = new ArrayList();
        queueActual.add( folder );

        while ( queueActual.size() > 0 )
        {
            final FileObject file = (FileObject)queueActual.remove( 0 );
            final FileInfo info = (FileInfo)queueExpected.remove( 0 );

            // Check the type is correct
            assertSame( info.type, file.getType() );

            if ( info.type == FileType.FILE )
            {
                continue;
            }

            // Check children
            final FileObject[] children = file.getChildren();

            // Make sure all children were found
            assertNotNull( children );
            assertEquals( "count children of \"" + file.getName() + "\"", info.children.size(), children.length );

            // Recursively check each child
            for ( int i = 0; i < children.length; i++ )
            {
                final FileObject child = children[ i ];
                final FileInfo childInfo = (FileInfo)info.children.get( child.getName().getBaseName() );

                // Make sure the child is expected
                assertNotNull( childInfo );

                // Add to the queue of files to check
                queueExpected.add( childInfo );
                queueActual.add( child );
            }
        }
    }

    /**
     * Tests type determination.
     */
    public void testType() throws Exception
    {
        // Test a file
        FileObject file = getReadFolder().resolveFile( "file1.txt" );
        assertSame( FileType.FILE, file.getType() );

        // Test a folder
        file = getReadFolder().resolveFile( "dir1" );
        assertSame( FileType.FOLDER, file.getType() );

        // Test an unknown file
        file = getReadFolder().resolveFile( "unknown-child" );
        assertSame( FileType.IMAGINARY, file.getType() );
    }

    /**
     * Tests the contents of root of file system can be listed.
     */
    public void testRoot() throws FileSystemException
    {
        final FileObject file = getReadFolder().getFileSystem().getRoot();
        file.getChildren();
    }

    /**
     * Tests that folders have no content.
     */
    public void testFolderContent() throws Exception
    {
        // Try getting the content of a folder
        FileObject folder = getReadFolder().resolveFile( "dir1" );
        try
        {
            folder.getContent().getInputStream();
            fail();
        }
        catch ( FileSystemException e )
        {
            assertSameMessage( "vfs.provider/read-not-file.error", folder, e );
        }
    }

    /**
     * Tests can perform operations on a folder while reading from a different files.
     */
    public void testConcurrentReadFolder() throws Exception
    {
        final FileObject file = getReadFolder().resolveFile( "file1.txt" );
        assertTrue( file.exists() );
        final FileObject folder = getReadFolder().resolveFile( "dir1" );
        assertTrue( folder.exists() );

        // Start reading from the file
        final InputStream instr = file.getContent().getInputStream();
        try
        {
            // Do some operations
            folder.exists();
            folder.getType();
            folder.getChildren();
        }
        finally
        {
            instr.close();
        }
    }

    /**
     * Tests that findFiles() works.
     */
    public void testFindFiles() throws Exception
    {
        final FileInfo fileInfo = buildExpectedStructure();
        final VerifyingFileSelector selector = new VerifyingFileSelector( fileInfo );

        // Find the files
        final FileObject[] actualFiles = getReadFolder().findFiles( selector );

        // Compare actual and expected list of files
        final List expectedFiles = selector.finish();
        assertEquals( expectedFiles.size(), actualFiles.length );
        final int count = expectedFiles.size();
        for ( int i = 0; i < count; i++ )
        {
            final FileObject expected = (FileObject)expectedFiles.get( i );
            final FileObject actual = actualFiles[ i ];
            assertEquals( expected, actual );
        }
    }
}
