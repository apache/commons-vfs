/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;

/**
 * A file provider.  Each file provider is responsible for handling files for
 * a particular URI scheme.
 *
 * <p>A provider may implement the LogEnabled and Disposable interfaces
 * from Avalon.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1 $ $Date: 2002/06/17 07:19:45 $
 *
 * @ant.role name="file-provider"
 */
public interface FileProvider
{
    String ROLE = FileProvider.class.getName();

    /**
     * Sets the context for this file provider.  This method is called before
     * any of the other provider methods.
     *
     * @todo - move this to a lifecycle interface (this interface is accessable
     * to other providers, so need to prevent this being called).
     */
    void setContext( FileSystemProviderContext context );

    /**
     * Locates a file object, by absolute URI.
     *
     * @param baseFile
     *          The base file to use for resolving the individual parts of
     *          a compound URI.
     * @param uri
     *          The absolute URI of the file to find.
     */
    FileObject findFile( FileObject baseFile, String uri )
        throws FileSystemException;

    /**
     * Creates a layered file system.
     *
     * @param scheme
     *          The URI scheme for the layered file system.
     * @param file
     *          The file to build the file system on.
     */
    FileObject createFileSystem( String scheme, FileObject file )
        throws FileSystemException;
}
