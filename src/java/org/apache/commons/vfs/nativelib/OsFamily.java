/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE.txt file.
 */
package org.apache.commons.vfs.nativelib;

/**
 * An enumerated type, which represents an OS family.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision: 1.2 $ $Date: 2002/07/05 03:21:54 $
 */
public final class OsFamily
{
    private final String m_name;
    private final OsFamily[] m_families;

    OsFamily( final String name )
    {
        m_name = name;
        m_families = new OsFamily[ 0 ];
    }

    OsFamily( final String name, final OsFamily[] families )
    {
        m_name = name;
        m_families = families;
    }

    /**
     * Returns the name of this family.
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Returns the OS families that this family belongs to.
     */
    public OsFamily[] getFamilies()
    {
        return m_families;
    }
}
