/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider;


/**
 * A partial FileProvider implementation.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.1 $ $Date: 2002/06/17 07:19:45 $
 */
public abstract class AbstractFileProvider
    implements FileProvider
{
    private FileSystemProviderContext m_context;

    /**
     * Returns the context for this provider.
     */
    protected FileSystemProviderContext getContext()
    {
        return m_context;
    }

    /**
     * Sets the context for this file system provider.  This method is called
     * before any of the other provider methods.
     */
    public void setContext( final FileSystemProviderContext context )
    {
        m_context = context;
    }
}
