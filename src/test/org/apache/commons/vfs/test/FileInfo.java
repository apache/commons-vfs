/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.test;

import org.apache.commons.vfs.FileType;
import java.util.Map;
import java.util.HashMap;

/**
 * Info about a file.
 */
class FileInfo
{
    String baseName;
    FileType type;
    Map children = new HashMap();
    FileInfo parent;

    public FileInfo( final String name, final FileType type )
    {
        baseName = name;
        this.type = type;
    }

    public FileInfo getParent()
    {
        return parent;
    }

    /** Adds a child. */
    public void addChild( final FileInfo child )
    {
        children.put( child.baseName, child );
        child.parent = this;
    }

    /** Adds a child. */
    public void addChild( final String baseName, final FileType type )
    {
        addChild( new FileInfo( baseName, type ) );
    }

    /** Adds a bunch of children. */
    public void addChildren( final String[] baseNames, final FileType type )
    {
        for( int i = 0; i < baseNames.length; i++ )
        {
            String baseName = baseNames[ i ];
            addChild( new FileInfo( baseName, type ) );
        }
    }
}
