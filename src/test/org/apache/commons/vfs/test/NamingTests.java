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

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.NameScope;

/**
 * Test cases for file naming.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.4 $ $Date: 2003/06/28 10:58:53 $
 *
 * @todo Add tests for all FileName methods 
 */
public class NamingTests
    extends AbstractProviderTestCase
{
    /**
     * Tests resolution of relative file names via the FS manager
     */
    public void testRelativeURI() throws Exception
    {
        // Build base dir
        getManager().setBaseFile( getReadFolder() );

        // Locate the base dir
        FileObject file = getManager().resolveFile( "." );
        assertSame( "file object", getReadFolder(), file );

        // Locate a child
        file = getManager().resolveFile( "some-child" );
        assertSame( "file object", getReadFolder(), file.getParent() );

        // Locate a descendent
        file = getManager().resolveFile( "some-folder/some-file" );
        assertSame( "file object", getReadFolder(), file.getParent().getParent() );

        // Locate parent
        file = getManager().resolveFile( ".." );
        assertSame( "file object", getReadFolder().getParent(), file );
    }

    /**
     * Tests encoding of relative URI.
     */
    public void testRelativeUriEncoding() throws Exception
    {
        // Build base dir
        getManager().setBaseFile( getReadFolder() );
        final String path = getReadFolder().getName().getPath();

        // Encode "some file"
        FileObject file = getManager().resolveFile( "%73%6f%6d%65%20%66%69%6c%65" );
        assertEquals( path + "/some file", file.getName().getPath() );

        // Encode "."
        file = getManager().resolveFile( "%2e" );
        assertEquals( path, file.getName().getPath() );

        // Encode '%'
        file = getManager().resolveFile( "a%25" );
        assertEquals( path + "/a%", file.getName().getPath() );

        // Encode /
        file = getManager().resolveFile( "dir%2fchild" );
        assertEquals( path + "/dir/child", file.getName().getPath() );

        // Encode \
        file = getManager().resolveFile( "dir%5cchild" );
        assertEquals( path + "/dir/child", file.getName().getPath() );

        // Use "%" literal
        try
        {
            getManager().resolveFile( "%" );
            fail();
        }
        catch ( FileSystemException e )
        {
        }

        // Not enough digits in encoded char
        try
        {
            getManager().resolveFile( "%5" );
            fail();
        }
        catch ( FileSystemException e )
        {
        }

        // Invalid digit in encoded char
        try
        {
            getManager().resolveFile( "%q" );
            fail();
        }
        catch ( FileSystemException e )
        {
        }
    }

    /**
     * Tests the root file name.
     */
    public void testRootFileName() throws Exception
    {
        // Locate the root file
        final FileName rootName = getReadFolder().getFileSystem().getRoot().getName();

        // Test that the root path is "/"
        assertEquals( "root path", "/", rootName.getPath() );

        // Test that the root basname is ""
        assertEquals( "root base name", "", rootName.getBaseName() );

        // Test that the root name has no parent
        assertNull( "root parent", rootName.getParent() );
    }

    /**
     * Tests child file names.
     */
    public void testChildName() throws Exception
    {
        final FileName baseName = getReadFolder().getName();
        final String basePath = baseName.getPath();
        final FileName name = baseName.resolveName( "some-child", NameScope.CHILD );

        // Test path is absolute
        assertTrue( "is absolute", basePath.startsWith( "/" ) );

        // Test base name
        assertEquals( "base name", "some-child", name.getBaseName() );

        // Test absolute path
        assertEquals( "absolute path", basePath + "/some-child", name.getPath() );

        // Test parent path
        assertEquals( "parent absolute path", basePath, name.getParent().getPath() );

        // Try using a compound name to find a child
        assertBadName( name, "a/b", NameScope.CHILD );

        // Check other invalid names
        checkDescendentNames( name, NameScope.CHILD );
    }

    /**
     * Name resolution tests that are common for CHILD or DESCENDENT scope.
     */
    private void checkDescendentNames( final FileName name,
                                       final NameScope scope )
        throws Exception
    {
        // Make some assumptions about the name
        assertTrue( !name.getPath().equals( "/" ) );
        assertTrue( !name.getPath().endsWith( "/a" ) );
        assertTrue( !name.getPath().endsWith( "/a/b" ) );

        // Test names with the same prefix
        String path = name.getPath() + "/a";
        assertSameName( path, name, path, scope );
        assertSameName( path, name, "../" + name.getBaseName() + "/a", scope );

        // Test an empty name
        assertBadName( name, "", scope );

        // Test . name
        assertBadName( name, ".", scope );
        assertBadName( name, "./", scope );

        // Test ancestor names
        assertBadName( name, "..", scope );
        assertBadName( name, "../a", scope );
        assertBadName( name, "../" + name.getBaseName() + "a", scope );
        assertBadName( name, "a/..", scope );

        // Test absolute names
        assertBadName( name, "/", scope );
        assertBadName( name, "/a", scope );
        assertBadName( name, "/a/b", scope );
        assertBadName( name, name.getPath(), scope );
        assertBadName( name, name.getPath() + "a", scope );
    }

    /**
     * Checks that a relative name resolves to the expected absolute path.
     * Tests both forward and back slashes.
     */
    private void assertSameName( final String expectedPath,
                                 final FileName baseName,
                                 final String relName,
                                 final NameScope scope )
        throws Exception
    {
        // Try the supplied name
        FileName name = baseName.resolveName( relName, scope );
        assertEquals( expectedPath, name.getPath() );

        // Replace the separators
        relName.replace( '\\', '/' );
        name = baseName.resolveName( relName, scope );
        assertEquals( expectedPath, name.getPath() );

        // And again
        relName.replace( '/', '\\' );
        name = baseName.resolveName( relName, scope );
        assertEquals( expectedPath, name.getPath() );
    }

    /**
     * Checks that a relative name resolves to the expected absolute path.
     * Tests both forward and back slashes.
     */
    private void assertSameName( String expectedPath,
                                 FileName baseName,
                                 String relName ) throws Exception
    {
        assertSameName( expectedPath, baseName, relName, NameScope.FILE_SYSTEM );
    }

    /**
     * Tests relative name resolution, relative to the base folder.
     */
    public void testNameResolution() throws Exception
    {
        final FileName baseName = getReadFolder().getName();
        final String parentPath = baseName.getParent().getPath();
        final String path = baseName.getPath();
        final String childPath = path + "/some-child";

        // Test empty relative path
        assertSameName( path, baseName, "" );

        // Test . relative path
        assertSameName( path, baseName, "." );

        // Test ./ relative path
        assertSameName( path, baseName, "./" );

        // Test .// relative path
        assertSameName( path, baseName, ".//" );

        // Test .///.///. relative path
        assertSameName( path, baseName, ".///.///." );
        assertSameName( path, baseName, "./\\/.\\//." );

        // Test <elem>/.. relative path
        assertSameName( path, baseName, "a/.." );

        // Test .. relative path
        assertSameName( parentPath, baseName, ".." );

        // Test ../ relative path
        assertSameName( parentPath, baseName, "../" );

        // Test ..//./ relative path
        assertSameName( parentPath, baseName, "..//./" );
        assertSameName( parentPath, baseName, "..//.\\" );

        // Test <elem>/../.. relative path
        assertSameName( parentPath, baseName, "a/../.." );

        // Test <elem> relative path
        assertSameName( childPath, baseName, "some-child" );

        // Test ./<elem> relative path
        assertSameName( childPath, baseName, "./some-child" );

        // Test ./<elem>/ relative path
        assertSameName( childPath, baseName, "./some-child/" );

        // Test <elem>/././././ relative path
        assertSameName( childPath, baseName, "./some-child/././././" );

        // Test <elem>/../<elem> relative path
        assertSameName( childPath, baseName, "a/../some-child" );

        // Test <elem>/<elem>/../../<elem> relative path
        assertSameName( childPath, baseName, "a/b/../../some-child" );
    }

    /**
     * Tests descendent name resolution.
     */
    public void testDescendentName()
        throws Exception
    {
        final FileName baseName = getReadFolder().getName();

        // Test direct child
        String path = baseName.getPath() + "/some-child";
        assertSameName( path, baseName, "some-child", NameScope.DESCENDENT );

        // Test compound name
        path = path + "/grand-child";
        assertSameName( path, baseName, "some-child/grand-child", NameScope.DESCENDENT );

        // Test relative names
        assertSameName( path, baseName, "./some-child/grand-child", NameScope.DESCENDENT );
        assertSameName( path, baseName, "./nada/../some-child/grand-child", NameScope.DESCENDENT );
        assertSameName( path, baseName, "some-child/./grand-child", NameScope.DESCENDENT );

        // Test badly formed descendent names
        checkDescendentNames( baseName, NameScope.DESCENDENT );
    }

    /**
     * Tests resolution of absolute names.
     */
    public void testAbsoluteNames() throws Exception
    {
        // Test against the base folder
        FileName name = getReadFolder().getName();
        checkAbsoluteNames( name );

        // Test against the root
        name = getReadFolder().getFileSystem().getRoot().getName();
        checkAbsoluteNames( name );

        // Test against some unknown file
        name = name.resolveName( "a/b/unknown" );
        checkAbsoluteNames( name );
    }

    /**
     * Tests resolution of absolute names.
     */
    private void checkAbsoluteNames( final FileName name ) throws Exception
    {
        // Root
        assertSameName( "/", name, "/" );
        assertSameName( "/", name, "//" );
        assertSameName( "/", name, "/." );
        assertSameName( "/", name, "/some file/.." );

        // Some absolute names
        assertSameName( "/a", name, "/a" );
        assertSameName( "/a", name, "/./a" );
        assertSameName( "/a", name, "/a/." );
        assertSameName( "/a/b", name, "/a/b" );

        // Some bad names
        assertBadName( name, "/..", NameScope.FILE_SYSTEM );
        assertBadName( name, "/a/../..", NameScope.FILE_SYSTEM );
    }

    /**
     * Asserts that a particular relative name is invalid for a particular
     * scope.
     */
    private void assertBadName( final FileName name,
                                final String relName,
                                final NameScope scope )
    {
        try
        {
            name.resolveName( relName, scope );
            fail( "expected failure" );
        }
        catch ( FileSystemException e )
        {
            // TODO - should check error message
        }
    }

    /**
     * Tests conversion from absolute to relative names.
     */
    public void testAbsoluteNameConvert() throws Exception
    {
        final FileName baseName = getReadFolder().getName();

        String path = "/test1/test2";
        FileName name = baseName.resolveName( path );
        assertEquals( path, name.getPath() );

        // Try child and descendent names
        testRelName( name, "child" );
        testRelName( name, "child1/child2" );

        // Try own name
        testRelName( name, "." );

        // Try parent, and root
        testRelName( name, ".." );
        testRelName( name, "../.." );

        // Try sibling and descendent of sibling
        testRelName( name, "../sibling" );
        testRelName( name, "../sibling/child" );

        // Try siblings with similar names
        testRelName( name, "../test2_not" );
        testRelName( name, "../test2_not/child" );
        testRelName( name, "../test" );
        testRelName( name, "../test/child" );

        // Try unrelated
        testRelName( name, "../../unrelated" );
        testRelName( name, "../../test" );
        testRelName( name, "../../test/child" );

        // Test against root
        path = "/";
        name = baseName.resolveName( path );
        assertEquals( path, name.getPath() );

        // Try child and descendent names (against root)
        testRelName( name, "child" );
        testRelName( name, "child1/child2" );

        // Try own name (against root)
        testRelName( name, "." );
    }

    /**
     * Checks that a file name converts to an expected relative path
     */
    private void testRelName( final FileName baseName,
                              final String relPath )
        throws Exception
    {
        final FileName expectedName = baseName.resolveName( relPath );

        // Convert to relative path, and check
        final String actualRelPath = baseName.getRelativeName( expectedName );
        assertEquals( relPath, actualRelPath );
    }
}
