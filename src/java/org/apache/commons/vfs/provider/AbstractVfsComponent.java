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
 * A partial {@link VfsComponent} implementation.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1 $ $Date: 2002/08/21 14:28:07 $
 */
public abstract class AbstractVfsComponent
    implements VfsComponent
{
    private FileSystemProviderContext context;
    private Log log;

    /**
     * Sets the Logger to use for the component.
     */
    public final void setLogger( final Log log )
    {
        this.log = log;
    }

    /**
     * Sets the context for this file system provider.
     */
    public final void setContext( final FileSystemProviderContext context )
    {
        this.context = context;
    }

    /**
     * Initialises the component.  This implementation does nothing.
     */
    public void init() throws FileSystemException
    {
    }

    /**
     * Closes the provider.  This implementation does nothing.
     */
    public void close()
    {
    }

    /**
     * Returns the logger for this file system to use.
     */
    protected final Log getLogger()
    {
        return log;
    }

    /**
     * Returns the context for this provider.
     */
    protected final FileSystemProviderContext getContext()
    {
        return context;
    }
}
