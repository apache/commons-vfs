/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.vfs2.util;

import org.apache.commons.vfs2.FileSystemException;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;


/**
 * UNIX permissions.
 * 
 * @since 2.1
 */
public class PosixPermissions
{


    static public enum Type
    {
        // User rights
        UserReadable(00400),
        UserWritable(00200),
        UserExecutable(00100),

        // Group rights
        GroupReadable(00040),
        GroupWritable(00020),
        GroupExecutable(00010),

        // Other rights
        OtherReadable(00004),
        OtherWritable(00002),
        OtherExecutable(00001);

        final private int mask;

        /**
         * Initialise with the mask
         */
        private Type(int mask)
        {
            this.mask = mask;
        }

        /**
         * Return the mask for this permission
         */
        public int getMask()
        {
            return mask;
        }

    }

    /**
     * Current permissions
     */
    int permissions;

    /**
     * If the user is the owner of the file
     */
    boolean isOwner;

    /**
     * If one user group is the group of the file
     */
    boolean isInGroup;


    /**
     * Creates a new PosixPermissions object
     * @param permissions The permissions
     * @param isOwner true if the user is the owner of the file
     * @param isInGroup true if the user is a group owner of the file
     */
    public PosixPermissions(int permissions, boolean isOwner, boolean isInGroup)
    {
        this.permissions = permissions;
        this.isOwner = isOwner;
        this.isInGroup = isInGroup;
    }

    public int getPermissions()
    {
        return permissions;
    }


    /**
     * Computes new permission from old ones
     *
     * @param values The permissions to set
     * @return The new permission
     */
    private int computeNewPermissions(Map<Type, Boolean> values)
    {
        int old = this.permissions;
        for (Map.Entry<Type, Boolean> entry : values.entrySet())
        {
            final Type type = entry.getKey();
            if (entry.getValue())
            {
                old |= type.getMask();
            } else
            {
                old &= ~type.getMask();
            }
        }

        return old;
    }


    /**
     * Test whether the bit corresponding to the permission is set
     */
    private boolean get(Type type)
    {
        return (type.getMask() & permissions) != 0;
    }

    /**
     * Check if whether the user can read the file
     *
     * @return true if the user can read
     */
    public boolean isReadable()
    {
        if (isOwner)
        {
            return get(Type.UserReadable);
        }
        if (isInGroup)
        {
            return get(Type.GroupReadable);
        }
        return get(Type.OtherReadable);
    }


    public Integer makeReadable(boolean readable, boolean ownerOnly)
    {
        EnumMap<Type, Boolean> map = new EnumMap<Type, Boolean>(Type.class);
        map.put(Type.UserReadable, readable);
        if (!ownerOnly)
        {
            map.put(Type.GroupReadable, readable);
            map.put(Type.OtherReadable, readable);
        }

        return this.computeNewPermissions(map);
    }

    public boolean isWritable()
    {
        if (isOwner)
        {
            return get(Type.UserWritable);
        }
        if (isInGroup)
        {
            return get(Type.GroupWritable);
        }
        return get(Type.OtherWritable);
    }

    public Integer makeWritable(boolean writable, boolean ownerOnly)
    {
        EnumMap<Type, Boolean> map = new EnumMap<Type, Boolean>(Type.class);
        map.put(Type.UserWritable, writable);
        if (!ownerOnly)
        {
            map.put(Type.GroupWritable, writable);
            map.put(Type.OtherWritable, writable);
        }

        return this.computeNewPermissions(map);
    }

    public boolean isExecutable()
    {
        if (isOwner)
        {
            return get(Type.UserExecutable);
        }
        if (isInGroup)
        {
            return get(Type.GroupExecutable);
        }
        return get(Type.OtherExecutable);
    }

    public int makeExecutable(boolean executable, boolean ownerOnly)
    {
        EnumMap<Type, Boolean> map = new EnumMap<Type, Boolean>(Type.class);
        map.put(Type.UserExecutable, executable);
        if (!ownerOnly)
        {
            map.put(Type.GroupExecutable, executable);
            map.put(Type.OtherExecutable, executable);
        }

        return this.computeNewPermissions(map);
    }
}
