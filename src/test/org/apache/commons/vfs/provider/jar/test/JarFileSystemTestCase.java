/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider.jar.test;

import java.io.File;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.provider.jar.JarFileSystemProvider;
import org.apache.commons.vfs.test.AbstractReadOnlyFileSystemTestCase;

/**
 * Tests for the Jar file system.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 */
public class JarFileSystemTestCase
    extends AbstractReadOnlyFileSystemTestCase
{
    public JarFileSystemTestCase( String name )
    {
        super( name );
    }

    /**
     * Returns the URI for the base folder.
     */
    protected FileObject getBaseFolder() throws Exception
    {
        File jarFile = getTestResource( "test.jar" );
        String uri = "jar:" + jarFile.getAbsolutePath() + "!basedir";
        getManager().addProvider( "jar", new JarFileSystemProvider() );
        return getManager().resolveFile( uri );
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
}
