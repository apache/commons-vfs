/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.provider;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.NameScope;

/**
 * A default file name implementation.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.4 $ $Date: 2002/07/05 04:08:17 $
 */
public final class DefaultFileName
    implements FileName
{
    private final UriParser parser;
    private final String rootPrefix;
    private final String absPath;

    // Cached stuff
    private String uri;
    private String baseName;

    public DefaultFileName( final UriParser parser,
                            final String rootPrefix,
                            final String absPath )
    {
        this.parser = parser;
        this.rootPrefix = rootPrefix;
        this.absPath = absPath;
    }

    /**
     * Returns the hashcode for this name.
     */
    public int hashCode()
    {
        return ( rootPrefix.hashCode() ^ absPath.hashCode() );
    }

    /**
     * Determines if this object is equal to another.
     */
    public boolean equals( final Object obj )
    {
        final DefaultFileName name = (DefaultFileName)obj;
        return ( rootPrefix.equals( name.rootPrefix ) && absPath.equals( absPath ) );
    }

    /**
     * Returns the URI of the file.
     */
    public String toString()
    {
        return getURI();
    }

    /**
     * Returns the base name of the file.
     */
    public String getBaseName()
    {
        if ( baseName == null )
        {
            baseName = parser.getBaseName( absPath );
        }
        return baseName;
    }

    /**
     * Returns the absolute path of the file, relative to the root of the
     * file system that the file belongs to.
     */
    public String getPath()
    {
        return absPath;
    }

    /**
     * Returns the name of a child of the file.
     */
    public FileName resolveName( final String name,
                                 final NameScope scope )
        throws FileSystemException
    {
        final String otherAbsPath = parser.resolvePath( absPath, name, scope );
        return new DefaultFileName( parser, rootPrefix, otherAbsPath );
    }

    /**
     * Returns the name of the parent of the file.
     */
    public FileName getParent()
    {
        final String parentPath = parser.getParentPath( absPath );
        if ( parentPath == null )
        {
            return null;
        }
        return new DefaultFileName( parser, rootPrefix, parentPath );
    }

    /**
     * Resolves a name, relative to the file.  If the supplied name is an
     * absolute path, then it is resolved relative to the root of the
     * file system that the file belongs to.  If a relative name is supplied,
     * then it is resolved relative to this file name.
     */
    public FileName resolveName( final String path ) throws FileSystemException
    {
        return resolveName( path, NameScope.FILE_SYSTEM );
    }

    /**
     * Returns the absolute URI of the file.
     */
    public String getURI()
    {
        if ( uri == null )
        {
            uri = parser.getUri( rootPrefix, absPath );
        }
        return uri;
    }

    /**
     * Converts a file name to a relative name, relative to this file name.
     */
    public String getRelativeName( final FileName name ) throws FileSystemException
    {
        return parser.makeRelative( absPath, name.getPath() );
    }
}
