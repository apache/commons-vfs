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
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;

/**
 * Responsible for making local replicas of files.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1 $ $Date: 2002/04/07 02:27:56 $
 */
public interface FileReplicator
{
    /**
     * Sets the context for the replicator.
     * @todo Move to a lifecycle interface.
     */
    void setContext( FileSystemProviderContext context );

    /**
     * Creates a local copy of the file, and all its descendents.
     *
     * @param srcFile The file to copy.
     * @param selector Selects the files to copy.
     *
     * @return The local copy of the source file.
     *
     * @throws FileSystemException
     *      If the source files does not exist, or on error copying.
     */
    File replicateFile( FileObject srcFile, FileSelector selector )
        throws FileSystemException;

    /**
     * Closes the replicator.
     * @todo Move to a lifecycle interface.
     */
    void close();
}
