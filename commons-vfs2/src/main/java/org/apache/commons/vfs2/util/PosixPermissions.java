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
 * UNIX permissions.
 *
 * @since 2.1
 */
public class PosixPermissions {

    /**
     * Permission types.
     */
    public static enum Type {
        /**
         * User right readable.
         */
        UserReadable(00400),

        /**
         * User right writable.
         */
        UserWritable(00200),

        /**
         * User right executable.
         */
        UserExecutable(00100),

        /**
         * Group right readable.
         */
        GroupReadable(00040),

        /**
         * Group right writable.
         */
        GroupWritable(00020),

        /**
         * Group right executable.
         */
        GroupExecutable(00010),

        /**
         * Other right readable.
         */
        OtherReadable(00004),

        /**
         * Other right writable.
         */
        OtherWritable(00002),

        /**
         * Other right executable.
         */
        OtherExecutable(00001);

        private final int mask;

        /**
         * Initialize with the mask
         */
        private Type(final int mask) {
            this.mask = mask;
        }

        /**
         * Return the mask for this permission.
         *
         * @return the mask for this permission.
         */
        public int getMask() {
            return this.mask;
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
        int newPerms = this.permissions;
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
        return (type.getMask() & this.permissions) != 0;
    }

    /**
     * Gets permissions.
     *
     * @return permissions.
     */
    public int getPermissions() {
        return this.permissions;
    }

    /**
     * Gets whether the permissions are executable.
     *
     * @return whether the permissions are executable.
     */
    public boolean isExecutable() {
        if (this.isOwner) {
            return this.get(Type.UserExecutable);
        }
        if (this.isInGroup) {
            return this.get(Type.GroupExecutable);
        }
        return this.get(Type.OtherExecutable);
    }

    /**
     * Gets whether the permissions are readable.
     *
     * @return whether the permissions are readable.
     */
    public boolean isReadable() {
        if (this.isOwner) {
            return this.get(Type.UserReadable);
        }
        if (this.isInGroup) {
            return this.get(Type.GroupReadable);
        }
        return this.get(Type.OtherReadable);
    }

    /**
     * Gets whether the permissions are writable.
     *
     * @return whether the permissions are writable.
     */
    public boolean isWritable() {
        if (this.isOwner) {
            return this.get(Type.UserWritable);
        }
        if (this.isInGroup) {
            return this.get(Type.GroupWritable);
        }
        return this.get(Type.OtherWritable);
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
        return this.computeNewPermissions(map);
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
        return this.computeNewPermissions(map);
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
        return this.computeNewPermissions(map);
    }
}
