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
package org.apache.commons.vfs.test;

import java.io.InputStream;
import java.util.Iterator;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.NameScope;

/**
 * Test cases for reading file content.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.5 $ $Date: 2003/03/14 03:48:37 $
 */
public class ContentTests
    extends AbstractProviderTestCase
{
    /**
     * Asserts that every expected file exists, and has the expected content.
     */
    public void testAllContent() throws Exception
    {
        final FileInfo baseInfo = buildExpectedStructure();
        final FileObject baseFolder = getReadFolder();

        assertSameContent( baseInfo, baseFolder );
    }

    /**
     * Asserts every file in a folder exists and has the expected content.
     */
    private void assertSameContent( final FileInfo expected,
                                    final FileObject folder ) throws Exception
    {
        for ( Iterator iterator = expected.children.values().iterator(); iterator.hasNext(); )
        {
            final FileInfo fileInfo = (FileInfo)iterator.next();
            final FileObject child = folder.resolveFile( fileInfo.baseName, NameScope.CHILD );

            assertTrue( child.exists() );
            if ( fileInfo.type == FileType.FILE )
            {
                assertSameContent( fileInfo.content, child );
            }
            else
            {
                assertSameContent( fileInfo, child );
            }
        }
    }

    /**
     * Tests existence determination.
     */
    public void testExists() throws Exception
    {
        // Test a file
        FileObject file = getReadFolder().resolveFile( "file1.txt" );
        assertTrue( "file exists", file.exists() );
        assertTrue( "file exists", file.getType() != FileType.IMAGINARY );

        // Test a folder
        file = getReadFolder().resolveFile( "dir1" );
        assertTrue( "folder exists", file.exists() );
        assertTrue( "folder exists", file.getType() != FileType.IMAGINARY );

        // Test an unknown file
        file = getReadFolder().resolveFile( "unknown-child" );
        assertTrue( "unknown file does not exist", !file.exists() );
        assertTrue( "unknown file does not exist",
                    file.getType() == FileType.IMAGINARY );

        // Test an unknown file in an unknown folder
        file = getReadFolder().resolveFile( "unknown-folder/unknown-child" );
        assertTrue( "unknown file does not exist", !file.exists() );
        assertTrue( "unknown file does not exist",
                    file.getType() == FileType.IMAGINARY );
    }

    /**
     * Tests root of file system exists.
     */
    public void testRoot() throws FileSystemException
    {
        final FileObject file = getReadFolder().getFileSystem().getRoot();
        assertTrue( file.exists() );
        assertTrue( file.getType() != FileType.IMAGINARY );
    }

    /**
     * Tests parent identity
     */
    public void testParent() throws FileSystemException
    {
        // Test when both exist
        FileObject folder = getReadFolder().resolveFile( "dir1" );
        FileObject child = folder.resolveFile( "file3.txt" );
        assertTrue( "folder exists", folder.exists() );
        assertTrue( "child exists", child.exists() );
        assertSame( folder, child.getParent() );

        // Test when file does not exist
        child = folder.resolveFile( "unknown-file" );
        assertTrue( "folder exists", folder.exists() );
        assertTrue( "child does not exist", !child.exists() );
        assertSame( folder, child.getParent() );

        // Test when neither exists
        folder = getReadFolder().resolveFile( "unknown-folder" );
        child = folder.resolveFile( "unknown-file" );
        assertTrue( "folder does not exist", !folder.exists() );
        assertTrue( "child does not exist", !child.exists() );
        assertSame( folder, child.getParent() );

        // Test the parent of the root of the file system
        // TODO - refactor out test cases for layered vs originating fs
        final FileSystem fileSystem = getReadFolder().getFileSystem();
        FileObject root = fileSystem.getRoot();
        if ( fileSystem.getParentLayer() == null )
        {
            // No parent layer, so parent should be null
            assertNull( "root has null parent", root.getParent() );
        }
        else
        {
            // Parent should be parent of parent layer.
            assertSame( fileSystem.getParentLayer().getParent(), root.getParent() );
        }
    }

    /**
     * Tests that children cannot be listed for non-folders.
     */
    public void testChildren() throws FileSystemException
    {
        // Check for file
        FileObject file = getReadFolder().resolveFile( "file1.txt" );
        assertSame( FileType.FILE, file.getType() );
        try
        {
            file.getChildren();
            fail();
        }
        catch ( FileSystemException e )
        {
            assertSameMessage( "vfs.provider/list-children-not-folder.error", file, e );
        }

        // Should be able to get child by name
        file = file.resolveFile( "some-child" );
        assertNotNull( file );

        // Check for unknown file
        file = getReadFolder().resolveFile( "unknown-file" );
        assertTrue( !file.exists() );
        try
        {
            file.getChildren();
            fail();
        }
        catch ( final FileSystemException e )
        {
            assertSameMessage( "vfs.provider/list-children-not-folder.error", file, e );
        }

        // Should be able to get child by name
        FileObject child = file.resolveFile( "some-child" );
        assertNotNull( child );
    }

    /**
     * Tests content.
     */
    public void testContent() throws Exception
    {
        // Test non-empty file
        FileObject file = getReadFolder().resolveFile( "file1.txt" );
        assertSameContent( FILE1_CONTENT, file );

        // Test empty file
        file = getReadFolder().resolveFile( "empty.txt" );
        assertSameContent( "", file );
    }

    /**
     * Tests that unknown files have no content.
     */
    public void testUnknownContent() throws Exception
    {

        // Try getting the content of an unknown file
        FileObject unknownFile = getReadFolder().resolveFile( "unknown-file" );
        FileContent content = unknownFile.getContent();
        try
        {
            content.getInputStream();
            fail();
        }
        catch ( FileSystemException e )
        {
            assertSameMessage( "vfs.provider/read-not-file.error", unknownFile, e );
        }
        try
        {
            content.getSize();
            fail();
        }
        catch ( final FileSystemException e )
        {
            assertSameMessage( "vfs.provider/get-size-not-file.error", unknownFile, e );
        }
    }

    /**
     * Tests concurrent reads on a file.
     */
    public void testConcurrentRead() throws Exception
    {
        final FileObject file = getReadFolder().resolveFile( "file1.txt" );
        assertTrue( file.exists() );

        // Start reading from the file
        final InputStream instr = file.getContent().getInputStream();
        try
        {
            // Start reading again
            file.getContent().getInputStream().close();
        }
        finally
        {
            instr.close();
        }
    }

    /**
     * Tests concurrent reads on different files works.
     */
    public void testConcurrentReadFiles() throws Exception
    {
        final FileObject file = getReadFolder().resolveFile( "file1.txt" );
        assertTrue( file.exists() );
        final FileObject emptyFile = getReadFolder().resolveFile( "empty.txt" );
        assertTrue( emptyFile.exists() );

        // Start reading from the file
        final InputStream instr = file.getContent().getInputStream();
        try
        {
            // Try to read from other file
            assertSameContent( "", emptyFile );
        }
        finally
        {
            instr.close();
        }
    }

    /**
     * Tests that content and file objects are usable after being closed.
     */
    public void testReuse() throws Exception
    {
        // Get the test file
        FileObject file = getReadFolder().resolveFile( "file1.txt" );
        assertEquals( FileType.FILE, file.getType() );

        // Get the file content
        assertSameContent( FILE1_CONTENT, file );

        // Read the content again
        assertSameContent( FILE1_CONTENT, file );

        // Close the content + file
        file.getContent().close();
        file.close();

        // Read the content again
        assertSameContent( FILE1_CONTENT, file );
    }

    /**
     * Tests that input streams are cleaned up on file close.
     */
    public void testInstrCleanup() throws Exception
    {
        // Get the test file
        FileObject file = getReadFolder().resolveFile( "file1.txt" );
        assertEquals( FileType.FILE, file.getType() );

        // Open some input streams
        final InputStream instr1 = file.getContent().getInputStream();
        assertTrue( instr1.read() == FILE1_CONTENT.charAt( 0 ) );
        final InputStream instr2 = file.getContent().getInputStream();
        assertTrue( instr2.read() == FILE1_CONTENT.charAt( 0 ) );

        // Close the file
        file.close();

        // Check
        assertTrue( instr1.read() == -1 );
        assertTrue( instr2.read() == -1 );
    }
}
