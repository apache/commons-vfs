/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.test;

import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemManagerFactory;
import org.apache.commons.vfs.FileObject;
import java.io.File;

/**
 * Test cases for the FileSystemManagerFactory.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.2 $ $Date: 2002/10/23 10:56:33 $
 */
public class FileSystemManagerFactoryTestCase
    extends AbstractVfsTestCase
{
    public FileSystemManagerFactoryTestCase( String name )
    {
        super( name );
    }

    /**
     * Sanity test.
     */
    public void testDefaultInstance() throws Exception
    {
        // Locate the default manager
        final FileSystemManager manager = FileSystemManagerFactory.getManager();

        // Lookup a test file
        final File testDir = getTestResource( "basedir" );
        final FileObject file = manager.toFileObject( testDir );
        assertNotNull( file );
        assertTrue( file.exists() );
    }

}
