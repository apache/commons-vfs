/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider;

import java.io.File;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;

/**
 * A file provider which handles local files.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1 $ $Date: 2002/06/17 07:19:45 $
 */
public interface LocalFileProvider
    extends FileProvider
{
    /**
     * Determines if a name is an absolute file name.
     *
     * @todo Move this to a general file name parser interface.
     *
     * @param name The name to test.
     */
    boolean isAbsoluteLocalName( final String name );

    /**
     * Finds a local file, from its local name.
     */
    FileObject findLocalFile( final String name )
        throws FileSystemException;

    /**
     * Converts from java.io.File to FileObject.
     */
    FileObject findLocalFile( final File file )
        throws FileSystemException;
}
