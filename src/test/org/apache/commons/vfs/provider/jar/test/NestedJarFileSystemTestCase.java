/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.jar.test;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.impl.VFSClassLoader;
import org.apache.commons.vfs.provider.jar.JarFileSystemProvider;
import org.apache.commons.vfs.test.AbstractReadOnlyFileSystemTestCase;

/**
 * Tests for the Zip file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 */
public class NestedJarFileSystemTestCase
        extends AbstractReadOnlyFileSystemTestCase
{
    public NestedJarFileSystemTestCase( String name )
    {
        super( name );
    }

    protected FileObject topFolder;

    protected FileObject getTopFolder() throws Exception
    {
        getManager().addProvider( "jar", new JarFileSystemProvider() );

        File jarFile = getTestResource( "nested.jar" );
        String uri = "jar:" + jarFile.getAbsolutePath() + "!/";
        return getManager().resolveFile( uri );
    }

    /**
     * Returns the URI for the base folder.
     */
    protected FileObject getBaseFolder() throws Exception
    {
        topFolder = getTopFolder();
        final FileObject jarFile = topFolder.resolveFile( "test.jar" );
        // Now build the nested file system
        final FileObject nestedFS =
            getManager().createFileSystem( "jar", jarFile );
        return nestedFS.resolveFile( "/basedir" );
    }

    /**
     * Verify the package loaded with class loader.
     * If the provider supports attributes override this method.
     */
    protected boolean verifyPackage( Package pack )
    {
        return "code".equals( pack.getName() ) &&
               "ImplTitle".equals( pack.getImplementationTitle() ) &&
               "ImplVendor".equals( pack.getImplementationVendor() ) &&
               "1.1".equals( pack.getImplementationVersion() ) &&
               "SpecTitle".equals( pack.getSpecificationTitle() ) &&
               "SpecVendor".equals( pack.getSpecificationVendor() ) &&
               "1.0".equals( pack.getSpecificationVersion() ) &&
               !pack.isSealed();
    }


    public void testJarClassLoader() throws Exception
    { 
        FileObject test = topFolder.resolveFile( "normal.jar" );
        final FileObject[] objects = { test };
        VFSClassLoader loader =
            new VFSClassLoader( objects, getManager() );

        Class testClass = loader.loadClass( "code.ClassToLoad" );
        assertTrue( verifyNormalPackage( testClass.getPackage() ) );
        
        Object testObject = testClass.newInstance();
        assertSame( "**PRIVATE**", testObject.toString() );

        URL resource = loader.getResource( "file1.txt" );
        assertNotNull( resource );
        URLConnection urlCon = resource.openConnection();
        assertSameURLContent( getCharContent(), urlCon );
    }

    /**
     * Verify the package loaded with class loader.
     * If the provider supports attributes override this method.
     */
    protected boolean verifyNormalPackage( Package pack )
    {
        return "code".equals( pack.getName() ) &&
               "NormalTitle".equals( pack.getImplementationTitle() ) &&
               "NormalVendor".equals( pack.getImplementationVendor() ) &&
               "1.2".equals( pack.getImplementationVersion() ) &&
               "NormalSpec".equals( pack.getSpecificationTitle() ) &&
               "NormalSpecVendor".equals( pack.getSpecificationVendor() ) &&
               "0.1".equals( pack.getSpecificationVersion() ) &&
               pack.isSealed();
    }
}
