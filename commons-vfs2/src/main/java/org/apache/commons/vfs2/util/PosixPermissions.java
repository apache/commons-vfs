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

import java.util.EnumMap;
import java.util.Map;

/**
 * Unix permissions.
 *
 * @since 2.1
 */
public class PosixPermissions {

    /**
     * Permission types.
     */
    public enum Type {

        /**
         * User right readable.
         */
        UserReadable(0x100),

        /**
         * User right writable.
         */
        UserWritable(0x080),

        /**
         * User right executable.
         */
        UserExecutable(0x040),

        /**
         * Group right readable.
         */
        GroupReadable(0x020),

        /**
         * Group right writable.
         */
        GroupWritable(0x010),

        /**
         * Group right executable.
         */
        GroupExecutable(0x008),

        /**
         * Other right readable.
         */
        OtherReadable(0x004),

        /**
         * Other right writable.
         */
        OtherWritable(0x002),

        /**
         * Other right executable.
         */
        OtherExecutable(0x001);

        private final int mask;

        /**
         * Initialize with the mask
         */
        Type(final int mask) {
            this.mask = mask;
        }

        /**
         * Gets the mask for this permission.
         *
         * @return the mask for this permission.
         */
        public int getMask() {
            return mask;
        }

    }

    /**
     * Current permissions.
     */
    private final int permissions;

    /**
     * If the user is the owner of the file.
     */
    private final boolean isOwner;

    /**
     * If one user group is the group of the file.
     */
    private final boolean isInGroup;

    /**
     * Creates a new PosixPermissions object.
     *
     * @param permissions The permissions
     * @param isOwner true if the user is the owner of the file
     * @param isInGroup true if the user is a group owner of the file
     */
    public PosixPermissions(final int permissions, final boolean isOwner, final boolean isInGroup) {
        this.permissions = permissions;
        this.isOwner = isOwner;
        this.isInGroup = isInGroup;
    }

    /**
     * Computes new permission from old ones.
     *
     * @param values The permissions to set.
     * @return The new permissions.
     */
    private int computeNewPermissions(final Map<Type, Boolean> values) {
        int newPerms = permissions;
        for (final Map.Entry<Type, Boolean> entry : values.entrySet()) {
            final Type type = entry.getKey();
            if (entry.getValue()) {
                newPerms |= type.getMask();
            } else {
                newPerms &= ~type.getMask();
            }
        }
        return newPerms;
    }

    /**
     * Tests whether the bit corresponding to the permission is set.
     *
     * @return whether the bit corresponding to the permission is set.
     */
    private boolean get(final Type type) {
        return (type.getMask() & permissions) != 0;
    }

    /**
     * Gets permissions.
     *
     * @return permissions.
     */
    public int getPermissions() {
        return permissions;
    }

    /**
     * Gets whether the permissions are executable.
     *
     * @return whether the permissions are executable.
     */
    public boolean isExecutable() {
        if (isOwner) {
            return get(Type.UserExecutable);
        }
        if (isInGroup) {
            return get(Type.GroupExecutable);
        }
        return get(Type.OtherExecutable);
    }

    /**
     * Gets whether the permissions are readable.
     *
     * @return whether the permissions are readable.
     */
    public boolean isReadable() {
        if (isOwner) {
            return get(Type.UserReadable);
        }
        if (isInGroup) {
            return get(Type.GroupReadable);
        }
        return get(Type.OtherReadable);
    }

    /**
     * Gets whether the permissions are writable.
     *
     * @return whether the permissions are writable.
     */
    public boolean isWritable() {
        if (isOwner) {
            return get(Type.UserWritable);
        }
        if (isInGroup) {
            return get(Type.GroupWritable);
        }
        return get(Type.OtherWritable);
    }

    /**
     * Creates new permissions based on these permissions.
     *
     * @param executable Whether the new permissions should be readable.
     * @param ownerOnly Whether the new permissions are only for the owner.
     * @return the new permissions.
     */
    public int makeExecutable(final boolean executable, final boolean ownerOnly) {
        final EnumMap<Type, Boolean> map = new EnumMap<>(Type.class);
        map.put(Type.UserExecutable, executable);
        if (!ownerOnly) {
            map.put(Type.GroupExecutable, executable);
            map.put(Type.OtherExecutable, executable);
        }
        return computeNewPermissions(map);
    }

    /**
     * Creates new permissions based on these permissions.
     *
     * @param readable Whether the new permissions should be readable.
     * @param ownerOnly Whether the new permissions are only for the owner.
     * @return the new permissions.
     */
    public Integer makeReadable(final boolean readable, final boolean ownerOnly) {
        final EnumMap<Type, Boolean> map = new EnumMap<>(Type.class);
        map.put(Type.UserReadable, readable);
        if (!ownerOnly) {
            map.put(Type.GroupReadable, readable);
            map.put(Type.OtherReadable, readable);
        }
        return computeNewPermissions(map);
    }

    /**
     * Creates new permissions based on these permissions.
     *
     * @param writable Whether the new permissions should be readable.
     * @param ownerOnly Whether the new permissions are only for the owner.
     * @return the new permissions.
     */
    public Integer makeWritable(final boolean writable, final boolean ownerOnly) {
        final EnumMap<Type, Boolean> map = new EnumMap<>(Type.class);
        map.put(Type.UserWritable, writable);
        if (!ownerOnly) {
            map.put(Type.GroupWritable, writable);
            map.put(Type.OtherWritable, writable);
        }
        return computeNewPermissions(map);
    }
}
