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
package org.apache.commons.vfs.provider.test;

import java.io.File;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.test.AbstractProviderTestCase;

/**
 * Test cases for the virtual file system provider.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.2 $ $Date: 2002/11/23 00:33:54 $
 */
public class VirtualProviderTestCase
    extends AbstractProviderTestCase
{
    private FileObject getBaseDir() throws FileSystemException
    {
        final File localDir = getTestDirectory( "read-tests" );
        return getManager().toFileObject( localDir );
    }

    /**
     * Checks nested junctions are not supported.
     */
    public void testNestedJunction() throws Exception
    {
        final FileSystem fs = getManager().createFileSystem( "vfs:" ).getFileSystem();
        final FileObject baseDir = getBaseDir();
        fs.addJunction( "/a", baseDir );

        // Nested
        try
        {
            fs.addJunction( "/a/b", baseDir );
            fail();
        }
        catch ( final Exception e )
        {
            assertSameMessage( "impl/nested-junction.error", "vfs:/a/b", e );
        }

        // At same point
        try
        {
            fs.addJunction( "/a", baseDir );
            fail();
        }
        catch ( final Exception e )
        {
            assertSameMessage( "impl/nested-junction.error", "vfs:/a", e );
        }
    }

    /**
     * Checks ancestors are created when a junction is created.
     */
    public void testAncestors() throws Exception
    {
        final FileSystem fs = getManager().createFileSystem( "vfs://" ).getFileSystem();
        final FileObject baseDir = getBaseDir();
        assertTrue( baseDir.exists() );

        // Make sure the file at the junction point and its ancestors do not exist
        FileObject file = fs.resolveFile( "/a/b" );
        assertFalse( file.exists() );
        file = file.getParent();
        assertFalse( file.exists() );
        file = file.getParent();
        assertFalse( file.exists() );

        // Add the junction
        fs.addJunction( "/a/b", baseDir );

        // Make sure the file at the junction point and its ancestors exist
        file = fs.resolveFile( "/a/b" );
        assertTrue( "Does not exist", file.exists() );
        file = file.getParent();
        assertTrue( "Does not exist", file.exists() );
        file = file.getParent();
        assertTrue( "Does not exist", file.exists() );
    }

    // Check that file @ junction point exists only when backing file exists
    // Add 2 junctions with common parent
    // Compare real and virtual files
    // Events
    // Remove junctions
}
