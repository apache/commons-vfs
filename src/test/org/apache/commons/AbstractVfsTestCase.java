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
package org.apache.commons;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import junit.framework.TestCase;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.util.Messages;

/**
 * A base class for VFS tests.  Provides utility methods for locating
 * test resources.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.5 $ $Date: 2002/07/14 06:14:37 $
 */
public abstract class AbstractVfsTestCase
    extends TestCase
{
    private static File baseDir;

    /**
     * Returns the name of the package containing a class.
     *
     * @return The . delimited package name, or an empty string if the class
     *         is in the default package.
     */
    public static String getPackageName( final Class clazz )
    {
        final Package pkg = clazz.getPackage();
        if ( null != pkg )
        {
            return pkg.getName();
        }

        final String name = clazz.getName();
        if ( -1 == name.lastIndexOf( "." ) )
        {
            return "";
        }
        else
        {
            return name.substring( 0, name.lastIndexOf( "." ) );
        }
    }

    /**
     * Locates a test resource, and asserts that the resource exists
     *
     * @param name path of the resource, relative to this test's base directory.
     */
    public static File getTestResource( final String name )
    {
        return getTestResource( name, true );
    }

    /**
     * Locates a test resource.
     *
     * @param name path of the resource, relative to this test's base directory.
     */
    public static File getTestResource( final String name, final boolean mustExist )
    {
        File file = new File( getTestDirectory(), name );
        file = getCanonicalFile( file );
        if ( mustExist )
        {
            assertTrue( "Test file \"" + file + "\" does not exist.", file.exists() );
        }
        else
        {
            assertTrue( "Test file \"" + file + "\" should not exist.", !file.exists() );
        }

        return file;
    }

    /**
     * Locates the base directory for this test.
     */
    public static File getTestDirectory()
    {
        if ( baseDir == null )
        {
            final String baseDirProp = System.getProperty( "test.basedir" );
            baseDir = getCanonicalFile( new File( baseDirProp ) );
        }
        return baseDir;
    }

    /**
     * Locates a test directory, creating it if it does not exist.
     *
     * @param name path of the directory, relative to this test's base directory.
     */
    public static File getTestDirectory( final String name )
    {
        File file = new File( getTestDirectory(), name );
        file = getCanonicalFile( file );
        assertTrue( "Test directory \"" + file + "\" does not exist or is not a directory.",
                    file.isDirectory() || file.mkdirs() );
        return file;
    }

    /**
     * Makes a file canonical
     */
    public static File getCanonicalFile( final File file )
    {
        try
        {
            return file.getCanonicalFile();
        }
        catch ( IOException e )
        {
            return file.getAbsoluteFile();
        }
    }

    /**
     * Asserts that an exception chain contains the expected messages.
     *
     * @param messages The messages, in order.  A null entry in this array
     *                 indicates that the message should be ignored.
     */
    public static void assertSameMessage( final String[] messages, final Throwable throwable )
    {
        Throwable current = throwable;
        for ( int i = 0; i < messages.length; i++ )
        {
            String message = messages[ i ];
            assertNotNull( current );
            if ( message != null )
            {
                assertEquals( message, current.getMessage() );
            }

            // Get the next exception in the chain
            current = getCause( current );
        }
    }

    /**
     * Returns the cause of an exception.
     */
    public static Throwable getCause( Throwable throwable )
    {
        try
        {
            Method method = throwable.getClass().getMethod( "getCause", null );
            return (Throwable)method.invoke( throwable, null );
        }
        catch ( Exception e )
        {
            return null;
        }
    }

    /**
     * Asserts that an exception contains the expected message.
     */
    public static void assertSameMessage( final String code,
                                          final Throwable throwable )
    {
        assertSameMessage( code, new Object[ 0 ], throwable );
    }

    /**
     * Asserts that an exception contains the expected message.
     */
    public static void assertSameMessage( final String code,
                                          final Object[] params,
                                          final Throwable throwable )
    {
        if ( throwable instanceof FileSystemException )
        {
            final FileSystemException fse = (FileSystemException)throwable;

            // Compare message code and params
            assertEquals( code, fse.getCode() );
            assertEquals( params.length, fse.getInfo().length );
            for ( int i = 0; i < params.length; i++ )
            {
                final Object param = params[ i ];
                assertEquals( String.valueOf( param ), fse.getInfo()[ i ] );
            }
        }

        // Compare formatted message
        final String message = Messages.getString( code, params );
        assertEquals( message, throwable.getMessage() );
    }

    /**
     * Asserts that an exception contains the expected message.
     */
    public static void assertSameMessage( final String code,
                                          final Object param,
                                          final Throwable throwable )
    {
        assertSameMessage( code, new Object[]{param}, throwable );
    }

    /**
     * Compares 2 objects for equality, nulls are equal.  Used by the test
     * classes' equals() methods.
     */
    public static boolean equals( final Object o1, final Object o2 )
    {
        if ( o1 == null && o2 == null )
        {
            return true;
        }
        if ( o1 == null || o2 == null )
        {
            return false;
        }
        return o1.equals( o2 );
    }
}
