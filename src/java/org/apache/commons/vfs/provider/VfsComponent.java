/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider;

import org.apache.commons.logging.Log;
import org.apache.commons.vfs.FileSystemException;

/**
 * This interface is used to manage the lifecycle of all VFS components.
 * This includes all implementations of the following interfaces:
 * <ul>
 * <li>{@link FileProvider}
 * <li>{@link FileSystem}
 * <li>{@link FileReplicator}
 * </ul>
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1 $ $Date: 2002/08/21 14:28:08 $
 */
public interface VfsComponent
{
    /**
     * Sets the Logger to use for the component.
     *
     * @param logger
     */
    void setLogger( Log logger );

    /**
     * Sets the context for the component.
     *
     * @param context The context.
     */
    void setContext( FileSystemProviderContext context );

    /**
     * Initialises the component.
     */
    void init() throws FileSystemException;

    /**
     * Closes the component.
     */
    void close();
}
