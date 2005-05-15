/*
 * Copyright 2002-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.vfs.util;

/**
 * An enumerated type, which represents an OS family.
 *
 * @author <a href="mailto:adammurdoch@apache.org">Adam Murdoch</a>
 * @version $Revision$ $Date$
 */
public final class OsFamily
{
    private final String name;
    private final OsFamily[] families;

    OsFamily(final String name)
    {
        this.name = name;
        families = new OsFamily[0];
    }

    OsFamily(final String name, final OsFamily[] families)
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
