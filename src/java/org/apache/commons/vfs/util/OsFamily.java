/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.util;

/**
 * An enumerated type, which represents an OS family.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.2 $ $Date: 2002/08/21 07:00:13 $
 */
public final class OsFamily
{
    private final String name;
    private final OsFamily[] families;

    OsFamily( final String name )
    {
        this.name = name;
        families = new OsFamily[ 0 ];
    }

    OsFamily( final String name, final OsFamily[] families )
    {
        this.name = name;
        this.families = families;
    }

    /**
     * Returns the name of this family.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the OS families that this family belongs to.
     */
    public OsFamily[] getFamilies()
    {
        return families;
    }
}
