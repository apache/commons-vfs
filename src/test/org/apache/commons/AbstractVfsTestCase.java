/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import junit.framework.TestCase;

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
    private final File baseDir;

    public AbstractVfsTestCase( final String name )
    {
        super( name );
        final String baseDirProp = System.getProperty( "test.basedir" );
        baseDir = getCanonicalFile( new File( baseDirProp ) );
    }

    /**
     * Returns the name of the package containing a class.
     *
     * @return The . delimited package name, or an empty string if the class
     *         is in the default package.
     */
    protected static String getPackageName( final Class clazz )
    {
        final Package pkg = clazz.getPackage();
        if( null != pkg )
        {
            return pkg.getName();
        }

        final String name = clazz.getName();
        if( -1 == name.lastIndexOf( "." ) )
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
    protected File getTestResource( final String name )
    {
        return getTestResource( name, true );
    }

    /**
     * Locates a test resource.
     *
     * @param name path of the resource, relative to this test's base directory.
     */
    protected File getTestResource( final String name, final boolean mustExist )
    {
        File file = new File( baseDir, name );
        file = getCanonicalFile( file );
        if( mustExist )
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
    protected File getTestDirectory()
    {
        return baseDir;
    }

    /**
     * Locates a test directory, creating it if it does not exist.
     *
     * @param name path of the directory, relative to this test's base directory.
     */
    protected File getTestDirectory( final String name )
    {
        File file = new File( baseDir, name );
        file = getCanonicalFile( file );
        assertTrue( "Test directory \"" + file + "\" does not exist or is not a directory.",
                    file.isDirectory() || file.mkdirs() );
        return file;
    }

    /**
     * Makes a file canonical
     */
    private File getCanonicalFile( final File file )
    {
        try
        {
            return file.getCanonicalFile();
        }
        catch( IOException e )
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
    protected void assertSameMessage( final String[] messages, final Throwable throwable )
    {
        Throwable current = throwable;
        for( int i = 0; i < messages.length; i++ )
        {
            String message = messages[ i ];
            assertNotNull( current );
            if( message != null )
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
    private Throwable getCause( Throwable throwable )
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
    protected void assertSameMessage( final String message,
                                      final Throwable throwable )
    {
        // TODO - implement this
        fail( "Not implemented." );
    }

    /**
     * Asserts that an exception contains the expected message.
     */
    protected void assertSameMessage( final String message,
                                      final Object[] info,
                                      final Throwable throwable )
    {
        // TODO - implement this
        fail( "Not implemented." );
    }

    /**
     * Asserts that an exception contains the expected message.
     */
    protected void assertSameMessage( final String message,
                                      final Object info,
                                      final Throwable throwable )
    {
        // TODO - implement this
        fail( "Not implemented." );
    }

    /**
     * Compares 2 objects for equality, nulls are equal.  Used by the test
     * classes' equals() methods.
     */
    public static boolean equals( final Object o1, final Object o2 )
    {
        if( o1 == null && o2 == null )
        {
            return true;
        }
        if( o1 == null || o2 == null )
        {
            return false;
        }
        return o1.equals( o2 );
    }
}
