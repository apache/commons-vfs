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
